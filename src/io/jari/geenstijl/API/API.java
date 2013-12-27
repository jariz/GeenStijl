package io.jari.geenstijl.API;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JARI.IO
 * Date: 15-12-13
 * Time: 17:58
 */
public class API {
    static String TAG = "GS.API";

    /**
     * Downloads & parses the articles and images
     *
     * @param force Force download and bypass cache
     * @return Artikel[]
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     */
    public static Artikel[] getArticles(boolean force, Context context) throws IOException, ParseException, URISyntaxException {
        if (!force) {
            Artikel[] cache = getCache(context);
            if (cache != null) return cache;
        }

        //we halen onze data van de html versie van geenstijl, omdat de RSS versie pure poep is, en omdat jsoup awesome is
        Document document = Jsoup.connect("http://www.geenstijl.nl").get();
        Elements artikelen = document.select("#content>article");
        ArrayList<Artikel> resultaat = new ArrayList<Artikel>();
        for (Element artikel_el : artikelen) {
            Artikel artikel = parseArtikel(artikel_el);

            resultaat.add(artikel);
        }
        Artikel[] arr_res = new Artikel[resultaat.size()];
        resultaat.toArray(arr_res);
        setCache(arr_res, context);
        return arr_res;
    }

    public static boolean vote(Artikel artikel, Comment comment, String direction) {
        try {
            JSONObject jsonObject = new JSONObject(downloadString(String.format("http://www.geenstijl.nl/modlinks/domod.php?entry=%s&cid=%s&mod=%s", artikel.id, comment.id, direction)));
            Log.d(TAG, "Feedback for comment " + comment.id + " on article " + artikel.id + " was " + jsonObject.getBoolean("success"));
            return jsonObject.getBoolean("success");
        } catch (Exception z) {
            Log.w(TAG, "vote() uncaught exception! Returning false");
            z.printStackTrace();
            return false;
        }
    }

    private static Artikel parseArtikel(Element artikel_el) throws ParseException {
        Artikel artikel = new Artikel();

        //id
        artikel.id = Integer.parseInt(artikel_el.attr("id").substring(1));

        //summary
        artikel.summary = artikel_el.select("a.more").first() != null;

        //titel
        artikel.titel = artikel_el.select("h1").text();

        //plaatje
        Element plaatje = artikel_el.select("img").first();
        if (plaatje != null) {
            try {
                String url = plaatje.attr("src");
                Log.d(TAG, "Downloading " + url);
//                    artikel.plaatje = Drawable.createFromStream(((java.io.InputStream)new URL(plaatje.attr("src")).getContent()), null);
                artikel.plaatje = readBytes((InputStream) new URL(plaatje.attr("src")).getContent());
                artikel.groot_plaatje = plaatje.hasClass("groot");
                if (artikel.groot_plaatje) Log.i(TAG, "    Done. Big image.");
                else Log.i(TAG, "    Done.");
            } catch (Exception ex) {
                Log.w(TAG, "Unable to download image, Falling back... Reason: " + ex.getMessage());
                artikel.plaatje = null;
            }
        }

        //embed
        if (artikel_el.select("div.embed").first() != null) {
            //atm alleen support voor iframes
            Element frame = artikel_el.select("div.embed>iframe").first();
            if (frame != null)
                artikel.embed = frame.attr("src");
        }

        //footer shit
        Element footer = artikel_el.select("footer").first();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
        artikel.datum = simpleDateFormat.parse(footer.select("time").first().attr("datetime"));

        StringTokenizer footer_items = new StringTokenizer(footer.text(), "|");
        artikel.auteur = footer_items.nextToken().trim();

        artikel.reacties = Integer.parseInt(footer.select("a.comments").text().replace(" reacties", ""));

        artikel.link = footer.select("a").first().attr("href");

        //clean up
        artikel_el.select("h1").remove();
        artikel_el.select(".embed").remove();
        artikel_el.select("img").remove();
        artikel_el.select("footer").remove();
        artikel_el.select("a.more").remove();
        artikel_el.select("script").remove();

        //inhoud
        artikel.inhoud = artikel_el.html();

        return artikel;
    }

    /**
     * Get article and comments (note that getArticles doesn't get the comments)
     *
     * @param url The direct url to the geenstijl article
     * @return Artikel The fetched article
     * @throws IOException
     * @throws ParseException
     */
    public static Artikel getArticle(String url) throws IOException, ParseException {
        Artikel artikel;
        Log.i(TAG, "GETARTICLE STEP 1/3: Getting/parsing article page & images... " + url);
        Document document = Jsoup.connect(url).get();
        Element artikel_el = document.select("#content>article").first();
        artikel = parseArtikel(artikel_el);

        //comment scores
        String jsurl = document.select("[src*=modlinks]").first().attr("src");

        Log.i(TAG, "GETARTICLE STEP 2/3: Getting scoremods... " + jsurl);
        String js = downloadString(jsurl);

        Log.i(TAG, "GETARTICLE STEP 3/3: Parsing scores and comments... " + jsurl);
        Pattern p = Pattern.compile("moderation\\['(\\d+)'\\] = '(-?[0-9]{0,4})';", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(js);
        HashMap<Integer, Integer> scores = new HashMap<Integer, Integer>();

        int i = 0;
        while (m.find()) {
            i++;
            Integer int1 = Integer.parseInt(m.group(1));
            Integer int2 = Integer.parseInt(m.group(2));
            Log.d(TAG + ".perf", "ScoreParser: Run " + i + " " + int1 + ":" + int2);
            scores.put(int1, int2);
        }

        ArrayList<Comment> comments = new ArrayList<Comment>();
        i = 0;
        Elements comments_el = document.select("#comments>.commentlist>article");
        for (Element comment_el : comments_el) {
            i++;
            Comment comment = new Comment();
            comment.id = Integer.parseInt(comment_el.attr("id").substring(1));
            comment.score = scores.get(comment.id);
            Element footer = comment_el.select("footer").first();
            StringTokenizer footer_items = new StringTokenizer(footer.text(), "|");
            comment.auteur = footer_items.nextToken().trim();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyHH:mm", Locale.US);
            comment.datum = simpleDateFormat.parse(footer_items.nextToken().trim() + footer_items.nextToken().trim());

//            comment_el.select("footer").remove();
            comment.inhoud = comment_el.select("p").first().html();

            Log.d(TAG + ".perf", "CommentParser: Parsed " + comment.id + ": " + i + "/" + comments_el.size());

            comments.add(comment);
        }

        Comment[] comm = new Comment[comments.size()];
        comments.toArray(comm);
        artikel.comments = comm;

        Log.i(TAG, "GETARTICLE: DONE");

        return artikel;
    }

    static String downloadString(String url) throws IOException {
        URLConnection con = new URL(url).openConnection();
        InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            baos.write(buf, 0, len);
        }
        return new String(baos.toByteArray(), encoding);
    }

    static byte[] readBytes(InputStream inputStream) throws IOException {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        // we need to know how may bytes were read to write them to the byteBuffer
        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        // and then we can return your byte array.
        return byteBuffer.toByteArray();
    }

    /**
     * getCache returns the latest cached items.
     * If the cache is over 30 mins old it'll return null and you're expected to download the new articles and set the cache again
     *
     * @return Artikel[]
     */
    private static Artikel[] getCache(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("geenstijl", 0);
        long age = preferences.getLong("items_age", 0);
        if (Calendar.getInstance().getTimeInMillis() - age <= 1800000)
            return (Artikel[]) SerializeObject.stringToObject(preferences.getString("items", ""));
        else return null;
    }

    /**
     * setCache saves the article array to the cache.
     * Expires in 30 mins.
     */
    private static void setCache(Artikel[] artikels, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("geenstijl", 0);
        preferences.edit().putString("items", SerializeObject.objectToString(artikels)).putLong("items_age", Calendar.getInstance().getTimeInMillis()).commit();
    }
}
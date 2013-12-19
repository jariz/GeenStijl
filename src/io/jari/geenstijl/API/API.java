package io.jari.geenstijl.API;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JARI.IO
 * Date: 15-12-13
 * Time: 17:58
 */
public class API {
    static String TAG = "GS.API";

    /**
     * Downloads & parses the articles and images
     * @return Artikel[]
     * @throws IOException
     * @throws ParseException
     * @throws URISyntaxException
     */
    public static Artikel[] getArticles() throws IOException, ParseException, URISyntaxException {
        //we halen onze data van de html versie van geenstijl, omdat de RSS versie pure poep is, en omdat jsoup awesome is
        Document document = Jsoup.connect("http://www.geenstijl.nl").get();
        Elements artikelen = document.select("#content article");
        ArrayList<Artikel> resultaat = new ArrayList<Artikel>();
        for (Element artikel_el : artikelen) {
            Artikel artikel = new Artikel();
            //titel
            artikel.titel = artikel_el.select("h1").text();

            //plaatje
            Element plaatje = artikel_el.select("img").first();
            if (plaatje != null) {
                try {
                    String url = plaatje.attr("src");
                    Log.d(TAG, "Downloading "+url);
                    artikel.plaatje = Drawable.createFromStream(((java.io.InputStream)new URL(plaatje.attr("src")).getContent()), null);
                    artikel.groot_plaatje = plaatje.hasClass("groot");
                    if(artikel.groot_plaatje) Log.i(TAG, "    Done. Big image.");
                    else Log.i(TAG, "    Done.");
                } catch (Exception ex) {
                    Log.w(TAG, "Unable to download image, Falling back... Reason: "+ex.getMessage());
                    artikel.plaatje = null;
                }
            }

            //footer shit
            Element footer = artikel_el.select("footer").first();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
            artikel.datum = simpleDateFormat.parse(footer.select("time").first().attr("datetime"));

            StringTokenizer footer_items = new StringTokenizer(footer.text(), "|");
            artikel.auteur = footer_items.nextToken().trim();

            artikel.reacties = Integer.parseInt(footer.select("a.comments").text().replace(" reacties", ""));

            artikel.link = Uri.parse(footer.select("a").first().attr("href"));

            //clean up
            artikel_el.select("h1").remove();
            artikel_el.select(".embed").remove();
            artikel_el.select("img").remove();
            artikel_el.select("footer").remove();
            artikel_el.select("a.more").remove();
            artikel_el.select("script").remove();

            //inhoud
            artikel.inhoud = artikel_el.html();


            resultaat.add(artikel);
        }
        Artikel[] arr_res = new Artikel[resultaat.size()];
        resultaat.toArray(arr_res);
        return arr_res;
    }
}

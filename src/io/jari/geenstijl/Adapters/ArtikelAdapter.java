package io.jari.geenstijl.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Article;
import io.jari.geenstijl.R;
import org.w3c.dom.Text;

/**
 * JARI.IO
 * Date: 16-12-13
 * Time: 16:50
 */
public class ArtikelAdapter extends ArrayAdapter<Artikel> implements ListAdapter {
    public ArtikelAdapter(Activity context, int resource, Artikel[] objects) {
        super(context, resource, objects);

        artikelen = objects;
        this.context = context;
    }

    Activity context;
    Artikel[] artikelen;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        final Artikel artikel = artikelen[position];

        if (item == null)
            item = context.getLayoutInflater().inflate(R.layout.blog_item, null);

        item = fillView(item, artikel, context, true);

        return item;
    }

    public static View fillView(View item, final Artikel artikel, final Context context, Boolean readmore_enabled) {
        ((TextView) item.findViewById(R.id.title)).setText(artikel.titel);
        TextView desc = (TextView) item.findViewById(R.id.desc);
        TextView footer = (TextView) item.findViewById(R.id.footer);

        desc.setText(Html.fromHtml(artikel.inhoud));
        desc.setMovementMethod(LinkMovementMethod.getInstance());

        footer.setText(String.format("%s | %s | %s reacties", artikel.auteur, artikel.datum.toString(), artikel.reacties)); //todo icons?

        ImageView big = (ImageView) item.findViewById(R.id.big_image);
        ImageView small = (ImageView) item.findViewById(R.id.small_image);

        if (artikel.plaatje != null) {
            BitmapDrawable bitmap = new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(artikel.plaatje, 0, artikel.plaatje.length));
            if (!artikel.groot_plaatje) {
                big.setVisibility(View.GONE);
                small.setVisibility(View.VISIBLE);
                small.setImageDrawable(bitmap);
            } else {
                big.setVisibility(View.VISIBLE);
                small.setVisibility(View.GONE);
//                Rect rekt = artikel.plaatje.getBounds();
//                big.setMinimumWidth(rekt.width());
//                big.setMinimumHeight(rekt.height());
//                big.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                big.setAdjustViewBounds(true);
//                big.setMinimumHeight(artikel.plaatje.getMinimumHeight());
//                big.setHei
//                big.setAdjustViewBounds(true);
//                big.setMinimumHeight((item.getWidth() / bitmap.getMinimumWidth()) * bitmap.getMinimumHeight() * 2);
//                big.setMinimumHeight(99999);
                big.getLayoutParams().height = (item.getWidth() / bitmap.getMinimumWidth()) * bitmap.getMinimumHeight();
                big.setImageDrawable(bitmap);
            }
        } else {
            big.setVisibility(View.GONE);
            small.setVisibility(View.GONE);
        }

        Button embed = (Button) item.findViewById(R.id.embed);
        if (artikel.embed == null) embed.setVisibility(View.GONE);
        else {
            embed.setVisibility(View.VISIBLE);
            embed.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);

                    //bugfix
                    if(artikel.embed.startsWith("://")) artikel.embed = "http"+artikel.embed;
                    if(artikel.embed.startsWith("//")) artikel.embed = "http:"+artikel.embed;

                    i.setData(Uri.parse(artikel.embed));
                    context.startActivity(i);
                }
            });
        }

        Button link = (Button) item.findViewById(R.id.more);
        if (!readmore_enabled) link.setVisibility(View.GONE);
        else
            link.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(artikel.link), context, Article.class));
                }
            });

        return item;
    }
}

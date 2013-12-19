package io.jari.geenstijl.Adapters;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import io.jari.geenstijl.API.Artikel;
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
        Artikel artikel = artikelen[position];

        if (item == null)
            item = context.getLayoutInflater().inflate(R.layout.blog_item, null);

        ((TextView) item.findViewById(R.id.title)).setText(artikel.titel);
        TextView desc = (TextView) item.findViewById(R.id.desc);
        TextView footer = (TextView) item.findViewById(R.id.footer);

        desc.setText(Html.fromHtml(artikel.inhoud));
        desc.setMovementMethod(LinkMovementMethod.getInstance());

        footer.setText(String.format("%s | %s | %s reacties", artikel.auteur, artikel.datum.toString(), artikel.reacties)); //todo icons?

        ImageView big = (ImageView)item.findViewById(R.id.big_image);
        ImageView small = (ImageView)item.findViewById(R.id.small_image);

        if (artikel.plaatje != null) {
            if (!artikel.groot_plaatje) {
                big.setVisibility(View.GONE);
                small.setVisibility(View.VISIBLE);
                small.setImageDrawable(artikel.plaatje);
            } else {
                big.setVisibility(View.VISIBLE);
                small.setVisibility(View.GONE);
                big.setImageDrawable(artikel.plaatje);
                big.setMinimumHeight(artikel.plaatje.getBounds().height() * 5); //todo find better way
            }
        } else {
            big.setVisibility(View.GONE);
            small.setVisibility(View.GONE);
        }

        return item;
    }
}

/*
 * Copyright 2014 Jari Zwarts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jari.geenstijl.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Article;
import io.jari.geenstijl.R;
import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * JARI.IO
 * Date: 16-12-13
 * Time: 16:50
 */
public class ArtikelAdapter extends ArrayAdapter<Artikel> implements ListAdapter {
    public ArtikelAdapter(Activity context, int resource, Artikel[] objects) {
        super(context, resource, objects);
        artikelen = new ArrayList<Artikel>(Arrays.asList(objects));
        this.context = context;
    }

    Activity context;
    ArrayList<Artikel> artikelen;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        final Artikel artikel = artikelen.get(position);

        if (item == null)
            item = context.getLayoutInflater().inflate(R.layout.blog_item, null);

        item = fillView(item, artikel, context, true);

        return item;
    }

    /*
     * We override these methods because we want to avoid android using it's own list
     * Cuz we update ours, and not theirs
     */

    @Override
    public int getCount() {
        return artikelen.size();
    }

    @Override
    public Artikel getItem(int position) {
        return artikelen.get(position);
    }

    @Override
    public int getPosition(Artikel item) {
        return artikelen.indexOf(item);
    }

    public void update(Artikel[] new_artikelen) {
        artikelen.addAll(Arrays.<Artikel>asList(new_artikelen));
        notifyDataSetChanged();
    }

    public static View fillView(View item, final Artikel artikel, final Context context, Boolean readmore_enabled) {
        ((TextView) item.findViewById(R.id.title)).setText(artikel.titel);
        TextView desc = (TextView) item.findViewById(R.id.desc);
        TextView footer = (TextView) item.findViewById(R.id.footer);

        desc.setText(Html.fromHtml(artikel.inhoud));
        desc.setMovementMethod(LinkMovementMethod.getInstance());

        footer.setText(String.format("%s | %s | %s reacties", artikel.auteur, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(artikel.datum), artikel.reacties));

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

                //ronde 22 van de vreselijke large image hoogte berekening code
                //ik denk dat ik 50% van het project aan het onderste gedeelte heb besteed :')
                //maar shit werkt freakin sweet :D
                Resources r = context.getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
                WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                float ratio = (display.getWidth() - px) / bitmap.getMinimumWidth();
                big.getLayoutParams().height = Math.round(bitmap.getMinimumHeight() * ratio);

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
        if(artikel.summary) link.setText(context.getResources().getString(R.string.read_more));
        else link.setText(context.getResources().getString(R.string.reaguursels));
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

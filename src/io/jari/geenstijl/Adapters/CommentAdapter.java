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
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.API.Comment;
import io.jari.geenstijl.API.RRTime;
import io.jari.geenstijl.Article;
import io.jari.geenstijl.R;

import java.util.Date;

/**
 * JARI.IO
 * Date: 16-12-13
 * Time: 16:50
 */
public class CommentAdapter extends ArrayAdapter<Comment> implements ListAdapter {
    public CommentAdapter(Activity context, int resource, Comment[] objects) {
        super(context, resource, objects);

        comments = objects;
        this.context = context;
    }

    Activity context;
    Comment[] comments;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        final Comment comment = comments[position];

        if (item == null)
            item = context.getLayoutInflater().inflate(R.layout.article_comment, null);

        ((TextView)item.findViewById(R.id.author)).setText(comment.auteur);
        if(comment.score == null) comment.score = 0;
        ((TextView)item.findViewById(R.id.score)).setText(Integer.toString(comment.score));
        ((TextView)item.findViewById(R.id.timespan)).setText(RRTime.formatDurationMs(new Date().getTime() - comment.datum.getTime(), context) + context.getResources().getString(R.string.ago));
        TextView cntnt = ((TextView)item.findViewById(R.id.content));
        cntnt.setText(Html.fromHtml(comment.inhoud));
        cntnt.setMovementMethod(LinkMovementMethod.getInstance());

        return item;
    }
}

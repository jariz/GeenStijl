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
import io.jari.geenstijl.Article;
import io.jari.geenstijl.R;

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
        ((TextView)item.findViewById(R.id.timespan)).setText(DateUtils.getRelativeTimeSpanString(comment.datum.getTime()));
        ((TextView)item.findViewById(R.id.content)).setText(Html.fromHtml(comment.inhoud));

        return item;
    }
}

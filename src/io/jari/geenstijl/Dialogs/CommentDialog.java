package io.jari.geenstijl.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.API.Comment;
import io.jari.geenstijl.R;

/**
 * Created by JariZ on 26/12/13.
 */
public class CommentDialog extends DialogFragment {

    public CommentDialog(Comment comment, Artikel article) {
        this.artikel = article;
        this.comment = comment;
    }

    final Comment comment;
    final Artikel artikel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.mod_score)
                .setItems(new String[]{
                        "+1",
                        "-1"
                }, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String direction = "";
                        switch (which) {
                            case 0:
                                comment.score += 1;
                                direction = "+1";
                                break;
                            case 1:
                                comment.score -= 1;
                                direction = "-1";
                                break;
                        }

                        new Thread(new Runnable() {
                            String direction;

                            public void run() {
                                if (!API.vote(artikel, comment, direction))
                                    Toast.makeText(getActivity(), String.format(getResources().getString(R.string.vote_fail), direction), Toast.LENGTH_SHORT).show();
                            }

                            public Runnable setDir(String dir) {
                                this.direction = dir;
                                return this;
                            }
                        }.setDir(direction)).start();
                    }
                });
        return builder.create();
    }
}

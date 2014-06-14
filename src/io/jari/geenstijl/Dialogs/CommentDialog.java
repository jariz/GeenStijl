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

package io.jari.geenstijl.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.API.Comment;
import io.jari.geenstijl.R;

/**
 * JARIZ.PRO
 * Date: 26/12/13
 * Time: 21:07
 * Author: JariZ
 */
@Deprecated
public class CommentDialog extends DialogFragment {

    public CommentDialog(Comment comment, Artikel article, Activity activity) {
        this.artikel = article;
        this.comment = comment;
        this.activity = activity;
    }

    final Comment comment;
    final Artikel artikel;
    final Activity activity;

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
//                                comment.score += 1;
                                direction = "+1";
                                break;
                            case 1:
//                                comment.score -= 1;
                                direction = "-1";
                                break;
                        }

                        new Thread(new Runnable() {
                            String direction;

                            public void run() {
                                if (!API.vote(artikel, comment, direction))
                                    activity.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Crouton.makeText(activity, activity.getString(R.string.vote_fail, direction), Style.ALERT, R.id.show).show();
                                        }
                                    });
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

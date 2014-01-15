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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.R;

/**
 * JARIZ.PRO
 * Date: 26/12/13
 * Time: 21:07
 * Author: JariZ
 */
public class ReplyDialog extends DialogFragment {

    public ReplyDialog(Activity activity, Artikel artikel) {
        this.activity = activity;
        this.artikel = artikel;
    }

    final Activity activity;
    final Artikel artikel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_reply, null);
        builder.setView(view)
                .setPositiveButton(R.string.reply, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReplyDialog.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.reply);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final View error = view.findViewById(R.id.error);
                final Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        positive.setEnabled(false);
                        negative.setEnabled(false);
                        final TextView reply = (TextView) view.findViewById(R.id.reply);
                        final View loading = view.findViewById(R.id.loading);
                        reply.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);
                        alertDialog.setCancelable(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean success;
                                try {
                                    success = API.reply(artikel, reply.getText().toString(), activity);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    success = false;
                                }
                                alertDialog.setCancelable(true);
                                if (!success) activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        reply.setVisibility(View.VISIBLE);
                                        error.setVisibility(View.VISIBLE);
                                        positive.setEnabled(true);
                                        negative.setEnabled(true);
                                        loading.setVisibility(View.GONE);
                                    }
                                });
                                else activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        alertDialog.dismiss();
                                    }
                                });
                            }
                        }).start();
                    }
                });
            }
        });

        return alertDialog;
    }
}

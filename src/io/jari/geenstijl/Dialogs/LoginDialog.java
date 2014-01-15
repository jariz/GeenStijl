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
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.API.Comment;
import io.jari.geenstijl.R;
import org.w3c.dom.Text;

import java.io.IOException;

/**
 * JARIZ.PRO
 * Date: 26/12/13
 * Time: 21:07
 * Author: JariZ
 */
public class LoginDialog extends DialogFragment {

    public LoginDialog(Activity activity) {
        this.activity = activity;
    }

    final Activity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.dialog_login, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.signin, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginDialog.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.signin);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //i wrote this code with a certain amount of alcohol in my blood, so i can't vouch for it's readability
                final View error = view.findViewById(R.id.error);
                final Button positive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                final Button negative = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                positive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        positive.setEnabled(false);
                        negative.setEnabled(false);
                        final TextView username = (TextView) view.findViewById(R.id.username);
                        final TextView password = (TextView) view.findViewById(R.id.password);
                        final View loading = view.findViewById(R.id.loading);
                        username.setVisibility(View.GONE);
                        password.setVisibility(View.GONE);
                        loading.setVisibility(View.VISIBLE);
                        error.setVisibility(View.GONE);
                        alertDialog.setCancelable(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean success;
                                try {
                                    success = API.logIn(username.getText().toString(), password.getText().toString(), LoginDialog.this.getActivity());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    success = false;
                                }
                                alertDialog.setCancelable(true);
                                if(!success) activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        username.setVisibility(View.VISIBLE);
                                        password.setVisibility(View.VISIBLE);
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
                                        forceOptionsReload();

                                        Crouton.makeText(activity, getString(R.string.loggedin, API.USERNAME), Style.INFO, R.id.ptr_layout).show();
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

    void forceOptionsReload() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.invalidateOptionsMenu();
            }
        });
    }
}

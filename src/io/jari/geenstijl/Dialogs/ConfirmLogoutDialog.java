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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.Blog;
import io.jari.geenstijl.R;

/**
 * JARIZ.PRO
 * Date: 26/12/13
 * Time: 21:07
 * Author: JariZ
 */
public class ConfirmLogoutDialog extends DialogFragment {

    public ConfirmLogoutDialog(Blog activity) {
        this.activity = activity;
    }

    final Blog activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //force dark dialog CUZ I WANT TO
        ContextThemeWrapper wrapper = new ContextThemeWrapper(activity, android.R.style.Theme_Holo);

        AlertDialog.Builder builder = new AlertDialog.Builder(wrapper);
        builder.setMessage(getResources().getString(R.string.confirm_logout))
                .setInverseBackgroundForced(true)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        API.logOut(activity);
                        forceOptionsReload();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ConfirmLogoutDialog.this.getDialog().cancel();
                    }
                })
                .setTitle(R.string.logout);

        return builder.create();
    }

    void forceOptionsReload() {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.reloadDrawer();
            }
        });
    }
}

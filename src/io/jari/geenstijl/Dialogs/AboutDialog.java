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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import io.jari.geenstijl.Blog;
import io.jari.geenstijl.R;

/**
 * JARIZ.PRO
 * Date: 26/12/13
 * Time: 21:07
 * Author: JariZ
 */
public class AboutDialog extends DialogFragment {

    public AboutDialog(Blog activity) {
        this.activity = activity;
    }

    final Blog activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_about, null);
        builder.setView(view);
        TextView version = (TextView) view.findViewById(R.id.about_version);
        TextView desc = (TextView) view.findViewById(R.id.about_desc);
        desc.setMovementMethod(LinkMovementMethod.getInstance());

        try {
            version.setText(
                    activity.getResources().getString(R.string.about_version)
                    +
                    activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            version.setText(activity.getResources().getString(R.string.about_version)+"????");
        }
        return builder.create();
    }
}

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

package io.jari.geenstijl;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Random;

/**
 * JARI.IO
 * Date: 16-12-13
 * Time: 23:48
 */
public class Base extends SherlockFragmentActivity {

    int STATE_ERROR = 1;
    int STATE_LOADING = 2;
    int STATE_SHOW = 3;

    String errorMessage = "Er is een fout opgetreden tijdens het ophalen van de fout...";

    SystemBarTintManager tintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //are you #bebebe?
        if(Build.VERSION.SDK_INT >= 19) {
            this.tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintResource(R.color.geenstijl);
            tintManager.setStatusBarAlpha(1f);
            tintManager.setStatusBarTintEnabled(true);
        }

        getSupportActionBar().setIcon(R.drawable.icon);

        //set error button click handler
        findViewById(R.id.error_button).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AlertDialog.Builder(Base.this)
                        .setTitle(R.id.error_button)
                        .setMessage(errorMessage)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    void switchState(int state) {
        final View error = findViewById(R.id.error);
        final View show = findViewById(R.id.show);
        final View loading = findViewById(R.id.loading);

        switch (state) {
            case 1: // STATE_ERROR

                TextView title = (TextView)error.findViewById(R.id.error_title);
                switch(new Random().nextInt(2)) {
                    case 0:
                        title.setText(R.string.error_title1);
                        break;
                    case 1:
                        title.setText(R.string.error_title2);
                        break;
                    case 2:
                        title.setText(R.string.error_title3);
                        break;
                }

                //todo find a nicer way for the identical blocks down below

                Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
                anim.setDuration(500);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {}
                    public void onAnimationEnd(Animation animation) {
                        show.setVisibility(View.GONE);
                        loading.setVisibility(View.GONE);
                        error.setVisibility(View.VISIBLE);
                    }
                    public void onAnimationRepeat(Animation animation) {}
                });
                loading.startAnimation(anim);


                break;
            case 3: // STATE_ERROR
                anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
                anim.setDuration(500);
                error.setVisibility(View.GONE);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {}
                    public void onAnimationEnd(Animation animation) {
                        show.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);
                    }
                    public void onAnimationRepeat(Animation animation) {}
                });
                loading.startAnimation(anim);


                break;
        }
    }
}

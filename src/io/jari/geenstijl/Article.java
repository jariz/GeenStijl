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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.manuelpeinado.fadingactionbar.FadingActionBarHelper;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
import io.jari.geenstijl.Adapters.CommentAdapter;
import io.jari.geenstijl.Dialogs.CommentDialog;
import io.jari.geenstijl.Dialogs.ReplyDialog;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * JARI.IO
 * Date: 24/12/13
 * Time: 01:46
 */
public class Article extends Base {

    Artikel currentArtikel = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.article);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        final Activity activity = this;

        //#HOLOYOLO
        if(Build.VERSION.SDK_INT >= 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            ViewGroup.LayoutParams params = findViewById(R.id.show).getLayoutParams();
            //jariz's home made actionbar hack!
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            ((ViewGroup.MarginLayoutParams)params).topMargin = config.getPixelInsetTop(true);
        }

        new Thread(new Runnable() {
            public void run() {
                if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                    String url = getIntent().getData().toString();

                    try {
                        final Artikel artikel = API.getArticle(url);
                        currentArtikel = artikel;
                        runOnUiThread(new Runnable() {
                            public void run() {

                                //Deze laten we nog even liggen, cool idee maar niet echt praktisch

//                                if(artikel.groot_plaatje) {
//                                    BitmapDrawable bitmap = new BitmapDrawable(getResources(), BitmapFactory.decodeByteArray(artikel.plaatje, 0, artikel.plaatje.length));
//                                    FadingActionBarHelper helper = new FadingActionBarHelper()
//                                            .contentLayout(R.layout.article)
//                                            .headerLayout(R.layout.header)
//                                            .actionBarBackground(bitmap);
//                                    setContentView(helper.createView(Article.this));
//                                    helper.initActionBar(Article.this);
//                                    ImageView header = (ImageView)findViewById(R.id.image_header);
//                                    header.setImageDrawable(bitmap);
////                                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
////                                    Display display = wm.getDefaultDisplay();
////                                    float ratio = (float)display.getWidth() / (float)bitmap.getMinimumWidth();
////                                    header.getLayoutParams().height = Math.round(bitmap.getMinimumHeight() * ratio);
//                                }

                                ListView comments = (ListView)findViewById(R.id.show);



                                ActionBar bar = getSupportActionBar();
                                bar.setDisplayHomeAsUpEnabled(true);
                                bar.setTitle(artikel.titel);

                                View header = getLayoutInflater().inflate(R.layout.blog_item, null);
                                //even artikeladapter lenen hiervoor, geen zin om die shit weer opnieuw te schrijven
                                ArtikelAdapter.fillView(header, artikel, getApplicationContext(), false);
                                comments.addHeaderView(header);

                                comments.setAdapter(new CommentAdapter(Article.this, 0, artikel.comments));

                                comments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        new CommentDialog(artikel.comments[position], artikel, activity).show(getSupportFragmentManager(), "CmmntDlg");
                                    }
                                });

                                switchState(STATE_SHOW);
                            }
                        });
                    } catch (Exception z) {
                        z.printStackTrace();
                        runOnUiThread(new Runnable() {
                            public void run() {
                                switchState(STATE_ERROR);
                            }
                        });
                    }
                } else
                    throw new InvalidParameterException("Didn't pass a uri to open");
            }
        }).start();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getSupportMenuInflater();
        if(!API.loggedIn(this))
            inflater.inflate(R.menu.article_actions, menu);
        else inflater.inflate(R.menu.article_actions_loggedin, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String data = getIntent().getData().toString();
                if(currentArtikel != null)  data = currentArtikel.titel + " (" + data + ")";
                sendIntent.putExtra(Intent.EXTRA_TEXT, data + " - via GeenStijl Reader http://is.gd/gsreader");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with_friends)));
                return true;
            case R.id.action_reply:
                //todo mogelijke nullpointer als je reply klikt voordat het artikel geladen is
                //todo betere oplossing lol (btn disablen?)
                if(currentArtikel == null) return true;
                new ReplyDialog(this, currentArtikel).show(getSupportFragmentManager(), "RplDg");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
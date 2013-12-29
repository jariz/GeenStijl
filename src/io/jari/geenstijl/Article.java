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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
import io.jari.geenstijl.Adapters.CommentAdapter;
import io.jari.geenstijl.Dialogs.CommentDialog;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * JARI.IO
 * Date: 24/12/13
 * Time: 01:46
 */
public class Article extends Base {
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
                        runOnUiThread(new Runnable() {
                            public void run() {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.article_actions, menu);
        return super.onCreateOptionsMenu(menu);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, getIntent().getData().toString());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_with_friends)));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
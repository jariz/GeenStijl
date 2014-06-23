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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
import io.jari.geenstijl.Adapters.CommentAdapter;
import io.jari.geenstijl.Dialogs.ReplyDialog;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.security.InvalidParameterException;

/**
 * JARI.IO
 * Date: 24/12/13
 * Time: 01:46
 */
public class Article extends Base {

    Artikel currentArtikel = null;
    String currentURL = null;

    boolean forceNoImmersive = false;
    ActionBar actionBar;

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.article);

        super.onCreate(savedInstanceState);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);

        new Thread(new Runnable() {
            public void run() {
                if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                    String url = getIntent().getData().toString();
                    currentURL = url;

                    try {
                        final Artikel artikel = API.getArticle(url, Article.this);
                        currentArtikel = artikel;
                        initUI(artikel);

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

    View header;


    int showTopPadding = 0;

    void initUI(final Artikel artikel) {
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

                final ListView comments = (ListView) findViewById(R.id.show);

                if(Article.this.header != null)
                    comments.removeHeaderView(Article.this.header);

                ActionBar bar = getSupportActionBar();
                bar.setDisplayHomeAsUpEnabled(true);
                bar.setTitle(artikel.titel);

                View header = Article.this.header = getLayoutInflater().inflate(R.layout.blog_item, null);
                //even artikeladapter lenen hiervoor, geen zin om die shit weer opnieuw te schrijven
                ArtikelAdapter.fillView(header, artikel, Article.this, false);

                comments.addHeaderView(header);

                comments.setAdapter(new CommentAdapter(Article.this, 0, artikel.comments));

                if(showTopPadding == 0) showTopPadding = comments.getPaddingTop();
                comments.setScrollingCacheEnabled(false);

                //#HOLOYOLO
                if(Build.VERSION.SDK_INT >= 19) {
                    SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
                    comments.setPadding(0, showTopPadding + config.getPixelInsetTop(true), 0, 0);
                }

                //hiding the actionbar when scrolling
                actionBar = getSupportActionBar();
                comments.setOnScrollListener(new AbsListView.OnScrollListener() {
                    int mLastFirstVisibleItem = 0;

                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (view.getId() == comments.getId()) {
                            final int currentFirstVisibleItem = comments.getFirstVisiblePosition();

                            if (currentFirstVisibleItem > mLastFirstVisibleItem && actionBar.isShowing()) {
                                enableImmersive(true, comments);
                            } else if (currentFirstVisibleItem < mLastFirstVisibleItem && !actionBar.isShowing()) {
                                enableImmersive(false, comments);
                            }

                            mLastFirstVisibleItem = currentFirstVisibleItem;
                        }
                    }
                });

//                comments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        new CommentDialog(artikel.comments[position - 1], artikel, Article.this).show(getSupportFragmentManager(), "CmmntDlg");
//                    }
//                });

                final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
                ActionBarPullToRefresh.from(Article.this)
                        .allChildrenArePullable()
                        .listener(new OnRefreshListener() {
                            public void onRefreshStarted(View view) {
                                try {
                                    new Thread(new Runnable() {
                                        public void run() {
                                            Artikel artikel = null;
                                            try {
                                                artikel = API.getArticle(currentURL, Article.this);
                                            } catch (final Exception e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    public void run() {
                                                        mPullToRefreshLayout.setRefreshComplete();
                                                        Crouton.makeText(Article.this, e.getMessage(), Style.ALERT).show();
                                                    }
                                                });
                                            }
                                            if (artikel != null) {
                                                currentArtikel = artikel;
                                                initUI(artikel);
                                            }
                                        }
                                    }).start();
                                } catch (final Exception z) {
                                    z.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            mPullToRefreshLayout.setRefreshComplete();
                                            Crouton.makeText(Article.this, z.getMessage(), Style.ALERT).show();
                                        }
                                    });
                                }
                            }
                        })
                        .setup(mPullToRefreshLayout);

                switchState(STATE_SHOW);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getSupportMenuInflater();
        if (!API.loggedIn(this))
            inflater.inflate(R.menu.article_actions, menu);
        else inflater.inflate(R.menu.article_actions_loggedin, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String data = getIntent().getData().toString();
                if (currentArtikel != null) data = currentArtikel.titel + " (" + data + ")";
                sendIntent.putExtra(Intent.EXTRA_TEXT, data + " - via GeenStijl Reader http://is.gd/gsreader");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_with_friends)));
                return true;
            case R.id.action_reply:
                if (currentArtikel == null) return true;
                new ReplyDialog(this, currentArtikel).show(getSupportFragmentManager(), "RplDg");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    void enableImmersive(boolean immersive, View view) {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_actionbar", true)) return;

        if(immersive && !forceNoImmersive) {
            if(Build.VERSION.SDK_INT >= 19) {
                tintManager.setStatusBarAlpha(0.5f);
                view.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE);
            }
            actionBar.hide();
        } else {
            if(Build.VERSION.SDK_INT >= 19) {
                tintManager.setStatusBarAlpha(1f);
                view.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            actionBar.show();
        }
    }
}
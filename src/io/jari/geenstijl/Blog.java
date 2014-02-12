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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
import io.jari.geenstijl.Dialogs.ConfirmLogoutDialog;
import io.jari.geenstijl.Dialogs.LoginDialog;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.util.ArrayList;
import java.util.Arrays;

public class Blog extends Base {
    String TAG = "GS.MAIN";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity thiz = this;

        setContentView(R.layout.blog);

        //#HOLOYOLO
        if(Build.VERSION.SDK_INT >= 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            ViewGroup.LayoutParams params = findViewById(R.id.ptr_layout).getLayoutParams();
            //jariz's home made actionbar hack!
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            ((ViewGroup.MarginLayoutParams)params).topMargin = config.getPixelInsetTop(true);
        }

        final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        final ListView show = (ListView)findViewById(R.id.show);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    public void onRefreshStarted(View view) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    final Artikel[] artikelen = API.getArticles(true, false, getApplicationContext());
                                    initUI(artikelen, false);
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            mPullToRefreshLayout.setRefreshComplete();
                                            Crouton.makeText(Blog.this, e.getMessage(), Style.ALERT, R.id.ptr_layout).show();
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                })
                .setup(mPullToRefreshLayout);

        new Thread(new Runnable() {
            public void run() {
                try {
                    final Artikel[] artikelen = API.getArticles(false, false, getApplicationContext());
                    initUI(artikelen, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            switchState(STATE_ERROR);
                        }
                    });
                }
            }
        }).start();
    }

    void initUI(final Artikel[] artikelen, final boolean doSwitchState) {
        final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        final ListView show = (ListView)findViewById(R.id.show);
        runOnUiThread(new Runnable() {
            public void run() {
                ListView show = (ListView) findViewById(R.id.show);
                show.setScrollingCacheEnabled(false);
                show.setAdapter(new ArtikelAdapter(Blog.this, 0, artikelen));
                //footer
                if (show.getAdapter().getClass() != HeaderViewListAdapter.class) { //check if footer is present, if not, add it
                    View footer = getLayoutInflater().inflate(R.layout.meerrr, null);
                    final View button = footer.findViewById(R.id.more);
                    if (Build.VERSION.SDK_INT >= 19) {
                        SystemBarTintManager tintManager = new SystemBarTintManager(Blog.this);
                        ViewGroup.LayoutParams params = button.getLayoutParams();
                        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
                        ((ViewGroup.MarginLayoutParams) params).bottomMargin = config.getPixelInsetBottom();
                    }
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            button.setEnabled(false);
                            mPullToRefreshLayout.setRefreshing(true);
                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        final Artikel[] artikelen2 = API.getArticles(true, true, getApplicationContext());
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                ListView show = (ListView) findViewById(R.id.show);
                                                //java is such a beautiful language *cough*
                                                ArtikelAdapter artikelAdapter = (ArtikelAdapter) ((HeaderViewListAdapter) show.getAdapter()).getWrappedAdapter();
                                                artikelAdapter.update(artikelen2);
                                                mPullToRefreshLayout.setRefreshComplete();
                                                button.setVisibility(View.GONE);
                                            }
                                        });
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                button.setEnabled(true);
                                                mPullToRefreshLayout.setRefreshComplete();
                                                Crouton.makeText(Blog.this, e.getMessage(), Style.ALERT, R.id.ptr_layout).show();
                                            }
                                        });
                                    }
                                }
                            }).start();

                        }
                    });
                    show.addFooterView(footer);
                }


                //grreat success!
                if(doSwitchState) switchState(STATE_SHOW);
                else mPullToRefreshLayout.setRefreshComplete();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getSupportMenuInflater();
        if(!API.loggedIn(this))
            inflater.inflate(R.menu.blog_actions, menu);
        else inflater.inflate(R.menu.blog_actions_loggedin, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_login:
                new LoginDialog(this).show(getSupportFragmentManager(), "LgnDg");
                return true;
            case R.id.action_logout:
                new ConfirmLogoutDialog(this).show(getSupportFragmentManager(), "LgtCnfrmDg");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}


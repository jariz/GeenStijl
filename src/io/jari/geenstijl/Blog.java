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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
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
            ViewGroup.LayoutParams params = ((ListView)findViewById(R.id.show)).getLayoutParams();
            //jariz's home made actionbar hack!
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            ((ViewGroup.MarginLayoutParams)params).topMargin = config.getPixelInsetTop(true);
        }

        // Now find the PullToRefreshLayout to setup
        final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    public void onRefreshStarted(View view) {
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    final Artikel[] artikelen = API.getArticles(true, false, getApplicationContext());
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            ListView show = (ListView)findViewById(R.id.show);
                                            show.setScrollingCacheEnabled(false);
                                            show.setAdapter(new ArtikelAdapter(thiz, 0, artikelen));
                                            mPullToRefreshLayout.setRefreshComplete();
                                        }
                                    });

                                } catch (Exception e) {
                                    Log.w(TAG, "Catched exception, going to error state.");
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
                })
                .setup(mPullToRefreshLayout);

        new Thread(new Runnable() {
            public void run() {
                try {
                    final Artikel[] artikelen = API.getArticles(false, false, getApplicationContext());
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ListView show = (ListView)findViewById(R.id.show);
                            show.setScrollingCacheEnabled(false);
                            show.setAdapter(new ArtikelAdapter(thiz, 0, artikelen));

                            //footer
                            View footer = getLayoutInflater().inflate(R.layout.meerrr, null);
                            final View button = footer.findViewById(R.id.more);
                            if(Build.VERSION.SDK_INT >= 19) {
                                SystemBarTintManager tintManager = new SystemBarTintManager(thiz);
                                ViewGroup.LayoutParams params = button.getLayoutParams();
                                SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
                                ((ViewGroup.MarginLayoutParams)params).bottomMargin = config.getPixelInsetBottom();
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
                                                        ArtikelAdapter artikelAdapter = (ArtikelAdapter)((HeaderViewListAdapter)show.getAdapter()).getWrappedAdapter();
                                                        artikelAdapter.update(artikelen2);
                                                        mPullToRefreshLayout.setRefreshComplete();
                                                        button.setVisibility(View.GONE);
                                                    }
                                                });
                                            } catch (Exception e) {
                                                Log.w(TAG, "Catched exception, going to error state.");
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
                            });
                            show.addFooterView(footer);

                            //grreat success!
                            switchState(STATE_SHOW);
                        }
                    });

                } catch (Exception e) {
                    Log.w(TAG, "Catched exception, going to error state.");
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
}


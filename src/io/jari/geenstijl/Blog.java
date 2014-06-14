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
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import io.jari.geenstijl.Dialogs.ConfirmLogoutDialog;
import io.jari.geenstijl.Dialogs.LoginDialog;
import it.gmariotti.changelibs.library.view.ChangeLogListView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class Blog extends Base {
    String TAG = "GS.MAIN";
    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;

    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.blog);

        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();
        final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);

        View drawer = findViewById(R.id.left_drawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                enableImmersive(false, drawerView);
//            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                enableImmersive(false, drawerLayout);
            }

//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//                enableImmersive(false, drawerView);
//            }
        };
        drawerLayout.setDrawerListener(drawerToggle);

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        if(Build.VERSION.SDK_INT >= 19) {
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            drawer.setPadding(drawer.getPaddingLeft(), drawer.getPaddingTop() + config.getPixelInsetTop(true), drawer.getPaddingRight(), drawer.getPaddingBottom());
        }
        ListView siteSwitch = (ListView)drawer.findViewById(R.id.site_switcher);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, R.id.wrap_text, new String[] {"GeenStijl.nl", "GeenStijl.tv"});
        siteSwitch.setAdapter(arrayAdapter);
        siteSwitch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        API.setDomain("www.geenstijl.nl", Blog.this);
                        break;
                    case 1:
                        API.setDomain("www.geenstijl.tv", Blog.this);
                        break;
                }

                forceNoImmersive = true;
                enableImmersive(false, drawerLayout);
                drawerLayout.closeDrawers();
                mPullToRefreshLayout.setRefreshing(true);
                new Thread(new Runnable() {
                    public void run() {
                        forceNoImmersive = true;
                        try {
                            final Artikel[] artikelen = API.getArticles(true, false, getApplicationContext());
                            initUI(artikelen, false);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    mPullToRefreshLayout.setRefreshComplete();
                                    forceNoImmersive = false;
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    forceNoImmersive = false;
                                    mPullToRefreshLayout.setRefreshComplete();
                                    Crouton.makeText(Blog.this, e.getLocalizedMessage() == null ? "Onbekende fout" : e.getLocalizedMessage(), Style.ALERT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(new OnRefreshListener() {
                    public void onRefreshStarted(View view) {
                        new Thread(new Runnable() {
                            public void run() {
                                forceNoImmersive = true;
                                try {
                                    final Artikel[] artikelen = API.getArticles(true, false, getApplicationContext());
                                    initUI(artikelen, false);
                                } catch (final Exception e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            forceNoImmersive = false;
                                            mPullToRefreshLayout.setRefreshComplete();
                                            Crouton.makeText(Blog.this, e.getLocalizedMessage() == null ? "Onbekende fout" : e.getLocalizedMessage(), Style.ALERT).show();
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
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            errorMessage = e.getMessage();
                            switchState(STATE_ERROR);
                        }
                    });
                }
            }
        }).start();

        //do changelog stuff
        SharedPreferences sPref = this.getSharedPreferences("geenstijl", 0);
        int version = 0;
        try {
            version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {}

        //is changelog already read
        if(!sPref.getBoolean("changelog-"+version, false)) {
            //set changelog to read
            sPref.edit().putBoolean("changelog-"+version, true).commit();

            //show dialog
            LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            ChangeLogListView chgList=(ChangeLogListView)layoutInflater.inflate(R.layout.changelog, null);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.changelog_title)
                    .setView(chgList)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .create()
                    .show();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    boolean forceNoImmersive = false;

    ActionBar actionBar;

    @SuppressLint("NewApi")
    void enableImmersive(boolean immersive, View view) {

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

    int showTopPadding = 0;
    void initUI(final Artikel[] artikelen, final boolean doSwitchState) {
        final PullToRefreshLayout mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        runOnUiThread(new Runnable() {
            public void run() {
                ListView siteSwitch = (ListView)findViewById(R.id.site_switcher);
                String domain = getSharedPreferences("geenstijl", 0).getString("gsdomain", "www.geenstijl.nl");
                if(domain.equals("www.geenstijl.nl")) {
                    actionBar.setTitle("GeenStijl");
                    siteSwitch.setItemChecked(0, true);
                }
                else {
                    actionBar.setTitle("GeenStijl.TV");
                    siteSwitch.setItemChecked(1, true);
                }

                final ListView show = (ListView) findViewById(R.id.show);
                if(showTopPadding == 0) showTopPadding = show.getPaddingTop();
                show.setScrollingCacheEnabled(false);
                show.setAdapter(new ArtikelAdapter(Blog.this, 0, artikelen));

                //#HOLOYOLO
                if(Build.VERSION.SDK_INT >= 19) {
                    ViewGroup.LayoutParams params = show.getLayoutParams();
                    //jariz's home made actionbar hack!
                    SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
                    show.setPadding(0, showTopPadding + config.getPixelInsetTop(true), 0, 0);
                }

                //hiding the actionbar when scrolling
                show.setOnScrollListener(new AbsListView.OnScrollListener() {
                    int mLastFirstVisibleItem = 0;
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                    }

                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        if (view.getId() == show.getId()) {
                            final int currentFirstVisibleItem = show.getFirstVisiblePosition();

                            if (currentFirstVisibleItem > mLastFirstVisibleItem && actionBar.isShowing()) {
                                enableImmersive(true, show);
                            } else if (currentFirstVisibleItem < mLastFirstVisibleItem && !actionBar.isShowing()) {
                                enableImmersive(false, show);
                            }

                            mLastFirstVisibleItem = currentFirstVisibleItem;
                        }
                    }
                });

                //footer
                if (show.getAdapter().getClass() != HeaderViewListAdapter.class) { //check if footer is present, if not, add it
                    View footer = getLayoutInflater().inflate(R.layout.meerrr, null);
                    final View button = footer.findViewById(R.id.more);
                    if (Build.VERSION.SDK_INT >= 19) {
                        ViewGroup.LayoutParams params = button.getLayoutParams();
                        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
                        ((ViewGroup.MarginLayoutParams) params).bottomMargin = config.getPixelInsetBottom();
                    }
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            button.setEnabled(false);
                            mPullToRefreshLayout.setRefreshing(true);
                            enableImmersive(false, show);
                            forceNoImmersive = true;
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
                                                forceNoImmersive = false;
                                            }
                                        });
                                    } catch (final Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                button.setEnabled(true);
                                                mPullToRefreshLayout.setRefreshComplete();
                                                forceNoImmersive = false;
                                                Crouton.makeText(Blog.this, e.getLocalizedMessage() == null ? "Onbekende fout" : e.getLocalizedMessage(), Style.ALERT).show();
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
            case android.R.id.home:
                View drawer = findViewById(R.id.left_drawer);
                if(!drawerLayout.isDrawerOpen(drawer))
                    drawerLayout.openDrawer(drawer);
                else drawerLayout.closeDrawer(drawer);
                break;
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


package io.jari.geenstijl;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import io.jari.geenstijl.API.API;
import io.jari.geenstijl.API.Artikel;
import io.jari.geenstijl.Adapters.ArtikelAdapter;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

public class Blog extends Base {
    int STATE_ERROR = 1;
    int STATE_LOADING = 2;
    int STATE_SHOW = 3;
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
                                    final Artikel[] artikelen = API.getArticles();
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
                    final Artikel[] artikelen = API.getArticles();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            ListView show = (ListView)findViewById(R.id.show);
                            show.setScrollingCacheEnabled(false);
                            show.setAdapter(new ArtikelAdapter(thiz, 0, artikelen));

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


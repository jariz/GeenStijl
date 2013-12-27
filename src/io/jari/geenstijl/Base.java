package io.jari.geenstijl;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //are you #bebebe?
        if(Build.VERSION.SDK_INT >= 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setTintColor(getResources().getColor(R.color.geenstijl));
            tintManager.setStatusBarTintEnabled(true);
        }

        getSupportActionBar().setIcon(R.drawable.icon);
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

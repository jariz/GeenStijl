package io.jari.geenstijl;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * JARI.IO
 * Date: 16-12-13
 * Time: 23:48
 */
public class Base extends SherlockActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //are you #bebebe?
        if(Build.VERSION.SDK_INT >= 19) {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setTintColor(Color.parseColor("#E94C88"));
            tintManager.setStatusBarTintEnabled(true);
        }
    }
}

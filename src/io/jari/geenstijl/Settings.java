package io.jari.geenstijl;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * JARI.IO
 * Date: 15-6-14
 * Time: 2:07
 */
public class Settings extends SherlockPreferenceActivity {
    SystemBarTintManager tintManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        //are you #bebebe?
        if(Build.VERSION.SDK_INT >= 19) {
            this.tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintResource(R.color.geenstijl);
            tintManager.setStatusBarAlpha(1f);
            tintManager.setStatusBarTintEnabled(true);
            ViewGroup.LayoutParams params = findViewById(android.R.id.list).getLayoutParams();
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            ((ViewGroup.MarginLayoutParams) params).topMargin = config.getPixelInsetTop(true);
        }

        getSupportActionBar().setIcon(R.drawable.icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return false;
        }
    }
}
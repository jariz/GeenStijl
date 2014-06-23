package io.jari.geenstijl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

import java.security.InvalidParameterException;

/**
 * JARI.IO
 * Date: 22-6-14
 * Time: 14:39
 */
public class Browser extends SherlockFragmentActivity {

    String currentURL;
    SystemBarTintManager tintManager;

    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);

        final SmoothProgressBar smoothProgressBar = (SmoothProgressBar)findViewById(R.id.smoothProgressBar);
        smoothProgressBar.progressiveStop();

        //are you #bebebe?
        if(Build.VERSION.SDK_INT >= 19) {
            this.tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintResource(R.color.geenstijl);
            tintManager.setStatusBarAlpha(1f);
            tintManager.setStatusBarTintEnabled(true);
            ViewGroup.LayoutParams params = findViewById(R.id.browserLAYOUT).getLayoutParams();
            SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
            ((ViewGroup.MarginLayoutParams) params).topMargin = config.getPixelInsetTop(true);
            smoothProgressBar.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        getSupportActionBar().setIcon(R.drawable.icon);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            String url = getIntent().getData().toString();
            currentURL = url;

            WebView webView = (WebView)findViewById(R.id.webView);
            webView.loadUrl(url);

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient(){
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    getSupportActionBar().setTitle(R.string.app_name);
                    smoothProgressBar.progressiveStart();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    getSupportActionBar().setTitle(view.getTitle());
                    smoothProgressBar.progressiveStop();
                }
            });

        } else throw new InvalidParameterException("No URL provided for the browser to open");
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

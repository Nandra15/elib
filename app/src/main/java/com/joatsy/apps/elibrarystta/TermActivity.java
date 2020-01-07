package com.joatsy.apps.elibrarystta;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.joatsy.apps.elibrarystta.MainActivity.SERVER_ADDRS;
import static com.joatsy.apps.elibrarystta.MainActivity.user_agent;

public class TermActivity extends AppCompatActivity {
    WebView webterm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);
        webterm = (WebView) findViewById(R.id.web_term);
        webterm.getSettings().setJavaScriptEnabled(true);
        webterm.getSettings().setBuiltInZoomControls(true);
        webterm.getSettings().setDisplayZoomControls(true);
        webterm.setWebViewClient(new TermActivity.Callback());
        String url = SERVER_ADDRS + "term.php";
        webterm.loadUrl(url);
        webterm.getSettings().setUserAgentString(user_agent);
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, String url) {
            return(false);
        }
    }
}

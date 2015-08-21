package com.tsukucha.tsukuchaapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.KeyEvent;
import android.widget.TextView;
import android.graphics.Color;


public class MainActivity extends Activity {
    WebView webView;
    TextView textView;
    MyCountDownTimer cdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView)findViewById(R.id.textView);
        textView.setBackgroundColor(Color.argb(0,0,0,0));
        webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://cookpad.com/");

        cdt = new MyCountDownTimer(1200000, 1000);
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            //textView.setBackgroundColor(Color.argb(255,255,0,0));
        }

        public void onFinish() {
            textView.setText("");
            textView.setBackgroundColor(Color.argb(0,0,0,0));
        }

        public void onTick(long millisUntilFinished) {
            textView.setText(Long.toString(millisUntilFinished/1000/60) + ":" + Long.toString(millisUntilFinished/1000%60));
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    webView.pageUp(false);
                    return true;
                    }
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    webView.pageDown(false);
                    return true;
                    }
            case KeyEvent.KEYCODE_POWER:
                if(event.getAction() == KeyEvent.ACTION_DOWN) {
                    textView.setBackgroundColor(Color.argb(255,255,0,0));
                    cdt.start();
                    }
                }
        super.dispatchKeyEvent(event);
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            WebView webView = (WebView)findViewById(R.id.webView);
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

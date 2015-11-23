package com.example.lin.handsfreerecipe;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private Button btnStart;
    private TextView recognizedWord;
    private ProgressBar voiceRMS;
    //private TextView simpleList;
    private TextView textView2;
    private TextView textView4;
    private SpeechRecognizer speechRecognizer = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "HandsFreeRecipe";
    private CountDownTimer mTimer = null;
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;
    private boolean started = false;
    private WebView webView;
    private WebView webView2;
    private MyCountDownTimer cdt;
    private Boolean recog_timer = false;
    private int minutes = 0;
    private int seconds = 0;
    final int MENU_TIMER = 0;
    final int MENU_TIMER_STOP = 1;
    final int MAKE_TAB = 2;
    final int CHANGE_TAB = 3;
    final int SELECT_SITE = 4;
    final int CHANGE_SCROLL_VERTICAL = 5;
    final int CHANGE_SCROLL_HORIZON = 6;
    SoundPool soundPool;
    private int sound;

    private Button google;
    private Button cookpad;
    private Button rakuten;
    private Button excite;
    private int tab = 1;
    private Boolean tab1 = false;
    private Boolean tab2 = false;

    private int width = 0;
    private int height = 0;
    private int scroll_Vertical = 300;
    private int scroll_Horizon = 300;
    private String[] items = new String[4];
    private int[] list_vertical = new int[4];
    private int[] list_horizon = new int[4];
    private int vertical = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.setVisibility(View.INVISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView2 = (WebView)findViewById(R.id.webView2);
        webView2.setWebViewClient(new WebViewClient());
        webView2.setVisibility(View.INVISIBLE);
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.getSettings().setBuiltInZoomControls(true);
        cdt = new MyCountDownTimer(1200000, 1000);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView2.setBackgroundColor(Color.argb(0, 0, 0, 0));
        textView2.setTextColor(Color.WHITE);
        textView4 = (TextView)findViewById(R.id.textView4);
        textView4.setBackgroundColor(Color.argb(0, 20, 20, 20));
        textView4.setTextColor(Color.BLACK);
        textView4.setVisibility(View.INVISIBLE);
        //The Button to switch start/end speech recognition
        btnStart = (Button) findViewById(R.id.start_cooking);
        btnStart.setText("Start cooking!");
        //The ProgressBar to visualize the voice
        voiceRMS = (ProgressBar) findViewById(R.id.voice_rms);
        voiceRMS.setVisibility(View.INVISIBLE);
        //The TextView to hold the recognized words
        recognizedWord = (TextView) findViewById(R.id.recognition_result);
        //The TextView to be tested scrolling on
        //simpleList = (TextView) findViewById(R.id.suggested_words);
        //simpleList.setMovementMethod(new ScrollingMovementMethod());
        // Initialize the SpeechRecognizer object
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        //Create recognizerIntent and set some extra info
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ja");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        //Start speech recognition when
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (started == false) {
                    btnStart.setText("Cooking...");
                    //Show the voice visualization
                    voiceRMS.setVisibility(View.VISIBLE);
                    voiceRMS.setIndeterminate(true);
                    speechRecognizer.startListening(recognizerIntent);
                    //Mute the sound effect while start listening
                    mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    started = true;
                } else {
                    started = false;
                    recognizedWord.setText("You just said...");
                    btnStart.setText("Start cooking!");
                    Log.d(LOG_TAG, "Stop Listening");
                    voiceRMS.setIndeterminate(false);
                    voiceRMS.setVisibility(View.INVISIBLE);
                    speechRecognizer.stopListening();

                }
            }
        });

        google = (Button)findViewById(R.id.google);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tab == 1) {
                    create_tab1("http://www.google.co.jp/");
                }
                else if(tab == 2) {
                    create_tab2("http://www.google.co.jp/");
                }
                invisible_button();
            }
        });

        cookpad = (Button)findViewById(R.id.cookpad);
        cookpad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tab == 1) {
                    create_tab1("http://cookpad.com/");
                }
                else if(tab == 2) {
                    create_tab2("http://cookpad.com/");
                }
                invisible_button();
            }
        });

        rakuten = (Button)findViewById(R.id.rakuten);
        rakuten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tab == 1) {
                    create_tab1("http://recipe.rakuten.co.jp/");
                }
                else if(tab == 2) {
                    create_tab2("http://recipe.rakuten.co.jp/");
                }
                invisible_button();
            }
        });

        excite = (Button)findViewById(R.id.excite);
        excite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tab == 1) {
                    create_tab1("http://erecipe.woman.excite.co.jp/");
                }
                else if(tab == 2) {
                    create_tab2("http://erecipe.woman.excite.co.jp/");
                }
                invisible_button();
            }
        });
    }

    public void visible_button() {
        google.setVisibility(View.VISIBLE);
        cookpad.setVisibility(View.VISIBLE);
        rakuten.setVisibility(View.VISIBLE);
        excite.setVisibility(View.VISIBLE);
    }

    public void invisible_button() {
        google.setVisibility(View.INVISIBLE);
        cookpad.setVisibility(View.INVISIBLE);
        rakuten.setVisibility(View.INVISIBLE);
        excite.setVisibility(View.INVISIBLE);
    }

    public void create_tab1(String url) {
        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
        tab1 = true;
        textView4.setText("tab1");
        textView4.setVisibility(View.VISIBLE);
    }

    public void create_tab2(String url) {
        webView2.loadUrl(url);
        webView2.setVisibility(View.VISIBLE);
        tab2 = true;
        textView4.setText("tab2");
        textView4.setVisibility(View.VISIBLE);
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            soundPool.play(sound, 1.0f, 1.0f, 0, 0, 1);
            clearTimer();
        }

        public void onTick(long millisUntilFinished) {
            String colon = "";
            if(millisUntilFinished/1000%60 < 10)
                colon = ":0";
            else
                colon = ":";
            textView2.setText(Long.toString(millisUntilFinished/1000/60) + colon + Long.toString(millisUntilFinished / 1000 % 60));
        }
    }

    public void clearTimer() {
        textView2.setText("");
        textView2.setBackgroundColor(Color.argb(0, 0, 0, 0));
        recog_timer = false;
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
                    textView2.setBackgroundColor(Color.argb(255,255,0, 0));
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

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        width = findViewById(R.id.webView).getWidth();
        height = findViewById(R.id.webView).getHeight();

        items[0] = "1/4ページ";
        items[1] = "半ページ";
        items[2] = "１ページ弱";
        items[3] = "１ページ";

        list_vertical[0] = (int)((double)height*0.25);
        list_vertical[1] = (int)((double)height*0.5);
        list_vertical[2] = (int)((double)height*0.8);
        list_vertical[3] = height;

        list_horizon[0] = (int)((double)width*0.25);
        list_horizon[1] = (int)((double)width*0.5);
        list_horizon[2] = (int)((double)width*0.8);
        list_horizon[3] = width;

        SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        int index = data.getInt("vertical_index", 300);
        if(index == 300)
            scroll_Vertical = 300;
        else
            scroll_Vertical = list_vertical[index];

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_TIMER, 0, "タイマー起動");
        menu.add(0, MENU_TIMER_STOP, 0, "タイマー終了");
        menu.add(0, MAKE_TAB, 0, "タブ作成");
        menu.add(0, CHANGE_TAB, 0, "タブ切り替え");
        menu.add(0, SELECT_SITE, 0, "サイト選択");
        menu.add(0, CHANGE_SCROLL_VERTICAL, 0, "上下スクロール量");
        menu.add(0, CHANGE_SCROLL_HORIZON, 0, "左右スクロール量");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == MENU_TIMER) {
            startTimer();
            return true;
        }
        else if (id == MENU_TIMER_STOP) {
            cdt.cancel();
            clearTimer();
            return true;
        }
        else if (id == MAKE_TAB) {
            if(!tab1) {
                Toast.makeText(this, "please make tab1", Toast.LENGTH_LONG).show();
            }
            else if(!tab2) {
                webView.setVisibility(View.INVISIBLE);
                visible_button();
                tab = 2;
                textView4.setVisibility(View.INVISIBLE);
            }
            else if(tab2) {
                Toast.makeText(this, "you can make 2 tabs", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == CHANGE_TAB) {
            if(!tab2) {
                Toast.makeText(this, "there is only tab1", Toast.LENGTH_SHORT).show();
            }
            else if(tab == 1) {
                webView.setVisibility(View.INVISIBLE);
                webView2.setVisibility(View.VISIBLE);
                tab = 2;
                textView4.setText("tab2");
            }
            else if(tab == 2) {
                webView2.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                invisible_button();
                tab = 1;
                textView4.setText("tab1");
            }
        }
        else if (id == SELECT_SITE) {
            if(tab == 1) {
                webView.setVisibility(View.INVISIBLE);
                tab1 = false;
                textView4.setVisibility(View.INVISIBLE);
            }
            else if(tab == 2) {
                webView2.setVisibility(View.INVISIBLE);
                tab2 = false;
                textView4.setVisibility(View.INVISIBLE);
            }
            visible_button();
        }
        else if (id == CHANGE_SCROLL_VERTICAL) {
            new AlertDialog.Builder(this)
                    .setTitle("上下スクロール量の設定")
                    .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            scroll_Vertical = list_vertical[which];
                            vertical = which;
                            SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = data.edit();
                            editor.putInt("vertical_index", which);
                            editor.apply();
                        }
                    })
                    .setPositiveButton("Close", null) .show();
        }
        else if (id == CHANGE_SCROLL_HORIZON) {
            new AlertDialog.Builder(this)
                    .setTitle("左右スクロール量の設定")
                    .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            scroll_Horizon = list_horizon[which];
                        }
                    })
                    .setPositiveButton("Close", null) .show();
        }

        return super.onOptionsItemSelected(item);
    }

    public void startTimer() {
        final EditText editView = new EditText(this);
        editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle("タイマー（分）")
                .setView(editView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int time = Integer.parseInt(editView.getText().toString());
                        cdt = new MyCountDownTimer(time * 60000, 1000);
                        cdt.start();
                        textView2.setBackgroundColor(Color.argb(255, 255, 0, 0));
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void setTimer() {
        final EditText editView = new EditText(this);
        //editView.setInputType(InputType.TYPE_CLASS_NUMBER);
        textView2.setBackgroundColor(Color.argb(255, 0, 0, 255));
        textView2.setText("時間入力...");
        recog_timer = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        soundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);
        sound = soundPool.load(this, R.raw.timer_sound, 1);
    }

    protected void onPause() {
        super.onPause();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.i(LOG_TAG, "destroy");
        }
        soundPool.release();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        //Restore the sound when speech has begun
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
        Log.i(LOG_TAG, "onReadyForSpeech:Cancel Timer");
        if(mTimer != null)
            mTimer.cancel();

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        voiceRMS.setIndeterminate(false);
        voiceRMS.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        voiceRMS.setProgress((int) rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
            voiceRMS.setIndeterminate(true);
            speechRecognizer.stopListening();
    }

    @Override
    public void onError(int errorCode) {
        if(started == true){
            String errorMessage = getErrorText(errorCode);
            Log.d(LOG_TAG, "FAILED " + errorMessage);
            recognizedWord.setText(errorMessage);
            if(mTimer != null)
                mTimer.cancel();
            speechRecognizer.cancel();
            restartListen();
        }
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches){
            if(recog_timer) {
                String[] character = result.split("");
                int minutes_index = 0;
                int seconds_index = 0;
                for(int n=0; n<character.length; n++) {
                    if(character[n].equals("分"))
                        minutes_index = n;
                    if(character[n].equals("秒"))
                        seconds_index = n;
                }
                if(minutes_index != 0 || seconds_index != 0) {
                    text = "時間入力";
                    String minutes_string = "";
                    String seconds_string = "";
                    minutes = 0;
                    seconds = 0;
                    if(minutes_index != 0) {
                        for (int n = 0; n < minutes_index; n++) {
                            minutes_string += character[n];
                        }
                        minutes = Integer.parseInt(minutes_string);
                    }
                    if(seconds_index != 0) {
                        for (int n = minutes_index + 1; n < seconds_index; n++) {
                            seconds_string += character[n];
                        }
                        seconds = Integer.parseInt(seconds_string);
                    }
                    String zero = "";
                    if(seconds < 10)
                        zero = "0";
                    textView2.setText(Integer.toString(minutes) + ":" + zero + Integer.toString(seconds));
                    break;
                }
                if(result.equals("スタート") || result.equals("start")) {
                    text = "タイマースタート";
                    cdt = new MyCountDownTimer(minutes * 60000 + seconds * 1000, 1000);
                    cdt.start();
                    textView2.setBackgroundColor(Color.argb(255, 255, 0, 0));
                    break;
                }
                if (result.equals("ストップ") || result.equals("stop")) {
                    text = "一時停止";
                    cdt.cancel();
                    String stop_time = textView2.getText().toString();
                    String[] stop_timers = stop_time.split(":", 0);
                    minutes = Integer.parseInt(stop_timers[0]);
                    seconds = Integer.parseInt(stop_timers[1]);
                    String zero = "";
                    if (seconds < 10)
                        zero = "0";
                    textView2.setText(Integer.toString(minutes) + ":" + zero + Integer.toString(seconds));
                    textView2.setBackgroundColor(Color.argb(255, 0, 0, 255));
                    break;
                }
                if (result.equals("リスタート") || result.equals("restart")) {
                    text = "再開";
                    cdt.cancel();
                    cdt = new MyCountDownTimer(minutes * 60000 + seconds * 1000, 1000);
                    cdt.start();
                    textView2.setBackgroundColor(Color.argb(255, 255, 0, 0));
                    break;
                }
            }
            if(result.equals("上") || result.equals("うえ") || result.equals("up")){
                text = "上にスクロール";
                if(tab == 1)
                    webView.scrollBy(0,-scroll_Vertical);
                else if(tab == 2)
                    webView2.scrollBy(0,-scroll_Vertical);
                break;
            }
            if(result.equals("下") || result.equals("した") || result.equals("down")){
                text = "下にスクロール";
                if(tab == 1)
                    webView.scrollBy(0,scroll_Vertical);
                else if(tab == 2)
                    webView2.scrollBy(0,scroll_Vertical);
                break;
            }
            if(result.equals("左") || result.equals("ひだり")|| result.equals("left")){
                text = "左にスクロール";
                if(tab == 1)
                    webView.scrollBy(-scroll_Horizon,0);
                else if(tab == 2)
                    webView2.scrollBy(-scroll_Horizon,0);
                break;
            }
            if(result.equals("右" )|| result.equals("みぎ") || result.equals("right")){
                text = "右にスクロール";
                if(tab == 1)
                    webView.scrollBy(scroll_Horizon,0);
                else if(tab == 2)
                    webView2.scrollBy(scroll_Horizon,0);
                break;
            }
            if(result.equals("タイマー") || result.equals("timer")){
                text = "タイマー起動";
                setTimer();
                break;
            }
            if(result.equals("リセット") || result.equals("reset")){
                text = "タイマー終了";
                cdt.cancel();
                clearTimer();
                break;
            }
            if(result.equals("タブ")) {
                if(tab == 1) {
                    if(!tab1) {
                        text = "please make tab1";
                    }
                    else if(!tab2) {
                        text = "make tab2";
                        webView.setVisibility(View.INVISIBLE);
                        visible_button();
                        tab = 2;
                    }
                    else if(tab2) {
                        text = "change tab2";
                        webView.setVisibility(View.INVISIBLE);
                        webView2.setVisibility(View.VISIBLE);
                        tab = 2;
                        textView4.setText("tab2");
                    }
                }
                else if(tab == 2) {
                    text = "change tab1";
                    webView2.setVisibility(View.INVISIBLE);
                    webView.setVisibility(View.VISIBLE);
                    invisible_button();
                    tab = 1;
                    textView4.setText("tab1");
                }
            }
            if(result.equals("拡大")) {
                text = "拡大";
                if(tab == 1)
                    webView.zoomIn();
                else if(tab == 2)
                    webView2.zoomIn();
            }
            if (result.equals("縮小")) {
                text = "縮小";
                if(tab == 1)
                    webView.zoomOut();
                else if(tab == 2)
                    webView2.zoomOut();
            }
            if (result.equals("増やす")) {
                if(vertical != 3) {
                    vertical++;
                    scroll_Vertical = list_vertical[vertical];
                    text = "上下スクロール量増加";
                    SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = data.edit();
                    editor.putInt("vertical_index", vertical);
                    editor.apply();
                }
                else {
                    text = "これ以上増えません";
                }
            }
            if (result.equals("減らす")) {
                if(vertical != 0) {
                    vertical--;
                    scroll_Vertical = list_vertical[vertical];
                    text = "上下スクロール量減少";
                    SharedPreferences data = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = data.edit();
                    editor.putInt("vertical_index", vertical);
                    editor.apply();
                } else {
                    text = "これ以上減りません";
                }
            }
        }
        recognizedWord.setText(text);
        btnStart.setText("Recognition finished");
        //Start listening again
        Log.i(LOG_TAG, "OnResults:Start listening again");
        if(mTimer != null)
            mTimer.cancel();
        restartListen();
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(LOG_TAG, "onEvent");
    }
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
    @Override
    protected void onDestroy (){
        super.onDestroy();
        if(mTimer != null)
           mTimer.cancel();
        if(speechRecognizer !=null)
            speechRecognizer.destroy();
    }

    //Restart listening while one recognition finishes or error occurs
    public void restartListen (){
        if(mTimer == null){
            mTimer = new CountDownTimer(1000,1000){
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    Log.d("Restart","Timer.onFinish: Time's up,restart listening");
                    btnStart.setText("Cooking...");
                    //Show the voice visualization
                    voiceRMS.setVisibility(View.VISIBLE);
                    voiceRMS.setIndeterminate(true);
                    speechRecognizer.startListening(recognizerIntent);
                    //Mute the sound effect while start listening
                    mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                }
            };
        }
        mTimer.start();
    }


}

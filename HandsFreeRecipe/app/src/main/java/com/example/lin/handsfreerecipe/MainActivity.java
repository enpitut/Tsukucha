package com.example.lin.handsfreerecipe;

import java.util.ArrayList;

import android.content.Context;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private Button btnStart;
    private TextView recognizedWord;
    private ProgressBar voiceRMS;
    private TextView simpleList;
    private SpeechRecognizer speechRecognizer = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "HandsFreeRecipe";
    private CountDownTimer mTimer = null;
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;
    private boolean started = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //The Button to switch start/end speech recognition
        btnStart = (Button) findViewById(R.id.start_cooking);
        btnStart.setText("Start cooking!");
        //The ProgressBar to visualize the voice
        voiceRMS = (ProgressBar) findViewById(R.id.voice_rms);
        voiceRMS.setVisibility(View.INVISIBLE);
        //The TextView to hold the recognized words
        recognizedWord = (TextView) findViewById(R.id.recognition_result);
        //The TextView to be tested scrolling on
        simpleList = (TextView) findViewById(R.id.suggested_words);
        simpleList.setMovementMethod(new ScrollingMovementMethod());
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
                if(started == false){
                    btnStart.setText("Cooking...");
                    //Show the voice visualization
                    voiceRMS.setVisibility(View.VISIBLE);
                    voiceRMS.setIndeterminate(true);
                    speechRecognizer.startListening(recognizerIntent);
                    //Mute the sound effect while start listening
                    mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    started = true;
                }
                else {
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

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            Log.i(LOG_TAG, "destroy");
        }

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
            if(result.equals("上") || result.equals("うえ") || result.equals("up")){
                text = "上にスクロール";
                simpleList.scrollBy(0,-48);
                break;
            }
            if(result.equals("下") || result.equals("した") || result.equals("down")){
                text = "下にスクロール";
                simpleList.scrollBy(0,48);
                break;
            }
            if(result.equals("左") || result.equals("ひだり")|| result.equals("left")){
                text = "左にスクロール";
                simpleList.scrollBy(-48, 0);
                break;
            }
            if(result.equals("右" )|| result.equals("みぎ") || result.equals("right")){
                text = "右にスクロール";
                simpleList.scrollBy(+48, 0);
                break;
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

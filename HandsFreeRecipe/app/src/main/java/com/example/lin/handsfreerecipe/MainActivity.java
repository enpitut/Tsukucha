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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    ToggleButton btnStart; //press to start cooking
    TextView recognizedWord; //recognized results
    ProgressBar voiceRMS; //voice rms bar
    ListView suggestedList; //suggested results list
    SpeechRecognizer lisener = null;
    Intent recognizer;
    private String LOG_TAG = "HandsFreeRecipe"; //for debug info
    CountDownTimer mTimer = null; //timer
    private AudioManager mAudioManager;
    private int mStreamVolume = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //disable sleep
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        btnStart = (ToggleButton) findViewById(R.id.start_cooking);
        voiceRMS = (ProgressBar) findViewById(R.id.voice_rms);
        voiceRMS.setVisibility(View.INVISIBLE);
        recognizedWord = (TextView) findViewById(R.id.recognition_result);
        suggestedList = (ListView) findViewById(R.id.suggested_words);
        lisener = SpeechRecognizer.createSpeechRecognizer(this);
        lisener.setRecognitionListener(this);
        recognizer = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "jp");
        recognizer.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizer.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                             RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizer.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        btnStart.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    voiceRMS.setVisibility(View.VISIBLE);
                    voiceRMS.setIndeterminate(true);
                    lisener.startListening(recognizer);
                    mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                } else {
                    Log.d(LOG_TAG, "Stop Listening");
                    voiceRMS.setIndeterminate(false);
                    voiceRMS.setVisibility(View.INVISIBLE);
                    lisener.stopListening();
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
        if (lisener != null) {
            lisener.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
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
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
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
            btnStart.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        recognizedWord.setText(errorMessage);
        lisener.cancel();
        lisener.startListening(recognizer);
    }

    @Override
    public void onResults(Bundle results) {
        if(mTimer != null)
            mTimer.cancel();
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches)
            text += result + "\n";

        recognizedWord.setText(text);

        //Start listening again
        Log.i(LOG_TAG, "OnResults:Start listening again");
        if(mTimer == null){
            mTimer = new CountDownTimer(2000,2000){
                @Override
                public void onTick(long millisUntilFinished) {

                }
                @Override
                public void onFinish() {
                    Log.d("Restart","Timer.onFinish: Time's up,restart listening");
                    btnStart.setChecked(true);
                }
            };
        }
        mTimer.start();
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
        if(lisener !=null)
            lisener.destroy();
    }


}

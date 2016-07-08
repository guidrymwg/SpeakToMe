package com.lightcone.speaktome;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import java.util.Locale;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

//Class to test text-to-speech capabilities

public class MainActivity extends AppCompatActivity implements OnInitListener, OnClickListener {

    private static final int CHECK_DATA = 0;
    private static final Locale defaultLocale = Locale.UK;   // British English
    private static final String TAG = "TTS";
    private TextToSpeech tts;
    private boolean isInit = false;
    private View speakButton;
    private View clearButton;
    private EditText speakText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Identify buttons and add click listeners
        speakButton = findViewById(R.id.speak_button);
        speakButton.setOnClickListener(this);
        clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);

        // Identify EditText field
        speakText = (EditText) findViewById(R.id.speak_input);

        // Disable text field and speak button until text to speech initialization is done
        // (See method onInit() below)

        speakButton.setEnabled(false);
        speakText.setEnabled(false);

        // Use an Intent and startActivityForResult to check whether TTS data installed on the
        // device. Result returned and acted on in method onActivityResult(int, int, Intent) below.

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, CHECK_DATA);
    }

    // Create the TTS instance if TextToSpeech language data are installed on device.  If not
    // installed, attempt to install it on the device.

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHECK_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {

                // Success, so create the TTS instance.  But can't use it to speak until
                // the onInit(status) callback defined below runs, indicating initialization.

                Log.i(TAG, "Success, let's talk");
                tts = new TextToSpeech(this, this);

                // Use static Locales method to list available locales on device
                Locale locales[] = Locale.getAvailableLocales();
                Log.i(TAG, "Locales Available on Device:");
                for (int i = 0; i < locales.length; i++) {
                    String temp = "Locale " + i + ": " + locales[i] + " Language="
                            + locales[i].getDisplayLanguage();
                    if (locales[i].getDisplayCountry() != "") {
                        temp += " Country=" + locales[i].getDisplayCountry();
                    }
                    Log.i(TAG, temp);
                }

            } else {
                // missing data, so install it on the device
                Log.i(TAG, "Missing Data; Install it");
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    // Must wait for initialization before any speech can be synthesized.  The class
    // implements OnInitListener and the following callback has been overridden to
    // implement actions that will be executed once initialized.

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            isInit = true;

            // Enable input text field and speak button now that we are initialized
            speakButton.setEnabled(true);
            speakText.setEnabled(true);

            // Set to a language locale after checking availability
            Log.i(TAG, "available=" + tts.isLanguageAvailable(Locale.UK));
            tts.setLanguage(defaultLocale);
            // Examples of voice controls.  Set to defaults of 1.0.
            tts.setPitch(1.0F);
            tts.setSpeechRate(1.0F);
            // Issue a greeting and instructions in the default language
            speakGreeting(defaultLocale.getDisplayLanguage());
        } else {
            isInit = false;
            Log.i(TAG, "Failure: TTS instance not properly initialized");
        }
    }

    // Method to issue greeting and instructions

    public void speakGreeting(String currentLanguage) {
        String text1 = "Let's test text to speech in " + currentLanguage + ". ";
        text1 += "Enter some " + currentLanguage + " text and press the speak button.";
        sayIt(text1, true);
    }

    // Implement text to speech for an arbitrary string entered in the EditText field
    // for the Speak button and text clear for the Clear button.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speak_button:
                String speakString = speakText.getText().toString();
                sayIt(speakString, true);
                break;
            case R.id.clear_button:
                speakText.setText("");
                break;
        }
    }

    // Method to speak a string.  The boolean flushQ determines whether the text is
    // appended to the queue (if false), or if the queue is flushed first (if true).

    public void sayIt(String text, boolean flushQ) {
        if (isInit) {
            if (flushQ) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        } else {
            Log.i(TAG, "Failure: TTS instance not properly initialized");
        }
    }

    // Release TTS resources when finished
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroy");
        tts.shutdown();
    }
}

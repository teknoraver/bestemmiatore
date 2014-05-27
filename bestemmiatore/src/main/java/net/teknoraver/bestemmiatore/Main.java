package net.teknoraver.bestemmiatore;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


public class Main extends ActionBarActivity implements TextToSpeech.OnInitListener {
    private ArrayList<String> aggettivi;
    private ArrayList<String> santi;
    private TextToSpeech tts;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        text = (TextView) findViewById(R.id.text);

        tts = new TextToSpeech(this, this);

        aggettivi = grep(R.raw.italian, ".*ato$");
        santi = grep(R.raw.tuttisanti, null);
    }

    private ArrayList<String> grep(int id, String regexp) {
        BufferedReader dict = new BufferedReader(new InputStreamReader(getResources().openRawResource(id)));
        ArrayList<String> ret = new ArrayList<>();
        String w;
        try {
            while ((w = dict.readLine()) != null) {
//                    if(regexp == null || w.matches(regexp))
                        ret.add(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    protected void onStart() {
        super.onStart();

        play(null);
    }

    public void play(View v) {
        int tipo = (int)(Math.random() * 3);
        String b = aggettivi.get((int) (Math.random() * aggettivi.size()));

        if(tipo == 0) {
            b = "Dio " + b;
        } else if(tipo == 1) {
            b = "Madonna " + b.replaceAll("o$", "a$");
        } else if(tipo == 2) {
            b = "Mannaggia San " + santi.get((int) (Math.random() * santi.size()));
        }

        text.setText(b);
        tts.speak(b, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(final int status) {
        tts.setLanguage(Locale.ITALIAN);
    }
}

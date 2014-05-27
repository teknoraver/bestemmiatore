package net.teknoraver.bestemmiatore;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class Main extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        BufferedReader dict = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.italian)));
        ArrayList<String> words = new ArrayList<>();
        String w;
        try {
            while((w = dict.readLine()) != null) {
                if(w.endsWith("ato"))
                    words.add(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView text = (TextView)findViewById(R.id.text);
        text.setText(words.get((int) (Math.random() * words.size())));
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
}

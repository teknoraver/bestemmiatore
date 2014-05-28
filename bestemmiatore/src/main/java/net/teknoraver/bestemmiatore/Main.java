package net.teknoraver.bestemmiatore;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;


public class Main extends ActionBarActivity implements TextToSpeech.OnInitListener {
	private ArrayList<String> aggettivi;
	private ArrayList<String> santi;
	private TextToSpeech tts;
	private TextView text;
	private String bestemmia;

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

		switch(tipo) {
			case 0:
				bestemmia = aggettivi.get((int) (Math.random() * aggettivi.size()));
				bestemmia = "Dio " + bestemmia;
				break;
			case 1:
				bestemmia = aggettivi.get((int) (Math.random() * aggettivi.size()));
				bestemmia = "Madonna " + bestemmia.replaceAll("o$", "a$");
				break;
			case 2:
				bestemmia = "Mannaggia San " + santi.get((int) (Math.random() * santi.size()));
				break;
		}

		text.setText(bestemmia);
		tts.speak(bestemmia, TextToSpeech.QUEUE_FLUSH, null);
	}

	public void text(View v) {
		startActivity(new Intent(android.content.Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(android.content.Intent.EXTRA_TEXT, bestemmia));
	}

	public void audio(View v) throws IOException {
		File outputFile = File.createTempFile("bestemmia", ".wav", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
		outputFile.deleteOnExit();
		tts.synthesizeToFile(bestemmia, null, outputFile.getAbsolutePath());
		startActivity(
			new Intent(Intent.ACTION_SEND)
				.setType("audio/*")
				.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + outputFile.getAbsolutePath())));
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

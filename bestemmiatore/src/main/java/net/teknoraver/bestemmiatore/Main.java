package net.teknoraver.bestemmiatore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Main extends Activity implements TextToSpeech.OnInitListener {
	private String aggettivi[];
	private String santi[];
	private TextToSpeech tts;
	private TextView text;
	private ImageButton pref;
	private String bestemmia;
	private SharedPreferences prefs;
	private int BESTEMMIA = 1;
	private boolean preferred;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setTitle(R.string.app_name);

		text = (TextView) findViewById(R.id.text);
		pref = (ImageButton) findViewById(R.id.pref);
		prefs = getSharedPreferences("bestemmie", MODE_PRIVATE);

		AdView adView = (AdView) findViewById(R.id.adView);
		adView.loadAd(new AdRequest.Builder().build());

		tts = new TextToSpeech(this, this);

		aggettivi = getResources().getStringArray(R.array.aggettivi);
		santi = getResources().getStringArray(R.array.tuttisanti);

		next(null);
	}

	@Override
	protected void onRestart() {
		super.onRestart();

		setStar();
	}

	/*
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
	}*/

	public void play(View v) {
		text.setText(bestemmia);
		tts.speak(bestemmia, TextToSpeech.QUEUE_FLUSH, null);
	}

	public void setStar() {
		preferred = prefs.getBoolean(bestemmia, false);
		if(preferred)
			pref.setBackgroundResource(R.drawable.star_on);
		else
			pref.setBackgroundResource(R.drawable.star_off);
	}

	public void next(View v) {
		int rnd = (int)(Math.random() * 4);
		switch(rnd) {
		case 0:
		case 1:
		case 2:
			bestemmia = aggettivi[(int) (Math.random() * aggettivi.length)];
			bestemmia = getString(R.string.b1 + rnd, bestemmia);
			break;
		case 3:
			bestemmia = getString(R.string.b4, santi[(int) (Math.random() * santi.length)]);
			break;
		}

		setStar();
		play(null);
	}

	private void shareText() {
		startActivity(new Intent(android.content.Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(android.content.Intent.EXTRA_TEXT, bestemmia));
	}

	private void shareAudio() throws IOException {
		File outputFile = File.createTempFile("bestemmia", ".wav", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
		outputFile.deleteOnExit();
		tts.synthesizeToFile(bestemmia, null, outputFile.getAbsolutePath());
		startActivity(
			new Intent(Intent.ACTION_SEND)
				.setType("audio/*")
				.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + outputFile.getAbsolutePath())));
	}

	public void share(View view) throws IOException {
		new AlertDialog.Builder(this)
		.setTitle(getString(R.string.shareas))
		.setItems(R.array.shareas, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				switch (i) {
				case 0: shareText();
					break;
				case 1:
					try {
						shareAudio();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}).show();
	}

	public void pref(View v) {
		ImageButton pref = (ImageButton) v;
		SharedPreferences.Editor edit = prefs.edit();
		if(preferred) {
			edit.remove(bestemmia);
			pref.setBackgroundResource(R.drawable.star_off);
		} else {
			edit.putBoolean(bestemmia, true).commit();
			pref.setBackgroundResource(R.drawable.star_on);
		}
		edit.commit();
		preferred = !preferred;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_prefs:
			startActivityForResult(new Intent(this, Preferiti.class), BESTEMMIA);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode != BESTEMMIA || data == null) {
			super.onActivityResult(requestCode, resultCode, data);
			return;
		}

		bestemmia = data.getStringExtra(Preferiti.BESTEMMIA);
		play(null);
	}

	@Override
	public void onInit(final int status) {
		tts.setLanguage(Locale.ITALIAN);
		if(bestemmia != null)
			play(null);
	}
}

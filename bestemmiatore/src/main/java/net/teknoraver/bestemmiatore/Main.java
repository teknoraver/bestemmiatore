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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		text = (TextView) findViewById(R.id.text);
		pref = (ImageButton) findViewById(R.id.pref);
		prefs = getSharedPreferences("bestemmie", MODE_PRIVATE);

		tts = new TextToSpeech(this, this);

		aggettivi = getResources().getStringArray(R.array.aggettivi);
		santi = getResources().getStringArray(R.array.tuttisanti);

		next(null);
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
		if(prefs.getBoolean(bestemmia, false))
			pref.setImageResource(R.drawable.btn_star_on_normal_holo_light);
		else
			pref.setImageResource(R.drawable.btn_star_off_normal_holo_light);

		text.setText(bestemmia);
		tts.speak(bestemmia, TextToSpeech.QUEUE_FLUSH, null);
	}

	public void next(View v) {
		switch((int)(Math.random() * 3)) {
		case 0:
			bestemmia = aggettivi[(int) (Math.random() * aggettivi.length)];
			bestemmia = getString(R.string.b1, bestemmia);
			break;
		case 1:
			bestemmia = aggettivi[(int) (Math.random() * aggettivi.length)];
			bestemmia = getString(R.string.b2, bestemmia);
			break;
		case 2:
			bestemmia = getString(R.string.b3, santi[(int) (Math.random() * santi.length)]);
			break;
		}

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
		if(prefs.getBoolean(bestemmia, false)) {
			edit.remove(bestemmia);
			pref.setImageResource(R.drawable.btn_star_off_normal_holo_light);
		} else {
			edit.putBoolean(bestemmia, true).commit();
			pref.setImageResource(R.drawable.btn_star_on_normal_holo_light);
		}
		edit.commit();
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

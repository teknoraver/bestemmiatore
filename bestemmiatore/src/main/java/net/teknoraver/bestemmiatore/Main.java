package net.teknoraver.bestemmiatore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;

public class Main extends Activity implements TextToSpeech.OnInitListener {
	private String[] aggettivi;
	private String[] santi;
	private TextToSpeech speaker, sharer;
	private TextView text;
	private ImageButton pref;
	private String bestemmia;
	private SharedPreferences prefs;
	private final int BESTEMMIA = 1;
	private boolean preferred;
	private boolean loop;
	private final Bundle params = new Bundle();

	private class Looper extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			next(null);
		}
	}


	private final UtteranceProgressListener speakerListener = new UtteranceProgressListener() {
		@Override
		public void onDone(String utteranceId) {
			if (!loop)
				return;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			new Looper().execute((Void) null);
		}

		@Override
		public void onStart(String utteranceId) { }

		@Override
		public void onError(String utteranceId) { }
	};

	private final UtteranceProgressListener sharerListener = new UtteranceProgressListener() {
		@Override
		public void onDone(String inputFile) {
			File aac = wavToAac(inputFile);
			if (aac != null) {
				Uri fileUri = FileProvider.getUriForFile(Main.this, "net.teknoraver.bestemmiatore.fileprovider", aac);
				startActivity(
						new Intent(Intent.ACTION_SEND)
								.setType("audio/mp4")
								.putExtra(Intent.EXTRA_STREAM, fileUri));
			} else {
				Toast.makeText(Main.this, R.string.enc_err, Toast.LENGTH_SHORT).show();
				return;
			}
			new File(inputFile).delete();
		}

		@Override
		public void onStart(String utteranceId) { }

		@Override
		public void onError(String utteranceId) { }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setTitle(R.string.app_name);

		text = (TextView) findViewById(R.id.text);
		pref = (ImageButton) findViewById(R.id.pref);
		prefs = getSharedPreferences("bestemmie", MODE_PRIVATE);

		speaker = new TextToSpeech(this, this);
		sharer = new TextToSpeech(this, this);

		aggettivi = getResources().getStringArray(R.array.aggettivi);
		santi = getResources().getStringArray(R.array.tuttisanti);

		next(null);

		if (Build.VERSION.SDK_INT >= 30){
			if (!Environment.isExternalStorageManager()){
				Intent getpermission = new Intent();
				getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
				startActivity(getpermission);
			}
		}
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
		speaker.speak(bestemmia, TextToSpeech.QUEUE_FLUSH, params, "speak-"+bestemmia.hashCode());
	}

	public void setStar() {
		preferred = prefs.getBoolean(bestemmia, false);
		if(preferred)
			pref.setBackgroundResource(R.drawable.star_on);
		else
			pref.setBackgroundResource(R.drawable.star_off);
	}

	public void next(View v) {
		if(loop && speaker.isSpeaking())
			return;

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
		startActivity(new Intent(Intent.ACTION_SEND)
			.setType("text/plain")
			.putExtra(Intent.EXTRA_TEXT, bestemmia));
	}

	private void shareAudio() throws IOException {
		File outputFile = File.createTempFile("bestemmia", ".wav", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
		sharer.synthesizeToFile(bestemmia, params, outputFile, outputFile.getAbsolutePath());
	}

	private static File wavToAac(String input) {
		final String COMPRESSED_AUDIO_FILE_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
		final int COMPRESSED_AUDIO_FILE_BIT_RATE = 32000;
		final int BUFFER_SIZE = 64 * 1024;
		final int CODEC_TIMEOUT_IN_MS = 5000;
		File outputFile = null;

		try {
			MediaExtractor mediaExtractor = new MediaExtractor();
			mediaExtractor.setDataSource(input);
			MediaFormat mediaFormat = mediaExtractor.getTrackFormat(0);
			int srcSamplingRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);

			mediaExtractor.release();

			FileInputStream fis = new FileInputStream(input);
			fis.skip(44); // skip WAV header

			outputFile = File.createTempFile("bestemmia", ".m4a", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

			MediaMuxer mux = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

			MediaFormat outputFormat = MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, srcSamplingRate, 1);
			outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
			outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, COMPRESSED_AUDIO_FILE_BIT_RATE);
			// outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);

			MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
			String codecName = mediaCodecList.findEncoderForFormat(outputFormat);

			MediaCodec codec = MediaCodec.createByCodecName(codecName);
			codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
			codec.start();

			MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
			byte[] tempBuffer = new byte[BUFFER_SIZE];
			boolean hasMoreData = true;
			double presentationTimeUs = 0;
			int audioTrackIdx = 0;
			int totalBytesRead = 0;
			do {
				int inputBufIndex = 0;
				while (inputBufIndex != -1 && hasMoreData) {
					inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_IN_MS);

					if (inputBufIndex >= 0) {
						// ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
						ByteBuffer dstBuf = codec.getInputBuffer(inputBufIndex);

						int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
						System.out.println("bytesRead Readed "+bytesRead);
						if (bytesRead == -1) { // -1 implies EOS
							hasMoreData = false;
							codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						} else {
							totalBytesRead += bytesRead;
							dstBuf.put(tempBuffer, 0, bytesRead);
							codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
							presentationTimeUs = 1000000l * (totalBytesRead / 2) / srcSamplingRate;
						}
					}
				}
				// Drain audio
				int outputBufIndex = 0;
				while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
					outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, CODEC_TIMEOUT_IN_MS);
					if (outputBufIndex >= 0) {
						ByteBuffer encodedData = codec.getOutputBuffer(outputBufIndex);
						encodedData.position(outBuffInfo.offset);
						encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
						if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && outBuffInfo.size != 0) {
							codec.releaseOutputBuffer(outputBufIndex, false);
						} else {
							mux.writeSampleData(audioTrackIdx, encodedData, outBuffInfo);
							codec.releaseOutputBuffer(outputBufIndex, false);
						}
					} else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
						outputFormat = codec.getOutputFormat();
						System.out.println("Output format changed - " + outputFormat);
						audioTrackIdx = mux.addTrack(outputFormat);
						mux.start();
					} else {
						System.out.println("Unknown return code from dequeueOutputBuffer - " + outputBufIndex);
					}
				}
			} while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);
			fis.close();
			mux.stop();
			mux.release();
			System.out.println("Compression done ...");
		} catch (IOException e) {
			e.printStackTrace();
		}

		//mStop = false;
		// Notify UI thread...

		return outputFile;
	}

	public void share(View view) {
		new AlertDialog.Builder(this)
			.setTitle(getString(R.string.shareas))
			.setItems(R.array.shareas, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					switch (i) {
					case 0:
						shareText();
						break;
					case 1:
						try {
							shareAudio();
						} catch (IOException e) {
							Toast.makeText(Main.this, R.string.waverr, Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
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
			edit.putBoolean(bestemmia, true);
			pref.setBackgroundResource(R.drawable.star_on);
		}
		edit.apply();
		preferred = !preferred;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_prefs) {
			startActivityForResult(new Intent(this, Preferiti.class), BESTEMMIA);
			return true;
		}
		if (item.getItemId() == R.id.action_loop) {
			loop = !loop;
			item.setChecked(loop);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		speaker.setLanguage(Locale.ITALIAN);
		speaker.setOnUtteranceProgressListener(speakerListener);
		sharer.setLanguage(Locale.ITALIAN);
		sharer.setOnUtteranceProgressListener(sharerListener);
		if(bestemmia != null)
			play(null);
	}
}

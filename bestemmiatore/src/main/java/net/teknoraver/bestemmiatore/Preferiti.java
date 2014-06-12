package net.teknoraver.bestemmiatore;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class Preferiti extends ListActivity {
	static final String BESTEMMIA = "bestemmia";
	private Adapter adapter;

	private class Adapter extends ArrayAdapter<String> {

		private class StarClickListener implements View.OnClickListener {
			private final int position;

			StarClickListener(int pos) {
				position = pos;
			}

			@Override
			public void onClick(View view) {
				System.out.println("unstar " + position);
			}
		}

		public Adapter(Context context, String objects[]) {
			super(context, R.layout.pref, objects);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.pref, null);

			final ImageButton star = (ImageButton) convertView.findViewById(R.id.pref_star);
			star.setOnClickListener(new StarClickListener(position));

			final TextView text = (TextView) convertView.findViewById(R.id.pref_text);
			text.setText(getItem(position));

			return convertView;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prefs);

		Set<String> s = getSharedPreferences("bestemmie", MODE_PRIVATE).getAll().keySet();
		String bestemmie[] = new String[s.size()];
		s.toArray(bestemmie);
		adapter = new Adapter(this, bestemmie);
//		adapter = new Adapter(this, new String[0]);

		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		getIntent().putExtra(BESTEMMIA, adapter.getItem(position));
		setResult(RESULT_OK, getIntent());
		finish();
	}
}

package net.teknoraver.bestemmiatore;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Preferiti extends ListActivity {
	static final String BESTEMMIA = "bestemmia";
	private Adapter adapter;

	private class Adapter extends ArrayAdapter<String> {

		public Adapter(Context context, String objects[]) {
			super(context, android.R.layout.simple_list_item_1, objects);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new Adapter(this, getSharedPreferences("bestemmie", MODE_PRIVATE).getAll().keySet().toArray(new String[0]));

		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		getIntent().putExtra(BESTEMMIA, adapter.getItem(position));
		setResult(RESULT_OK, getIntent());
		finish();
	}
}

package link.androidapps.quicknotes.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.comparator.AlphabeticComparator;
import link.androidapps.quicknotes.db.QuickNoteModel;

/**
 * Created by PKamenov on 28.11.15.
 */
public class ListQuickNotesAdapter extends ArrayAdapter<QuickNoteModel> {
	private Context context;
	private List<QuickNoteModel> allData;
	private List<QuickNoteModel> adapterData;
	private Filter mFilter;
	private Bitmap imgReminderOff;
	private Bitmap imgReminderOn;

	public ListQuickNotesAdapter(Context context, int resource, List<QuickNoteModel> adapterData) {
		super(context, resource, adapterData);
		this.context = context;
		this.adapterData = adapterData;
		this.allData = new ArrayList<>(adapterData);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CustomListRow customListRow;
		QuickNoteModel note = adapterData.get(position);

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.custom_list_row, null);
			customListRow = new CustomListRow();
			convertView.setTag(customListRow);
		} else {
			customListRow = (CustomListRow) convertView.getTag();
		}

		customListRow.textView = (TextView) convertView.findViewById(R.id.title);
		customListRow.icon = (ImageView) convertView.findViewById(R.id.image);

		customListRow.textView.setText(note.getTitle());

		if(note.getRemindTime() > -1) {
			if(imgReminderOn == null)
				imgReminderOn = BitmapFactory.decodeResource(context.getResources(), R.drawable.on);
			customListRow.icon.setImageBitmap(imgReminderOn);
		} else {
			if(imgReminderOff == null)
				imgReminderOff = BitmapFactory.decodeResource(context.getResources(), R.drawable.off);
			customListRow.icon.setImageBitmap(imgReminderOff);
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return this.adapterData.size();
	}

	@Override
	public void clear() {
		adapterData.clear();
		allData.clear();
	}

	@Override
	public void remove(QuickNoteModel note) {
		adapterData.remove(note);
		allData.remove(note);
	}

	@Override
	public QuickNoteModel getItem(int position) {
		return this.adapterData.get(position);
	}

	@Override
	public int getPosition(QuickNoteModel note) {
		return adapterData.indexOf(note);
	}

	@Override
	public void insert(QuickNoteModel note, int index) {
		adapterData.add(index, note);
		allData.add(index, note);
	}

	@Override
	public void add(QuickNoteModel note) {
		adapterData.add(note);
		allData.add(note);
	}

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ItemsFilter();
		}
		return mFilter;
	}

	private class ItemsFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			List<QuickNoteModel> filtered = new ArrayList<>();
			if (prefix == null || prefix.length() == 0) {
				results.values = allData;
				results.count = allData.size();
			} else {
				for (QuickNoteModel note : allData) {
					if (note.getTitle().startsWith(prefix.toString())) {
						filtered.add(note);
					}
				}
				results.values = filtered;
				results.count = filtered.size();
			}
			return results;
		}

		@Override
		protected void publishResults(CharSequence prefix, FilterResults results) {
			adapterData.clear();
			adapterData.addAll((List<QuickNoteModel>) results.values);
			sort(AlphabeticComparator.instance());
			notifyDataSetChanged();
		}
	}

	static class CustomListRow {
		ImageView icon;
		TextView textView;
	}

	public List<QuickNoteModel> getAllData() {
		return  this.allData;
	}
}

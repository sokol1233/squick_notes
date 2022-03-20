package link.androidapps.quicknotes.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import link.androidapps.quicknotes.R;

/**
 * Created by PKamenov on 06.12.15.
 */
public class FilePickerAdapter extends ArrayAdapter<String> {
	private Activity activity;
	private List<String> files;

	public FilePickerAdapter(Activity activity, List<String> files) {
		super(activity, R.layout.file_picker_row, new ArrayList<String>());
		this.activity = activity;
		this.files = files;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		String fileName = this.getItem(pos);

		TextView textView;
		if (convertView == null) {
			convertView = LayoutInflater.from(activity).inflate(R.layout.file_picker_row, parent, false);
			textView = (TextView) convertView.findViewById(R.id.file_name);
			convertView.setTag(textView);
		} else {
			textView = (TextView) convertView.getTag();
		}

		textView.setText(fileName);
		if(fileName.equals(FilePicker.PARENT_DIR)) {
			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.back, 0, 0, 0);
		} else if(files.contains(fileName)) {
			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file, 0, 0, 0);
		} else {
			textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder, 0, 0, 0);
		}

		return convertView;
	}
}

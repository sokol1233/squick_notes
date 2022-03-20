package link.androidapps.quicknotes.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import link.androidapps.quicknotes.Constants;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.listener.ActivityListener;

/**
 * Created by PKamenov on 12.11.15.
 */
public class EditQuickNoteFragment extends Fragment {
	private long id = -1;
	private EditText title;
	private EditText content;
	private String originalTitle = "";
	private String originalContent = "";
	private ActivityListener activity;
	private LinearLayout editLayout;
    private TextView footerDate;
    private java.text.DateFormat dateFormat;
    private java.text.DateFormat timeFormat;
    private long remindTime = -1;

    private String formatFooterDate(long remindTime) {
        Date date = new Date(remindTime);
        return getString(R.string.label_remind_on) + " " + dateFormat.format(date) + "; " + timeFormat.format(date);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		activity = (ActivityListener)getActivity();
        dateFormat = SimpleDateFormat.getDateInstance();
        timeFormat = DateFormat.getTimeFormat(getActivity());

		View view = inflater.inflate(R.layout.fragment_edit, container, false);
        editLayout = LinearLayout.class.cast(view.findViewById(R.id.layout_edit));
		title = EditText.class.cast(view.findViewById(R.id.title));
		content = EditText.class.cast(view.findViewById(R.id.content));
        footerDate = TextView.class.cast(view.findViewById(R.id.footer_date));
		if(savedInstanceState != null) {
			id = savedInstanceState.getLong(Constants.INTENT_EXTRA_ID, -1L);
			if(id > -1) {
				content.setText(savedInstanceState.getString(Constants.INTENT_EXTRA_CONTENT));
                title.setText(savedInstanceState.getString(Constants.INTENT_EXTRA_TITLE));
                originalTitle = savedInstanceState.getString(Constants.INTENT_EXTRA_ORIG_TITLE);
                originalContent = savedInstanceState.getString(Constants.INTENT_EXTRA_ORIG_CONTENT);
                updateRemindTime(savedInstanceState.getLong(Constants.INTENT_EXTRA_REMIND_TIME, -1L));
                editLayout.setVisibility(View.VISIBLE);
            }
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		content.requestFocus();
	}

	@Override
	public void onPause() {
		if(hasChanges()) {
			activity.saveNote(id, originalTitle, title.getText().toString(), content.getText().toString(), remindTime);
		}
		super.onPause();
	}

	public void updateRemindTime(long remindTime) {
		this.remindTime = remindTime;
		if(remindTime > -1) {
			footerDate.setText(formatFooterDate(remindTime));
		} else {
			footerDate.setText(getString(R.string.label_no_reminder));
		}
	}

	public void updateContent(long id, String titleData, String contentData, long remindTime) {
        if(id != this.id) {
	        if(hasChanges()) {
		        activity.saveNote(this.id, originalTitle, title.getText().toString(), content.getText().toString(), this.remindTime);
	        }
            editLayout.setVisibility(View.VISIBLE);
			title.setText(titleData);
			content.setText(contentData);
			this.id = id;
			this.originalTitle = titleData;
	        this.originalContent = contentData != null ? contentData : "";
		}
        if(this.remindTime != remindTime) {
            updateRemindTime(remindTime);
        }
	}

	public void deleteContent(long id) {
		if(id != this.id && hasChanges()) {
			activity.saveNote(this.id, originalTitle, title.getText().toString(), content.getText().toString(), this.remindTime);
		}
		if(this.id != -1) {
			this.id = -1;
			this.originalTitle = "";
			this.originalContent = "";
			title.setText("");
			content.setText("");
            remindTime = -1;
			updateRemindTime(-1);
            editLayout.setVisibility(View.GONE);
		}
	}

	public boolean hasChanges() {
		if(id == -1) {
			return false;
		}

		String titleStr = title.getText().toString();
		String contentStr = content.getText().toString();

		return !originalTitle.equals(titleStr) || !originalContent.equals(contentStr);
	}

	public long getDisplayedId() {
		return this.id;
	}

	public QuickNoteModel getDisplayedNote() {
		return new QuickNoteModel(id, originalTitle, originalContent, remindTime);
	}
}

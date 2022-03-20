package link.androidapps.quicknotes.view;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import link.androidapps.quicknotes.Constants;
import link.androidapps.quicknotes.QuickNotesBroadcastReceiver;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.fragment.EditQuickNoteFragment;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.listener.ActivityListener;

/**
 * Created by PKamenov on 13.11.15.
 */
public class EditQuickNoteActivity extends Activity implements ActivityListener {
	private EditQuickNoteFragment detailsFrag;
	private DateTimePicker dateTimePicker;
    private Menu menu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
		detailsFrag = (EditQuickNoteFragment) getFragmentManager().findFragmentById(R.id.details_frag);

		long id = getIntent().getLongExtra(Constants.INTENT_EXTRA_ID, -1);
		String title = getIntent().getStringExtra(Constants.INTENT_EXTRA_TITLE);
		String content = getIntent().getStringExtra(Constants.INTENT_EXTRA_CONTENT);
		long remindTime = getIntent().getLongExtra(Constants.INTENT_EXTRA_REMIND_TIME, -1);
		EditQuickNoteFragment detailsFrag = (EditQuickNoteFragment) getFragmentManager()
				.findFragmentById(R.id.details_frag);
		detailsFrag.updateContent(id, title, content, remindTime);
	}

    @Override
    public void onPause() {
        if(dateTimePicker != null) {
            dateTimePicker.dismissOpenDialogs();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        this.menu = menu;
        if(detailsFrag.getDisplayedNote().getRemindTime() > -1) {
            MenuItem item = menu.findItem(R.id.action_remove);
            item.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_schedule:
                scheduleReminder(detailsFrag.getDisplayedNote());
                return true;
            case R.id.action_remove:
                QuickNoteModel note = detailsFrag.getDisplayedNote();
                note.setRemindTime(-1L);
                updateRemindTime(note);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onListItemClick(QuickNoteModel quickNote) {

	}

	@Override
	public void onItemDeleted(long id) {

	}

	public void saveNote(long id, String origTitle, String title, String content, long remindTime) {
        QuickNoteModel quickNote = QuickNoteDAO.self(this).getQuickNoteByTitle(title);
        if(quickNote != null && quickNote.getId() != id) {
            Toast.makeText(getApplicationContext(), R.string.title_exists, Toast.LENGTH_LONG).show();
            title = origTitle;
        } else if(title.trim().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.empty_title, Toast.LENGTH_LONG).show();
            title = origTitle;
        } else {
            Toast.makeText(this, R.string.quick_note_saved, Toast.LENGTH_LONG).show();
        }
        QuickNoteDAO.self(this).updateQuickNote(id, title, content, remindTime);
	}

	@Override
	public void clearDetails() {

	}

	@Override
	public void collapseSearch() {

	}

	@Override
	public void scheduleReminder(QuickNoteModel item) {
		dateTimePicker = new DateTimePicker(this, item);
		dateTimePicker.showDialog();
	}

	@Override
	public void updateRemindTime(QuickNoteModel note) {
        QuickNoteDAO.self(this).updateRemindTime(note);

		detailsFrag.updateRemindTime(note.getRemindTime());

		if(note.getRemindTime() > -1) {
            this.menu.findItem(R.id.action_remove).setVisible(true);
            QuickNotesBroadcastReceiver.scheduleNotification(this, note.getId(), note.getRemindTime(), note.getTitle());
        } else {
            this.menu.findItem(R.id.action_remove).setVisible(false);
            QuickNotesBroadcastReceiver.cancelNotification(this, note.getId());
        }
	}
}

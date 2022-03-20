package link.androidapps.quicknotes.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import link.androidapps.quicknotes.Constants;
import link.androidapps.quicknotes.QuickNotesBroadcastReceiver;
import link.androidapps.quicknotes.fragment.EditQuickNoteFragment;
import link.androidapps.quicknotes.fragment.ListQuickNotesFragment;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.io.Exporter;
import link.androidapps.quicknotes.io.Importer;
import link.androidapps.quicknotes.listener.DialogOnClickListener;
import link.androidapps.quicknotes.listener.ActivityListener;

/**
 * Created by PKamenov on 22.09.15.
 */
public class ListQuickNotesActivity extends Activity implements ActivityListener, FilePicker.FileSelectedListener {
	private static final String TAG = "ListQuickNotesActivity";
    private static final int REQUEST_EXTERNAL_STORAGE_EXPORT = 1;
    private static final int REQUEST_EXTERNAL_STORAGE_IMPORT = 2;

	private boolean isActivityOnFront = false;
	private ListQuickNotesFragment listFrag;
	private EditQuickNoteFragment detailsFrag;
	private MenuItem searchItem;
	private Dialog alertDialog;
	private FilePicker filePicker;
	private DateTimePicker dateTimePicker;

	public void onListItemClick(QuickNoteModel note) {
		if(detailsFrag != null && detailsFrag.getDisplayedId() == note.getId()) {
			return;
		}

		displayDetails(note);
	}

	public void displayDetails(QuickNoteModel note) {
		if (detailsFrag == null) {
			startEditActivity(note);
		} else {
			detailsFrag.updateContent(note.getId(), note.getTitle(), note.getContent(), note.getRemindTime());
		}
	}

	public void onItemDeleted(long id) {
		if (detailsFrag != null) {
			detailsFrag.deleteContent(id);
		}
	}

	public void onDeleteAll() {
        for(QuickNoteModel item : listFrag.getAllItems()) {
            QuickNotesBroadcastReceiver.cancelNotification(this, item.getId());
        }
        listFrag.refreshNotes();
		if (detailsFrag != null) {
			detailsFrag.deleteContent(-1);
		}
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
        refreshNotes();
	}

    public void refreshNotes() {
        listFrag.refreshNotes();
    }

	public void clearDetails() {
		if(detailsFrag != null) {
			detailsFrag.deleteContent(-1);
		}
	}

	public void collapseSearch() {
		if(searchItem != null)
			searchItem.collapseActionView();
	}


	public void scheduleReminder(QuickNoteModel item) {
		dateTimePicker = new DateTimePicker(this, item);
		dateTimePicker.showDialog();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        if(getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
		detailsFrag = (EditQuickNoteFragment) getFragmentManager().findFragmentById(R.id.details_frag);
		listFrag = (ListQuickNotesFragment) getFragmentManager().findFragmentById(R.id.list_frag);

		if(getIntent().getAction().startsWith(Constants.REMIND_ACTION)) {
			long id = getIntent().getLongExtra(Constants.INTENT_EXTRA_ID, -1L);
            QuickNoteModel note = QuickNoteDAO.self(this).getQuickNoteById(id);
			note.setRemindTime(-1L);
            QuickNoteDAO.self(this).updateRemindTime(note);
			QuickNotesBroadcastReceiver.cancelNotification(this, id);
            displayDetails(note);
        }
	}

	@Override
	public void onPause() {
		if(searchItem != null)
			searchItem.collapseActionView();
		isActivityOnFront = false;
		if(alertDialog != null) {
			alertDialog.dismiss();
		}
		if(filePicker != null) {
			filePicker.dismissOpenDialogs();
		}
		if(dateTimePicker != null) {
			dateTimePicker.dismissOpenDialogs();
		}
		super.onPause();
	}

	@Override
	public void onResume() {
		isActivityOnFront = true;
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		searchItem = menu.findItem(R.id.action_search);
		SearchView sv = new SearchView(this);
		sv.setQueryHint(getResources().getString(R.string.hint_search));
		sv.setOnQueryTextListener(listFrag);
		searchItem.setOnActionExpandListener(listFrag);
		searchItem.setActionView(sv);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add:
				searchItem.collapseActionView();
				alertDialog = new AlertDialog.Builder(this).setTitle(R.string.add_quick_note)
						.setPositiveButton(R.string.ok, new DialogOnClickListener(DialogOnClickListener.Action.ADD, listFrag, this))
						.setNegativeButton(R.string.cancel, null).setView(this.getLayoutInflater().inflate(R.layout.create_dialog, null)).create();

				alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						View input = ((Dialog)dialog).findViewById(R.id.create);
						input.requestFocus();
						InputMethodManager in = (InputMethodManager) ListQuickNotesActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
						in.showSoftInput(input, 0);
					}
				});
				alertDialog.show();
				return true;
			case R.id.action_dell_all:
				if(listFrag != null) {
					if(listFrag.isEmpty()) {
						Toast.makeText(this, R.string.nothing_for_deletion, Toast.LENGTH_LONG).show();
					} else {
						alertDialog = new AlertDialog.Builder(this).setTitle(R.string.confirm_delete_all)
								.setPositiveButton(R.string.ok, new DialogOnClickListener(DialogOnClickListener.Action.DELETE_ALL, listFrag, this))
								.setNegativeButton(R.string.cancel, null).show();
					}
				}
				return true;
			case R.id.action_export:
				clearDetails();
				if(hasPermissions(REQUEST_EXTERNAL_STORAGE_EXPORT)) {
					pickupFile(true);
				}
				return true;
			case R.id.action_import:
				if(hasPermissions(REQUEST_EXTERNAL_STORAGE_IMPORT)) {
					pickupFile(false);
				}
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void startEditActivity(QuickNoteModel note) {
		Intent intent = new Intent(this, EditQuickNoteActivity.class);
		intent.putExtra(Constants.INTENT_EXTRA_ID, note.getId());
		intent.putExtra(Constants.INTENT_EXTRA_TITLE, note.getTitle());
		intent.putExtra(Constants.INTENT_EXTRA_CONTENT, note.getContent());
        intent.putExtra(Constants.INTENT_EXTRA_REMIND_TIME, note.getRemindTime());
		startActivity(intent);
	}

	public boolean isActivityOnFront() {
		return isActivityOnFront;
	}

	public void addFromImport(List<QuickNoteModel> notes) {
		if(listFrag != null) {
			for(QuickNoteModel note : notes) {
				listFrag.insertIntoAdapter(note);
			}
		}
	}

	public boolean hasPermissions(int requestId) {
        int permissionResult;
		switch (requestId) {
			case REQUEST_EXTERNAL_STORAGE_EXPORT:case REQUEST_EXTERNAL_STORAGE_IMPORT:
                permissionResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestId);
                    return false;
                }
                return true;
			default:
				Log.d(TAG, "Wrong request permission flag provided: " + requestId);
                return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_EXTERNAL_STORAGE_EXPORT:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					pickupFile(true);
				}
				break;
			case REQUEST_EXTERNAL_STORAGE_IMPORT:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					pickupFile(false);
				}
		}
	}

	private void pickupFile(boolean isExport) {
		filePicker = new FilePicker(this, isExport).setFileListener(this);
		filePicker.showDialog();
	}

	@Override
	public void fileSelected(File file, boolean isExport) {
		if(isExport) {
			new Exporter(this).execute(file);
		} else {
			new Importer(this).execute(file.getAbsolutePath());
		}
	}

	public void updateRemindTime(QuickNoteModel note) {
        QuickNoteDAO.self(this).updateRemindTime(note);
        if(detailsFrag != null)
            detailsFrag.updateContent(note.getId(), note.getTitle(), note.getContent(), note.getRemindTime());

        if(note.getRemindTime() > -1)
            QuickNotesBroadcastReceiver.scheduleNotification(this, note.getId(), note.getRemindTime(), note.getTitle());
        else
            QuickNotesBroadcastReceiver.cancelNotification(this, note.getId());

		listFrag.refreshNotes();
	}
}

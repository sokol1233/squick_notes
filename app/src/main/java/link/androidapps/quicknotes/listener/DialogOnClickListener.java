package link.androidapps.quicknotes.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.Toast;

import link.androidapps.quicknotes.view.ListQuickNotesActivity;
import link.androidapps.quicknotes.fragment.ListQuickNotesFragment;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;

/**
 * Created by PKamenov on 21.11.15.
 */
public class DialogOnClickListener implements DialogInterface.OnClickListener {

	public enum Action {
		ADD, DELETE_ALL
	}

	private Action action;
	private ListQuickNotesFragment listFrag;
	private ListQuickNotesActivity activity;

	public DialogOnClickListener(Action action, ListQuickNotesFragment listFrag, ListQuickNotesActivity activity) {
		this.action = action;
		this.listFrag = listFrag;
		this.activity = activity;
	}

	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		switch (action) {
			case ADD:
				EditText input = (EditText) ((AlertDialog) dialog).findViewById(R.id.create);
				QuickNoteModel newNote = null;
				String newTitle = input.getText().toString().trim();
				QuickNoteModel existing = new QuickNoteModel();
				existing.setTitle(newTitle);
				if (newTitle.length() == 0) {
					Toast.makeText(activity, R.string.err_empty_title, Toast.LENGTH_LONG).show();
				} else if (listFrag.contains(existing)) {
					Toast.makeText(activity, R.string.err_title_exists, Toast.LENGTH_LONG).show();
				} else {
					long id = QuickNoteDAO.self(activity).createQuickNote(input.getText().toString(), null, -1L);
					newNote = QuickNoteDAO.self(activity).getQuickNoteById(id);
					Toast.makeText(listFrag.getActivity(), R.string.quick_note_saved, Toast.LENGTH_LONG).show();
				}
				if (newNote != null) {
					activity.refreshNotes();
					activity.displayDetails(newNote);
				}
				break;
			case DELETE_ALL:
				QuickNoteDAO.self(activity).deleteAllQuickNotes();
				activity.onDeleteAll();
				break;
			default:
		}
	}
}

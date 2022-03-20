package link.androidapps.quicknotes.listener;

import link.androidapps.quicknotes.db.QuickNoteModel;

/**
 * Created by PKamenov on 21.11.15.
 */
public interface ActivityListener {
	void onListItemClick(QuickNoteModel quickNote);
	void onItemDeleted(long id);
	void saveNote(long id, String origTitle, String title, String content, long remindTime);
	void clearDetails();
	void collapseSearch();
	void scheduleReminder(QuickNoteModel item);
	void updateRemindTime(QuickNoteModel note);
}

package link.androidapps.quicknotes.io;

import java.util.List;

import link.androidapps.quicknotes.db.QuickNoteModel;

/**
 * Created by plamen on 07.01.16.
 */
public class QuickNotesContainer {
	private List<QuickNoteModel> quickNotes;

	public QuickNotesContainer() {

	}

	public QuickNotesContainer(List<QuickNoteModel> quickNotes) {
		this.quickNotes = quickNotes;
	}

	public List<QuickNoteModel> getQuickNotes() {
		return quickNotes;
	}

	public void setQuickNotes(List<QuickNoteModel> quickNotes) {
		this.quickNotes = quickNotes;
	}
}

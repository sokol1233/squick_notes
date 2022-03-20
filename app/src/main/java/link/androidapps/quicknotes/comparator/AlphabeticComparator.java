package link.androidapps.quicknotes.comparator;

import java.util.Comparator;

import link.androidapps.quicknotes.db.QuickNoteModel;

/**
 * Created by PKamenov on 22.11.15.
 */
public class AlphabeticComparator implements Comparator<QuickNoteModel> {
	private static AlphabeticComparator instance;

	private AlphabeticComparator() {

	}

	public static AlphabeticComparator instance() {
		if(instance == null) {
			instance = new AlphabeticComparator();
		}
		return instance;
	}

	@Override
	public int compare(QuickNoteModel lhs, QuickNoteModel rhs) {
		return lhs.getTitle().compareTo(rhs.getTitle());
	}
}

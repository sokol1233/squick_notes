package link.androidapps.quicknotes;

import android.os.Environment;

import java.io.File;

/**
 * Created by PKamenov on 13.11.15.
 */
public class Constants {
	public static final int REQUEST_CODE_EDIT = 0;

	public static final String REMIND_ACTION = "link.androidapps.quicknotes.REMIND_ACTION";

	public static final String INTENT_EXTRA_ID = "id";
	public static final String INTENT_EXTRA_TITLE = "title";
	public static final String INTENT_EXTRA_CONTENT = "content";
	public static final String INTENT_EXTRA_ORIG_TITLE = "origTitle";
	public static final String INTENT_EXTRA_ORIG_CONTENT = "origContent";
	public static final String INTENT_EXTRA_REMIND_TIME = "remindTime";

	public static final File EXT_STOR = Environment.getExternalStorageDirectory();
}

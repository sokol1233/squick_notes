package link.androidapps.quicknotes.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper wrapper class for database creation and update operations.
 *
 * Created by PKamenov on 22.09.15.
 */
public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_QUICK_NOTES = "quick_notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_REMIND_TIME = "remind_time";

    private static final String DATABASE_NAME = "quickNotes.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table " + TABLE_QUICK_NOTES + "(" + COLUMN_ID + " integer primary key autoincrement, " + COLUMN_TITLE
            + " text not null unique," + COLUMN_CONTENT + " text," + COLUMN_REMIND_TIME + " integer);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

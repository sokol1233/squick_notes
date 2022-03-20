package link.androidapps.quicknotes.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * DAO class handling different database operations related to QuickNoteModel.
 *
 * Created by PKamenov on 22.09.15.
 */
public class QuickNoteDAO {

	private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_TITLE, SQLiteHelper.COLUMN_CONTENT, SQLiteHelper.COLUMN_REMIND_TIME};

	private static QuickNoteDAO self;

	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;

    /**
     * Private modifier to prevent direct instantiation of default constructor.
     */
    private QuickNoteDAO() {

    }

    private QuickNoteDAO(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    /**
     * Method for retrieving the singleton instance.
     *
     * @param context The Application context object.
     * @return The singleton instance kept in this class.
     */
    public static QuickNoteDAO self(Context context) {
        if (self == null) {
            self = new QuickNoteDAO(context);
        }

        return self;
    }

    /**
     * Opens the underlying database.
     *
     * @throws SQLException
     */
    private void open() throws SQLException {
        if(database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
    }

	/**
	 * Closes the underlying database.
	 */
	private void close() {
		if (database != null && database.isOpen())
			dbHelper.close();
	}

	public synchronized long createQuickNote(String title, String content, long remindTime) {
		open();
		ContentValues contentValues = new ContentValues();
		contentValues.put(SQLiteHelper.COLUMN_TITLE, title);
		contentValues.put(SQLiteHelper.COLUMN_CONTENT, content);
        contentValues.put(SQLiteHelper.COLUMN_REMIND_TIME, remindTime);
		long result = database.insert(SQLiteHelper.TABLE_QUICK_NOTES, SQLiteHelper.COLUMN_TITLE, contentValues);
		close();
		return result;
	}

	public synchronized QuickNoteModel getQuickNoteById(long id) {
		open();
		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, SQLiteHelper.COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null,
				null);
		cursor.moveToFirst();

		QuickNoteModel quickNote = cursorToQuickNote(cursor);
		cursor.close();
		close();
		return quickNote;
	}

	public synchronized QuickNoteModel getQuickNoteByTitle(String title) {
		open();
		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, SQLiteHelper.COLUMN_TITLE + "=?", new String[]{title}, null, null, null);
		cursor.moveToFirst();

		QuickNoteModel quickNote = cursorToQuickNote(cursor);
		cursor.close();
		close();
		return quickNote;
	}

	public synchronized List<QuickNoteModel> getQuickNoteTitleLike(String titlePart) {
		List<QuickNoteModel> result = new ArrayList<>();
		open();
		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, SQLiteHelper.COLUMN_TITLE + " LIKE ?", new String[]{"%"+titlePart+"%"},
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(cursorToQuickNote(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return result;
	}

	public synchronized List<QuickNoteModel> getQuickNoteContentLike(String contentPart) {
		List<QuickNoteModel> result = new ArrayList<>();
		open();
		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, SQLiteHelper.COLUMN_CONTENT + " LIKE ?", new String[]{"%"+contentPart+"%"},
				null, null, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(cursorToQuickNote(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		close();
		return result;
	}

	public synchronized int updateQuickNote(long id, String title, String content, long remindTime) {
		open();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_TITLE, title);
		values.put(SQLiteHelper.COLUMN_CONTENT, content);
		values.put(SQLiteHelper.COLUMN_REMIND_TIME, remindTime);
		int result = database.update(SQLiteHelper.TABLE_QUICK_NOTES, values, "id=?", new String[]{Long.toString(id)});
		close();
		return result;
	}

	public synchronized int deleteQuickNote(long id) {
		open();
		int result = database.delete(SQLiteHelper.TABLE_QUICK_NOTES, SQLiteHelper.COLUMN_ID + "=?", new String[]{Long.toString(id)});
		close();
		return result;
	}

	public synchronized int deleteAllQuickNotes() {
		open();
		int result = database.delete(SQLiteHelper.TABLE_QUICK_NOTES, "1", null);
		close();
		return result;
	}

	public synchronized List<QuickNoteModel> getAllQuickNotes() {
		open();
		List<QuickNoteModel> quickNotes = new ArrayList<>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			quickNotes.add(cursorToQuickNote(cursor));
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return quickNotes;
	}

	public synchronized List<String> getAllTitles() {
		open();
		List<String> titles = new ArrayList<>();

		Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, new String[]{SQLiteHelper.COLUMN_TITLE}, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			titles.add(cursor.getString(0));
			cursor.moveToNext();
		}

		cursor.close();
		close();
		return titles;
	}

    public synchronized List<QuickNoteModel> getAllWithReminder() {
        open();
        List<QuickNoteModel> result = new ArrayList<>();

        Cursor cursor = database.query(SQLiteHelper.TABLE_QUICK_NOTES, allColumns, SQLiteHelper.COLUMN_REMIND_TIME + ">-1", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            result.add(cursorToQuickNote(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        close();
        return result;
    }

	public synchronized void updateRemindTime(QuickNoteModel note) {
		open();
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_REMIND_TIME, note.getRemindTime());
		database.update(SQLiteHelper.TABLE_QUICK_NOTES, values, SQLiteHelper.COLUMN_ID + "=?", new String[]{Long.toString(note.getId())});
		close();
	}

	private QuickNoteModel cursorToQuickNote(Cursor cursor) {
		if (cursor.getCount() > 0) {
			QuickNoteModel quickNote = new QuickNoteModel();
			quickNote.setId(cursor.getLong(0));
			quickNote.setTitle(cursor.getString(1));
			quickNote.setContent(cursor.getString(2));
			quickNote.setRemindTime(cursor.getLong(3));

			return quickNote;
		}
		return null;
	}
}

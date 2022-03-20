package link.androidapps.quicknotes.io;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import link.androidapps.quicknotes.QuickNotesApplication;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.util.JsonUtil;
import link.androidapps.quicknotes.view.ListQuickNotesActivity;

public class Importer extends AsyncTask<String, Void, List<QuickNoteModel>> {
	private ListQuickNotesActivity activity;
	private QuickNoteDAO db;
	private String message;

	public Importer(ListQuickNotesActivity activity) {
		this.activity = activity;
		this.db = QuickNoteDAO.self(activity);
	}

	@Override
	protected List<QuickNoteModel> doInBackground(String... params) {
		List<QuickNoteModel> importedNotes = new ArrayList<>();
		try {
			String json = readFromFile(params[0]);
			QuickNotesContainer data = JsonUtil.toObject(QuickNotesContainer.class, json);
			int count = 0;
			synchronized (db) {
				List<QuickNoteModel> notesInDb = db.getAllQuickNotes();
				for (QuickNoteModel model : data.getQuickNotes()) {
					if (model.getTitle() == null || notesInDb.contains(model)) {
						continue;
					}
					long newId = db.createQuickNote(model.getTitle(), model.getContent(), model.getRemindTime());
					QuickNoteModel created = db.getQuickNoteById(newId);
					importedNotes.add(created);
					count++;
				}
			}
			message = String.valueOf(count) + " " + activity.getString(R.string.success_import);
		} catch (Exception e) {
			message = activity.getString(R.string.error_import);
			Log.e("IMPORTER", e.getMessage());
		}
		return importedNotes;
	}

	@Override
	protected void onPostExecute(final List<QuickNoteModel> result) {
		if (activity.isActivityOnFront()) {
			activity.addFromImport(result);
			Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
		}
	}

	private String readFromFile(String fullPath) throws Exception {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		try {
			br = openStream(fullPath);
			String line = br.readLine();
			if(!QuickNotesApplication.getMd5().equals(line)){
				throw new Exception();
			}
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			closeStream(br);
		}

		return sb.toString();
	}

	private BufferedReader openStream(String fullPath) throws Exception {
		File fileOnSD = new File(fullPath);
		InputStream is = new FileInputStream(fileOnSD);
		InputStreamReader isr = new InputStreamReader(is);
		return new BufferedReader(isr);
	}

	private void closeStream(BufferedReader br) {
		try {
			if (br != null)
				br.close();
		} catch (IOException e) {
		}
	}
}

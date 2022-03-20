package link.androidapps.quicknotes.io;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import link.androidapps.quicknotes.QuickNotesApplication;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.util.JsonUtil;
import link.androidapps.quicknotes.view.ListQuickNotesActivity;

public class Exporter extends AsyncTask<File, Void, String> {
	private Context context;

	public Exporter(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(File... files) {
		String message = null;
		PrintWriter pw = null;
		QuickNoteDAO db = QuickNoteDAO.self(this.context);
		synchronized (db) {
			try {
				files[0].createNewFile();
				pw = new PrintWriter(new FileWriter(files[0], false), true);
				pw.println(QuickNotesApplication.getMd5());
				QuickNotesContainer data = new QuickNotesContainer(db.getAllQuickNotes());
				String json = JsonUtil.toJson(data);
				pw.println(json);
				message = context.getString(R.string.success_export) + files[0].getAbsolutePath();
			} catch (Exception e) {
				message = context.getString(R.string.error_export);
				Log.e("Exporter", e.getMessage());
			} finally {
				closeStream(pw);
			}
		}

		return message;
	}

	@Override
	protected void onPostExecute(final String result) {
		if (((ListQuickNotesActivity) context).isActivityOnFront()) {
			((Activity) context).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(context, result, Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	private void closeStream(PrintWriter pw) {
		if (pw != null)
			pw.close();
	}
}

package link.androidapps.quicknotes.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import link.androidapps.quicknotes.Constants;
import link.androidapps.quicknotes.R;

/**
 * Created by PKamenov on 29.11.15.
 */
public class FilePicker {
	public static final String PARENT_DIR = "..";
	private static final String DEFAULT_EXPORT_FILENAME = "quick_notes";

	private EditText editNameBox;
	private ListView list;
	private File currentPath;
	private FileSelectedListener fileListener;
	private ArrayAdapter adapter;
	private List<String> files = new ArrayList<>();
	private Dialog pickerDialog;
	private Dialog alertDialog;
	private Dialog createFolderDialog;

	public interface FileSelectedListener {
		void fileSelected(File file, boolean isExport);
	}

	public FilePicker setFileListener(FileSelectedListener fileListener) {
		this.fileListener = fileListener;
		return this;
	}

	public FilePicker(final Activity activity, final boolean isExport) {
		adapter = new FilePickerAdapter(activity, files);
		pickerDialog = new Dialog(activity);
		TextView textView = (TextView) pickerDialog.findViewById(android.R.id.title);
		textView.setTextSize(15);
		pickerDialog.setContentView(R.layout.file_picker);
		pickerDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
		editNameBox = (EditText) pickerDialog.findViewById(R.id.file_picker_text_box);

		if (isExport) {
			editNameBox.setEnabled(true);
			editNameBox.setText(DEFAULT_EXPORT_FILENAME);
			editNameBox.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					v.requestFocus();
					InputMethodManager in = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					in.showSoftInput(v, 0);
				}
			});
		} else {
			editNameBox.setEnabled(false);
		}

		setupButtons(activity, isExport);

		list = (ListView) pickerDialog.findViewById(R.id.file_picker_list);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
				String fileChosen = (String) list.getItemAtPosition(which);
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory()) {
					loadFiles(chosenFile);
				} else if(isExport){
					editNameBox.setText(chosenFile.getName());
				} else {
					FilePicker.this.notifyFileSelectedListener(false, chosenFile);
				}
			}
		});
		list.setAdapter(adapter);
		loadFiles(Constants.EXT_STOR);
	}

	private void setupButtons(final Activity activity, final boolean isExport) {
		Button btnExport = (Button) pickerDialog.findViewById(R.id.file_picker_export);
		Button btnCreate = (Button) pickerDialog.findViewById(R.id.file_picker_create);
		Button btnClose = (Button) pickerDialog.findViewById(R.id.file_picker_close);

		btnClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pickerDialog.dismiss();
			}
		});
		if (isExport) {
			btnCreate.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					createFolderDialog = new AlertDialog.Builder(activity).setTitle(R.string.new_folder)
							.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									EditText input = (EditText) ((AlertDialog) dialog).findViewById(R.id.create);
									if (StringUtils.isEmpty(input.getText().toString())) {
										alertDialog = new AlertDialog.Builder(activity).setMessage(R.string.empty_filename).setNeutralButton(R.string.close, null).show();
										return;
									}
									File newDir = getChosenFile(input.getText().toString());
									if (newDir.exists()) {
										alertDialog = new AlertDialog.Builder(activity).setMessage(R.string.folder_exist).setNeutralButton(R.string.close, null).show();
										return;
									}
									if (newDir.mkdir()) {
										loadFiles(newDir);
									}
								}
							}).setNeutralButton(R.string.close, null).setView(activity.getLayoutInflater().inflate(R.layout.create_dialog, null)).show();

				}
			});
			btnExport.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (StringUtils.isEmpty(editNameBox.getText().toString())) {
						alertDialog = new AlertDialog.Builder(activity).setMessage(R.string.empty_filename).setNeutralButton(R.string.close, null).show();
						return;
					}
					final File chosenFile = getChosenFile(editNameBox.getText().toString());
					if (chosenFile.exists()) {
						if(chosenFile.isDirectory()) {
							alertDialog = new AlertDialog.Builder(activity).setMessage(R.string.folder_exist).setNeutralButton(R.string.close, null).show();
						} else {
							alertDialog = new AlertDialog.Builder(activity).setTitle(R.string.confirm_override)
									.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											FilePicker.this.notifyFileSelectedListener(isExport, chosenFile);
										}
									}).setNegativeButton(R.string.cancel, null).show();
						}
					} else {
						FilePicker.this.notifyFileSelectedListener(isExport, chosenFile);
					}
				}
			});
		} else {
			btnCreate.setVisibility(View.GONE);
			LinearLayout btnLayout = (LinearLayout) pickerDialog.findViewById(R.id.fp_buttons);
			btnLayout.setWeightSum(1);
			btnExport.setVisibility(View.GONE);
			LinearLayout.LayoutParams paramsBtnClose = (LinearLayout.LayoutParams) btnClose.getLayoutParams();
			paramsBtnClose.weight = 1f;
			btnClose.setLayoutParams(paramsBtnClose);
		}
	}

	public void showDialog() {
		pickerDialog.show();
	}

	/**
	 * Sort and display the files for the given path.
	 */
	private void loadFiles(File path) {
		this.files.clear();
		this.currentPath = path;
		if (path.exists()) {
			File[] dirs = path.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return (file.isDirectory());
				}
			});
			Arrays.sort(dirs);
			File[] files = path.listFiles(new FileFilter() {
				@Override
				public boolean accept(File file) {
					return (!file.isDirectory());
				}
			});
			Arrays.sort(files);
			adapter.clear();
			if (!Constants.EXT_STOR.equals(path)) {
				adapter.add(PARENT_DIR);
			}
			for (File dir : dirs) {
				adapter.add(dir.getName());
			}
			for (File file : files ) {
				adapter.add(file.getName());
				this.files.add(file.getName());
			}

			// refresh the user interface
			pickerDialog.setTitle(currentPath.getPath());
		}
	}

	/**
	 * Convert a relative filename into an actual File object.
	 */
	private File getChosenFile(String fileChosen) {
		if (fileChosen.equals(PARENT_DIR)) {
			return currentPath.getParentFile();
		} else {
			return new File(currentPath, fileChosen);
		}
	}

	private void notifyFileSelectedListener(boolean isExport, File chosenFile) {
		if (fileListener != null) {
			fileListener.fileSelected(chosenFile, isExport);
		}
		pickerDialog.dismiss();
	}

	public void dismissOpenDialogs() {
		if(alertDialog != null) {
			alertDialog.dismiss();
		}
		if(createFolderDialog != null) {
			createFolderDialog.dismiss();
		}
		if (pickerDialog != null) {
			pickerDialog.dismiss();
		}
	}
}

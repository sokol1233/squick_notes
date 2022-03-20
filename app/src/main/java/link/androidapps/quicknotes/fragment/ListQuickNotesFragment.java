package link.androidapps.quicknotes.fragment;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.List;

import link.androidapps.quicknotes.QuickNotesBroadcastReceiver;
import link.androidapps.quicknotes.comparator.AlphabeticComparator;
import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.listener.ActivityListener;
import link.androidapps.quicknotes.view.ListQuickNotesAdapter;

/**
 * Created by PKamenov on 13.11.15.
 */
public class ListQuickNotesFragment extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
	private static final int MENU_CONTEXT_DELETE_ID = 1;
	private static final int MENU_CONTEXT_SCHEDULE_ID = 2;
	private static final int MENU_CONTEXT_REMOVE_REMIND_ID = 3;
	private ListQuickNotesAdapter adapter;
	private ActivityListener activity;
	private AlertDialog alertDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = (ActivityListener)getActivity();
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
    public void onResume() {
		registerForContextMenu(getListView());
        refreshNotes();
        super.onResume();
    }

	@Override
	public void onPause() {
		activity.collapseSearch();
		if(alertDialog != null) {
			alertDialog.dismiss();
		}
		super.onPause();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		activity.onListItemClick(adapter.getItem(position));
		super.onListItemClick(l, v, position, id);
	}

	public void refreshNotes() {
		List<QuickNoteModel> allData = QuickNoteDAO.self(getActivity()).getAllQuickNotes();
		adapter = new ListQuickNotesAdapter(getActivity(), R.layout.custom_list_row, allData);
		setListAdapter(adapter);
		adapter.sort(AlphabeticComparator.instance());
		adapter.notifyDataSetChanged();
	}

	protected void confirmDelete(final QuickNoteModel note) {
		alertDialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.confirm_delete)
			.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(note.getRemindTime() > -1)
					QuickNotesBroadcastReceiver.cancelNotification(getActivity(), note.getId());
				int id = QuickNoteDAO.self(getActivity()).deleteQuickNote(note.getId());
				refreshNotes();
				activity.onItemDeleted(id);
			}
		}).setNegativeButton(R.string.cancel, null).show();
	}

	public void insertIntoAdapter(QuickNoteModel note) {
		adapter.add(note);
		adapter.sort(AlphabeticComparator.instance());
		adapter.notifyDataSetChanged();
	}

	public boolean contains(QuickNoteModel note) {
		return adapter.getPosition(note) > -1;
	}

	public boolean isEmpty() {
		return adapter.isEmpty();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		activity.clearDetails();
		adapter.getFilter().filter(newText);
		adapter.notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean onMenuItemActionExpand(MenuItem item) {
		activity.clearDetails();
		return true;
	}

	@Override
	public boolean onMenuItemActionCollapse(MenuItem item) {
		if(item.getItemId() == R.id.action_search) {
			adapter.getFilter().filter("");
			adapter.notifyDataSetChanged();
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		ListView lv = (ListView) view;
		AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
		QuickNoteModel note = (QuickNoteModel) lv.getItemAtPosition(acmi.position);

		menu.setHeaderTitle(note.getTitle());
		menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, getString(R.string.delete));
		menu.add(Menu.NONE, MENU_CONTEXT_SCHEDULE_ID, Menu.NONE, getString(R.string.schedule));
		if(note.getRemindTime() > -1) {
			menu.add(Menu.NONE, MENU_CONTEXT_REMOVE_REMIND_ID, Menu.NONE, getString(R.string.remove));
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case MENU_CONTEXT_DELETE_ID:
				confirmDelete(adapter.getItem(info.position));
				return true;
			case MENU_CONTEXT_SCHEDULE_ID:
				activity.scheduleReminder(adapter.getItem(info.position));
				return true;
            case MENU_CONTEXT_REMOVE_REMIND_ID:
                QuickNoteModel model = adapter.getItem(info.position);
                model.setRemindTime(-1);
                activity.updateRemindTime(model);
                return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	public List<QuickNoteModel> getAllItems() {
		return adapter.getAllData();
	}
}

package link.androidapps.quicknotes.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import link.androidapps.quicknotes.R;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.listener.ActivityListener;

/**
 * Created by PKamenov on 23.01.16.
 */
public class DateTimePicker {
	private ActivityListener activity;
	private Activity castedActivity;
	private AlertDialog pickerDialog;
	private DatePickerDialog dateDialog;
	private TimePickerDialog timeDialog;
	private TextView dateText;
	private TextView timeText;
	private Calendar calDate;
	private Calendar calTime;
	private Calendar calPicked;
	private java.text.DateFormat dateFormat;
	private java.text.DateFormat timeFormat;
	private QuickNoteModel note;
    private int fYear;
    private int fMonth;
    private int fDay;
    private int fHour;
    private int fMinute;

	public DateTimePicker(ActivityListener activity, QuickNoteModel note) {
		this.activity = activity;
		this.castedActivity = Activity.class.cast(activity);
		this.note = note;
		dateFormat = SimpleDateFormat.getDateInstance();
		timeFormat = DateFormat.getTimeFormat(castedActivity);

        calDate = Calendar.getInstance();
        calTime = Calendar.getInstance();
        if(note.getRemindTime() > -1) {
            Date date = new Date(note.getRemindTime());
            calDate.setTime(date);
            calTime.setTime(date);
        }
        fYear = calDate.get(Calendar.YEAR);
        fMonth = calDate.get(Calendar.MONTH);
        fDay = calDate.get(Calendar.DAY_OF_MONTH);
        fHour = calTime.get(Calendar.HOUR_OF_DAY);
        fMinute = calTime.get(Calendar.MINUTE);

		LayoutInflater inflater = (LayoutInflater)castedActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.date_time_picker, null);

		createPickerDialog(content);
		createDatePickerDialog();
		createTimePickerDialog();

		dateText = (TextView) content.findViewById(R.id.dt_picker_date);
		dateText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dateDialog.updateDate(fYear, fMonth, fDay);
				dateDialog.show();
			}
		});

		timeText = (TextView) content.findViewById(R.id.dt_picker_time);
		timeText.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				timeDialog.updateTime(fHour, fMinute);
				timeDialog.show();
			}
		});
	}

	private void createPickerDialog(View content) {
		pickerDialog = new AlertDialog.Builder(castedActivity).setTitle(R.string.schedule).setView(content)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
                        calPicked = new GregorianCalendar(fYear, fMonth, fDay, fHour, fMinute);
						note.setRemindTime(calPicked.getTimeInMillis());
						activity.updateRemindTime(note);
					}
				}).setNegativeButton(R.string.cancel, null).create();
	}

	private void createDatePickerDialog() {
		dateDialog = new DatePickerDialog(castedActivity, new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calDate.set(year, monthOfYear, dayOfMonth);
                fYear = year;
                fMonth = monthOfYear;
                fDay = dayOfMonth;
				dateText.setText(dateFormat.format(calDate.getTime()));
			}
		}, calDate.get(Calendar.YEAR), calDate.get(Calendar.MONTH), calDate.get(Calendar.DAY_OF_MONTH));
	}

	private void createTimePickerDialog() {
		timeDialog = new TimePickerDialog(castedActivity, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calTime.set(Calendar.MINUTE, minute);
                fHour = hourOfDay;
                fMinute = minute;
				timeText.setText(timeFormat.format(calTime.getTime()));
			}
		}, calTime.get(Calendar.HOUR_OF_DAY), calTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(castedActivity));
	}

	public void showDialog() {
		dateText.setText(dateFormat.format(calDate.getTime()));
		timeText.setText(timeFormat.format(calTime.getTime()));
		pickerDialog.show();
	}

	public void dismissOpenDialogs() {
		if(dateDialog != null) {
			dateDialog.dismiss();
		}
		if(timeDialog != null) {
			timeDialog.dismiss();
		}
		if (pickerDialog != null) {
			pickerDialog.dismiss();
		}
	}
}

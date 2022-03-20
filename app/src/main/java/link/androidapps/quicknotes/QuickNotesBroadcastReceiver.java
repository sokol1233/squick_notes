package link.androidapps.quicknotes;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.util.List;

import link.androidapps.quicknotes.db.QuickNoteDAO;
import link.androidapps.quicknotes.db.QuickNoteModel;
import link.androidapps.quicknotes.view.ListQuickNotesActivity;

/**
 * Created by PKamenov on 23.01.16.
 */
public class QuickNotesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "QuickNotesBroadcastReceiver";
    private static final long REPEAT_INTERVAL = 5 * 60 * 1000; // five minutes
    private static final long TEN_SECONDS = 10 * 1000;
	private PowerManager.WakeLock wakeLock;

	@Override
	public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            List<QuickNoteModel> allWithReminder = QuickNoteDAO.self(context).getAllWithReminder();
            for (QuickNoteModel model : allWithReminder) {
                long now = System.currentTimeMillis();
                if(model.getRemindTime() > now + TEN_SECONDS)
                    scheduleNotification(context, model.getId(), model.getRemindTime(), model.getTitle());
                else
                    scheduleNotification(context, model.getId(), now + TEN_SECONDS, model.getTitle());
            }
        }
		if(intent.getAction().startsWith(Constants.REMIND_ACTION)) {
			PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			wakeLock.acquire();
			riseNotification(context, intent);
		}
	}

    private void riseNotification(Context context, Intent broadcastIntent) {
        long id = broadcastIntent.getLongExtra(Constants.INTENT_EXTRA_ID, -1L);
        Intent intent = new Intent(context, ListQuickNotesActivity.class);
        intent.setAction(Constants.REMIND_ACTION + id);
        intent.setFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.INTENT_EXTRA_ID, id);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, Long.valueOf(id).intValue(), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        String title = broadcastIntent.getStringExtra(Constants.INTENT_EXTRA_TITLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.notification);
        builder.setTicker(title);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        builder.setContentTitle(context.getString(R.string.label_notification_title));
        builder.setContentText(title);
        builder.setDefaults(Notification.DEFAULT_VIBRATE|Notification.DEFAULT_SOUND);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Long.valueOf(id).intValue(), notification);

        if(wakeLock != null)
            wakeLock.release();
    }

    public static void scheduleNotification(Context context, long id, long when, String title) {
        PendingIntent pendingIntent = createPendingIntent(context, id, title);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, when, REPEAT_INTERVAL, pendingIntent);
    }

    public static void cancelNotification(Context context, long id) {
        PendingIntent pendingIntent = createPendingIntent(context, id, null);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public static PendingIntent createPendingIntent(Context context, long id, String title) {
        Intent broadcastIntent = new Intent(context, QuickNotesBroadcastReceiver.class);
        broadcastIntent.setAction(Constants.REMIND_ACTION + id);
        broadcastIntent.putExtra(Constants.INTENT_EXTRA_ID, id);
        broadcastIntent.putExtra(Constants.INTENT_EXTRA_TITLE, title);
        return PendingIntent.getBroadcast(context, Long.valueOf(id).intValue(), broadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}

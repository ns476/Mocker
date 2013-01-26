package uk.ac.cam.ns476.mocker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class PermissionMockedIntentReceiver extends BroadcastReceiver {
	final static String MOCKED_INTENT = "uk.ac.cam.ns476.intent.action.MOCKED";
	final static String MOCKED_FOREGROUND_TAG = "mock_foreground";
	final static int MOCKED_FOREGROUND_ID = 1;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(Mocker.TAG, "Intent received");
		Log.i(Mocker.TAG, intent.getAction());

		CharSequence contentTitle = null;
		CharSequence contentText = null;
		int icon = R.drawable.stat_sys_mocked_foreground;
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		String packageName = null;
		String permission = null;
		
		if (intent.getAction().equals(MOCKED_INTENT)) {
			Log.i(Mocker.TAG, "Received mocked intent");
			permission = intent.getExtras().getString("permission");
			packageName = intent.getExtras().getString("packageName");
			contentTitle = context.getString(R.string.MockedNotificationTitle);
			contentText = String.format(context.getString(R.string.MockedNotificationData), 
					InstalledPackage.getReadablePackageName(context, packageName), 
					InstalledPackage.getReadablePermissionDescription(context, permission));
			icon = R.drawable.stat_sys_mocked_foreground;
		} else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			Log.i(Mocker.TAG, "Received action package added");
			packageName = intent.getData().getSchemeSpecificPart();
			contentTitle = context.getString(R.string.NewAppNotificationTitle);
			contentText = String.format(context.getString(R.string.NewAppNotificationData), InstalledPackage.getReadablePackageName(context, packageName));
			icon = R.drawable.stat_sys_mocked_background;
		}
		long when = System.currentTimeMillis();
		
		Intent toRun = new Intent(context, SetMockActivity.class);
		toRun.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		toRun.putExtra("pkg", packageName);
		if (permission != null) {
			toRun.putExtra("perm", permission);
		}	
		PendingIntent activity = PendingIntent.getActivity(context, 0, toRun, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Notification n = new Notification(icon, contentTitle, when);
		n.flags |= Notification.FLAG_AUTO_CANCEL;
		n.setLatestEventInfo(context, contentTitle, contentText, activity);
		
		nm.notify(packageName, MOCKED_FOREGROUND_ID, n);
	}
}
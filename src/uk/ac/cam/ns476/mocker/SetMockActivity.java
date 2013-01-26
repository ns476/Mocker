package uk.ac.cam.ns476.mocker;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SetMockActivity extends ListActivity {
	ListView lv = null;
	InstalledPackage pkg;
	String mockedPerm;
	
	Map<String, Permission> perms;
	NotificationManager nm;

	private static class Permission {
		private String name;
		private String label;
		private boolean mocked;
		public Permission() {
			mocked = false;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public void setMocked(boolean mocked) {
			this.mocked = mocked;
		}
		public boolean isMocked() {
			return mocked;
		}
	}

	// A list of interesting permissions we can mock
	private static String[] INTERESTING_PERMS = {"android.permission.INTERNET",
		"android.permission.READ_SMS",
		"android.permission.WRITE_SMS",
		"android.permission.RECEIVE_SMS",
		"android.permission.RECEIVE_MMS",
		"android.permission.READ_CONTACTS",
		"android.permission.WRITE_CONTACTS",
		"android.permission.READ_CALENDAR",
		"android.permission.WRITE_CALENDAR",
		"android.permission.ACCESS_COARSE_LOCATION",
		"android.permission.ACCESS_FINE_LOCATION",
		"android.permission.READ_PHONE_STATE"
	};
	
	private Intent i;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		i = getIntent();
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.addmock);
		Arrays.sort(INTERESTING_PERMS);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		String pkgName = (String) i.getExtras().get("pkg");
		Log.i(Mocker.TAG, pkgName);
		mockedPerm = (String) i.getExtras().get("perm");
		
		if (mockedPerm != null) Log.i(Mocker.TAG, mockedPerm);
		
		Drawable defaultIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
		PackageManager pm = getPackageManager();
		PackageInfo pi = null;
		perms = new LinkedHashMap<String, Permission>();
		
		try {
			pkg = new InstalledPackage(pm ,pkgName, defaultIcon);
			pi = pm.getPackageInfo(pkg.getPkg(), PackageManager.GET_PERMISSIONS | PackageManager.GET_RECEIVERS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}


		TextView tv = (TextView) findViewById(R.id.Label);
		tv.setText(pkg.getName());
		ImageView iv = (ImageView) findViewById(R.id.Icon);
		iv.setImageDrawable(pkg.getIcon());
		lv = (ListView) findViewById(android.R.id.list);
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		LinkedHashSet<String> requestedPermissions;

		if (pi != null) {
			if (pi.requestedPermissions == null) {
				requestedPermissions = new LinkedHashSet<String>();
			} else {
				requestedPermissions = new LinkedHashSet<String>(Arrays.asList(pi.requestedPermissions));
			}
			if (pi.receivers != null) {
				for (ActivityInfo receiver: pi.receivers) {
					if (receiver.permission != null) {
						requestedPermissions.add(receiver.permission);
					}
				}
			}
			
			for(String permName: requestedPermissions) {
				PermissionInfo permInfo = null;
				String desc = permName;
				try {
					permInfo = pm.getPermissionInfo(permName, PackageManager.GET_META_DATA);
					desc = (String) pm.getText(permInfo.packageName, permInfo.labelRes, null);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				if (Arrays.binarySearch(INTERESTING_PERMS, permName) >= 0) {
					Permission p = new Permission();
					p.setName(permName);
					p.setLabel(desc);
					perms.put(permName, p);
				}
			}
		}
		updateMockedPermissions();

		final Context c = (Context) this;

		lv.setAdapter(new ArrayAdapter<Permission>(this, android.R.id.list, new ArrayList<Permission>(perms.values())) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Resources r = getResources();
				LayoutInflater li = LayoutInflater.from(c);
				View v = li.inflate(android.R.layout.simple_list_item_checked, parent, false);

				CheckedTextView tv = (CheckedTextView) v.findViewById(android.R.id.text1);
				
				
				Permission p = getItem(position);	

				if (!p.isMocked()) {
					tv.setTextColor(r.getColor(R.color.unmocked));
					tv.setChecked(false);
				} else {
					tv.setTextColor(r.getColor(R.color.mocked));
					tv.setChecked(true);
				}
				
				tv.setText(p.getLabel());

				return v;
			}
		});
		lv.invalidate();
	}

	private void updateMockedPermissions() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("/etc/permissions/mock/" + pkg.getPkg())));
			String line;
			while((line = br.readLine()) != null) {
				if (perms.containsKey(line)) {
					Permission p = perms.get(line);
					p.setMocked(true);
					perms.put(line, p);
				}
			}

		} catch (FileNotFoundException e) {} 
		catch (IOException e) {}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Permission p = (Permission) l.getItemAtPosition(position);
		CheckedTextView tv = (CheckedTextView) v.findViewById(android.R.id.text1);

		p.setMocked(!p.isMocked());

		if (!p.isMocked()) {
			tv.setTextColor(0xffdf6926);
			tv.setChecked(false);
		} else {
			tv.setTextColor(0xff26df6f);
			tv.setChecked(true);
		}
		
		if (p.getName().equals("android.permission.INTERNET")) {
			ActivityManager m = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			m.restartPackage(pkg.getName());
		}
		
		nm.cancel(pkg.getPkg(), PermissionMockedIntentReceiver.MOCKED_FOREGROUND_ID);

		perms.put(p.getName(), p);
		rewriteMockPermissions();
	}

	private void rewriteMockPermissions() {
		List<Permission> mocked = new ArrayList<Permission>(perms.values());
		File f = new File("/etc/permissions/mock/" + pkg.getPkg());
		BufferedWriter bw = null;
		f.delete();
		try {
			bw = new BufferedWriter(new FileWriter(f));
			for (Permission p: mocked) {
				if (p.isMocked()) {
					bw.write(p.getName());
					bw.write("\n");
				}
			}
			bw.flush();
			bw.close();
			SyscallHelper.chmod(f, 0660);
			SyscallHelper.chgrp(f, "mock");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}

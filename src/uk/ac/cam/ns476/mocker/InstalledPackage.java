package uk.ac.cam.ns476.mocker;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;

public class InstalledPackage implements Comparable<InstalledPackage>{
	private String name;
	private Drawable icon;
	private String pkg;

	public InstalledPackage(String name, Drawable icon, String pkg) {
		this.name = name;
		this.icon = icon;
		this.pkg = pkg;
	}
	
	public InstalledPackage(PackageManager pm, ApplicationInfo ai, Drawable defaultIcon) {
    	name = ai.packageName;
    	icon = defaultIcon;
    	pkg = ai.packageName;
    	if (ai.labelRes != 0) {
    		name = (String) pm.getText(ai.packageName, ai.labelRes, ai);
    	}
    	if (ai.icon != 0) {
    		icon = pm.getDrawable(ai.packageName, ai.icon, ai);
    	}
	}
	
	public InstalledPackage(PackageManager pm, String pkg, Drawable defaultIcon) throws NameNotFoundException {
		this(pm, pm.getApplicationInfo(pkg, 0), defaultIcon);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	@Override
	public int compareTo(InstalledPackage another) {
		return this.name.compareTo(another.getName());
	}
	
	public static String getReadablePackageName(Context c, String packageName) {
		PackageManager pm = c.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			return packageName;
		}
		
		return new InstalledPackage(pm, pi.applicationInfo, null).getName();
	}
	
	public static String getReadablePermissionDescription(Context c, String permName) {
		PackageManager pm = c.getPackageManager();
		try {
			PermissionInfo permInfo = pm.getPermissionInfo(permName, PackageManager.GET_META_DATA);
			return (String) pm.getText(permInfo.packageName, permInfo.labelRes, null);
		} catch (NameNotFoundException e) {
			return permName;
		}
	}
	
}
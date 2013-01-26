package uk.ac.cam.ns476.mocker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class Mocker extends ListActivity {
	public static final String TAG = "Mocker";
	private List<InstalledPackage> installedPackages = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ListView lv = (ListView) findViewById(android.R.id.list);
        installedPackages = new ArrayList<InstalledPackage>();
        
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> pkgs = pm.getInstalledApplications(0);
        Drawable defaultIcon = getResources().getDrawable(android.R.drawable.sym_def_app_icon);
                
        for(ApplicationInfo i: pkgs) {
        	String label = i.packageName;
        	Drawable icon = defaultIcon;
        	String pkg = i.packageName;
        	if (i.labelRes != 0) {
        		label = (String) pm.getText(i.packageName, i.labelRes, i);
        	}
        	if (i.icon != 0) {
        		icon = pm.getDrawable(i.packageName, i.icon, i);
        	}
        	installedPackages.add(new InstalledPackage(label, icon, pkg));
        }
        Collections.sort(installedPackages);
        
        final Context c = (Context) this;
        

        lv.setAdapter(new ArrayAdapter<InstalledPackage>(this, android.R.id.list, installedPackages) {
        	@Override
        	public View getView(int position, View convertView, ViewGroup parent) {
        		LayoutInflater inflater = LayoutInflater.from(c);
        		View row = convertView;
        		
        		if (row == null) {
        			row = inflater.inflate(R.layout.listitem, parent, false);
        		}
        		
        		TextView label = (TextView) row.findViewById(R.id.Label);
        		ImageView icon = (ImageView) row.findViewById(R.id.Icon);
        		
        		InstalledPackage p = getItem(position);
        		
        		label.setText(p.getName());
        		icon.setImageDrawable(p.getIcon());
        		icon.setScaleType(ScaleType.FIT_XY);
        		
        		return row;
        	}
        });
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	InstalledPackage pkg = (InstalledPackage) l.getAdapter().getItem(position);
    	Intent i = new Intent(this, SetMockActivity.class);
    	i.putExtra("pkg", pkg.getPkg());
    	
    	ImageView icon = (ImageView) v.findViewById(R.id.Icon);
    	Log.i(Mocker.TAG, icon.getScaleType().toString());
    	
    	
    	startActivity(i);
    }
}
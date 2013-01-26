package uk.ac.cam.ns476.mocker;

import java.io.File;
import java.io.IOException;

import android.util.Log;

// TODO: Probably use JNI for this
public class SyscallHelper {
	private static int exec(String cmd) {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process p = runtime.exec(cmd);
			return p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static int chmod(File f, int i) {	
		String cmd = "chmod " + Integer.toOctalString(i) + " " + f.getAbsolutePath();
		Log.i(Mocker.TAG, "Running command: " + cmd);
		return exec(cmd);
	}
	
	public static int chown(File f, String uid) {
		String cmd = "chown " + uid + " " + f.getAbsolutePath();
		Log.i(Mocker.TAG, "Running command: " + cmd);
		return exec(cmd);
	}
	
	public static int chgrp(File f, String gid) {
		String cmd = "chgrp " + gid + " " + f.getAbsolutePath();
		Log.i(Mocker.TAG, "Running command: " + cmd);
		return exec(cmd);
	}
}

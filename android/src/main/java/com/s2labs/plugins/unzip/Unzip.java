package com.s2labs.plugins.unzip;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.obsez.android.lib.filechooser.ChooserDialog;

import net.lingala.zip4j.ZipFile;

import java.io.File;

@NativePlugin
public class Unzip extends Plugin {

	@PluginMethod
	public void chooseFile(final PluginCall call) {
		getBridge().executeOnMainThread(() -> new ChooserDialog(getBridge().getActivity())
				.withFilter(false, false, call.getString("filter"))
				.withChosenListener((path, pathFile) -> {
					JSObject obj = new JSObject();
					obj.put("path", pathFile.getAbsolutePath());
					call.success(obj);
				})
				// to handle the back key pressed or clicked outside the dialog:
				.withOnCancelListener(dialog -> call.reject("File selection cancelled", "CANCELLED"))
				.build()
				.show());
	}

	@PluginMethod
	public void unzipFile(final PluginCall call) {
		try {
			File file = getContext().getCacheDir();
			if (file == null) {
				call.reject("Can't open file system", "FILE_SYSTEM_FAILED");
				return;
			}
			File zipPath = new File(call.getString("path"));
			String password = call.getString("password", null);
			if (!zipPath.exists()) {
				call.reject("Can't find zip file", "FILE_NOT_FOUND");
				return;
			}
			ZipFile zipFile;
			if (password != null) {
				zipFile = new ZipFile(zipPath, password.toCharArray());
			} else {
				zipFile = new ZipFile(zipPath);
			}
			String folderName = System.currentTimeMillis() + "";
			File extractFolder = new File(file, folderName);
			if (!extractFolder.exists()) {
				extractFolder.mkdirs();
			}
			zipFile.extractAll(extractFolder.getAbsolutePath());
			JSObject obj = new JSObject();
			obj.put("output", folderName);
			call.success(obj);
		} catch (Exception e) {
			e.printStackTrace();
			call.reject("Exception Occurred", e);
		}
	}
}

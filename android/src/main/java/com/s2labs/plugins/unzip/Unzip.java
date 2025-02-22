package com.s2labs.plugins.unzip;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.content.pm.PackageManager;

import androidx.annotation.RequiresApi;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import net.lingala.zip4j.ZipFile;
import org.json.JSONArray;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@NativePlugin(
  requestCodes = {
    Unzip.FILE_SELECT_CODE,
    Unzip.REQUEST_EXTERNAL_STORAGE_CODE,
  }
)
public class Unzip extends Plugin {
  static final int REQUEST_EXTERNAL_STORAGE_CODE = 5690;
  static final int FILE_SELECT_CODE = 4721;
  private PluginCall savedCall;

  @PluginMethod
  public void filePicker(PluginCall call) {
    if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
      saveCall(call);
      pluginRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, REQUEST_EXTERNAL_STORAGE_CODE);
    } else {
      this.savedCall = call;
      try {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        startActivityForResult(this.savedCall, Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
      } catch (android.content.ActivityNotFoundException ex) {
        this.savedCall.reject("Please install a File Manager.");
      }
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  protected void handleOnActivityResult(int requestCode, int resultCode, Intent intent) {
    if (resultCode == Activity.RESULT_OK ) {
      if(intent != null)  {
        Uri fileUri = intent.getData();
        String fileName;
        if(fileUri.toString().startsWith("file:")) {
          fileName = fileUri.getPath();
        } else {
          Cursor cursor = getContext().getContentResolver().query(fileUri, null, null, null, null);
          int fileNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
          cursor.moveToFirst();
          fileName = cursor.getString(fileNameIndex);
          cursor.close();
        }
        JSObject obj = new JSObject();
        obj.put("filename", fileName);
        obj.put("uri", fileUri);
        this.savedCall.success(obj);
      }
    }
  }

  @Override
  protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    this.savedCall = getSavedCall();
    if (savedCall == null) {
      return;
    }
    for(int result : grantResults) {
      if (result == PackageManager.PERMISSION_DENIED) {
        savedCall.error("User denied permission");
        return;
      }
    }
    // Add the methods which required permission
    if (this.savedCall.getMethodName().equals("filePicker")) {
      filePicker(this.savedCall);
    }
  }

  @PluginMethod
  public void unzipFile(final PluginCall call) {
    try {
      File file = getContext().getCacheDir();
      Uri uri = Uri.parse(call.getString("path"));
      InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
      String password = call.getString("password", null);
      /* Extracted Path */
      String tempZipFileName = "ota_file.zip";
      String folderName = System.currentTimeMillis() + "";
      File extractFolder = new File(file, folderName);
      if (!extractFolder.exists()) {
        extractFolder.mkdirs();
      }
      File newFile = new File(extractFolder, tempZipFileName);
      OutputStream outputStream = new FileOutputStream(newFile);
      byte[] buffer = new byte[1024];
      int readLen = 0;
      while((readLen = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer,0, readLen);
      }
      outputStream.flush();
      outputStream.close();
      inputStream.close();

      ZipFile zipFile;
      if (password != null) {
        zipFile = new ZipFile(newFile, password.toCharArray());
      } else {
        zipFile = new ZipFile(newFile);
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

	@PluginMethod
	public void getAllIps(PluginCall call) {
		JSONArray arr = new JSONArray();
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						arr.put(inetAddress.getHostAddress());
					}
				}
			}
		} catch (SocketException ignored) {}
		JSObject obj = new JSObject();
		obj.put("ips", arr);
		call.success(obj);
	}
}

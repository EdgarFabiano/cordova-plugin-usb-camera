package com.estafeta.usbcameraplugin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.pm.PackageManager;
import android.util.Log;

/**
 * This class echoes a string called from JavaScript.
 */
public class USBCameraPlugin extends CordovaPlugin {
	
	private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		
        Context context = cordova.getActivity().getApplicationContext();
        if (action.equals("StartCamera")) {
            if (ContextCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(cordova.getActivity(),
                        new String[]{Manifest.permission.CAMERA}, 1);
            } else {
                this.StartCamera(context);
                return true;
            }
        }
        return false;
    }

    private void StartCamera(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        //Init ActivityFor Result on Cordova
		try{
			this.cordova.startActivityForResult(this,intent,0);
		}catch(Exception e){
			Log.e("USBCameraPlugin", "Un error ocurrió al procesar la cámara");
		}
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		String photo = "";
		if (resultCode == android.app.Activity.RESULT_OK) {
                
            try {
				photo=intent.getStringExtra("result");
				//Log.v("Foto: ", photo);
				this.callbackContext.success(photo);
                 
            } catch (Exception e) {
				this.callbackContext.error("Error");
            }
			
        } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
            this.callbackContext.error("Error");
		} else {
            this.callbackContext.error("Unexpected error");
		}
	}
}

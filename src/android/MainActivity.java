package com.estafeta.usbcameraplugin;

import com.estafeta.pdv.R;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.content.Intent;


import com.jiangdg.usbcamera.UVCCameraHelper;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.CameraDialog;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.common.AbstractUVCCameraHandler;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import java.io.ByteArrayOutputStream;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import java.util.HashMap;
import java.util.Iterator;
import android.content.Context;
import android.util.Log;




//public class MainActivity extends AppCompatActivity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback{
public class MainActivity extends Activity implements CameraDialog.CameraDialogParent, CameraViewInterface.Callback{

    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;
    private View mTextureView;
    private Button buttonClick;
    private boolean isRequest;
    private boolean isPreview;
	 






    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {

        @Override
        public void onAttachDev(UsbDevice device) {
		boolean isCameraModelConnected=false;
		//LEGH: Se piden permisos de uso de USB
		int indexDevice=-1;
		UsbManager usbManager;
		HashMap<String, UsbDevice> deviceList;
		Iterator<UsbDevice> deviceIterator;

		try{
			usbManager =  (UsbManager) MainActivity.this.getSystemService(Context.USB_SERVICE);		
			deviceList = usbManager.getDeviceList();
			deviceIterator = deviceList.values().iterator();
			while (deviceIterator.hasNext()) {
				indexDevice=indexDevice+1;
				UsbDevice deviceCamera = deviceIterator.next();
				Log.d("USBCameraPlugin", "VendorID: " + deviceCamera.getVendorId());
				Log.d("USBCameraPlugin", "ProductID: " + deviceCamera.getProductId());
				if(deviceCamera.getVendorId()==1133 || //Logitech
						deviceCamera.getVendorId()==11902 || //ViewSonic
						deviceCamera.getVendorId()==9157 || //Brobotics
						deviceCamera.getVendorId()==3141 || //Sonix
						deviceCamera.getVendorId()==1423 || //Warrior maeve webcam HD 1080P . AC340
						deviceCamera.getVendorId()==9228){
					isCameraModelConnected=true;
					break;				
				}				
			}
			
			if(isCameraModelConnected){
				
				// request open permission
				if (!isRequest) {
					isRequest = true;
					if (mCameraHelper != null) {
						mCameraHelper.requestPermission(indexDevice);
					}
				}
			}else{
					//Devolver el resultado Erroneo
					showShortMsg("La cámara ha sido desconectada");
					Log.e("USBCameraPlugin", "No se detecto la cámara");
					Intent returnIntent = new Intent();
					returnIntent.putExtra("result", "No se detecto la camara");
					setResult(Activity.RESULT_CANCELED, returnIntent);
					finish();
			}			
			
			
		}catch(Exception e){
				//Devolver el resultado Erroneo
				showShortMsg("Error al leer la camara. "+e.toString());
				Log.e("USBCameraPlugin", "Error al leer la camara. "+e.toString());
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "Error al leer la camara. "+e.toString());
				setResult(Activity.RESULT_CANCELED, returnIntent);
				finish();
		}
		

		
		
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            // close camera
            if (isRequest) {
				
				if(device.getVendorId()==1133 ||
						device.getVendorId()==9157 ||
						device.getVendorId()==1423 || //Warrior maeve webcam HD 1080P . AC340
						device.getVendorId()==9228){
					try{
						
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						isRequest = false;
						isPreview = false;
						if (mCameraHelper != null) {
							mCameraHelper.stopPreview();					
							mCameraHelper.closeCamera();						
							mCameraHelper.unregisterUSB();
							mCameraHelper.release();
						}		
						FileUtils.releaseFile();						
					}catch(Exception e){
						Log.e("USBCameraPlugin", "Error al cerrar la camara al desconectar USB. "+e.toString());
					}
					
					//Devolver el resultado Erroneo
					showShortMsg("La cámara ha sido desconectada");
					Log.e("USBCameraPlugin", "La cámara ha sido desconectada");
					Intent returnIntent = new Intent();
					returnIntent.putExtra("result","La cámara ha sido desconectada");
					setResult(Activity.RESULT_CANCELED, returnIntent);
					finish();
					
				}
				

            }
        }

        @Override
        public void onConnectDev(UsbDevice device, boolean isConnected) {
            if (!isConnected) {
                showShortMsg("Falló la conexión, verifique dispositivo o permisos");
                isPreview = false;
            } else {
                isPreview = true;
                showShortMsg("Conectando");
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {

            //showShortMsg("Desconectando");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		try{
			mTextureView=findViewById(R.id.camera_view);
			// step.1 initialize UVCCameraHelper
			mUVCCameraView = (CameraViewInterface) mTextureView;
			mUVCCameraView.setCallback(this);
			mCameraHelper = UVCCameraHelper.getInstance();
			//mCameraHelper.setDefaultPreviewSize(640,360);
			mCameraHelper.setDefaultPreviewSize(1280,720);
			mCameraHelper.setDefaultFrameFormat(UVCCameraHelper.FRAME_FORMAT_MJPEG);
			mCameraHelper.initUSBMonitor(this, mUVCCameraView, listener);
			this.setTitle("");
		}catch(Exception e){
				//Devolver el resultado Erroneo
				showShortMsg("Error al leer la camara. "+e.toString());
				Log.e("USBCameraPlugin", "Error al leer la camara. "+e.toString());
				Intent returnIntent = new Intent();
				returnIntent.putExtra("result", "Error al leer la camara. "+e.toString());
				setResult(Activity.RESULT_CANCELED, returnIntent);
				finish();
		}

    }




    @Override
    protected void onStart() {
        super.onStart();
        // step.2 register USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.registerUSB();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.releaseFile();
        // step.4 release uvc camera resources
        if (mCameraHelper != null) {
            mCameraHelper.release();
        }
    }
    @Override
    public USBMonitor getUSBMonitor() {
        return mCameraHelper.getUSBMonitor();
    }

    @Override
    public void onDialogResult(boolean canceled) {
        if (canceled) {
            showShortMsg("Cancelar operación");
        }
    }


    @Override
    public void onSurfaceCreated(CameraViewInterface view, Surface surface) {
        if (!isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.startPreview(mUVCCameraView);
            isPreview = true;
        }
    }

    @Override
    public void onSurfaceChanged(CameraViewInterface view, Surface surface, int width, int height) {

    }

    @Override
    public void onSurfaceDestroy(CameraViewInterface view, Surface surface) {
        if (isPreview && mCameraHelper.isCameraOpened()) {
            mCameraHelper.stopPreview();
            isPreview = false;
        }
    }

    public void clickFlow(View view){
        if (mCameraHelper == null || !mCameraHelper.isCameraOpened()) {
            showShortMsg("Conexión fallida, verifique conexión de la cámara o permisos");
        }else {
            String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "usbcameraplugin/images/"
                    + "CapturedPhoto" + UVCCameraHelper.SUFFIX_JPEG;

            mCameraHelper.capturePicture(picPath, new AbstractUVCCameraHandler.OnCaptureListener() {
                @Override
                public void onCaptureResult(String path) {
                    if (TextUtils.isEmpty(path)) {
                        return;
                    }
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
							
							Bitmap imageBit = null;
							ByteArrayOutputStream ByteArrayObject = null;
							byte[] byteArray = null;
							String encodeString = null;
							try{
								imageBit = BitmapFactory.decodeFile(path);
								ByteArrayObject = new ByteArrayOutputStream();
								imageBit.compress(Bitmap.CompressFormat.JPEG, 80, ByteArrayObject);
								byteArray = ByteArrayObject.toByteArray();
								encodeString = Base64.encodeToString(byteArray, Base64.DEFAULT);
								
								try{						
									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									isRequest = false;
									isPreview = false;
									if (mCameraHelper != null) {
										mCameraHelper.stopPreview();					
										mCameraHelper.closeCamera();						
										mCameraHelper.unregisterUSB();
										mCameraHelper.release();
									}		
									FileUtils.releaseFile();						
								}catch(Exception e){
									Log.e("USBCameraPlugin", "Error al cerrar la camara al desconectar USB. "+e.toString());
								}
								
								//Devolver el resultado Exitoso
								Intent returnIntent = new Intent();
								returnIntent.putExtra("result", encodeString);
								setResult(Activity.RESULT_OK, returnIntent);
								finish();
							}
							catch (Exception e){
								//Devolver el resultado Erroneo
								Log.e("USBCameraPlugin", "Error al cerrar la camara despues de capturar. "+e.toString());
								Intent returnIntent = new Intent();
								returnIntent.putExtra("result", "Error: No fue posible generar la imagen");
								setResult(Activity.RESULT_CANCELED, returnIntent);
								finish();
								e.printStackTrace();
							}
                            
							//Toast.makeText(MainActivity.this, "save path:" + path, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

	}
	
		
	public void clickClose(View view){
		try{
			mCameraHelper.stopPreview();
			isPreview = false;
			onStop();
			onDestroy();
		}
		catch(Exception e){
			Log.e("USBCameraPlugin", "Error al cerrar la camara en el boton cancelar. "+e.toString());
		}
		
		
		//Devolver el resultado Exitoso
		Intent returnIntent = new Intent();
		returnIntent.putExtra("result", "CLOSE");
		setResult(Activity.RESULT_OK, returnIntent);
		finish();
	}
	
    /*if(isRequest) {
        // close camera
        mCameraHelper.closeCamera();
        FileUtils.releaseFile();

        // step.3 unregister USB event broadcast
        if (mCameraHelper != null) {
            mCameraHelper.unregisterUSB();
        }

        // step.4 release uvc camera resources
        mCameraHelper.release();
        isRequest=false;

    }*/


    private void showShortMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
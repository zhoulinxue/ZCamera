package org.zhx.common.camera.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.zhx.common.camera.AutoFocusManager;
import org.zhx.common.camera.util.DisplayUtil;
import org.zhx.common.camera.util.ImageUtil;
import org.zhx.common.camera.util.PermissionsUtil;
import org.zhx.common.camera.widget.OverlayerView;

import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements Camera.PictureCallback,
        SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = CameraActivity.class.getSimpleName();

    public static BitmapFactory.Options opt;
    static {
        // 缩小原图片大小
        opt = new BitmapFactory.Options();
        opt.inSampleSize = 1;
    }

    private SurfaceView mPreView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    private boolean isPreview = false;
    private Point displayPx;
    private ImageView tpImg, showImg;
    private Button saveBtn;
    // 取景框
    private OverlayerView mLayer;
    private boolean isTake = false;
    private String[] flashMedols={Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};
    private int[]    modelResId={org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_auto_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_on_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_off_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_torch_normal};
    /**
     * 切换摄像头
     */
    private ImageView swImg;
    private ImageView flashModelImg;
    /**
     * 当前是否是前置摄像头
     */
    private boolean isFrontCamera = false;
    int modelIndex=0;
    private final int CAMERA =10;
    private AutoFocusManager autoFocusManager;
    private boolean isSurfaceDestoryed=false;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zcamera_base_layout);
        displayPx = DisplayUtil.getScreenMetrics(this);
        mPreView = (SurfaceView) findViewById(org.zhx.common.camera.R.id.z_base_camera_preview);
        tpImg = (ImageView) findViewById(org.zhx.common.camera.R.id.z_take_pictrue_img);
        saveBtn = (Button) findViewById(org.zhx.common.camera.R.id.z_base_camera_save);
        showImg = (ImageView) findViewById(org.zhx.common.camera.R.id.z_base_camera_showImg);
        mLayer = (OverlayerView) findViewById(org.zhx.common.camera.R.id.z_base_camera_over_img);
        swImg = (ImageView) findViewById(org.zhx.common.camera.R.id.btn_switch_camera);
        swImg.setOnClickListener(this);
        flashModelImg= (ImageView) findViewById(org.zhx.common.camera.R.id.btn_flash_mode);
        flashModelImg.setOnClickListener(this);
        int rectwidth=DisplayUtil.dip2px(this, 200);
        mLayer.setCenterRect(rectwidth,rectwidth);
        mLayer.showScan(true);

        saveBtn.setOnClickListener(this);
        showImg.setOnClickListener(this);
        tpImg.setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionsUtil.checkPermission(this, Manifest.permission.CAMERA, CAMERA);
        }else {
            // 如果是 Android 6.0 以下 请手动打开 权限 或者 使用第三方库 统一申请权限并成功后调用
            showCameraDelay(500);
        }
        getLifecycle().addObserver(mLayer);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG,"onRestart");
        if(!isSurfaceDestoryed)
        restartCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG,"onStop");
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG,"onPause");
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.e(TAG,"surfaceCreated");
        openCamera();
        isSurfaceDestoryed=false;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        Log.e(TAG,"surfaceChanged");
        initCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // 当holder被回收时 释放硬件
        Log.e(TAG,"surfaceDestroyed");
        releaseCamera();
        isSurfaceDestoryed=true;
    }

    void releaseCamera() {
        if (mCamera != null) {
            if (isPreview) {
               stopPreview();
            }
            mCamera.release();
            mCamera = null;
        }
        isPreview = false;
    }

    void switchCamera() throws Exception {
        isFrontCamera = !isFrontCamera;
        releaseCamera();
       restartCamera();
    }

    private void restartCamera() {
        openCamera();
        initCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    showCameraDelay(500);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
  private void  showCameraDelay(long time){
      Log.e(TAG,"showCameraDelay");
      new Handler().postDelayed(new Runnable() {
          @Override
          public void run() {
              mHolder = mPreView.getHolder();
              mHolder.addCallback(CameraActivity.this);
              mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
              restartCamera();
          }
      },time);
  }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    void openCamera() {
        if (!isFrontCamera) {
            mCamera = Camera.open();
        } else {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                {
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        isFrontCamera = true;
                    }
                }
            }
        }
    }

    /**
     *
     * @param
     * @return
     * @throws Exception
     * @author zhx
     */
    public void initCamera() {

        if (mCamera != null && !isPreview) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                // 设置闪光灯为自动 前置摄像头时 不能设置
                if (!isFrontCamera) {
                    parameters.setFlashMode(flashMedols[modelIndex]);
                }

                resetCameraSize(parameters);
                // 设置图片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                // 设置JPG照片的质量
                parameters.set("jpeg-quality", 100);
                // 通过SurfaceView显示取景画面
                mCamera.setPreviewDisplay(mHolder);
                // 开始预览
               restartPreview();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                isPreview = false;
            }

        }

    }


    /**
     * 旋转相机和设置预览大小
     *
     * @param parameters
     */
    public void resetCameraSize(Camera.Parameters parameters) {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
        List<Camera.Size>  sizeList = parameters.getSupportedPictureSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            for (Camera.Size size : sizeList) {
                if (size.width * size.height == displayPx.x * displayPx.y) {
                    cameraSize = size;
                    break;
                }
            }
            // 设置图片大小 为设备长宽
            parameters.setPictureSize(cameraSize.width, cameraSize.height);
        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.z_take_pictrue_img:
                // 拍照前 线对焦 对焦后 拍摄（适用于自动对焦）
                isTake = true;
                // 手动对焦
                mCamera.takePicture(null, null, CameraActivity.this);
                break;
            case R.id.btn_switch_camera:
                try {
                    switchCamera();
                } catch (Exception e) {
                    mCamera = null;
                    e.printStackTrace();
                }
                break;
            case R.id.btn_flash_mode:
                modelIndex++;
                if(modelIndex>=flashMedols.length){
                    modelIndex=0;
                }
                Camera.Parameters parameters=mCamera.getParameters();
                List<String> flashmodels=parameters.getSupportedFlashModes();
                if(flashmodels.contains(flashMedols[modelIndex])){
                    parameters.setFlashMode(flashMedols[modelIndex]);
                    flashModelImg.setImageResource(modelResId[modelIndex]);
                }
                mCamera.setParameters(parameters);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        // TODO Auto-generated method stub
        isTake = false;
        // 拍照回掉回来的 图片数据。
        Bitmap bitmap =getBitmap(data);
        Bitmap bm=bitmap;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.setRotate(90, 0.1f, 0.1f);
            bm = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            if (isFrontCamera) {
                //前置摄像头旋转图片270度。
                matrix.setRotate(-90);
                bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                Log.e(TAG,bm.getWidth()+"!!"+bm.getHeight());
            }
        }

        if (mLayer.getCenterRect() != null&&bm!=null) {
            bitmap = ImageUtil.cropBitmap(this,bm, mLayer.getCenterRect().width(),mLayer.getCenterRect().height());
        }
        ImageUtil.recycleBitmap(bm);
        showImg.setImageBitmap(bitmap);
        if (mCamera != null) {
            stopPreview();
            restartPreview();
        }
    }

    private Bitmap getBitmap(byte[] data) {
        //只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        opt.inJustDecodeBounds = true;
        //String path = Environment.getExternalStorageDirectory() + "/dog.jpg";
        BitmapFactory.decodeByteArray(data,0,data.length, opt);
        int imageWidth = opt.outWidth;
        int imageHeight = opt.outHeight;
        Log.e(TAG,imageWidth+"!!@"+imageHeight);
        int scale = 1;
        int scaleX = imageWidth / displayPx.x;
        int scaleY = imageHeight / displayPx.y;
        if(scaleX >= scaleY && scaleX > 1){
            scale = scaleX;
        }else if(scaleX < scaleY && scaleY > 1){
            scale = scaleY;
        }

        System.out.println(scale);

        //按照缩放比例加载图片
        //设置缩放比例
        opt.inSampleSize = scale;
        opt.inJustDecodeBounds=false;
      return BitmapFactory.decodeByteArray(data, 0, data.length, opt);
    }

    private void stopPreview() {
        if(mCamera!=null) {
            mCamera.stopPreview();
            if (autoFocusManager != null) {
                autoFocusManager.stop();
                autoFocusManager = null;
            }
        }
    }

    private void restartPreview() {
        if(mCamera!=null){
            mCamera.startPreview();
            autoFocusManager =new AutoFocusManager(this,mCamera);
            isPreview = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }
}

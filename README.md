# ZCamera
## 1、如何利用 Camera 开发自定义相机？
   a、Camera.getNumberOfCameras()  获取 相机数目返回一个 可用相机集合（前/后置）
   ```
      int num=Camera.getNumberOfCameras();
   ```
   b、遍历 相机 Camera.CameraInfo.CAMERA_FACING_BACK 是后置|| Camera.CameraInfo.CAMERA_FACING_FRONT 是前置相机
   
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                 if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                      //前置相机
                 }else{
                  //后置相机
                 }
                 }
          }
          
   c、打开相机 获得Camera 对象
   
   ```
     Camera camera = Camera.open();
     //Camera camera = Camera.open(i); 打开前置 用position 去打开
   ```
   
   2、设置相机
   
   a、获取相机属性
   
     ```
    Camera.Parameters parameters =  camera.getParameters()
     // 设置图片格式
            parameters.setPictureFormat(ImageFormat.JPEG);
            // 设置JPG照片的质量
            parameters.set("jpeg-quality", 100);
      ```
   b、设置相机属性
   
        // 设置图片格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        // 设置JPG照片的质量
        parameters.set("jpeg-quality", 100);
        //预览的宽高 （很重要）
        parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // 设置预览图像大小
        //设置 闪光灯模式Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH
        parameters.setFlashMode(String valus);
        camera.setParameters(parameters);
        
        
   c、设置 设备方
   
          if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
          //横屏 旋转90
             camera.setDisplayOrientation(90);
            } else {
            //竖屏 不旋转
             camera.setDisplayOrientation(0);
            }
            // 设置预览设备 （SurfaceView 的holder）
           camera.setPreviewDisplay(mHolder)
   
   3、开始预览
        ```
        camera.startPreview();
        ```
        
   4、拍照
        ```
         camera.takePicture(null, null, new Camera.PictureCallback())
       ```

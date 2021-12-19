package org.zhx.common.camera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CameraGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    Context mContext;
    //以OpenGL ES纹理的形式从图像流中捕获帧,我把叫做纹理层
    SurfaceTexture mSurface;
    DirectDrawer mDirectDrawer;
    private GLSurfaceView.Renderer mRender;

    public CameraGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mContext = context;
        setEGLContextClientVersion(2);
        mDirectDrawer = new DirectDrawer(this);
        setRenderer(mDirectDrawer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setCameraId(int cameraId) {
        if (mDirectDrawer != null) {
            mDirectDrawer.setCameraId(cameraId);
        }
    }

    public void setViewRender(Renderer mRender) {
        this.mRender = mRender;
    }

    public SurfaceTexture getSurface() {
        return mSurface;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurface = new SurfaceTexture(createOESTextureObject());
        //监听纹理层
        mSurface.setOnFrameAvailableListener(this);
        mRender.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mRender.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurface.updateTexImage();
    }

    private int createOESTextureObject() {
        int[] tex = new int[1];
        //生成一个纹理
        GLES20.glGenTextures(1, tex, 0);
        //将此纹理绑定到外部纹理上
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        //设置纹理过滤参数
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //纹理层有新数据，就通知view绘制
        this.requestRender();
    }

}

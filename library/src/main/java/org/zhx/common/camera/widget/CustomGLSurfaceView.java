package org.zhx.common.camera.widget;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CustomGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
    Context mContext;
    //以OpenGL ES纹理的形式从图像流中捕获帧,我把叫做纹理层
    SurfaceTexture mSurface;
    CustomRender mPreviewRender;
    private Renderer mViewCallback;

    public CustomGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {
        mContext = context;
        setEGLContextClientVersion(2);
        mPreviewRender = new CustomRender(this);
        setRenderer(mPreviewRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurface = new SurfaceTexture(textures[0]);
    }

    public void setRotation(Rotation rotation,boolean isFrontCamera) {
        mPreviewRender.setRotation(rotation,isFrontCamera);
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //监听纹理层
        mSurface.setOnFrameAvailableListener(this);
        if (mViewCallback != null) {
            mViewCallback.onSurfaceCreated(gl, config);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mViewCallback != null) {
            mViewCallback.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mSurface.updateTexImage();
        if (mViewCallback != null) {
            mViewCallback.onDrawFrame(gl);
        }
    }


    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //纹理层有新数据，就通知view绘制
        this.requestRender();
    }

    public SurfaceTexture getSurface() {
        return mSurface;
    }

    public void setViewRender(GLSurfaceView.Renderer callback) {
        this.mViewCallback = callback;
    }

    public void onPreviewFrame(byte[] data,int width, int height,boolean isFirstFrame) {
        if(mPreviewRender!=null) {
            mPreviewRender.onPreviewFram(data,width,height,isFirstFrame);
        }
    }

}

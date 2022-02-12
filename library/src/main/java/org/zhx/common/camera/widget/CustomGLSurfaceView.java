package org.zhx.common.camera.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.zhx.common.camera.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CustomGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    Context mContext;
    //以OpenGL ES纹理的形式从图像流中捕获帧,我把叫做纹理层
    SurfaceTexture mSurface;
    CustomRender mPreviewRender;
    private Renderer mViewCallback;
    private float mTopmargin = 0;

    public CustomGLSurfaceView(Context context) {
        super(context);
        init(context, null);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setEGLContextClientVersion(2);
        mPreviewRender = new CustomRender(this);

        if (null != attrs) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomGLSurfaceView);
            mTopmargin = ta.getDimension(R.styleable.CustomGLSurfaceView_gl_top_margin, 0);
            ta.recycle();
        }

        mPreviewRender.setTopMargin(mTopmargin);
        setRenderer(mPreviewRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        mSurface = new SurfaceTexture(textures[0]);
    }

    public void setRotation(Rotation rotation, boolean isFrontCamera) {
        mPreviewRender.setRotation(rotation, isFrontCamera);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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

    public SurfaceTexture getSurface() {
        return mSurface;
    }

    public void setViewRender(GLSurfaceView.Renderer callback) {
        this.mViewCallback = callback;
    }

    public void onPreviewFrame(byte[] data, int width, int height, boolean isFirstFrame) {
        if (mPreviewRender != null) {
            mPreviewRender.onPreviewFram(data, width, height, isFirstFrame);
            this.requestRender();
        }
    }

    public void setCanvasTopmargin(float topmargin) {
        this.mTopmargin = topmargin;
        if (null != mPreviewRender) {
            mPreviewRender.setTopMargin(topmargin);
        }
    }
}

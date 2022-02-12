package org.zhx.common.camera.renders;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.zhx.common.camera.widget.TextureRotationUtil;
import org.zhx.common.util.ZCameraLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class BaseRender implements GLSurfaceView.Renderer {
    protected IntBuffer glRgbBuffer;

    protected int glProgId;

    protected FloatBuffer glCubeBuffer;
    protected float vertextBufferId;

    protected FloatBuffer glTextureBuffer;
    protected int glTextureId = -1;

    private boolean isInitialized;
    protected RenderCallback mCallback;

    protected String vertexShader;
    protected String fragmentShader;

    protected final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public BaseRender(RenderCallback mCallback) {
        this.vertexShader = getVerTexShader();
        this.fragmentShader = getFragmentShader();
        this.mCallback = mCallback;
    }


    protected abstract String getFragmentShader();

    protected abstract String getVerTexShader();

    protected void onInit() {
        glProgId = loadProgram(getVerTexShader(), getFragmentShader());
        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        isInitialized = true;
    }

    private int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            ZCameraLog.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            ZCameraLog.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            ZCameraLog.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        return iProgId;
    }

    private int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            ZCameraLog.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}

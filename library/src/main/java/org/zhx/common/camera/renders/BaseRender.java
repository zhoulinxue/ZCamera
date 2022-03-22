package org.zhx.common.camera.renders;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.zhx.common.camera.widget.GLHelper;
import org.zhx.common.camera.widget.TextureRotationUtil;
import org.zhx.common.util.ZCameraLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class BaseRender implements GLSurfaceView.Renderer {
    private final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    private final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

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

    protected void onInit() {
        glProgId = GLHelper.loadProgram(getVerTexShader(), getFragmentShader());
        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        isInitialized = true;
    }

    protected abstract void onDrawArraysPre();

    protected String getFragmentShader() {
        return NO_FILTER_FRAGMENT_SHADER;
    }

    protected String getVerTexShader() {
        return NO_FILTER_VERTEX_SHADER;
    }


    public boolean isInitialized() {
        return isInitialized;
    }
}

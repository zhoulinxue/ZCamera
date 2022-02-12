package org.zhx.common.camera.widget;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.zhx.common.camera.YUVTorgb;
import org.zhx.common.util.ZCameraLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomRender implements GLSurfaceView.Renderer {
    public static final String NO_FILTER_VERTEX_SHADER = "" +
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
    public static final String NO_FILTER_FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";
    private Queue<Runnable> runOnDraw;
    private Queue<Runnable> runOnDrawEnd;

    private IntBuffer glRgbBuffer;
    private int glTextureId = -1;
    private final String vertexShader;
    private final String fragmentShader;

    private final LinkedList<Runnable> runOnDrawList;

    private int glProgId;
    private int glAttribPosition;
    private int glUniformTexture;
    private int glAttribTextureCoordinate;
    private int outputWidth;
    private int outputHeight;
    private boolean isInitialized;
    private int imageWidth;
    private int imageHeight;
    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    private FloatBuffer glCubeBuffer;
    private FloatBuffer glTextureBuffer;
    private boolean flipHorizontal;
    private boolean flipVertical;
    private GLSurfaceView.Renderer mCallback;
    private float mTopMargin;
    private Rotation rotation = Rotation.NORMAL;


    public CustomRender(GLSurfaceView.Renderer callback) {
        this(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
        this.mCallback = callback;
    }

    public CustomRender(final String vertexShader, final String fragmentShader) {
        runOnDraw = new LinkedList<>();
        runOnDrawEnd = new LinkedList<>();
        runOnDrawList = new LinkedList<>();
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;

        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    public void onInit() {
        glProgId = loadProgram(vertexShader, fragmentShader);
        glAttribPosition = GLES20.glGetAttribLocation(glProgId, "position");
        glUniformTexture = GLES20.glGetUniformLocation(glProgId, "inputImageTexture");
        glAttribTextureCoordinate = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate");
        isInitialized = true;
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
        this.rotation = rotation;
    }

    public void setRotation(final Rotation rotation, boolean isFrontCamera) {
        setRotation(rotation, isFrontCamera, false);
    }

    public static int loadProgram(final String strVSource, final String strFSource) {
        int iVShader;
        int iFShader;
        int iProgId;
        int[] link = new int[1];
        iVShader = loadShader(strVSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d("Load Program", "Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(strFSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d("Load Program", "Fragment Shader Failed");
            return 0;
        }

        iProgId = GLES20.glCreateProgram();

        GLES20.glAttachShader(iProgId, iVShader);
        GLES20.glAttachShader(iProgId, iFShader);

        GLES20.glLinkProgram(iProgId);

        GLES20.glGetProgramiv(iProgId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d("Load Program", "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        GLES20.glDeleteShader(iFShader);
        return iProgId;
    }

    public static int loadShader(final String strSource, final int iType) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(iType);
        GLES20.glShaderSource(iShader, strSource);
        GLES20.glCompileShader(iShader);
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
            return 0;
        }
        return iShader;
    }


    public void ifNeedInit() {
        if (!isInitialized) {
            onInit();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        ZCameraLog.e("onSurfaceCreated");

        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        ifNeedInit();
        if (mCallback != null) {
            mCallback.onSurfaceCreated(gl, config);
        }
    }

    public final Object surfaceChangedWaiter = new Object();

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        ZCameraLog.e("onSurfaceChanged");
        outputWidth = width;
        outputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(glProgId);
        adjustImageScaling();
        synchronized (surfaceChangedWaiter) {
            surfaceChangedWaiter.notifyAll();
        }
        if (mCallback != null) {
            mCallback.onSurfaceChanged(gl, width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        runAll(runOnDraw);
        onDraw(glTextureId, glCubeBuffer, glTextureBuffer);
        runAll(runOnDrawEnd);

        if (mCallback != null) {
            mCallback.onDrawFrame(gl);
        }
    }

    protected void runPendingOnDrawTasks() {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDrawList.removeFirst().run();
            }
        }
    }

    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {
        GLES20.glUseProgram(glProgId);
        if (!isInitialized) {
            return;
        }

        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);
        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(glUniformTexture, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(glAttribPosition);
        GLES20.glDisableVertexAttribArray(glAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.poll().run();
            }
        }
    }

    public void onPreviewFram(byte[] yuvData, int width, int height, boolean isFirstFrame) {
        if (glRgbBuffer == null) {
            glRgbBuffer = IntBuffer.allocate(width * height);
        }

        if (isFirstFrame) {
            if (imageWidth != width) {
                imageWidth = width;
                imageHeight = height;
            }
            
            adjustImageScaling();
        }

        if (runOnDraw.isEmpty()) {
            runOnDraw(() -> {
                YUVTorgb.YUVtoRBGA(yuvData, width, height, glRgbBuffer.array());
                glTextureId = createTextureIfNeeded(glRgbBuffer, width, height, glTextureId);
            });
        }
    }

    public static int createTextureIfNeeded(final IntBuffer data, final int width, final int height, final int usedTexId) {
        int textures[] = new int[1];
        if (usedTexId == -1) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height,
                    0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTexId);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, width,
                    height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, data);
            textures[0] = usedTexId;
        }
        return textures[0];
    }


    protected void runOnDraw(final Runnable runnable) {
        synchronized (runOnDraw) {
            runOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(final Runnable runnable) {
        synchronized (runOnDrawEnd) {
            runOnDrawEnd.add(runnable);
        }
    }

    private void adjustImageScaling() {
        float outputWidth = this.outputWidth;
        float outputHeight = this.outputHeight;
        if (rotation == Rotation.ROTATION_270 || rotation == Rotation.ROTATION_90) {
            outputWidth = this.outputHeight;
            outputHeight = this.outputWidth;
        }

        float ratio1 = outputWidth / imageWidth;
        float ratio2 = outputHeight / imageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);

        float ratioWidth = imageWidthNew / outputWidth;
        float ratioHeight = imageHeightNew / outputHeight;


        ZCameraLog.e("ratioWidth:" + ratioWidth + ", ratioHeight:" + ratioHeight);
        float[] textureCords = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        float[] cube = new float[]{
                CUBE[0] / ratioWidth, CUBE[1] / ratioHeight + mTopMargin / outputHeight,
                CUBE[2] / ratioWidth, CUBE[3] / ratioHeight + mTopMargin / outputHeight,
                CUBE[4] / ratioWidth, CUBE[5] / ratioHeight + mTopMargin / outputHeight,
                CUBE[6] / ratioWidth, CUBE[7] / ratioHeight + mTopMargin / outputHeight,
        };

        glCubeBuffer.clear();
        glCubeBuffer.put(cube).position(0);
        glTextureBuffer.clear();
        glTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setTopMargin(float topMargin) {
        this.mTopMargin = topMargin;
    }

    public int getGlTextureId() {
        return glTextureId;
    }
}

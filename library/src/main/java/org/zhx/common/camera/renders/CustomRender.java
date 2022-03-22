package org.zhx.common.camera.renders;

import android.graphics.Bitmap;
import android.opengl.GLES20;

import org.zhx.common.camera.YUVTorgb;
import org.zhx.common.camera.widget.GLHelper;
import org.zhx.common.camera.widget.Rotation;
import org.zhx.common.camera.widget.TextureRotationUtil;
import org.zhx.common.util.ZCameraLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomRender extends BaseRender {

    private final String VERTEX_SHADER = "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            "attribute vec4 inputTextureCoordinate2;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 textureCoordinate2;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "    textureCoordinate2 = inputTextureCoordinate2.xy;\n" +
            "}";

    private Queue<Runnable> runOnDraw;
    private Queue<Runnable> runOnDrawEnd;

    private IntBuffer glRgbBuffer;
    private int glTextureId = -1;


    private LinkedList<Runnable> runOnDrawList;

    private int glProgId;
    private int glAttribPosition;
    private int glUniformTexture;
    private int glAttribTextureCoordinate;
    private int outputWidth;
    private int outputHeight;
    private boolean isInitialized;
    private int imageWidth;
    private int imageHeight;
    private FloatBuffer glCubeBuffer;
    private FloatBuffer glTextureBuffer;
    private boolean flipHorizontal;
    private boolean flipVertical;

    private float mTopMargin;
    private Rotation rotation = Rotation.NORMAL;


    public CustomRender(RenderCallback callback) {
        super(callback);
        runOnDraw = new LinkedList<>();
        runOnDrawEnd = new LinkedList<>();
        runOnDrawList = new LinkedList<>();
        glCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        glCubeBuffer.put(CUBE).position(0);

        glTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
    }

    private int filterSecondTextureCoordinateAttribute;
    private int filterInputTextureUniform2;
    private int filterSourceTexture2 = -1;
    private ByteBuffer texture2CoordinatesBuffer;
    private Bitmap bitmap;

    public void onInit() {
        glProgId = GLHelper.loadProgram(vertexShader, fragmentShader);
        glAttribPosition = GLES20.glGetAttribLocation(glProgId, "position");
        glUniformTexture = GLES20.glGetUniformLocation(glProgId, "inputImageTexture");
        glAttribTextureCoordinate = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate");
        isInitialized = true;

        filterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(glProgId, "inputTextureCoordinate2");
        filterInputTextureUniform2 = GLES20.glGetUniformLocation(glProgId, "inputImageTexture2"); // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader
        GLES20.glEnableVertexAttribArray(filterSecondTextureCoordinateAttribute);

    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glEnableVertexAttribArray(filterSecondTextureCoordinateAttribute);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, filterSourceTexture2);
        GLES20.glUniform1i(filterInputTextureUniform2, 3);

        texture2CoordinatesBuffer.position(0);
        GLES20.glVertexAttribPointer(filterSecondTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, texture2CoordinatesBuffer);
    }

    public void setRotation(final Rotation rotation,
                            final boolean flipHorizontal, final boolean flipVertical) {
        this.flipHorizontal = flipHorizontal;
        this.flipVertical = flipVertical;
        this.rotation = rotation;

        float[] buffer = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        ByteBuffer bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder());
        FloatBuffer fBuffer = bBuffer.asFloatBuffer();
        fBuffer.put(buffer);
        fBuffer.flip();

        texture2CoordinatesBuffer = bBuffer;
    }

    public void setRotation(final Rotation rotation, boolean isFrontCamera) {
        setRotation(rotation, isFrontCamera, false);
    }

    public void ifNeedInit() {
        if (!isInitialized) {
            onInit();
        }
    }

    public void setBitmap(final Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            return;
        }

        if (this.bitmap == null) {
            return;
        }
        runOnDraw(new Runnable() {
            public void run() {
                if (filterSourceTexture2 == -1) {
                    if (bitmap == null || bitmap.isRecycled()) {
                        return;
                    }
                    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                    filterSourceTexture2 = GLHelper.loadTexture(bitmap, -1, false);
                }
            }
        });
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void recycleBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        ZCameraLog.e("onSurfaceCreated");

        GLES20.glClearColor(1.0f, 0, 0, 1);
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

        GLES20.glVertexAttribPointer(glAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(glAttribPosition);
        GLES20.glVertexAttribPointer(glAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        GLES20.glEnableVertexAttribArray(glAttribTextureCoordinate);
        if (textureId != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(glUniformTexture, 0);
        }
        onDrawArraysPre();

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
                glTextureId = GLHelper.createTextureIfNeeded(glRgbBuffer, width, height, glTextureId);
            });
        }
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


        float[] textureCords = TextureRotationUtil.getRotation(rotation, flipHorizontal, flipVertical);

        ZCameraLog.e("startleftY:" + (((1 - CUBE[5] / ratioHeight) - mTopMargin / outputHeight) * outputHeight) +
                "ratioWidth:" + ratioWidth +
                ", ratioHeight:" + ratioHeight +
                ", topMargin:" + mTopMargin / outputWidth);

        float[] cube = new float[]{
                CUBE[0] / ratioWidth, CUBE[1] / ratioHeight + (1 - CUBE[5] / ratioHeight) - mTopMargin / outputHeight,
                CUBE[2] / ratioWidth, CUBE[3] / ratioHeight + (1 - CUBE[7] / ratioHeight) - mTopMargin / outputHeight,
                CUBE[4] / ratioWidth, CUBE[5] - mTopMargin / outputHeight,
                CUBE[6] / ratioWidth, CUBE[7] - mTopMargin / outputHeight,
        };

        mCallback.onCanvasReuslt(mTopMargin, (CUBE[5] - mTopMargin / outputHeight - (CUBE[1] / ratioHeight + (1 - CUBE[5] / ratioHeight) - mTopMargin / outputHeight)) * outputHeight, outputHeight);

        glCubeBuffer.clear();
        glCubeBuffer.put(cube).position(0);
        glTextureBuffer.clear();
        glTextureBuffer.put(textureCords).position(0);
    }

    @Override
    protected String getVerTexShader() {
        return VERTEX_SHADER;
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

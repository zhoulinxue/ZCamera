package org.zhx.common.camera.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DirectDrawer implements GLSurfaceView.Renderer {
    private final String vertexShaderCode = "uniform mat4 textureTransform;\n" +
            "attribute vec2 inputTextureCoordinate;\n" +
            "attribute vec4 position;            \n" +//NDK坐标点
            "varying   vec2 textureCoordinate; \n" +//纹理坐标点变换后输出
            "\n" +
            " void main() {\n" +
            "     gl_Position = position;\n" +
            "     textureCoordinate = inputTextureCoordinate;\n" +
            " }";

    private final String fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES videoTex;\n" +
            "varying vec2 textureCoordinate;\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 tc = texture2D(videoTex, textureCoordinate);\n" +
            "    float color = tc.r * 0.3 + tc.g * 0.59 + tc.b * 0.11;\n" +  //所有视图修改成黑白
            "    gl_FragColor = vec4(color,color,color,1.0);\n" +
//                "    gl_FragColor = vec4(tc.r,tc.g,tc.b,1.0);\n" +
            "}\n";



    private FloatBuffer mPosBuffer;
    private FloatBuffer mTexBuffer;
    private float[] mPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
    private float[] mTexCoordinateBackRight = {1, 1, 0, 1, 1, 0, 0, 0};//顺时针转90并沿Y轴翻转  后摄像头正确，前摄像头上下颠倒
    private float[] mTexCoordinateForntRight = {0, 1, 1, 1, 0, 0, 1, 0};//顺时针旋转90  后摄像头上下颠倒了，前摄像头正确

    public int mProgram;
    public boolean mBoolean = false;
    private GLSurfaceView.Renderer mCallback;
    private int mCameraId;

    public DirectDrawer(GLSurfaceView.Renderer callback) {
        this.mCallback = callback;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mCameraMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        // 添加上面编写的着色器代码并编译它
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void setCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
        if (!mBoolean) {
            mBoolean = true;
        }
    }

    private FloatBuffer convertToFloatBuffer(float[] buffer) {
        FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(buffer);
        fb.position(0);
        return fb;
    }

    private int uPosHandle;
    private int aTexHandle;
    private int mMVPMatrixHandle;
    private float[] mProjectMatrix = new float[16];
    private float[] mCameraMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mTempMatrix = new float[16];

    //添加程序到ES环境中
    private void activeProgram() {
        // 将程序添加到OpenGL ES环境
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的位置的句柄
        uPosHandle = GLES20.glGetAttribLocation(mProgram, "position");
        aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform");

        mPosBuffer = convertToFloatBuffer(mPosCoordinate);
        if (mCameraId == 0) {
            mTexBuffer = convertToFloatBuffer(mTexCoordinateBackRight);
        } else {
            mTexBuffer = convertToFloatBuffer(mTexCoordinateForntRight);
        }

        GLES20.glVertexAttribPointer(uPosHandle, 2, GLES20.GL_FLOAT, false, 0, mPosBuffer);
        GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);

        // 启用顶点位置的句柄
        GLES20.glEnableVertexAttribArray(uPosHandle);
        GLES20.glEnableVertexAttribArray(aTexHandle);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mCallback.onSurfaceCreated(gl, config);
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        // 创建空的OpenGL ES程序
        mProgram = GLES20.glCreateProgram();
        // 添加顶点着色器到程序中
        GLES20.glAttachShader(mProgram, vertexShader);
        // 添加片段着色器到程序中
        GLES20.glAttachShader(mProgram, fragmentShader);
        // 创建OpenGL ES程序可执行文件
        GLES20.glLinkProgram(mProgram);
        // 释放shader资源
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        activeProgram();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.scaleM(mMVPMatrix, 0, 1, -1, 1);
        float ratio = (float) width / height;
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -ratio, ratio, 1, 7);// 3和7代表远近视点与眼睛的距离，非坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);// 3代表眼睛的坐标点
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBoolean) {
            activeProgram();
            mBoolean = false;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mCallback.onDrawFrame(gl);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPosCoordinate.length / 2);
    }
}

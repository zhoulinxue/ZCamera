package org.zhx.common.camera;

public class CameraProxy<T> {
    private T camera;
    private int width;
    private int height;

    public CameraProxy(T camera, int width, int height) {
        this.camera = camera;
        this.width = width;
        this.height = height;
    }

    public CameraProxy(T proxy) {
        this.camera = proxy;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public T getCamera() {
        return camera;
    }

    public void setCamera(T camera) {
        this.camera = camera;
    }
}

package org.zhx.common.camera;

public class CameraProxy<T> {
    private T camera;

    public CameraProxy(T proxy) {
        this.camera = proxy;
    }

    public T getCamera() {
        return camera;
    }

    public void setCamera(T camera) {
        this.camera = camera;
    }
}

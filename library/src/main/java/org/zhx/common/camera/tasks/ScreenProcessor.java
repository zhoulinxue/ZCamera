package org.zhx.common.camera.tasks;

import android.graphics.Point;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.util.CameraUtil;

public class ScreenProcessor {
    private CameraModel.view mView;
    private int width;
    private int height;

    public ScreenProcessor(CameraModel.view view) {
        this.mView = mView;
        Point point = CameraUtil.getScreenMetrics(view.getContext());
        width = Math.min(point.x, point.y);
        height = Math.max(point.x, point.y);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}

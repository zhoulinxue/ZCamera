package org.zhx.common.camera;

public enum CameraRatio {
    SCANLE_4_3(4, 3), SCANLE_16_9(16, 9),SCANLE_1_1(1, 1);

    CameraRatio(int heightRatio, int widthRatio) {
        this.heightRatio = heightRatio;
        this.widthRatio = widthRatio;
    }

    public int getWidthRatio() {
        return widthRatio;
    }

    public void setWidthRatio(int widthRatio) {
        this.widthRatio = widthRatio;
    }

    public int getHeightRatio() {
        return heightRatio;
    }

    public void setHeightRatio(int heightRatio) {
        this.heightRatio = heightRatio;
    }

    private int heightRatio;
    private int widthRatio;

}

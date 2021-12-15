package org.zhx.common.mvp;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

public interface BaseView {
    public void onError(@StringRes int msg);

    boolean hasPermission(String permission);

    void requestPermission(String permission, int requestCode);

    int getOrientation();

    int getRotation();
}

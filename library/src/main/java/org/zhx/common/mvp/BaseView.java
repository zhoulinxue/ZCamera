package org.zhx.common.mvp;

import androidx.annotation.StringRes;

public interface BaseView {
    void onError(@StringRes int msg);

    boolean hasPermission(String permission);

    void requestPermission(String permission, int requestCode);
}

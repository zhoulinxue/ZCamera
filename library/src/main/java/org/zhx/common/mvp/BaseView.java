package org.zhx.common.mvp;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

public interface BaseView {
    public AppCompatActivity getContext();

    public void onError(@StringRes int msg);

}

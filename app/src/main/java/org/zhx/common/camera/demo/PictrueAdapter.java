package org.zhx.common.camera.demo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.zhx.common.camera.ImageData;
import org.zhx.common.util.ImageUtil;

import java.io.IOException;
import java.util.List;

public class PictrueAdapter extends BaseQuickAdapter<ImageData, BaseViewHolder> {

    public PictrueAdapter(@Nullable List<ImageData> data) {
        super(R.layout.pictrue_item_layout, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ImageData item) {
        final SubsamplingScaleImageView showImage = helper.getView(R.id.z_base_camera_showImg_big);
        final Uri uri = item.getContentUri();
        final ProgressBar mBar = helper.getView(R.id.load_process);
        try {
            showImage.setImage(ImageSource.bitmap(ImageUtil.getBitmapFormUri(mContext, uri)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ImageUtil.getBitmapFromUri(mContext, uri);
                showImage.post(new Runnable() {
                    @Override
                    public void run() {
                        mBar.setVisibility(View.GONE);
                        showImage.setImage(ImageSource.bitmap(bitmap));
                    }
                });
            }
        }).start();
    }
}

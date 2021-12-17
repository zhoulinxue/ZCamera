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
import org.zhx.common.camera.ImageLoaderModel;
import org.zhx.common.camera.tasks.ImageloaderProcessor;

import java.util.List;

public class PictrueAdapter extends BaseQuickAdapter<ImageData, BaseViewHolder> {
    private ImageloaderProcessor mImageSearchProcessor;

    public PictrueAdapter(@Nullable List<ImageData> data) {
        super(R.layout.pictrue_item_layout, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, ImageData item) {
        if (mImageSearchProcessor == null) {
            mImageSearchProcessor = new ImageloaderProcessor(mContext);
        }
        final SubsamplingScaleImageView showImage = helper.getView(R.id.z_base_camera_showImg_big);
        final Uri uri = item.getContentUri();
        final ProgressBar mBar = helper.getView(R.id.load_process);
        mImageSearchProcessor.loadImags(uri, helper.getLayoutPosition(), new ImageLoaderModel.view() {
            @Override
            public void onBitmapLoadSuc(Bitmap bitmap, int position) {
                if (position == helper.getLayoutPosition()) {
                    mBar.setVisibility(View.GONE);
                    showImage.setImage(ImageSource.bitmap(bitmap));
                }
            }
        });
    }
}

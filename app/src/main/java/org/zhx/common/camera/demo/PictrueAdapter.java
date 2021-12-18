package org.zhx.common.camera.demo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.zhx.common.camera.ImageData;
import org.zhx.common.camera.ImageLoaderModel;
import org.zhx.common.camera.tasks.ImageloaderProcessor;
import org.zhx.common.util.ZCameraLog;

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
        final ProgressBar mBar = helper.getView(R.id.load_process);

        final Uri uri = item.getContentUri();
        Glide.with(mContext)
                .asBitmap() //指定格式为Bitmap
                .load(uri)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        mBar.setVisibility(View.GONE);
                        //加载失败
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        ZCameraLog.e("onResourceReady, width:" + resource.getWidth() + ", height:" + resource.getHeight());
                        //加载成功，resource为加载到的bitmap
                        showImage.post(() -> {
                            mBar.setVisibility(View.GONE);
                            showImage.setImage(ImageSource.bitmap(resource));
                        });
                        return true;
                    }
                }).submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);//加载原图大小

    }
}

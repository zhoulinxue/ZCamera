package org.zhx.common.camera.demo;

import android.os.Bundle;

import android.view.WindowManager;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.zhx.common.camera.Constants;
import org.zhx.common.camera.ImageData;

import java.util.List;

public class ShowImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));

        RecyclerView recyclerView = findViewById(R.id.pictrue_group);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(manager);
        List<ImageData> datas = getIntent().getParcelableArrayListExtra(Constants.HISTORE_PICTRUE);
        recyclerView.setAdapter(new PictrueAdapter(datas));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

}

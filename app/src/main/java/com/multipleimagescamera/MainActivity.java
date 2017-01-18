package com.multipleimagescamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.cameralib.CameraActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.photosGridView)
    RecyclerView photosGridView;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        photosGridView.setLayoutManager(gridLayoutManager);
        galleryAdapter = new GalleryAdapter(this);
        photosGridView.setAdapter(galleryAdapter);
    }

    @OnClick(R.id.cameraBtn)
    void cameraBtnClicked() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("directoryPath", getDirName());
        startActivityForResult(intent, 0);
    }

    private String getDirName() {
        return getExternalFilesDir(null).getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            List<String> resultImages = data.getStringArrayListExtra("resultImages");
            galleryAdapter.addPhotos(resultImages);
        }
    }
}

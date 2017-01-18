/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cameralib;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;

import java.util.ArrayList;

public class CameraActivity extends Activity implements CameraResultListener {
    private ArrayList<String> resultImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        String directoryPath = getIntent().getStringExtra("directoryPath");
        if (savedInstanceState == null) {
            Fragment fragment = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? Camera2Fragment.newInstance(directoryPath) :
                    Camera1Fragment.newInstance(directoryPath);
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }

    @Override
    public void onImageTaken(String path) {
        resultImages.add(path);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("resultImages", resultImages);
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    //    @SuppressLint("NewApi")
//    private boolean isCamera2Supported() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            for (String cameraId : manager.getCameraIdList()) {
//                CameraCharacteristics characteristics
//                        = manager.getCameraCharacteristics(cameraId);
//                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
//                if (cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
//                    return characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

}

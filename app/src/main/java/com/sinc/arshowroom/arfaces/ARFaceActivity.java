/*
 * Copyright 2019 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sinc.arshowroom.arfaces;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.AugmentedFaceNode;
import com.sinc.arshowroom.R;
import com.sinc.arshowroom.arimage.ARImageActivity;
import com.sinc.arshowroom.arimage.ARImagePreview;
import com.sinc.arshowroom.arvideorec.VideoRecorder;
import com.sinc.arshowroom.main.ListActivity;
import com.sinc.arshowroom.offline.StoreDBActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ARFaceActivity extends AppCompatActivity {

  private ARFaceFragment arFragment;
  private ModelRenderable faceRegionsRenderable;
  private Texture faceMeshTexture;
  private Button btn ;
  private VideoRecorder videoRecorder;
  private String latest;


  private final HashMap<AugmentedFace, AugmentedFaceNode> faceNodeMap = new HashMap<>();

  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_face_mesh);

      CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
      View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
      BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
      behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> if " +behavior.getState());
//      if( BottomSheetBehavior.STATE_DRAGGING == behavior.getState() ) {
//          System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> if ");
//          behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
//      }
//
//      //behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//      //behavior.setState(BottomSheetBehavior.STATE_SETTLING);

      bottomSheet.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                  behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
              } else {
                  behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
              }
          }
      });

      behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
          @Override
          public void onStateChanged(@NonNull View view, int newState) {
              //behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
          }

          @Override
          public void onSlide(@NonNull View view, float slideOffset) {

          }

      });
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 1");
    arFragment = (ARFaceFragment) getSupportFragmentManager().findFragmentById(R.id.face_fragment);



    ModelRenderable.builder()
        .setSource(this, R.raw.sun)
        .build()
        .thenAccept(
            modelRenderable -> {
              faceRegionsRenderable = modelRenderable;
              modelRenderable.setShadowCaster(false);
              modelRenderable.setShadowReceiver(false);
            });

    ArSceneView sceneView = arFragment.getArSceneView();
    sceneView.setCameraStreamRenderPriority(Renderable.RENDER_PRIORITY_FIRST);
    Scene scene = sceneView.getScene();
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 2");



//      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+bottomSheet);
//      Message msg = new Message();
//      msg.obj = bottomSheet;
//      handler.sendMessage(msg);

    scene.addOnUpdateListener(
        (FrameTime frameTime) -> {

          Collection<AugmentedFace> faceList =
              sceneView.getSession().getAllTrackables(AugmentedFace.class);
           // System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 3");
          for (AugmentedFace face : faceList) {
            if (!faceNodeMap.containsKey(face)) {
              AugmentedFaceNode faceNode = new AugmentedFaceNode(face);
              faceNode.setParent(scene);
              faceNode.setLocalScale(new Vector3(-0.1f, 0.1f, -0.1f));
              faceNode.setFaceRegionsRenderable(faceRegionsRenderable);
              faceNode.setFaceMeshTexture(faceMeshTexture);
              faceNodeMap.put(face, faceNode);
            }
          }
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> 4");
          Iterator<Map.Entry<AugmentedFace, AugmentedFaceNode>> iter = faceNodeMap.entrySet().iterator();
          while (iter.hasNext()) {
            Map.Entry<AugmentedFace, AugmentedFaceNode> entry = iter.next();
            AugmentedFace face = entry.getKey();
            if (face.getTrackingState() == TrackingState.STOPPED) {
              AugmentedFaceNode faceNode = entry.getValue();
              faceNode.setParent(null);
              iter.remove();
            }
          }

        });

        // BottomSheet
      setBtnListener();
      Button photo = findViewById(R.id.btn_photo);
      photo.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
              if (ActivityCompat.checkSelfPermission(ARFaceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                  ActivityCompat.requestPermissions(ARFaceActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                  Toast.makeText(ARFaceActivity.this, "다시 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
              } else if (ActivityCompat.checkSelfPermission(ARFaceActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                  takePhoto();
              }
          }
      });
      Button recBtn = findViewById(R.id.btn_video);
      recBtn.setOnClickListener(view -> {
          if (videoRecorder == null) {
              videoRecorder = new VideoRecorder();
              videoRecorder.setSceneView(arFragment.getArSceneView());
              int orientation = getResources().getConfiguration().orientation;
              videoRecorder.setVideoQuality(CamcorderProfile.QUALITY_HIGH, orientation);
          }
        boolean isRecording = videoRecorder.onToggleRecord();

          if (isRecording) {
              Toast.makeText(this, "Started Recording", Toast.LENGTH_SHORT).show();
          } else {
              Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
          }
      });


    }
    public void setBtnListener() {

        Button offlineBtn = (Button) findViewById(R.id.offline);
        Button onlineBtn = (Button) findViewById(R.id.online);


        offlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ARFaceActivity.this, StoreDBActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "offlineBtn", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        PixelCopy.request(view, bitmap, (copyResult) -> {
            if (copyResult == PixelCopy.SUCCESS) {
                try {
                    saveBitmapToDisk(bitmap, filename);
                } catch (IOException e) {
                    Toast toast = Toast.makeText(ARFaceActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            } else {
                Toast toast = Toast.makeText(ARFaceActivity.this,
                        "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                toast.show();
            }



            handlerThread.quitSafely();
        }, new Handler(handlerThread.getLooper()));
    }

    private String generateFilename() {
        String date =
                new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        latest = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + "AR갤러리/" + date + "_screenshot.jpg";
        return latest;
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {


            Context context = getApplicationContext();
            Drawable drawable = getResources().getDrawable(R.drawable.shin, null);
            Bitmap ss = ((BitmapDrawable)drawable).getBitmap();

            bitmap = watermark(bitmap, ss, 0, 0);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();



            //팝업
            File photoFile = new File(filename);
            Uri photoURI = FileProvider.getUriForFile(ARFaceActivity.this,
                    ARFaceActivity.this.getPackageName() + ".name.provider",
                    photoFile);
            Intent intent = new Intent(this, ARImagePreview.class);
            intent.putExtra("uri", photoURI);
            startActivity(intent);


        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private Bitmap watermark(Bitmap baseBmp, Bitmap overlayBmp, int distanceLeft, int distanceTop) {
        Bitmap resultBmp = Bitmap.createBitmap(baseBmp.getWidth() +
                        distanceLeft, baseBmp.getHeight() + distanceTop,
                baseBmp.getConfig());
        Canvas canvas = new Canvas(resultBmp);
        canvas.drawBitmap(baseBmp, distanceLeft, distanceTop, null);
        canvas.drawBitmap(overlayBmp, baseBmp.getWidth() - overlayBmp.getWidth() - 70, baseBmp.getHeight() - overlayBmp.getHeight() - 75, null);
        return resultBmp;
    }


}

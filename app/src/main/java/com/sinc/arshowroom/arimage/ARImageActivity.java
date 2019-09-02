package com.sinc.arshowroom.arimage;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.sinc.arshowroom.R;
import com.sinc.arshowroom.arvideorec.VideoRecorder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ARImageActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private Frame frame;
    private ModelRenderable pikachu, eevee, pokeball, test1 ,test2;
    private Map<AugmentedImage, ARImageNode> augmentedImageMap = new HashMap<>();
    private Collection<AugmentedImage> updatedAugmentedImages;
    private AnchorNode aNode;
    private Anchor anchor;
    private String latest;
    private ExternalTexture texture;
    private ModelRenderable renderable;
    private boolean isImageDetected = false;
    private boolean isImage2Detected = false;
    private ModelAnimator modelAnimator;
    private int i = 0;
    private MediaPlayer mediaPlayer;
    private VideoRecorder videoRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ari_activity_main);

        //model 4 : Image to Video
        texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this, R.raw.acnestudio2019);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);


        Button cameraBtn = findViewById(R.id.cameraButton);
        Button recBtn = findViewById(R.id.btn_record);
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

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(ARImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ARImageActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    Toast.makeText(ARImageActivity.this, "다시 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
                } else if (ActivityCompat.checkSelfPermission(ARImageActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
               }
            }
        });

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        mInit();

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (pikachu == null || eevee == null || pokeball == null) {
                        return;
                    }
                });
    }

    private void playVideo(Anchor anchor, float extentX, float extentZ) {
        mediaPlayer.start();
        AnchorNode anchorNode = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });
        anchorNode.setWorldScale(new Vector3(extentX * 2, 1f, extentZ * 2));
        arFragment.getArSceneView().getScene().addChild(anchorNode);

    }
    private void createModel(Anchor anchor, ArFragment arFragment) {
        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("acnecuberotate2.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    AnchorNode anchorNode = new AnchorNode(anchor);

                    SkeletonNode skeletonNode = new SkeletonNode();
                    skeletonNode.setParent(anchorNode);
                    skeletonNode.setRenderable(modelRenderable);

                    arFragment.getArSceneView().getScene().addChild(anchorNode);

                    Button button = findViewById(R.id.button);

                    button.setOnClickListener(view -> animateModel(modelRenderable));
                });
    }

    private void animateModel(ModelRenderable modelRenderable) {
        if (modelAnimator != null && modelAnimator.isRunning()) {
            modelAnimator.end();
        }
        int animationCount = modelRenderable.getAnimationDataCount();

        if (i == animationCount) {
            i = 0;
        }

        AnimationData animationData = modelRenderable.getAnimationData(i);

        modelAnimator = new ModelAnimator(animationData, modelRenderable);
        modelAnimator.start();
        i++;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (augmentedImageMap.isEmpty()) {
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void onUpdateFrame(FrameTime frameTime) {
        frame = arFragment.getArSceneView().getArFrame();

        if (frame == null) {
            return;
        }

        updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    final TextView mSwitcher = findViewById(R.id.MD_detail_popup);
                    if (augmentedImage.getIndex() == 0) {
                        mSwitcher.setText("이것은 피카츄입니다");
                    } else if (augmentedImage.getIndex() == 1) {
                        mSwitcher.setText("이것은 이브이입니다");
                    } else if (augmentedImage.getIndex() == 2) {
                        mSwitcher.setText("이것은 포켓볼입니다");
                    }

                    final Animation in = new AlphaAnimation(0.0f, 0.1f);
                    in.setDuration(0);

                    final Animation out = new AlphaAnimation(1.0f, 0.0f);
                    out.setDuration(500);
                    out.setStartOffset(2500);

                    mSwitcher.startAnimation(in);
                    mSwitcher.startAnimation(out);

                    mSwitcher.setVisibility(View.INVISIBLE);

                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    // Create a new anchor for newly found images.

                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
                        };

                        if (augmentedImage.getAnchors().size() == 0) {
                            anchor = augmentedImage.createAnchor(augmentedImage.getCenterPose());
                            aNode = new AnchorNode(anchor);
                            aNode.setParent(arFragment.getArSceneView().getScene());

                            augmentedImageMap.put(augmentedImage, new ARImageNode(this));

                            TransformableNode tNode = new TransformableNode(arFragment.getTransformationSystem());
                            tNode.getScaleController().setMinScale(0.01f);
                            tNode.getScaleController().setMaxScale(0.05f);
                            tNode.getTranslationController().setEnabled(false);
                            tNode.setParent(aNode);

                            switch (augmentedImage.getIndex()) {
                                case 0:
                                    playVideo(augmentedImage.createAnchor(augmentedImage.getCenterPose()), augmentedImage.getExtentX(), augmentedImage.getExtentZ());
                                    break;
                                case 1:
                                    createModel(augmentedImage.createAnchor(augmentedImage.getCenterPose()), arFragment);
                                    break;
                                case 2:
//                                    tNode.setRenderable(pikachu);
                                    break;

                                case 3:
                                    tNode.setRenderable(eevee);
                                    break;

                                case 4:
                                    tNode.setRenderable(pokeball);
                                    break;


                            }

                            int mdView = -1;

                            if (augmentedImage.getIndex() == 0) {
                                mdView = R.layout.md_picachu;
                            } else if (augmentedImage.getIndex() == 1) {
                                mdView = R.layout.md_eevee;
                            } else if (augmentedImage.getIndex() == 2) {
                                mdView = R.layout.md_pokeball;
                            }

                            if (mdView != -1) {
                                ViewRenderable.builder().setView(this, mdView)
                                        .build()
                                        .thenAccept(viewRenderable -> {
                                            TransformableNode nameTag = new TransformableNode(arFragment.getTransformationSystem());
                                            nameTag.setLocalPosition(new Vector3(0.0f, tNode.getLocalPosition().y + 0.05f, 0.0f));
                                            nameTag.getTranslationController().setEnabled(false);
                                            nameTag.getScaleController().setEnabled(false);
                                            nameTag.getRotationController().setEnabled(false);
                                            nameTag.setParent(aNode);

                                            Switch sw = findViewById(R.id.md_switch);
                                            sw.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                                if (isChecked) {
                                                    nameTag.setRenderable(viewRenderable);
                                                } else {
                                                    nameTag.setRenderable(null);
                                                }
                                            });
                                        });
                            }

                            tNode.select();
                        }
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    private void mInit() {
        //model 1
        ModelRenderable.builder()
                .setSource(this, R.raw.pikachu)
                .build()
                .thenAccept(renderable -> pikachu = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //model 2
        ModelRenderable.builder()
                .setSource(this, R.raw.eevee)
                .build()
                .thenAccept(renderable -> eevee = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //model 3
        ModelRenderable.builder()
                .setSource(this, R.raw.pokeball)
                .build()
                .thenAccept(renderable -> pokeball = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        //model 4 : Image to Video

        ModelRenderable.builder()
                .setSource(this, Uri.parse("video_screen.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    modelRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1f, 0.098f));
                    renderable = modelRenderable;
                });


        ModelRenderable.builder()
                .setSource(this, Uri.parse("acnecuberotate2.sfb"))
                .build()
                .thenAccept(renderable -> test2 = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
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
                    Toast toast = Toast.makeText(ARImageActivity.this, e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
            } else {
                Toast toast = Toast.makeText(ARImageActivity.this,
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
            Uri photoURI = FileProvider.getUriForFile(ARImageActivity.this,
                    ARImageActivity.this.getPackageName() + ".name.provider",
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
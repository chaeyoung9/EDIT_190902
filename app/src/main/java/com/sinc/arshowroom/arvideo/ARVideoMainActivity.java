package com.sinc.arshowroom.arvideo;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.SkeletonNode;
import com.google.ar.sceneform.animation.ModelAnimator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.AnimationData;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.sinc.arshowroom.R;

import java.util.Collection;

public class ARVideoMainActivity extends AppCompatActivity {
    private ExternalTexture texture;
    private MediaPlayer mediaPlayer;
    private CustomArFragment arFragment;
    private Scene scene;
    private ModelRenderable renderable;
    private boolean isImageDetected = false;
    private boolean isImage2Detected = false;
    private ModelAnimator modelAnimator;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        texture = new ExternalTexture();

        mediaPlayer = MediaPlayer.create(this, R.raw.acnestudio2019);
        mediaPlayer.setSurface(texture.getSurface());
        mediaPlayer.setLooping(true);

        ModelRenderable
                .builder()
                .setSource(this, Uri.parse("video_screen.sfb"))
                .build()
                .thenAccept(modelRenderable -> {
                    modelRenderable.getMaterial().setExternalTexture("videoTexture", texture);
                    modelRenderable.getMaterial().setFloat4("keyColor", new Color(0.01843f, 1f, 0.098f));
                    renderable = modelRenderable;
                });

        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        scene = arFragment.getArSceneView().getScene();
        scene.addOnUpdateListener(this::onUpdate);
    }

    private void onUpdate(FrameTime frameTime) {
//        if (isImageDetected) {
//            return;
//        }

        Frame frame = arFragment.getArSceneView().getArFrame();
        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);

        for (AugmentedImage image : augmentedImages) {
            if(image.getTrackingState() == TrackingState.TRACKING) {
                if (image.getName().equals("acne") && !isImageDetected) {
                    isImageDetected = true;
                    playVideo(image.createAnchor(image.getCenterPose()), image.getExtentX(), image.getExtentZ());
//                    break;
                } else if (image.getName().equals("cube") && !isImage2Detected) {
                    isImage2Detected = true;
                    createModel(image.createAnchor(image.getCenterPose()), arFragment);
//                    break;
                }
            }
        }
    }

    private void playVideo(Anchor anchor, float extentX, float extentZ) {
        mediaPlayer.start();
        AnchorNode anchorNode = new AnchorNode(anchor);
        texture.getSurfaceTexture().setOnFrameAvailableListener(surfaceTexture -> {
            anchorNode.setRenderable(renderable);
            texture.getSurfaceTexture().setOnFrameAvailableListener(null);
        });
        anchorNode.setWorldScale(new Vector3(extentX * 2, 1f, extentZ * 2));
        scene.addChild(anchorNode);
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
}

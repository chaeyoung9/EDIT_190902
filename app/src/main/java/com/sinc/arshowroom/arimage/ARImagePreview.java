package com.sinc.arshowroom.arimage;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.sinc.arshowroom.R;


public class ARImagePreview extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        ImageView iv = findViewById(R.id.iv_preview2);

        Toast.makeText(this, "OPENED", Toast.LENGTH_SHORT).show();
        Uri uri = getIntent().getParcelableExtra("uri");
        iv.setImageURI(uri);

    }
}

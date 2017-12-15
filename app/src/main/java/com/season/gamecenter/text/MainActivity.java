package com.season.gamecenter.text;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.season.gamecenter.R;
import com.season.gamecenter.ball.single.LogConsole;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.gameView);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        LogConsole.log("type = "+type);
        //Bitmap too large to be uploaded into a texture (4160x3120, max=4096x4096)
        if (action.equals(Intent.ACTION_SEND) && type.equals("image/*")) {
            Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            LogConsole.log("uri = "+uri);
            if (uri != null) {
                try {
                    LogConsole.log("uri.getPath() = "+uri.getPath());
                    FileInputStream fileInputStream = new FileInputStream(uri.getPath());
                    Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        if (action.equals(Intent.ACTION_SEND_MULTIPLE) && type.equals("image/*")) {
            ArrayList<Uri> uris=intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            LogConsole.log("uri = "+ uris.size());
            if (uris != null && uris.size() > 0) {
                try {
                    LogConsole.log("uri.getPath() = "+ uris.get(0).getPath());
                    FileInputStream fileInputStream = new FileInputStream(uris.get(0).getPath());
                    Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
    }
}
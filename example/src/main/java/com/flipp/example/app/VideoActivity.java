package com.flipp.example.app;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;


import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get data from intent
        final String videoUrl = getIntent().getStringExtra("videoUrl");
        try {
            setContentView(R.layout.activity_video);
            final VideoView videoView = (VideoView) findViewById(R.id.video);
            try {
                // Start the MediaController
                MediaController mediacontroller = new MediaController(
                        VideoActivity.this);
                mediacontroller.setAnchorView(videoView);
                // Get the URL from String VideoURL
                Uri video = Uri.parse(videoUrl);
                videoView.setMediaController(mediacontroller);
                videoView.setVideoURI(video);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                // Close the progress bar and play the video
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } catch (Exception e) {
            Log.e("VideoActivity", "Invalid Video Url");
        }
    }
}

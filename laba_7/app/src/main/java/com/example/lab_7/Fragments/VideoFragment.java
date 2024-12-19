package com.example.lab_7.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lab_7.R;

public class VideoFragment extends Fragment {

    public VideoFragment() {}

    private Button buttonChooseVideo;
    private VideoView videoView;
    private ActivityResultLauncher<Intent> launcher;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
           if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
               Uri uri = result.getData().getData();
               videoView.setVideoURI(uri);
               videoView.setVisibility(View.VISIBLE);
               videoView.start();
           }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);

        buttonChooseVideo = view.findViewById(R.id.buttonChooseVideo);
        videoView = view.findViewById(R.id.videoView);

        buttonChooseVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("video/*");
                launcher.launch(intent);
            }
        });


        return view;
    }
}
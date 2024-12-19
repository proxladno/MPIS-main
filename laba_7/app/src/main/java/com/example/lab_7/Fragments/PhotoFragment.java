package com.example.lab_7.Fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.lab_7.R;

import java.io.File;
import java.io.FileNotFoundException;

public class PhotoFragment extends Fragment {

    private Button buttonTakePhoto;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> launcher;

    public PhotoFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                File file = new File(requireActivity().getExternalFilesDir(null), "image.jpg");
                Uri uri = Uri.fromFile(file);
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(requireActivity().getContentResolver().openInputStream(uri));
                } catch (FileNotFoundException e) {
                    Log.e("PhotoFragment", "Error reading image", e);
                    throw new RuntimeException(e);
                }
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);

        buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);
        imageView = view.findViewById(R.id.imageView);

        buttonTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(requireActivity().getExternalFilesDir(null), "image.jpg");
                Uri uri = FileProvider.getUriForFile(requireActivity(), requireActivity().getPackageName() + ".provider", file);
                intent_photo.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                intent_photo.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                intent_photo.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                launcher.launch(intent_photo);
            }
        });

        return view;
    }
}
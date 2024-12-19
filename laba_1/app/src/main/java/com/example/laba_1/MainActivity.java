package com.example.laba_1;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText editText;
    private ImageView imageView;

    private String ImageURL = "";
    private String pageURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        Button button = findViewById(R.id.button_for_searching);
        editText = findViewById(R.id.input_for_searching);
        imageView = findViewById(R.id.imageView);

        // Установить прослушку
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClick(v);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (!ImageURL.isEmpty()) {
                    switch (item.getItemId()) {
                        case R.id.download:
                            // Логика для скачивания изображения
                            downloadImage(ImageURL);
                            Toast.makeText(MainActivity.this, "Изображение скачано", Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Изображение скачано", Toast.LENGTH_SHORT).show();
                            return true;
                        case R.id.rate:
                            // Логика для оценки изображения
                            showRatingDialog();
                            return true;
                        case R.id.site:
                            // Переход на сайт изображения
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(pageURL));
                            MainActivity.this.startActivity(intent);
                        default:
                            return false;
                    }
                }
                return false;
            }
        });

        popupMenu.show();
    }

    private void showRatingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate_image, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        builder.setPositiveButton("Оценить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                float rating = ratingBar.getRating();
                // Логика для обработки рейтинга
                Toast.makeText(MainActivity.this, "Вы оценили изображение на " + rating + " звезд", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void downloadImage(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Downloading Image");
        request.setDescription("Downloading image using DownloadManager.");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "downloadedImage.jpg");

        DownloadManager manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public void ButtonClick(View view) {
        String query = editText.getText().toString();
        if (!query.isEmpty()) {

            progressBar.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);

            String encodedQuery = "";

            try {
                encodedQuery = URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String accessKey = BuildConfig.UNSPLASH_ACCESS_KEY;
            String url = "https://pixabay.com/api/?key=" + accessKey + "&q=" + encodedQuery + "&image_type=photo&orientation=vertical&lang=ru&per_page=3";

            new MainActivity.FetchImageTask().execute(url);
        }
    }

    private class FetchImageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            Log.d("FetchImageTask", "doInBackground started");
            String jsonString;
            try {
                jsonString = getJsonFromUrl(urls[0]);
                Log.d("FetchImageTask", "JSON received: " + jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray results = jsonObject.getJSONArray("hits");
                if (results.length() == 0) {
                    ImageURL = "";
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("FetchImageTask", "Setting progressBar to INVISIBLE");
                            progressBar.setVisibility(View.INVISIBLE);
                            showInfoDialog("Ничего не найдено :(");
                        }
                    });
                    return "No result";
                }
                JSONObject result = results.getJSONObject(0);
                ImageURL = result.getString("largeImageURL");
                pageURL = result.getString("pageURL");
                Log.d("FetchImageTask", "Image URL: " + ImageURL);
                return ImageURL;
            } catch (IOException | JSONException e) {
                Log.e("FetchImageTask", "Error: " + e.getMessage());
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.INVISIBLE);
                        showInfoDialog(e.getMessage());
                    }
                });
                return "Error";
            }
        }


        @Override
        protected void onPostExecute(String rawUrl) {
            super.onPostExecute(rawUrl);
            Log.d("FetchImageTask", "onPostExecute: " + rawUrl);
            if (rawUrl != null && !rawUrl.equals("No result") && !rawUrl.equals("Error")) {
                Glide.with(MainActivity.this).load(rawUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.d("Glide", "onLoadFailed");
                        showInfoDialog("Ошибка загрузки изображения");
                        progressBar.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("Glide", "onResourceReady");
                        imageView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        return false;
                    }

                }).transition(DrawableTransitionOptions.withCrossFade(400)).into(imageView);

            }

        }

        public String getJsonFromUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                return readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        }

        private String readStream(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        }

        private void showInfoDialog(String message) {
            new AlertDialog.Builder(MainActivity.this).setMessage(message).setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }

    }
}

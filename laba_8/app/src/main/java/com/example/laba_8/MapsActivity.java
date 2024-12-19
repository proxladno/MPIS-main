package com.example.laba_8;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity {

    private MapView mapView;
    private Button visitedPlaceButton;
    private Button centerOnUserButton;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker currentLocationMarker;
    private List<GeoPoint> visitedPlaces;
    private SharedPreferences sharedPreferences;
    private List<GeoPoint> routePoints; // Список для хранения точек маршрута
    private Polyline routePolyline; // Линия маршрута
    private Handler handler; // Обработчик для периодического добавления точек
    private Runnable addPointRunnable; // Задача для добавления точки

    private void setMargins(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.leftMargin = insets.left;
            mlp.bottomMargin = insets.bottom;
            mlp.rightMargin = insets.right;
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);

        visitedPlaceButton = findViewById(R.id.visitedPlaceButton);
        centerOnUserButton = findViewById(R.id.centerOnUserButton);

        setMargins(visitedPlaceButton);
        setMargins(centerOnUserButton);
        setMargins(findViewById(R.id.textFIO));

        mapView = findViewById(R.id.map);
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Настройка LocationRequest
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 секунд
        locationRequest.setFastestInterval(5000); // 5 секунд

        // Инициализация списка посещенных мест и SharedPreferences
        visitedPlaces = new ArrayList<>();
        sharedPreferences = getSharedPreferences("VisitedPlaces", MODE_PRIVATE);
        loadVisitedPlaces();

        // Инициализация маркера текущего местоположения и линии маршрута
        initializeCurrentLocationMarker();
        routePoints = new ArrayList<>();
        routePolyline = new Polyline();
        mapView.getOverlays().add(routePolyline);

        // Установка начального уровня зума
        mapView.getController().setZoom(15);

        // Обработка обновлений местоположения
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                    updateCurrentLocationMarker(); // Обновляем маркер текущего местоположения

                    // Добавляем точку маршрута
                    GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    addRoutePoint(geoPoint);
                }
            }
        };

        visitedPlaceButton.setOnClickListener(v -> {
            if (currentLocation != null) {
                GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                addMarker(geoPoint, "Посещенное место");
                saveVisitedPlace(geoPoint); // Сохраняем метку
            } else {
                Toast.makeText(this, "Текущее местоположение недоступно", Toast.LENGTH_SHORT).show();
            }
        });

        centerOnUserButton.setOnClickListener(v -> centerOnUser());

        checkPermissionsAndGetLocation();
        startAddingRoutePoints();
    }

    private void startAddingRoutePoints() {
        handler = new Handler();
        addPointRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentLocation != null) {
                    GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    addRoutePoint(geoPoint);
                }
                handler.postDelayed(this, 120000); // Добавляем точку каждые 2 минуты
            }
        };
        handler.postDelayed(addPointRunnable, 120000); // Запускаем через 2 минуты
    }

    private void addRoutePoint(GeoPoint geoPoint) {
        routePoints.add(geoPoint);
        routePolyline.setPoints(routePoints);
        mapView.invalidate(); // Обновляем карту
    }

    private void checkPermissionsAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void addMarker(GeoPoint geoPoint, String title) {
        Marker marker = new Marker(mapView);
        marker.setPosition(geoPoint);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
        mapView.invalidate();
    }

    private void centerOnUser() {
        if (currentLocation != null) {
            GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            mapView.getController().animateTo(geoPoint);
            Toast.makeText(this, "Центрировано на пользователе", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Неудается получить текущее местоположение", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeCurrentLocationMarker() {
        currentLocationMarker = new Marker(mapView);
        currentLocationMarker.setIcon(getResources().getDrawable(R.drawable.ic_current_location)); // Укажите иконку для маркера
        mapView.getOverlays().add(currentLocationMarker);
    }

    private void updateCurrentLocationMarker() {
        if (currentLocation != null) {
            GeoPoint geoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
            currentLocationMarker.setPosition(geoPoint);
            currentLocationMarker.setTitle("Текущее местоположение");
            mapView.invalidate();
        }
    }

    private void loadVisitedPlaces() {
        String placesString = sharedPreferences.getString("places", "");
        if (!placesString.isEmpty()) {
            String[] placesArray = placesString.split(";");
            for (String place : placesArray) {
                String[] latLong = place.split(",");
                if (latLong.length == 2) {
                    double latitude = Double.parseDouble(latLong[0]);
                    double longitude = Double.parseDouble(latLong[1]);
                    visitedPlaces.add(new GeoPoint(latitude, longitude));
                    addMarker(new GeoPoint(latitude, longitude), "Посещенное место");
                }
            }
        }
    }

    private void saveVisitedPlace(GeoPoint geoPoint) {
        visitedPlaces.add(geoPoint);
        StringBuilder placesString = new StringBuilder();
        for (GeoPoint place : visitedPlaces) {
            placesString.append(place.getLatitude()).append(",").append(place.getLongitude()).append(";");
        }
        sharedPreferences.edit().putString("places", placesString.toString()).apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(); // Запрашиваем местоположение, если разрешение предоставлено
            } else {
                Toast.makeText(this, "Разрешение на доступ к местоположению отклонено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        mapView.onPause();
        handler.removeCallbacks(addPointRunnable); // Останавливаем добавление точек
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDetach();
        handler.removeCallbacks(addPointRunnable); // Останавливаем добавление точек
    }
}
package com.example.weatherreport;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HomeActivity extends AppCompatActivity {

    GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    Button sign_out, weather;
    TextView weatherDisplay;
    FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sign_out = findViewById(R.id.sign_out_button);
        weather = findViewById(R.id.Weather);
        weatherDisplay = findViewById(R.id.weather_display);

        weather.setOnClickListener(v -> fetchWeather());

        sign_out.setOnClickListener(v -> gsc.signOut().addOnCompleteListener(task -> {
            LoginManager.getInstance().logOut();

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
            Toast.makeText(HomeActivity.this, "Sign-Out Successful!", Toast.LENGTH_SHORT).show();
            finish();
        }
        ));

    }

    private void fetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                new FetchWeatherTask().execute(location.getLatitude(), location.getLongitude());
            } else {
                weatherDisplay.setText("Unable to fetch location");
            }
        });
    }

    private class FetchWeatherTask extends AsyncTask<Double, Void, String> {
        @Override
        protected String doInBackground(Double... params) {
            double latitude = params[0];
            double longitude = params[1];
            String apiKey = "f1bc3dd512305377ec8489ec5bbcc9dc";
            String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + apiKey + "&units=metric";

            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String weather = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
                String temperature = jsonResponse.getJSONObject("main").getString("temp");

                return "Weather: " + weather + "\nTemperature: " + temperature + "°C";

            } catch (Exception e) {
                e.printStackTrace();
                return "Error fetching weather";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            weatherDisplay.setText(result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchWeather();
            } else {
                weatherDisplay.setText("Permission denied");
            }
        }
    }
}

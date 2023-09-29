package com.subk.testing;

import static co.subk.zoomsdk.ZoomSdkHelper.RENDER_TYPE_ZOOMRENDERER;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.subk.testing.databinding.ActivityMainBinding;

import co.subk.zoomsdk.MeetingActivity;

public class MainActivity extends AppCompatActivity {
    protected final static int REQUEST_VIDEO_AUDIO_CODE = 1010;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {
            String sessionName = "Location Getter - Arul";
            String name = "Rajan Sanghvi";
            String password = "789789789";
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwicm9sZV90eXBlIjoxLCJ0cGMiOiJMb2NhdGlvbiBHZXR0ZXIgLSBBcnVsIiwidmVyc2lvbiI6MSwiaWF0IjoxNjk1OTEzOTQyLCJleHAiOjE2OTU5MjExNDIsInVzZXJfaWRlbnRpdHkiOiIzNTQ4Iiwic2Vzc2lvbl9rZXkiOiI5YzcyZDU5YS0zYmM4LTQ1MTUtYTVmNC00NTgwZDkzMzM4NzciLCJwd2QiOiI3ODk3ODk3ODkiLCJjbG91ZF9yZWNvcmRpbmdfb3B0aW9uIjowLCJjbG91ZF9yZWNvcmRpbmdfZWxlY3Rpb24iOjF9._-f85ns4W50LLbTzw5CinhKdvNjx88_0atzWmbQHf28";

            Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("tokens", token);
            intent.putExtra("password", password);
            intent.putExtra("sessionName", sessionName);
            intent.putExtra("render_type", RENDER_TYPE_ZOOMRENDERER);
            intent.putExtra("allow_to_invite_attendee", true);
            intent.putExtra("allow_to_share_screen", true);
            intent.putExtra("allow_to_mute_audio", true);
            intent.putExtra("allow_to_hide_video", true);
            intent.putExtra("allow_to_end_meeting", true);
            intent.putExtra("allow_to_take_screenshot", true);
            intent.putExtra("allow_to_get_location", true);
            startActivity(intent);
        });

        requestPermission();
    }

    protected boolean requestPermission() {

        String[] permissions = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_MEDIA_IMAGES,
                    android.Manifest.permission.READ_MEDIA_VIDEO,
                    android.Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT
            };
        } else if (Build.VERSION.SDK_INT >= 31) {
            permissions = new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.BLUETOOTH_CONNECT
            };
        }

        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_VIDEO_AUDIO_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_VIDEO_AUDIO_CODE) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    Build.VERSION.SDK_INT >= 31 &&
                    checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted();
            }
        }
    }

    public void onPermissionGranted() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
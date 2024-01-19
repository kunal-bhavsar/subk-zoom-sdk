package com.subk.testing.ui;

import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_END_MEETING;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_GET_LOCATION;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_HIDE_VIDEO;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_INVITE_ATTENDEE;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_MUTE_AUDIO;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_SHARE_SCREEN;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_ALLOW_TO_TAKE_SCREENSHOT;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_PASSWORD;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_RENDER_TYPE;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_SESSION_NAME;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_TOKEN;
import static co.subk.zoomsdk.ZoomSdkHelper.PARAM_USERNAME;
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

import com.subk.testing.R;
import com.subk.testing.databinding.ActivityMainBinding;
import com.subk.testing.service.EventManagementService;

import co.subk.zoomsdk.MeetingActivity;
import co.subk.zoomsdk.ZoomSdkInitializer;
import co.subk.zoomsdk.meeting.exceptions.ZoomInitializationException;

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
            String sessionName = "Android 14 Test Arul";
            String name = "Rajan Sanghvi";
            String password = "789789789";
            String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBfa2V5IjoiSFRvZVBWVkhSbmhONEV5dmQxc3Q3RmJyN1hJZkJLc08xQmEzIiwicm9sZV90eXBlIjowLCJ0cGMiOiJBbmRyb2lkIDE0IFRlc3QgQXJ1bCIsInZlcnNpb24iOjEsImlhdCI6MTcwNTU4MTE3OCwiZXhwIjoxNzA1NTg4Mzc4LCJ1c2VyX2lkZW50aXR5IjoiODQ1NCIsInNlc3Npb25fa2V5IjoiM2Y4MzYyNmItZWNkNC00ZWY3LTk2ZmUtOTFjMWM5YjgwNmVjIiwicHdkIjoiNzg5Nzg5Nzg5In0.53tXoUqJ_OOJl8GzO2FsssvIILMu-cF_j0SkUeahJ4E";

            Intent intent = new Intent(MainActivity.this, MeetingActivity.class);
            intent.putExtra(PARAM_USERNAME, name);
            intent.putExtra(PARAM_TOKEN, token);
            intent.putExtra(PARAM_PASSWORD, password);
            intent.putExtra(PARAM_SESSION_NAME, sessionName);
            intent.putExtra(PARAM_RENDER_TYPE, RENDER_TYPE_ZOOMRENDERER);
            intent.putExtra(PARAM_ALLOW_TO_INVITE_ATTENDEE, true);
            intent.putExtra(PARAM_ALLOW_TO_SHARE_SCREEN, true);
            intent.putExtra(PARAM_ALLOW_TO_MUTE_AUDIO, true);
            intent.putExtra(PARAM_ALLOW_TO_HIDE_VIDEO, true);
            intent.putExtra(PARAM_ALLOW_TO_END_MEETING, true);
            intent.putExtra(PARAM_ALLOW_TO_TAKE_SCREENSHOT, true);
            intent.putExtra(PARAM_ALLOW_TO_GET_LOCATION, true);
            startActivity(intent);
        });

        requestPermission();

        startService(new Intent(this, EventManagementService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopService(new Intent(this, EventManagementService.class));
    }

    protected boolean requestPermission() {

        String[] permissions = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.POST_NOTIFICATIONS
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
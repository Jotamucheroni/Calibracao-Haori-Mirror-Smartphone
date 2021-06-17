package com.example.opengles_exemplo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.appcompat.app.AppCompatActivity;

import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    public final ActivityResultLauncher<Intent> activateBluetoothLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if ( result.getResultCode() != AppCompatActivity.RESULT_OK ) {
                    Log.e("Bluetooth", "O Bluetooth não pôde ser iniciado" );
                }
            });

    public final ActivityResultLauncher<Intent> discoverableBluetoothLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if ( result.getResultCode() == AppCompatActivity.RESULT_CANCELED ) {
                        Log.e("Bluetooth",
                              "Não foi possível disponibilizar o dispositivo para comunicação"
                        );
                    }
                }
        );

    private MyGLSurfaceView glView;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        glView = new MyGLSurfaceView(this );
        setContentView( glView );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
           hideSystemUI();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(
        new RequestPermission(),
        isGranted -> {
            if ( ! isGranted ) {
                finishAndRemoveTask();
            }
        });

    public void requisitarPermissoes() {
        final String[] permissoes = new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
        };

        for ( String permissao: permissoes )
            if ( checkSelfPermission( permissao ) == PackageManager.PERMISSION_DENIED )
                requestPermissionLauncher.launch( permissao );
    }

    private void hideSystemUI() {
        // Coloca a atividade em modo tela cheia imersivo fixo
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        glView.liberarRecursos();
    }
}
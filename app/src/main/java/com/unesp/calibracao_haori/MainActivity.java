package com.unesp.calibracao_haori;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private SuperficieOpenGL glView;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        
        glView = new SuperficieOpenGL( this );
        setContentView( glView );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
    }
    
    private void hideSystemUI() {
        // Coloca a atividade em modo tela cheia imersivo fixo
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            |   View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            |   View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            |   View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            |   View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            |   View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }
    
    @Override
    public void onWindowFocusChanged( boolean hasFocus ) {
        super.onWindowFocusChanged( hasFocus );
        
        if ( hasFocus )
            hideSystemUI();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        hideSystemUI();
    }
    
    Recurso permissao = new Recurso();
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
        registerForActivityResult(
            new RequestPermission(),
            isGranted -> {
                if ( isGranted )
                    permissao.liberarEspera();
                else
                    finishAndRemoveTask();
            }
        );
    
    public void requisitarPermissao( String nomePermissao ) {
        if ( checkSelfPermission( nomePermissao ) != PackageManager.PERMISSION_DENIED )
            return;
        
        requestPermissionLauncher.launch( nomePermissao );
        permissao.esperar();
    }
    
    Recurso bluetooth = new Recurso();
    
    private final ActivityResultLauncher<Intent> activateBluetoothLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if ( result.getResultCode() == AppCompatActivity.RESULT_OK )
                    bluetooth.liberarEspera();
                else {
                    Log.e("Bluetooth", "O Bluetooth não pôde ser iniciado");
                    finishAndRemoveTask();
                }
            }
        );
    
    public void ativarBluetooth () {
        activateBluetoothLauncher.launch( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ) );
        bluetooth.esperar();
    }
    
    private final ActivityResultLauncher<Intent> discoverableBluetoothLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if ( result.getResultCode() != AppCompatActivity.RESULT_CANCELED )
                    bluetooth.liberarEspera();
                else {
                    Log.e(
                        "Bluetooth",
                        "Não foi possível disponibilizar o dispositivo para comunicação"
                    );
                    finishAndRemoveTask();
                }
            }
        );
    
    public void tornarDispositivoVisivel( int segundos ) {
        if ( segundos < 0 )
            discoverableBluetoothLauncher.launch(
                new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE )
            );
        else
            discoverableBluetoothLauncher.launch(
                new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE )
                    .putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, segundos )
            );
        
        bluetooth.esperar();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        glView.liberarRecursos();
    }
}
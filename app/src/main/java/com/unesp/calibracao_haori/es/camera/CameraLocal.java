package com.unesp.calibracao_haori.es.camera;

import android.util.Size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.unesp.calibracao_haori.Recurso;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraLocal extends Camera implements ImageAnalysis.Analyzer {
    private CameraSelector camera;
    private AppCompatActivity atividade;
    private final ListenableFuture<ProcessCameraProvider> listenable;
    private final Executor executorAtividade;
    private final Recurso conexaoCamera;
    
    public CameraLocal(
        AppCompatActivity atividade, CameraSelector camera, int largImg, int altImg, int numCompCor
    ) {
        setAtividade( atividade );
        setCamera( camera );
        setLargImg( largImg );
        setAltImg( altImg );
        setNumCompCor( numCompCor );

        executorAtividade = ContextCompat.getMainExecutor( atividade );
        listenable = ProcessCameraProvider.getInstance( atividade );
        
        conexaoCamera = new Recurso();
    }
    
    public CameraLocal(
        AppCompatActivity atividade, CameraSelector camera, int largImg, int altImg
    ) {
        this( atividade, camera, largImg, altImg, 3 );
    }
    
    public CameraLocal( AppCompatActivity atividade, CameraSelector camera, int numCompCor ) {
        this( atividade, camera, 640, 480, numCompCor );
    }
    
    public CameraLocal( AppCompatActivity atividade, CameraSelector camera ) {
        this( atividade, camera, 640, 480, 3 );
    }
    
    public CameraLocal( AppCompatActivity atividade ) {
        this( atividade, CameraSelector.DEFAULT_BACK_CAMERA, 640, 480, 3 );
    }
    
    public void setAtividade( AppCompatActivity atividade ) {
        this.atividade = atividade;
    }
    
    public void setCamera( CameraSelector camera ) {
        this.camera = camera;
    }
    
    public CameraSelector getCamera() {
        return camera;
    }
    
    public AppCompatActivity getAtividade() {
        return atividade;
    }

    private ExecutorService cameraExecutor;
    private ImageAnalysis analisador;
    
    @Override
    public void ligar() {
        if ( ligada | atividade == null | camera == null )
            return;
        
        setBuffer();
        cameraExecutor = Executors.newSingleThreadExecutor();
        analisador = new ImageAnalysis.Builder()
            .setTargetResolution( new Size( getLargImg(), getAltImg() ) )
            .setBackpressureStrategy( ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST )
            .build();
        analisador.setAnalyzer( cameraExecutor, this );
        listenable.addListener(
            () -> {
                try {
                    listenable.get().bindToLifecycle( atividade, camera, analisador );
                    conexaoCamera.liberarEspera();
                } catch ( ExecutionException | InterruptedException e ) {
                    e.printStackTrace();
                }
            },
            executorAtividade
        );
        
        conexaoCamera.esperar();
        ligada = true;
    }
    
    public void desligar() {
        if ( !ligada )
            return;
        
        ligada = false;

        listenable.addListener(
            () -> {
                try {
                    listenable.get().unbind( analisador );
                    conexaoCamera.liberarEspera();
                } catch ( ExecutionException | InterruptedException e ) {
                    e.printStackTrace();
                }
            },
            executorAtividade
        );
        conexaoCamera.esperar();
        
        analisador.clearAnalyzer();
        cameraExecutor.shutdown();
    }
    
    @Override
    public void analyze( @NonNull ImageProxy imagem ) {
        final ByteBuffer bb = imagem.getPlanes()[0].getBuffer();
        
        bb.rewind();
        buffer.rewind();
        buffer.put( bb );
        
        imagem.close();
    }
}
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
    private AppCompatActivity atividade;
    private CameraSelector cameraSelector;
    private final ListenableFuture<ProcessCameraProvider> listenable;
    private final Executor executorAtividade;
    private final Recurso conexaoCamera;
    
    public CameraLocal(
        AppCompatActivity atividade,
        CameraSelector cameraSelector, int largImg, int altImg, int numCompCor
    ) {
        setAtividade( atividade );
        setCameraSelector( cameraSelector );
        setLarguraImagem( largImg );
        setAlturaImagem( altImg );
        setNumeroComponentesCorImagem( numCompCor );
        
        executorAtividade = ContextCompat.getMainExecutor( atividade );
        listenable = ProcessCameraProvider.getInstance( atividade );
        
        conexaoCamera = new Recurso();
    }
    
    public CameraLocal(
        AppCompatActivity atividade, CameraSelector cameraSelector, int largImg, int altImg
    ) {
        this( atividade, cameraSelector, largImg, altImg, 3 );
    }
    
    public CameraLocal(
        AppCompatActivity atividade, CameraSelector cameraSelector, int numCompCor
    ) {
        this( atividade, cameraSelector, 640, 480, numCompCor );
    }
    
    public CameraLocal( AppCompatActivity atividade, CameraSelector cameraSelector ) {
        this( atividade, cameraSelector, 640, 480, 3 );
    }
    
    public CameraLocal( AppCompatActivity atividade ) {
        this( atividade, CameraSelector.DEFAULT_BACK_CAMERA, 640, 480, 3 );
    }
    
    public void setAtividade( AppCompatActivity atividade ) {
        this.atividade = atividade;
    }
    
    public void setCameraSelector( CameraSelector cameraSelector ) {
        this.cameraSelector = cameraSelector;
    }
    
    public CameraSelector getCameraSelector() {
        return cameraSelector;
    }
    
    private ExecutorService cameraExecutor;
    private ImageAnalysis analisador;
    
    @Override
    public void ligar() {
        if ( ligada | atividade == null | cameraSelector == null )
            return;
        
        setBuffer();
        cameraExecutor = Executors.newSingleThreadExecutor();
        analisador = new ImageAnalysis.Builder()
            .setTargetResolution( new Size( getLarguraImagem(), getAlturaImagem() ) )
            .setBackpressureStrategy( ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST )
            .build();
        analisador.setAnalyzer( cameraExecutor, this );
        listenable.addListener(
            () -> {
                try {
                    listenable.get().bindToLifecycle( atividade, cameraSelector, analisador );
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
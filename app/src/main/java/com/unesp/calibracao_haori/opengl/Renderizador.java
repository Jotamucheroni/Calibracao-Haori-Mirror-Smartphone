package com.unesp.calibracao_haori.opengl;

import android.Manifest;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;

import androidx.camera.core.CameraSelector;

import com.unesp.calibracao_haori.MainActivity;
import com.unesp.calibracao_haori.es.Bluetooth;
import com.unesp.calibracao_haori.es.camera.Camera;
import com.unesp.calibracao_haori.es.camera.CameraLocal;
import com.unesp.calibracao_haori.opengl.renderbuffer.FrameBufferObject;
import com.unesp.calibracao_haori.opengl.renderbuffer.Tela;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderizador implements GLSurfaceView.Renderer, AutoCloseable {
    private final MainActivity atividade;
    
    public Renderizador( MainActivity atividade ) {
        super();
        
        this.atividade = atividade;
    }
    
    private final float[]
        refQuad = {
            // Coordenadas  Textura
            -1.0f,  1.0f,   0.0f,   0.0f,
            -1.0f,  -1.0f,  0.0f,   1.0f,
            1.0f,   -1.0f,  1.0f,   1.0f,
            1.0f,   1.0f,   1.0f,   0.0f
        };
    
    private final int[] refElementos = { 0, 1, 2, 2, 3, 0 };
    
    private Camera camera;
    private Bluetooth bluetooth;
    private FrameBufferObject frameBufferCamera;
    private Textura texturaCamera;
    
    private Objeto imagemCamera;
    private DetectorBorda detectorBordaCamera;
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        atividade.requisitarPermissao( Manifest.permission.CAMERA );
        camera = new CameraLocal(
            atividade, CameraSelector.DEFAULT_BACK_CAMERA, 320, 240, 1
        );
        camera.ligar();
        
        frameBufferCamera = new FrameBufferObject( 3, 640, 480 );
        
        texturaCamera = new Textura(
            camera.getLargImg(), camera.getAltImg(), true
        );
        texturaCamera.alocar();
        
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        imagemCamera = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturaCamera
        );
        
        detectorBordaCamera = new DetectorBorda( frameBufferCamera.getNumBytes() );
        detectorBordaCamera.alocar();
        
        atividade.requisitarPermissao( Manifest.permission.BLUETOOTH );
        bluetooth = new Bluetooth( atividade, camera.getTamImg(), camera.getImagem() );
        bluetooth.abrirServidor();
    }
    
    private final Tela tela = Tela.getInstance();
    
    @Override
    public void onSurfaceChanged( GL10 unused, int width, int height ) {
        tela.setLargura( width );
        tela.setAltura( height );
    }
    
    @Override
    public void onDrawFrame( GL10 unused ) {
        texturaCamera.carregarImagem( camera.getImagem() );
        
        frameBufferCamera.clear();
        frameBufferCamera.draw( imagemCamera );
        
        if ( detectorBordaCamera.pronto() ) {
//            System.out.println( "Píxeis: " + detectorBordaCamera.getSaida() );
            frameBufferCamera.lerRenderBuffer( 3, detectorBordaCamera.getImagem() );
            detectorBordaCamera.executar();
        }
        
        tela.clear();
        frameBufferCamera.copiar(
            tela, tela.getLargura(), tela.getAltura(), 3, 1
        );
    }
    
    @Override
    public void close() {
        bluetooth.close();
        detectorBordaCamera.close();
        texturaCamera.close();
        frameBufferCamera.close();
        camera.close();
    }
}
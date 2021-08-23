package com.unesp.calibracao_haori.opengl;

import android.Manifest;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;

import androidx.camera.core.CameraSelector;

import com.unesp.calibracao_haori.MainActivity;
import com.unesp.calibracao_haori.es.Bluetooth;
import com.unesp.calibracao_haori.es.camera.Camera;
import com.unesp.calibracao_haori.es.camera.CameraLocal;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderizador implements GLSurfaceView.Renderer, AutoCloseable {
    private final MainActivity activity;
    
    public Renderizador(MainActivity activity ) {
        super();
        
        this.activity = activity;
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
    private FrameBuffer frameBuffer;
    private Textura texturaCamera;
    
    private Objeto imagemCamera;
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        // Câmera
        activity.requisitarPermissao( Manifest.permission.CAMERA );
        camera = new CameraLocal(
                activity, CameraSelector.DEFAULT_BACK_CAMERA, 320, 240, 1
        );
        camera.ligar();
        
        // Bluetooth
        activity.requisitarPermissao( Manifest.permission.BLUETOOTH );
        try {
            bluetooth = new Bluetooth( activity, camera.getTamImg(), camera.getImagem() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        bluetooth.abrirServidor();
        
        // Framebuffer
        frameBuffer = new FrameBuffer( 2, 640, 480 );
        
        texturaCamera = new Textura(
            camera.getLargImg(), camera.getAltImg(), true
        );
        texturaCamera.alocar();
        
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        imagemCamera = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturaCamera
        );
    }
    
    private int larguraTela, alturaTela;
    
    @Override
    public void onSurfaceChanged( GL10 unused, int width, int height ) {
        larguraTela = width;
        alturaTela = height;
    }
    
    @Override
    public void onDrawFrame( GL10 unused ) {
        texturaCamera.carregarImagem( camera.getImagem() );
        
        frameBuffer.draw( imagemCamera );
        
        // Desenha na tela
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
        frameBuffer.exibir( larguraTela, alturaTela, 2, 1 );
    }
    
    @Override
    public void close() {
        texturaCamera.close();
        frameBuffer.close();
        bluetooth.close();
        camera.close();
    }
}
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
    private DetectorBorda detectorBordaCamera, detectorTeste, detectorTeste2;
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        // Câmera
        atividade.requisitarPermissao( Manifest.permission.CAMERA );
        camera = new CameraLocal(
            atividade, CameraSelector.DEFAULT_BACK_CAMERA, 320, 240, 1
        );
        camera.ligar();
        
        // Framebuffer
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
        
        detectorTeste = new DetectorBorda( frameBufferCamera.getNumBytes() );
        detectorTeste.alocar();

        detectorTeste2 = new DetectorBorda( frameBufferCamera.getNumBytes() );
        detectorTeste2.alocar();

        // Bluetooth
        atividade.requisitarPermissao( Manifest.permission.BLUETOOTH );
        try {
            bluetooth = new Bluetooth( atividade, camera.getTamImg(), camera.getImagem() );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
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
            System.out.println( "Píxeis(1): " + detectorBordaCamera.getSaida() );
            frameBufferCamera.lerRenderBuffer( 1, detectorBordaCamera.getImagem() );
            detectorBordaCamera.executar();
        }
        
        if ( detectorTeste.pronto() ) {
            System.out.println( "Píxeis(2): " + detectorTeste.getSaida() );
            frameBufferCamera.lerRenderBuffer( 2, detectorTeste.getImagem() );
            detectorTeste.executar();
        }
        
        if ( detectorTeste2.pronto() ) {
            System.out.println( "Píxeis(3): " + detectorTeste2.getSaida() );
            frameBufferCamera.lerRenderBuffer( 3, detectorTeste2.getImagem() );
            detectorTeste2.executar();
        }
        
        System.out.println( " " );
        
        tela.clear();
        frameBufferCamera.copiar(
            tela, tela.getLargura(), tela.getAltura(), 3, 1
        );
    }
    
    @Override
    public void close() {
        detectorBordaCamera.close();
        detectorTeste.close();
        detectorTeste2.close();
        texturaCamera.close();
        frameBufferCamera.close();
        bluetooth.close();
        camera.close();
    }
}
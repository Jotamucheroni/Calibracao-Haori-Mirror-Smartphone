package com.unesp.calibracao_haori.opengl;

import android.Manifest;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;

import androidx.camera.core.CameraSelector;

import com.unesp.calibracao_haori.MainActivity;
import com.unesp.calibracao_haori.es.Bluetooth;
import com.unesp.calibracao_haori.es.Dispositivo;
import com.unesp.calibracao_haori.es.camera.CameraLocal;
import com.unesp.calibracao_haori.opengl.framebuffer.Tela;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderizador implements GLSurfaceView.Renderer, AutoCloseable {
    private final MainActivity atividade;
    
    public Renderizador( MainActivity atividade ) {
        super();
        
        this.atividade = atividade;
    }
    
    private Dispositivo cameraTraseira;
    private Bluetooth bluetooth;
    
    private final int NUMERO_PARAMETROS_TEXTURA = 2;
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        
        cameraTraseira = new Dispositivo(
            "Câmera traseira",
            new CameraLocal(
                atividade, CameraSelector.DEFAULT_BACK_CAMERA, 320, 240, 1
            )
        );
        cameraTraseira.alocar();
        atividade.requisitarPermissao( Manifest.permission.CAMERA );
        cameraTraseira.ligar();

        Desenho desenho = cameraTraseira.getDesenho();
        for ( int i = 0; i < NUMERO_PARAMETROS_TEXTURA; i++ )
            desenho.setParametroTextura( i, 0.1f );
        
        atividade.requisitarPermissao( Manifest.permission.BLUETOOTH );
        bluetooth = new Bluetooth(
            atividade,
            cameraTraseira.getCamera().getTamImg(), cameraTraseira.getCamera().getImagem()
        );
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
        cameraTraseira.atualizarTextura();
        cameraTraseira.draw();
//        cameraTraseira.atualizarImagemDetector( 3 );
        
        tela.clear();
        cameraTraseira.getFrameBufferObject().copiar(
            tela, tela.getLargura(), tela.getAltura(), 3, 1
        );
    }
    
    @Override
    public void close() {
        bluetooth.close();
        cameraTraseira.close();
    }
}
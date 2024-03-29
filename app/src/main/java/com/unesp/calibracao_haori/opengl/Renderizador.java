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
    private Desenho quadradoCalibracao, quadradoTeste;
    private Bluetooth bluetooth;
    
    private final int NUMERO_PARAMETROS_TEXTURA = 2;
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        
        atividade.requisitarPermissao( Manifest.permission.CAMERA );
        cameraTraseira = new Dispositivo(
            "Câmera traseira",
            new CameraLocal(
                atividade, CameraSelector.DEFAULT_BACK_CAMERA, 320, 240, 1
            )
        );
        cameraTraseira.ligar();
        
        Desenho desenho = cameraTraseira.getDesenho();
        for ( int i = 0; i < NUMERO_PARAMETROS_TEXTURA; i++ )
            desenho.setParametroTextura( i, 0.1f );
        
        float[] verticesQuadradoBranco = new float[] {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
             1.0f, -1.0f,
             1.0f,  1.0f
        };
        
        quadradoCalibracao = new Desenho(
            2,
            verticesQuadradoBranco,
            Desenho.getRefElementos()
        );
        quadradoTeste = new Desenho(
            2,
            verticesQuadradoBranco,
            Desenho.getRefElementos()
        );
        
        atividade.requisitarPermissao( Manifest.permission.BLUETOOTH );
        bluetooth = new Bluetooth( atividade, cameraTraseira.getCamera().getImagem() );
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
        cameraTraseira.atualizarImagemDetector( 3 );
        
        tela.clear();
        
        if ( bluetooth.exibirQuadradoCalibracao() ) {
            float[] parametro = bluetooth.getParametrosQuadradoCalibracao();
            float
                escalaX = parametro[2],
                escalaY = parametro[3],
                translacaoX = escalaX + ( parametro[0] - 1 ),
                translacaoY = -escalaY + ( parametro[1] - 1 );
            
            quadradoCalibracao.setEscala( escalaX, escalaY, 0 );
            quadradoCalibracao.setRotacao(
                (float) Math.toRadians( parametro[4] - 90 ),
                (float) Math.toRadians( parametro[5] - 90 ),
                (float) Math.toRadians( parametro[6] - 90 )
            );
            quadradoCalibracao.setTranslacao( translacaoX, translacaoY, -1 );
            
            tela.draw( tela.getLargura() / 2, tela.getAltura(), quadradoCalibracao );
        }
        
        if ( bluetooth.exibirQuadradoTeste() ) {
            float[] parametro = bluetooth.getParametrosQuadradoTeste();
            
            quadradoTeste.setEscala( parametro[7] - 1000, parametro[8] - 1000, 1 );
            quadradoTeste.setTranslacao(
                parametro[9] - 1000, parametro[10] - 1000, parametro[11] - 1000
            );
            
            quadradoTeste.setProjecao( parametro[0] - 1000, parametro[1] - 1000 );
            quadradoTeste.setTranslacaoTela(
                parametro[2] - 1000, parametro[3] - 1000, 
                parametro[12] - 1000, parametro[13] - 1000
            );
            quadradoTeste.setRotacaoTela(
                (float) Math.toRadians( parametro[4] - 90 - 1000 ),
                (float) Math.toRadians( parametro[5] - 90 - 1000 ),
                (float) Math.toRadians( parametro[6] - 90 - 1000 )
            );
            
            tela.draw( tela.getLargura() / 2, tela.getAltura(), quadradoTeste );
        }
    }
    
    @Override
    public void close() {
        bluetooth.close();
        cameraTraseira.close();
    }
}
package com.unesp.calibracao_haori;

import android.Manifest;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;

import androidx.camera.core.CameraSelector;

import com.unesp.calibracao_haori.es.Bluetooth;
import com.unesp.calibracao_haori.es.camera.Camera;
import com.unesp.calibracao_haori.es.camera.CameraLocal;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderizadorOpenGL implements GLSurfaceView.Renderer, AutoCloseable {
    private final MainActivity activity;
    
    public RenderizadorOpenGL( MainActivity activity ) {
        super();
        
        this.activity = activity;
    }
    
    private final float[]
/*        refTriangulo = {
            // Coordenadas          Cor
            0.0f,   0.622008459f,   1.0f, 0.0f, 0.0f,
            -0.5f,  -0.311004243f,  0.0f, 1.0f, 0.0f,
            0.5f,   -0.311004243f,  0.0f, 0.0f, 1.0f
        },*/
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
    
    final int[] texturas = new int[3];
    private void setTexParams() {
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR
        );
    }
    
    private Objeto[] objeto;
    
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
        
        // Texturas
        GLES32.glGenTextures( texturas.length, texturas, 0 );
        for( int textura : texturas ) {
            GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, textura );
            setTexParams();
        }
        
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, GLES32.GL_R8,
            camera.getLargImg(), camera.getAltImg(), 0,
            GLES32.GL_RED, GLES32.GL_UNSIGNED_BYTE, null
        );
        
        /*Resources resources = activity.getResources();
        new ImagemOpenGL(
            BitmapFactory.decodeResource( resources, R.drawable.cachorrinho ),
            texturas[1]
        ).carregar();
        new ImagemOpenGL(
            BitmapFactory.decodeResource( resources, R.drawable.gatinho ),
            texturas[2]
        ).carregar();*/
        
        /*float[] copiaTri = refTriangulo.clone();
        
        objetos[0] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );
        
        copiaTri[1] = 0.3f;     copiaTri[2] = 0.0f;
        copiaTri[8] = 0.0f;
        copiaTri[14] = 0.0f;
        objetos[1] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );
        
        copiaTri[1] = -0.1f;    copiaTri[2] = 1.0f;
        copiaTri[8] = 1.0f;
        copiaTri[14] = 1.0f;
        objetos[2] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );
        
        for ( int i = 0; i < 3; i ++ ) {
            objetos[i].setEscala( 0.5f, 0.5f, 0.0f );
            objetos[i].setTrans( 0.5f, 0.5f, 0.0f );
        }
        
        objetos[3] = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturas[0]
        );
        objetos[3].setTrans( -0.5f, -0.5f, 0.0f );
        
        objetos[4] = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturas[1]
        );
        objetos[4].setTrans( -0.5f, 0.5f, 0.0f );
        
        for ( int i = 3; i < 5; i ++ )
            objetos[i].setEscala( 0.25f, 0.25f, 0.0f );*/
        
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        objeto = new Objeto[1];
        objeto[0] = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturas[0], true
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
        // Copia a imagem atual da câmera traseira para a textura 0
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        GLES32.glTexSubImage2D(
            GLES32.GL_TEXTURE_2D, 0, 0, 0,
            camera.getLargImg(), camera.getAltImg(), GLES32.GL_RED,
            GLES32.GL_UNSIGNED_BYTE, camera.getImagem()
        );
        
        // Desenha no framebuffer intermediário
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, frameBuffer.getId() );
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
        GLES32.glViewport( 0, 0, frameBuffer.getLargura(), frameBuffer.getAltura() );
        for ( Objeto obj : objeto)
            obj.draw();
        
        // Desenha na tela
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
        frameBuffer.exibir( larguraTela, alturaTela, 2, 1 );
    }
    
    @Override
    public void close() {
        ProgramaOpenGL.close();
        GLES32.glDeleteTextures( texturas.length, texturas, 0 );
        frameBuffer.close();
        bluetooth.close();
        camera.close();
    }
}
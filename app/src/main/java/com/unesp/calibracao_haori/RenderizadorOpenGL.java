package com.unesp.calibracao_haori;

/*import android.graphics.Bitmap;
import android.graphics.BitmapFactory;*/
import android.Manifest;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class RenderizadorOpenGL implements GLSurfaceView.Renderer, ImageAnalysis.Analyzer {
    private final MainActivity activity;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    
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
    
    private final int larImgCam = 320, altImgCam = 240, tamImagem = larImgCam * altImgCam;
    private final ByteBuffer imagemCamera = ByteBuffer.allocateDirect( tamImagem );
    private final ByteBuffer imagemCameraVis = imagemCamera.asReadOnlyBuffer();
    
    Bluetooth bt;
    
    //    private boolean inicilizado = false;
    
    @Override
    public void analyze( @NonNull ImageProxy imagem ) {
//        final String TAG = "Câmera: Imagem";
        
        final ByteBuffer bb = imagem.getPlanes()[0].getBuffer();
        
        bb.rewind();
        imagemCamera.rewind();
        imagemCamera.put( bb );
        
        /*if ( ! inicilizado ) {
            inicilizado = true;
            
            Log.i( TAG, "Formato: " + imagem.getFormat() );
            Log.i( TAG, "Largura: " + imagem.getWidth() );
            Log.i( TAG, "Altura: " + imagem.getHeight() );
            
            final Rect ret = imagem.getCropRect();
            
            Log.i( TAG, "Largura do corte: " + ret.width() );
            Log.i( TAG, "Altura do corte: " + ret.height() );
            
            Log.i( TAG, "Rotação: " + imagem.getImageInfo().getRotationDegrees() );
            
            final ImageProxy.PlaneProxy lum = imagem.getPlanes()[0];
            
            Log.i( TAG, "Tamanho dos píxeis: " + lum.getPixelStride() );
            Log.i( TAG, "Tamanho das linhas: " + lum.getRowStride() );
            
            // ByteBuffer
            Log.i( TAG, "Tamanho do Buffer: " + bb.capacity() );
            Log.i( TAG, "Limite do Buffer: " + bb.limit() );
        }*/
        
        imagem.close();
        
        /*byte[] valoresImagem = new byte[ imagemCamera.limit() ];
        imagemCamera.rewind();
        imagemCamera.get( valoresImagem );
        
        int media = 0;
        for ( byte valor: valoresImagem )
            media += Byte.toUnsignedInt( valor );
        
        Log.i( TAG, "Luminosidade média: " + ( (float) media / valoresImagem.length ) );*/
    }
    
    final int numLinhas = 2, numColunas = 4, numLinhasM1 = numLinhas - 1;
    
    final int[] fbo = new int[1];
    final int[] rbo = new int[numLinhas * numColunas];
    final int[] drawBuffers = new int[rbo.length];
    
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
    
    // Objetos
    private final Objeto[] objetos = new Objeto[1];
    
    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        // Câmera
        activity.requisitarPermissao( Manifest.permission.CAMERA );
        ImageAnalysis analisador = new ImageAnalysis.Builder()
            .setTargetResolution( new Size( larImgCam, altImgCam ) )
            .setBackpressureStrategy( ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST )
            .build();
        analisador.setAnalyzer( cameraExecutor, this );
        CameraSelector camera =  CameraSelector.DEFAULT_BACK_CAMERA;
        ListenableFuture<ProcessCameraProvider> listenable =
            ProcessCameraProvider.getInstance( activity );
        listenable.addListener(
            () -> {
                try {
                    listenable.get().bindToLifecycle( activity, camera, analisador );
                } catch ( ExecutionException | InterruptedException e ) {
                    e.printStackTrace();
                }
            },
            ContextCompat.getMainExecutor( activity )
        );
        
        // Bluetooth
        activity.requisitarPermissao( Manifest.permission.BLUETOOTH );
        try {
            bt = new Bluetooth( activity, tamImagem, imagemCamera );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        bt.abrirServidor();
        
        // Framebuffer
        GLES32.glGenFramebuffers( fbo.length, fbo, 0 );
        GLES32.glGenRenderbuffers( rbo.length, rbo, 0 );
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, fbo[0] );
        for ( int i = 0; i < drawBuffers.length; i++ )
            drawBuffers[i] = GLES32.GL_COLOR_ATTACHMENT0 + i;
        GLES32.glDrawBuffers( drawBuffers.length, drawBuffers, 0  );
        
        GLES32.glClearColor( 0.0f, 0.0f, 0.0f, 1.0f );
        
        // Texturas
        GLES32.glGenTextures( texturas.length, texturas, 0 );
        for( int textura : texturas ) {
            GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, textura );
            setTexParams();
        }
        
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, GLES32.GL_R8,
            larImgCam, altImgCam, 0,
            GLES32.GL_RED, GLES32.GL_UNSIGNED_BYTE, null
        );

        Resources resources = activity.getResources();
        new ImagemOpenGL(
            BitmapFactory.decodeResource( resources, R.drawable.cachorrinho ),
            texturas[1]
        ).carregar();
        new ImagemOpenGL(
            BitmapFactory.decodeResource( resources, R.drawable.gatinho ),
            texturas[2]
        ).carregar();
        
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
        
        objetos[0] = new Objeto(
            GLES32.GL_TRIANGLES, 2, 2,
            refQuad, refElementos, texturas[1], true
        );
    }
    
    private int viewWidth;
    private int viewHeight;
    
    @Override
    public void onSurfaceChanged( GL10 unused, int width, int height ) {
        int newWidth = width / numColunas;
        int newHeight = height / numLinhas;
        
        if ( viewWidth != newWidth || viewHeight != newHeight ) {
            viewWidth = newWidth;
            viewHeight = newHeight;
            
            GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, fbo[0] );
            
            for ( int i = 0; i < rbo.length; i++ ) {
                GLES32.glBindRenderbuffer( GLES32.GL_RENDERBUFFER, rbo[i] );
                GLES32.glRenderbufferStorage(
                    GLES32.GL_RENDERBUFFER, GLES32.GL_RGB8,
                    viewWidth, viewHeight
                );
                GLES32.glFramebufferRenderbuffer(
                    GLES32.GL_DRAW_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0 + i,
                    GLES32.GL_RENDERBUFFER, rbo[i]
                );
            }
            
            GLES32.glViewport( 0, 0, viewWidth, viewHeight );
        }
    }
    
    @Override
    public void onDrawFrame( GL10 unused ) {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        imagemCameraVis.rewind();
        GLES32.glTexSubImage2D(
            GLES32.GL_TEXTURE_2D, 0, 0, 0,
            larImgCam,  altImgCam, GLES32.GL_RED,
            GLES32.GL_UNSIGNED_BYTE, imagemCameraVis
        );
        
        GLES32.glBindFramebuffer( GLES32.GL_FRAMEBUFFER, fbo[0] );
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
        
        for ( Objeto obj: objetos )
            obj.draw();
        
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
        for ( int i = 0; i < drawBuffers.length; i++ ) {
            int coluna = i % numColunas;
            int linha = numLinhasM1 - ( i / numColunas );
            
            GLES32.glReadBuffer( GLES32.GL_COLOR_ATTACHMENT0 + i );
            GLES32.glBlitFramebuffer(
                0,0, viewWidth, viewHeight,
                coluna * viewWidth, linha * viewHeight,
                ( coluna + 1 ) * viewWidth, ( linha  + 1 ) * viewHeight,
                GLES32.GL_COLOR_BUFFER_BIT, GLES32.GL_NEAREST
            );
        }
    }
    
    public void liberarRecursos() {
        ProgramaOpenGL.liberarRecursos();
        
        GLES32.glDeleteTextures( texturas.length, texturas, 0 );
        GLES32.glDeleteRenderbuffers( rbo.length, rbo, 0 );
        GLES32.glDeleteFramebuffers( fbo.length, fbo, 0 );
        
        cameraExecutor.shutdown();
    }
}
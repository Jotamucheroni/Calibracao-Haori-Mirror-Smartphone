package com.example.opengles_exemplo;

import android.graphics.Bitmap;
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
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer, ImageAnalysis.Analyzer {
    private final MainActivity activity;
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();

    public MyGLRenderer( MainActivity activity ) {
        super();

        this.activity = activity;
    }

    private final float[] /*refTriangulo = {
            // Coordenadas          // Cor
            0.0f,    0.622008459f,   1.0f, 0.0f, 0.0f,
           -0.5f,   -0.311004243f,   0.0f, 1.0f, 0.0f,
            0.5f,   -0.311004243f,   0.0f, 0.0f, 1.0f
    },*/
                        refQuad = {
           -1.0f,  1.0f,      0.0f, 0.0f,
           -1.0f, -1.0f,      0.0f, 1.0f,
            1.0f, -1.0f,      1.0f, 1.0f,
            1.0f,  1.0f,      1.0f, 0.0f
    };


    private final int[] refElementos = {
            0, 1, 2,
            2, 3, 0
    };

    private final int larImgCam = 320,
                      altImgCam = 240,
                      tamImagem = larImgCam * altImgCam;
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

/*        if ( ! inicilizado ) {
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

    final int numLinhas = 2, numColunas = 4,
              numLinhasM1 = numLinhas - 1;

    final int[] fbo = new int[1];
    final int[] rbo = new int[numLinhas * numColunas];
    final int[] drawBuffers = new int[rbo.length];

    final int[] texturas = new int[3];

    private void setTexParams() {
        GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE );
        GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE );
        GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR );
        GLES32.glTexParameteri( GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR );
    }

    // Objetos
    private final Objeto[] objetos = new Objeto[1];

    @Override
    public void onSurfaceCreated( GL10 unused, EGLConfig config ) {
        //printGLConfig(new int[]{  } );

        activity.requisitarPermissoes();

        try { bt = new Bluetooth( activity, tamImagem, imagemCamera ); }
        catch ( Exception e ) { e.printStackTrace(); }
        bt.abrirServidor();

        ImageAnalysis analisador = new ImageAnalysis.Builder()
                .setTargetResolution( new Size( larImgCam, altImgCam ) )
                .setBackpressureStrategy( ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST )
                .build();

        analisador.setAnalyzer( cameraExecutor, this );

        CameraSelector camera =  CameraSelector.DEFAULT_BACK_CAMERA;

        ListenableFuture<ProcessCameraProvider> listenable =
                ProcessCameraProvider.getInstance( activity );

        listenable.addListener( () -> {
            try {
                listenable
                        .get()
                        .bindToLifecycle( activity, camera, analisador );
            } catch ( ExecutionException | InterruptedException e ) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor( activity ) );

        GLES32.glGenFramebuffers( fbo.length, fbo, 0 );
        GLES32.glGenRenderbuffers( rbo.length, rbo, 0 );
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, fbo[0] );
        for ( int i = 0; i < drawBuffers.length; i++ )
            drawBuffers[i] = GLES32.GL_COLOR_ATTACHMENT0 + i;
        GLES32.glDrawBuffers( drawBuffers.length, drawBuffers, 0  );

        GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f );

        GLES32.glGenTextures( texturas.length, texturas, 0 );

        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        setTexParams();

        GLES32.glTexImage2D( GLES32.GL_TEXTURE_2D, 0, GLES32.GL_R8,
                larImgCam,  altImgCam, 0,
                GLES32.GL_RED, GLES32.GL_UNSIGNED_BYTE, null );

        int[] imageTex = new int[]{ R.drawable.cachorrinho, R.drawable.gatinho };
        for (int i = 1; i < texturas.length; i++) {
            GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[i] );
            setTexParams();

            Bitmap bitmap = BitmapFactory.decodeResource( activity.getResources(), imageTex[i - 1] );
            ByteBuffer bb = ByteBuffer.allocateDirect( bitmap.getByteCount() );
            bb.order( ByteOrder.nativeOrder() );
            bitmap.copyPixelsToBuffer( bb );
            bb.position(0 );

            GLES32.glTexImage2D(GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA8,
                    bitmap.getWidth(), bitmap.getHeight(), 0,
                    GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, bb);
        }

        /*float[] copiaTri = refTriangulo.clone();

        objetos[0] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );

        copiaTri[1] = 0.3f;   copiaTri[2] = 0.0f;
        copiaTri[8] = 0.0f;
        copiaTri[14] = 0.0f;
        objetos[1] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );


        copiaTri[1] = -0.1f;    copiaTri[2] = 1.0f;
        copiaTri[8] = 1.0f;
        copiaTri[14] = 1.0f;
        objetos[2] = new Objeto( GLES32.GL_TRIANGLES, 2, 3, copiaTri );

        for ( int i = 0; i < 3; i ++ ) {
            objetos[i].setEscala(0.5f, 0.5f, 0.0f );
            objetos[i].setTrans(0.5f, 0.5f, 0.0f );
        }

        objetos[3] = new Objeto( GLES32.GL_TRIANGLES, 2, 2,
                                 refQuad, refElementos, texturas[0] );
        objetos[3].setTrans(-0.5f, -0.5f, 0.0f );

        objetos[4] = new Objeto( GLES32.GL_TRIANGLES, 2, 2,
                                 refQuad, refElementos, texturas[1] );
        objetos[4].setTrans(-0.5f, 0.5f, 0.0f );

        for ( int i = 3; i < 5; i ++ )
            objetos[i].setEscala(0.25f, 0.25f, 0.0f );*/

        objetos[0] = new Objeto( GLES32.GL_TRIANGLES, 2, 2,
                                 refQuad, refElementos, texturas[0], true );
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
                GLES32.glRenderbufferStorage( GLES32.GL_RENDERBUFFER, GLES32.GL_RGB8, viewWidth, viewHeight );
                GLES32.glFramebufferRenderbuffer( GLES32.GL_DRAW_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0 + i, GLES32.GL_RENDERBUFFER, rbo[i] );
            }

            GLES32.glViewport(0, 0, viewWidth, viewHeight );
        }
    }

    @Override
    public void onDrawFrame( GL10 unused ) {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, texturas[0] );
        imagemCameraVis.rewind();
        GLES32.glTexSubImage2D( GLES32.GL_TEXTURE_2D, 0, 0, 0,
                larImgCam,  altImgCam, GLES32.GL_RED,
                GLES32.GL_UNSIGNED_BYTE, imagemCameraVis );

        GLES32.glBindFramebuffer( GLES32.GL_FRAMEBUFFER, fbo[0] );
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );

        for ( Objeto obj: objetos )
            obj.draw();

        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
        for ( int i = 0; i < drawBuffers.length; i++ ) {
            int coluna = i % numColunas;
            int linha = numLinhasM1 - ( i / numColunas );

            GLES32.glReadBuffer( GLES32.GL_COLOR_ATTACHMENT0 + i );
            GLES32.glBlitFramebuffer(0,0, viewWidth, viewHeight,
                    coluna * viewWidth, linha * viewHeight, ( coluna + 1 ) * viewWidth, ( linha  + 1 ) * viewHeight,
                    GLES32.GL_COLOR_BUFFER_BIT, GLES32.GL_NEAREST );
        }
    }

    public static void printGLConfig( @NonNull int[] configs ) {
        final int[] valConfig =  new int[1];

        for (int config : configs) {
            GLES32.glGetIntegerv(config, valConfig, 0);
            Log.i("OpenGL Config.", Integer.toString(valConfig[0]));
        }
    }

    public static void printGLConfig( @NonNull int[] configs, @NonNull int[] configNumComp ) {
        final int[] valConfig =  new int[ Arrays.stream(configNumComp).max().getAsInt() ];

        for ( int i = 0; i < configs.length; i++ ) {
            GLES32.glGetIntegerv(configs[i], valConfig, 0 );

            StringBuilder info = new StringBuilder(Integer.toString(valConfig[0]));
            for ( int j = 1; j < configNumComp[i]; j++ )
                info.append(", ").append(valConfig[j]);
            Log.i("OpenGL Config.", String.valueOf(info));
        }
    }

    public static void printGLConfig( @NonNull int[] configs, @NonNull String[] configNames ) {
        final int[] valConfig =  new int[1];

        for ( int i = 0; i < configs.length; i++ ) {
            GLES32.glGetIntegerv( configs[i], valConfig, 0 );
            Log.i("OpenGL Config.", configNames[i] + ": " + valConfig[0]);
        }
    }

    public static void printBufferConfig( int tipo, int buffer ) {
        final int[] bufferConfig =  new int[10];

        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32. GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, bufferConfig, 0 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32. GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, bufferConfig, 1 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE, bufferConfig, 2 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE, bufferConfig, 3 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE, bufferConfig, 4 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE, bufferConfig, 5 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE, bufferConfig, 6 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE, bufferConfig, 7 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE, bufferConfig, 8 );
        GLES32.glGetFramebufferAttachmentParameteriv( tipo, buffer, GLES32. GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING, bufferConfig, 9 );

        String tipoAlvo;
        switch ( bufferConfig[0] ) {
            case GLES32.GL_NONE:
                tipoAlvo = "nenhum";
                break;
            case GLES32.GL_FRAMEBUFFER_DEFAULT:
                tipoAlvo = "default framebuffer";
                break;
            case GLES32.GL_TEXTURE:
                tipoAlvo = "textura";
                break;
            case GLES32.GL_RENDERBUFFER:
                tipoAlvo = "renderbuffer";
                break;
            default:
                tipoAlvo = "indeterminado";
        }
        Log.i("Buffer Config - Tipo", tipoAlvo );

        Log.i( "Buffer Config - Nome", Integer.toString( bufferConfig[1] ) );

        Log.i("Buffer Config - Red", bufferConfig[2] + " bits" );
        Log.i("Buffer Config - Green", bufferConfig[3] + " bits" );
        Log.i("Buffer Config - Blue", bufferConfig[4] + " bits" );
        Log.i("Buffer Config - Alpha", bufferConfig[5] + " bits" );
        Log.i("Buffer Config - Depth", bufferConfig[6] + " bits" );
        Log.i("Buffer Config - Stencil", bufferConfig[7] + " bits" );

        String formatoInterno;
        switch ( bufferConfig[8] ) {
            case GLES32.GL_FLOAT:
                formatoInterno = "float";
                break;
            case GLES32.GL_INT:
                formatoInterno = "int";
                break;
            case GLES32.GL_UNSIGNED_INT:
                formatoInterno = "unsigned int";
                break;
            case GLES32.GL_SIGNED_NORMALIZED:
                formatoInterno = "signed normalized";
                break;
            case GLES32.GL_UNSIGNED_NORMALIZED:
                formatoInterno = "unsigned normalized";
                break;
            default:
                formatoInterno = "indeterminado";
        }
        Log.i("Buffer Config - Formato interno", formatoInterno );

        String espacoCor;
        switch ( bufferConfig[9] ) {
            case GLES32.GL_LINEAR:
                espacoCor = "linear";
                break;
            case GLES32.GL_SRGB:
                espacoCor = "sRGB";
                break;
            default:
                espacoCor = "indeterminado";
        }
        Log.i("Buffer Config - Espaço de cores", espacoCor );
    }

    public static int loadShader( int type, String shaderCode ) {
        int shader = GLES32.glCreateShader( type );

        GLES32.glShaderSource( shader, shaderCode );
        GLES32.glCompileShader( shader );
        int[] compilado = new int[1];
        GLES32.glGetShaderiv( shader, GLES32.GL_COMPILE_STATUS, compilado, 0 );

        // Verifica se houve erro de compilação
        if ( compilado[0] == 0 ) {
            // Imprime o código onde está localizado o erro
            System.out.println( "Um programa não pôde ser compilado:\n" + "Código fonte:" );
            int i = 1;
            for( String linha: shaderCode.split( "\n" ) ) {
                System.out.println( i + "\t" + linha );
                i++;
            }
        }

        return shader;
    }

    public static int gerarPrograma( @NonNull String vertexShaderCode, @NonNull String fragmentShaderCode ) {
        int program = GLES32.glCreateProgram();

        int vertexShader = loadShader( GLES32.GL_VERTEX_SHADER, vertexShaderCode );
        int fragmentShader = loadShader( GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode );

        GLES32.glAttachShader( program, vertexShader );
        GLES32.glAttachShader( program, fragmentShader );
        GLES32.glLinkProgram( program );

        return program;
    }

    private static final String vertexShaderCabRef =
                      "#version 320 es\n"

                    + "uniform mat4 escala;\n"
                    + "uniform mat4 rotX;\n"
                    + "uniform mat4 rotY;\n"
                    + "uniform mat4 rotZ;\n"
                    + "uniform mat4 trans;\n"

                    + "in vec4 pos;\n"
                    + "in vec4 cor;\n"
                    + "in vec2 tex;\n"
                    ;

    private static final String vertexShaderMainRef =
                      "void main() {\n"
                    + "  gl_Position = trans * rotZ * rotY * rotX * escala * pos;\n"
                    ;

    private static final String fragmentCabRef =
                      "#version 320 es\n"

                    + "precision mediump float;\n"

                    + "layout(binding = 0) uniform sampler2D imagem;\n"

/*                    + "vec4 getInv( in vec4 corOrig ) {\n"

                    + "    return vec4( 1.0 ) - corOrig;\n"
                    + "}\n"

                    + "vec4[3][3] getInv( in vec4 janela[3][3] ) {\n"

                    + "    for ( int i = 0; i < 9; i++ )\n"
                    + "        janela[ i / 3 ][ i % 3 ] = getInv( janela[ i / 3 ][ i % 3 ] );\n"

                    + "    return janela;\n"
                    + "}\n"

                    + "vec4 getCinzaMed( in vec4 corOrig ) {\n"
                    + "    float cinza = ( corOrig.r + corOrig.g + corOrig.b ) / 3.0;\n"

                    + "    return vec4( cinza, cinza, cinza, corOrig.a );\n"
                    + "}\n"

                    + "vec4[3][3] getCinzaMed( in vec4 janela[3][3] ) {\n"

                    + "    for ( int i = 0; i < 9; i++ )\n"
                    + "        janela[ i / 3 ][ i % 3 ] = getCinzaMed( janela[ i / 3 ][ i % 3 ] );\n"

                    + "    return janela;\n"
                    + "}\n"

                    + "vec4 getCinzaPond( in vec4 corOrig ) {\n"
                    + "    float cinza = 0.2126 * corOrig.r + 0.7152 * corOrig.g + 0.0722 * corOrig.b;\n"

                    + "    return vec4( cinza, cinza, cinza, corOrig.a );\n"
                    + "}\n"

                    + "vec4[3][3] getCinzaPond( in vec4 janela[3][3] ) {\n"

                    + "    for ( int i = 0; i < 9; i++ )\n"
                    + "        janela[ i / 3 ][ i % 3 ] = getCinzaPond( janela[ i / 3 ][ i % 3 ] );\n"

                    + "    return janela;\n"
                    + "}\n"

                    + "vec4 getBor( in vec4 janela[3][3] ) {\n"
                    + "    vec4 soma = vec4( 0.0 );\n"

                    + "    for ( int i = 0; i < 9; i++ )\n"
                    + "        soma += janela[ i / 3 ][ i % 3 ];\n"

                    + "    return soma / 9.0;\n"
                    + "}\n"

                    + "vec4 getSobel( in vec4 janela[3][3] ) {\n"
                    + "    vec4 dx = - ( janela[0][0] + 2.0 * janela[1][0] + janela[2][0] ) + ( janela[0][2] + 2.0 * janela[1][2] + janela[2][2] );\n"
                    + "    vec4 dy =   ( janela[0][0] + 2.0 * janela[0][1] + janela[0][2] ) - ( janela[2][0] + 2.0 * janela[2][1] + janela[2][2] );\n"

                    + "    return sqrt( dx * dx + dy * dy );\n"
                    + "}\n"*/
                    ;

    private static final String fragmentCabMainRef =
                      "void main() {\n"
                    ;

    private static final String corFragRef =
                      "saida[0] = saida[4] = corFrag;\n"
                    + "saida[1] = saida[5] = getInv( saida[0] );\n"
                    + "saida[2] = saida[6] = getCinzaMed( saida[0] );\n"
                    + "saida[3] = saida[7] = getCinzaPond( saida[0] );\n"
            ;

    private static final int numSaidas = 8;

    private static final int[][][] programas = new int[][][] { { { 0, 0 }, { 0, 0 } }, { { 0, 0 }, { 0, 0 } } };

    public static int gerarPrograma( int numCompCor, int numCompTex, boolean texPb ) {
        int cor = (numCompCor > 0) ? 1 : 0, tex = (numCompTex > 0) ? 1 : 0, pb = texPb ? 1 : 0;

        if ( programas[cor][tex][pb] != 0 )
            return programas[cor][tex][pb];

        StringBuilder vertexShaderCode = new StringBuilder( vertexShaderCabRef );
        StringBuilder fragmentShaderCode = new StringBuilder( fragmentCabRef );

        if ( numCompCor > 0 ) {
            vertexShaderCode.append( "out vec4 corFrag;\n" );
            fragmentShaderCode.append( "in vec4 corFrag;\n" );
        }
        if ( numCompTex > 0 ) {
            vertexShaderCode.append( "out vec2 texFrag;\n" );
            fragmentShaderCode.append( "in vec2 texFrag;\n" );
        }

        vertexShaderCode.append( vertexShaderMainRef );
        fragmentShaderCode.append( "out vec4 saida[" ).append( numSaidas ).append( "];\n" ).append( fragmentCabMainRef );

        if ( ! ( numCompCor > 0 || numCompTex > 0 ) ) {
            fragmentShaderCode.append( "vec4 corFrag = vec4( 1.0, 1.0, 1.0, 1.0 );\n" );
            fragmentShaderCode.append( corFragRef );
        }
        else {
            if (numCompCor > 0) {
                vertexShaderCode.append( "corFrag = cor;\n" );

                if ( ! ( numCompTex > 0 ) )
                    fragmentShaderCode.append( corFragRef );
            }
            if (numCompTex > 0) {
                vertexShaderCode.append( "texFrag = tex;\n" );
                fragmentShaderCode.append(
                      "ivec2 tamanho = textureSize( imagem, 0 );\n"

                    + "vec2 dist = vec2( 1.0 / float( tamanho.x ), 1.0 / float( tamanho.y ) );\n"

                    + "float gx[3][3] = float[3][3]( float[3]( -1.0,  0.0,  1.0 ), float[3]( -2.0,  0.0,  2.0 ), float[3]( -1.0,  0.0,  1.0 ) );\n"
                    + "float gy[3][3] = float[3][3]( float[3](  1.0,  2.0,  1.0 ), float[3](  0.0,  0.0,  0.0 ), float[3]( -1.0, -2.0, -1.0 ) );\n"

                    + "vec4 janela[3][3];\n"

                    + "vec4 dx = vec4( 0.0 );\n"
                    + "vec4 dy = vec4( 0.0 );\n"

                    + "for ( int y = -1; y <= 1; y++ )\n"
                    + "    for ( int x = -1; x <= 1; x++ ) {\n"
                    + "         int i = y + 1, j = x + 1;\n"
                    + "         vec4 corTex = texture( imagem, vec2( texFrag.x + float(x) * dist.x, texFrag.y + float(y) * dist.y ) );\n"
                );

                if ( pb > 0 ) {
                    fragmentShaderCode.append(
                          "     corTex.b = corTex.g = corTex.r;\n"
                    );
                }

                if (numCompCor > 0) {
                        fragmentShaderCode.append(
                          "     janela[i][j] = corTex = 0.5 * corFrag + 0.5 * corTex;\n"
                        );
                }
                else {
                    fragmentShaderCode.append(
                          "     janela[i][j] = corTex;\n"
                    );
                }

                fragmentShaderCode.append(
                      "         dx += gx[i][j] * corTex;\n"
                    + "         dy += gy[i][j] * corTex;\n"
                    + "}\n"

                    + "vec4 sobel = sqrt( dx * dx + dy * dy );\n"

                    + "saida[0] = janela[1][1];\n"
                    + "saida[1] = sobel;\n"
                    + "saida[2] = ( sobel.r > 0.5 ) ? sobel : vec4( 0.0 );\n"
                    + "saida[3] = ( sobel.r > 0.7 ) ? sobel : vec4( 0.0 );\n"
                    + "saida[4] = ( sobel.r > 0.9 ) ? sobel : vec4( 0.0 );\n"
                    + "saida[5] = ( sobel.r > 1.1 ) ? sobel : vec4( 0.0 );\n"
                    + "saida[6] = ( sobel.r > 1.3 ) ? sobel : vec4( 0.0 );\n"
                    + "saida[7] = ( sobel.r > 1.5 ) ? sobel : vec4( 0.0 );\n"
                );
            }
        }

        vertexShaderCode.append( "}" );
        fragmentShaderCode.append( "}" );

        programas[cor][tex][pb] = gerarPrograma( vertexShaderCode.toString(), fragmentShaderCode.toString() );
        return programas[cor][tex][pb];
    }

    public void liberarRecursos() {
        for ( int[][] matProg : programas )
            for ( int[] vetProg : matProg )
                for ( int programa: vetProg )
                    GLES32.glDeleteProgram( programa );

        GLES32.glDeleteTextures( texturas.length, texturas, 0 );
        GLES32.glDeleteRenderbuffers( rbo.length, rbo, 0 );
        GLES32.glDeleteFramebuffers( fbo.length, fbo, 0 );

        cameraExecutor.shutdown();
        bt.encerrarPesquisa();
    }
}
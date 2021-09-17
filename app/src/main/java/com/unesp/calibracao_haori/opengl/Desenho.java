package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Desenho implements AutoCloseable {
    public static final int
        PONTOS = GLES32.GL_POINTS,
        LINHAS = GLES32.GL_LINES,
        TRIANGULOS = GLES32.GL_TRIANGLES;
    
    private static final int[] vetorModoDesenho = new int[]{
        PONTOS, LINHAS, TRIANGULOS
    };
    
    public static final int
        PADRAO = TRIANGULOS;
    
    private static final float[]
        refQuad = {
        //  Posição             Textura
           -1.0f,   1.0f,       0.0f,   0.0f,
           -1.0f,  -1.0f,       0.0f,   1.0f,
            1.0f,  -1.0f,       1.0f,   1.0f,
            1.0f,   1.0f,       1.0f,   0.0f
    };
    
    private static final int[] refElementos = { 0, 1, 2, 2, 3, 0 };
    
    public static float[] getRefQuad() {
        return refQuad.clone();
    }
    
    public static int[] getRefElementos() {
        return refElementos.clone();
    }
    
    private final int vertexBufferObject;
    private final int elementBufferObject;
    
    private final int vertexArrayObject;
    private final int programa;
    private final int numElementos;
    
    private final int
        ponteiroMatrizEscala,
        ponteiroMatrizRotX, ponteiroMatrizRotY, ponteiroMatrizRotZ,
        ponteiroMatrizTrans;
    
    private Textura textura;
    private int ponteiroParametroTextura;
    
    private int modoDesenho;
    
    public Desenho(
        int numCompPos, int numCompCor, int numCompTex,
        float[] vertices, int[] elementos, Textura textura,
        int modoDesenho
    ) {
        {
            final int[] leitorId = new int[2];
            
            GLES32.glGenBuffers( 2, leitorId, 0 );
            vertexBufferObject = leitorId[0];
            elementBufferObject = leitorId[1];
            
            GLES32.glGenVertexArrays( 1, leitorId, 0 );
            vertexArrayObject = leitorId[0];
        }
        
        programa = Programa.gerarPrograma(
            numCompCor > 0,
            numCompTex > 0,
            textura != null && textura.getMonocromatica()
        );
        
        GLES32.glBindVertexArray( vertexArrayObject );
        
        GLES32.glBindBuffer( GLES32.GL_ARRAY_BUFFER, vertexBufferObject );
        
        {
            final int tamanhoTotalVertices = vertices.length * Float.BYTES;
            
            GLES32.glBufferData(
                GLES32.GL_ARRAY_BUFFER, tamanhoTotalVertices,
                ByteBuffer
                    .allocateDirect( tamanhoTotalVertices )
                    .order( ByteOrder.nativeOrder() )
                    .asFloatBuffer()
                    .put( vertices )
                    .position( 0 ),
                GLES32.GL_STATIC_DRAW
            );
        }
        
        {
            final int[] ponteiroVertice = new int[]{
                GLES32.glGetAttribLocation( programa, "pos" ),
                GLES32.glGetAttribLocation( programa, "cor" ),
                GLES32.glGetAttribLocation( programa, "tex" )
            };
            final int[] numComp = new int[] { numCompPos, numCompCor, numCompTex };
            final int tamanhoVertice = ( numCompPos + numCompCor + numCompTex ) * Float.BYTES;
            int deslocamento = 0;
            
            for ( int i = 0; i < ponteiroVertice.length; i++ ) {
                GLES32.glEnableVertexAttribArray( ponteiroVertice[i] );
                GLES32.glVertexAttribPointer(
                    ponteiroVertice[i], numComp[i], GLES32.GL_FLOAT, false,
                    tamanhoVertice, deslocamento
                );
                deslocamento += numComp[i] * Float.BYTES;
            }
        }
        
        numElementos = elementos.length;
        GLES32.glBindBuffer( GLES32.GL_ELEMENT_ARRAY_BUFFER, elementBufferObject );
        
        {
            final int tamanhoElementos = numElementos * Integer.BYTES;
            
            GLES32.glBufferData(
                GLES32.GL_ELEMENT_ARRAY_BUFFER, tamanhoElementos,
                ByteBuffer
                    .allocateDirect( tamanhoElementos )
                    .order( ByteOrder.nativeOrder() )
                    .asIntBuffer()
                    .put( elementos )
                    .position( 0 ),
                GLES32.GL_STATIC_DRAW
            );
        }
        
        GLES32.glBindVertexArray( 0 );
        GLES32.glBindBuffer( GLES32.GL_ELEMENT_ARRAY_BUFFER, 0 );
        GLES32.glBindBuffer( GLES32.GL_ARRAY_BUFFER, 0 );
        
        ponteiroMatrizEscala = GLES32.glGetUniformLocation( programa, "escala" );
        ponteiroMatrizRotX = GLES32.glGetUniformLocation( programa, "rotX" );
        ponteiroMatrizRotY = GLES32.glGetUniformLocation( programa, "rotY" );
        ponteiroMatrizRotZ = GLES32.glGetUniformLocation( programa, "rotZ" );
        ponteiroMatrizTrans = GLES32.glGetUniformLocation( programa, "trans" );
        
        setTextura( textura );
        ponteiroParametroTextura = GLES32.glGetUniformLocation(
            programa, "parametroTextura" 
        );
        
        setModoDesenho( modoDesenho );
    }
    
    public Desenho(
        int numCompPos, int numCompCor, int numCompTex,
        float[] vertices, int[] elementos, Textura textura
    ) {
        this(
            numCompPos, numCompCor, numCompTex,
            vertices, elementos, textura,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor,
        float[] vertices, int[] elementos,
        int modoDesenho
    ) {
        this(
            numCompPos, numCompCor, 0,
            vertices, elementos, null,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor,
        float[] vertices, int[] elementos
    ) {
        this(
            numCompPos, numCompCor, 0,
            vertices, elementos, null,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor, int numCompTex,
        float[] vertices, Textura textura,
        int modoDesenho
    ) {
        this(
            numCompPos, numCompCor, numCompTex,
            vertices, getElementos( numCompPos, numCompCor, numCompTex, vertices ), textura,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor, int numCompTex,
        float[] vertices, Textura textura
    ) {
        this(
            numCompPos, numCompCor, numCompTex,
            vertices, getElementos( numCompPos, numCompCor, numCompTex, vertices ), textura,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor,
        float[] vertices,
        int modoDesenho
    ) {
        this(
            numCompPos, numCompCor, 0,
            vertices, getElementos( numCompPos, numCompCor, 0, vertices ), null,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos, int numCompCor,
        float[] vertices
    ) {
        this(
            numCompPos, numCompCor, 0,
            vertices, getElementos( numCompPos, numCompCor, 0, vertices ), null,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos, int numCompTex,
        float[] vertices, int[] elementos, Textura textura,
        int modoDesenho
    ) {
        this(
            numCompPos, 0, numCompTex,
            vertices, elementos, textura,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos, int numCompTex,
        float[] vertices, int[] elementos, Textura textura
    ) {
        this(
            numCompPos, 0, numCompTex,
            vertices, elementos, textura,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos,
        float[] vertices, int[] elementos,
        int modoDesenho
    ) {
        this(
            numCompPos, 0, 0,
            vertices, elementos, null,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos,
        float[] vertices, int[] elementos
    ) {
        this(
            numCompPos, 0, 0,
            vertices, elementos, null,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos, int numCompTex,
        float[] vertices, Textura textura,
        int modoDesenho
    ) {
        this(
            numCompPos, 0, numCompTex,
            vertices, getElementos( numCompPos, 0, numCompTex, vertices ), textura,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos, int numCompTex,
        float[] vertices, Textura textura
    ) {
        this(
            numCompPos, 0, numCompTex,
            vertices, getElementos( numCompPos, 0, numCompTex, vertices ), textura,
            PADRAO
        );
    }
    
    public Desenho(
        int numCompPos,
        float[] vertices,
        int modoDesenho
    ) {
        this(
            numCompPos, 0, 0,
            vertices, getElementos( numCompPos, 0, 0, vertices ), null,
            modoDesenho
        );
    }
    
    public Desenho(
        int numCompPos,
        float[] vertices
    ) {
        this(
            numCompPos, 0, 0,
            vertices, getElementos( numCompPos, 0, 0, vertices ), null,
            PADRAO
        );
    }
    
    private static int[] getElementos(
            int numCompPos, int numCompCor, int numCompTex,
            @NonNull float[] vertices
    ) {
        int numVertices = vertices.length / ( numCompPos + numCompCor + numCompTex );
        int[] elementos = new int[numVertices];
        
        for ( int i = 0; i < numVertices; i++ )
            elementos[i] = i;
        
        return elementos;
    }
    
    public void setModoDesenho( int modoDesenho ) {
        boolean modoDesenhoValido = false;
        
        for( int modo : vetorModoDesenho )
            if( modo == modoDesenho ) {
                modoDesenhoValido = true;
                break;
            }
        
        if ( modoDesenhoValido )
            this.modoDesenho = modoDesenho;
        else
            this.modoDesenho = PADRAO;
    }
    
    public void setTextura( Textura textura ) {
        this.textura = textura;
    }
    
    public int getModoDesenho() {
        return modoDesenho;
    }
    
    public Textura getTextura() {
        return textura;
    }
    
    private static final float[] matrizId = {
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    };
    
    private final float[]
        matrizEscala = matrizId.clone(),
        matrizRotX = matrizId.clone(),
        matrizRotY = matrizId.clone(),
        matrizRotZ = matrizId.clone(),
        matrizTrans = matrizId.clone();
    
    public void setEscala( float x, float y, float z ) {
        matrizEscala[0] = x;    //                0                    0           0
        /*                0*/   matrizEscala[5] = y;    //             0           0
        /*                0                       0*/   matrizEscala[10] = z;   // 0
        //                0                       0                        0       1
    }
    
    public void setRotacao( float x, float y, float z ) {
        double
            sinX = Math.sin( x ), cosX = Math.cos( x ),
            sinY = Math.sin( y ), cosY = Math.cos( y ),
            sinZ = Math.sin( z ), cosZ = Math.cos( z );
        
        // 1                                0                                  0        0
        /* 0*/  matrizRotX[5] = (float)  cosX;    matrizRotX[6] = (float)  -sinX;    // 0
        /* 0*/  matrizRotX[9] = (float)  sinX;    matrizRotX[10] = (float)  cosX;    // 0
        // 0                                0                                  0        1
        
        matrizRotY[0] = (float)  cosY;    /* 0*/  matrizRotY[2] = (float)   sinY;    // 0
        //                          0        1                                 0        0
        matrizRotY[8] = (float) -sinY;    /* 0*/  matrizRotY[10] = (float)  cosY;    // 0
        //                          0        0                                 0        1
        
        matrizRotZ[0] = (float)  cosZ;    matrizRotZ[1] = (float) -sinZ;    // 0    0
        matrizRotZ[4] = (float)  sinZ;    matrizRotZ[5] = (float)  cosZ;    // 0    0
        //                          0                                 0        1    0
        //                          0                                 0        0    1
    }
    
    public void setTranslacao( float x, float y, float z ) {
        /*                    1                    0                    0*/   matrizTrans[3] =  x;
        /*                    0                    1                    0*/   matrizTrans[7] =  y;
        /*                    0                    0                    1*/   matrizTrans[11] = z;
        //                    0                    0                    0                       1
    }
    
    public float[] getMatrizEscala() {
        return matrizEscala.clone();
    }
    
    public float[] getMatrizRotacaoX() {
        return matrizRotX.clone();
    }
    
    public float[] getMatrizRotacaoY() {
        return matrizRotY.clone();
    }
    
    public float[] getMatrizRotacaoZ() {
        return matrizRotZ.clone();
    }
    
    public float[] getMatrizTranslacao() {
        return matrizTrans.clone();
    }
    
    private final float[] parametroTextura = new float[Programa.MAXIMO_PARAMETROS_TEXTURA];
    
    private int getIndiceParametroValido( int indiceOriginal ) {
        if ( indiceOriginal < 0 )
            return 0;
        
        if ( indiceOriginal >= parametroTextura.length )
            return parametroTextura.length - 1;
        
        return indiceOriginal;
    }
    
    public void setParametroTextura( int indiceParametro, float valor ) {
        parametroTextura[getIndiceParametroValido( indiceParametro )] = valor;
    }
    
    public float getParametroTextura( int indiceParametro ) {
        return parametroTextura[getIndiceParametroValido( indiceParametro )];
    }
    
    public void draw() {
        if ( textura != null )
            textura.bind();
        
        GLES32.glUseProgram( programa );
        
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizEscala, 1, true, matrizEscala, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotX, 1, true, matrizRotX, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotY, 1, true, matrizRotY, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotZ, 1, true, matrizRotZ, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizTrans, 1, true, matrizTrans, 0
        );
        
        GLES32.glUniform1fv(
            ponteiroParametroTextura, parametroTextura.length, parametroTextura, 0
        );
        
        GLES32.glBindVertexArray( vertexArrayObject );
        GLES32.glDrawElements( modoDesenho, numElementos, GLES32.GL_UNSIGNED_INT, 0 );
        GLES32.glBindVertexArray( 0 );
        
        GLES32.glUseProgram( 0 );
        
        if ( textura != null )
            textura.unbind();
    }
    
    @Override
    public void close(){
        GLES32.glDeleteVertexArrays( 1, new int[]{ vertexArrayObject }, 0 );
        GLES32.glDeleteBuffers( 2, new int[]{ elementBufferObject, vertexBufferObject }, 0 );
    }
}
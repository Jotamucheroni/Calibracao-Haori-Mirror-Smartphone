package com.unesp.calibracao_haori;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Objeto {
    private final int modoDes;
    
    private final int textura;
    
    private final int numElementos;
    
    private final int[] vao = new int[1];
    
    private final int program;
    
    private final int
        pontMatrizEscala,
        pontMatrizRotX, pontMatrizRotY, pontMatrizRotZ,
        pontMatrizTrans;
    
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
    
    public Objeto(
        int modoDes, int numCompPos, int numCompCor, int numCompTex,
        @NonNull float[] vertices, @NonNull int[] elementos,
        int textura, boolean texPb
    ) {
        this.modoDes = modoDes;
        this.textura = textura;
        
        int numCompTotal = numCompPos + numCompCor + numCompTex;
        int tamVertice = numCompTotal * Float.BYTES;
        int tamVertices = vertices.length * Float.BYTES;
        numElementos = elementos.length;
        int tamElementos = numElementos * Integer.BYTES;
        
        final int[] vbo = new int[1];
        GLES32.glGenBuffers( vbo.length, vbo, 0 );
        GLES32.glBindBuffer( GLES32.GL_ARRAY_BUFFER, vbo[0] );
        
        ByteBuffer bb = ByteBuffer.allocateDirect( tamVertices );
        bb.order( ByteOrder.nativeOrder() );
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put( vertices );
        fb.position( 0 );
        GLES32.glBufferData( GLES32.GL_ARRAY_BUFFER, tamVertices, fb, GLES32.GL_STATIC_DRAW );
        
        program = RenderizadorOpenGL.gerarPrograma( numCompCor, numCompTex, texPb );
        
        pontMatrizEscala = GLES32.glGetUniformLocation( program, "escala" );
        pontMatrizRotX = GLES32.glGetUniformLocation( program, "rotX" );
        pontMatrizRotY = GLES32.glGetUniformLocation( program, "rotY" );
        pontMatrizRotZ = GLES32.glGetUniformLocation( program, "rotZ" );
        pontMatrizTrans = GLES32.glGetUniformLocation( program, "trans" );
        int pontPos = GLES32.glGetAttribLocation( program, "pos" );
        int pontCor = GLES32.glGetAttribLocation( program, "cor" );
        int pontTex = GLES32.glGetAttribLocation( program, "tex" );
        
        GLES32.glGenVertexArrays( vao.length, vao, 0 );
        GLES32.glBindVertexArray( vao[0] );
        
        GLES32.glBindBuffer( GLES32.GL_ARRAY_BUFFER, vbo[0] );
        
        int desl = 0;
        
        GLES32.glVertexAttribPointer( pontPos, numCompPos, GLES32.GL_FLOAT,false, tamVertice, 0 );
        desl += numCompPos * Float.BYTES;
        
        GLES32.glVertexAttribPointer( pontCor, numCompCor, GLES32.GL_FLOAT,false, tamVertice, desl );
        desl += numCompCor * Float.BYTES;
        
        GLES32.glVertexAttribPointer( pontTex, numCompTex, GLES32.GL_FLOAT,false, tamVertice, desl );
        
        GLES32.glEnableVertexAttribArray( pontPos );
        GLES32.glEnableVertexAttribArray( pontCor );
        GLES32.glEnableVertexAttribArray( pontTex );
        
        final int[] ebo = new int[1];
        
        GLES32.glGenBuffers( ebo.length, ebo, 0 );
        GLES32.glBindBuffer( GLES32.GL_ELEMENT_ARRAY_BUFFER, ebo[0] );
        bb = ByteBuffer.allocateDirect( tamElementos );
        bb.order( ByteOrder.nativeOrder() );
        IntBuffer ib = bb.asIntBuffer();
        ib.put( elementos );
        ib.position( 0 );
        GLES32.glBufferData( GLES32.GL_ELEMENT_ARRAY_BUFFER, tamElementos, ib, GLES32.GL_STATIC_DRAW );
        
        GLES32.glBindVertexArray( 0 );
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
    
    public Objeto(
        int modoDes, int numCompPos, int numCompCor, int numCompTex,
        @NonNull float[] vertices,
        int textura, boolean texPb
    ) {
        this(
            modoDes, numCompPos, numCompCor, numCompTex,
            vertices, getElementos( numCompPos, numCompCor, numCompTex, vertices ),
            textura, texPb
        );
    }
    
    public Objeto(
        int modoDes, int numCompPos, int numCompCor,
        @NonNull float[] vertices, @NonNull int[] elementos
    ) {
        this(
            modoDes, numCompPos, numCompCor, 0,
            vertices, elementos,
            0, false
        );
    }
    
    public Objeto(
        int modoDes, int numCompPos, int numCompCor,
        @NonNull float[] vertices
    ) {
        this(
            modoDes, numCompPos, numCompCor, 0,
            vertices, getElementos( numCompPos, numCompCor, 0, vertices ),
            0, false
        );
    }
    
    public Objeto(
        int modoDes, int numCompPos, int numCompTex,
        @NonNull float[] vertices, @NonNull int[] elementos,
        int textura, boolean texPb
    ) {
        this(
            modoDes, numCompPos, 0, numCompTex,
            vertices, elementos,
            textura, texPb
        );
    }
    
    public Objeto(
        int modoDes, int numCompPos, int numCompTex,
        @NonNull float[] vertices,
        int textura, boolean texPb
    ) {
        this(
            modoDes, numCompPos, 0, numCompTex,
            vertices, getElementos( numCompPos, 0, numCompTex, vertices ),
            textura, texPb
        );
    }
    
    public void setEscala( float x, float y, float z ) {
        matrizEscala[0] = x;    //                0                    0           0
        /*                0*/   matrizEscala[5] = y;    //             0           0
        /*                0                       0*/   matrizEscala[10] = z;   // 0
        //                0                       0                        0       1
    }
    
    public void setRot( float x, float y, float z ) {
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
    
    public void setTrans( float x, float y, float z ) {
        /*                    1                    0                    0*/   matrizTrans[3] =  x;
        /*                    0                    1                    0*/   matrizTrans[7] =  y;
        /*                    0                    0                    1*/   matrizTrans[11] = z;
        //                    0                    0                    0                       1
    }
    
    public void draw() {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, textura );
        
        GLES32.glUseProgram( program );
        
        GLES32.glUniformMatrix4fv( pontMatrizEscala, 1, true, matrizEscala, 0 );
        GLES32.glUniformMatrix4fv( pontMatrizRotX, 1, true, matrizRotX, 0 );
        GLES32.glUniformMatrix4fv( pontMatrizRotY, 1, true, matrizRotY, 0 );
        GLES32.glUniformMatrix4fv( pontMatrizRotZ, 1, true, matrizRotZ, 0 );
        GLES32.glUniformMatrix4fv( pontMatrizTrans, 1, true, matrizTrans, 0 );
        
        GLES32.glBindVertexArray( vao[0] );
        
        GLES32.glDrawElements( modoDes, numElementos, GLES32.GL_UNSIGNED_INT, 0 );
        
        GLES32.glBindVertexArray( 0 );
        
        GLES32.glUseProgram( 0 );
        
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, 0 );
    }
}
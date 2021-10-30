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
    
    private final int
        vertexBufferObject, elementBufferObject,
        vertexArrayObject,
        numElementos;
    
    private final Programa programa;
    
    private final int
        ponteiroMatrizEscala,
        ponteiroMatrizRotX, ponteiroMatrizRotY, ponteiroMatrizRotZ,
        ponteiroMatrizTrans,
        ponteiroMatrizProjecao,
        ponteiroMatrizTranslacaoTela,
        ponteiroMatrizRotacaoTelaX, ponteiroMatrizRotacaoTelaY, ponteiroMatrizRotacaoTelaZ;
    
    private final Textura textura;
    private final int ponteiroParametroTextura;
    
    private final int modoDesenho;
    
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
        
        programa = new Programa(
            numCompCor > 0,
            numCompTex > 0,
            textura != null && textura.getNumeroComponentesCor() == 1
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
                    programa.getAttribLocation( "pos" ),
                    programa.getAttribLocation( "cor" ),
                    programa.getAttribLocation( "tex" )
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
        
        ponteiroMatrizEscala = programa.getUniformLocation( "escala" );
        ponteiroMatrizRotX = programa.getUniformLocation( "rotX" );
        ponteiroMatrizRotY = programa.getUniformLocation( "rotY" );
        ponteiroMatrizRotZ = programa.getUniformLocation( "rotZ" );
        ponteiroMatrizTrans = programa.getUniformLocation( "trans" );
        ponteiroMatrizProjecao = programa.getUniformLocation( "projecao" );
        ponteiroMatrizTranslacaoTela = programa.getUniformLocation( "translacaoTela" );
        ponteiroMatrizRotacaoTelaX = programa.getUniformLocation( "rotacaoTelaX" );
        ponteiroMatrizRotacaoTelaY = programa.getUniformLocation( "rotacaoTelaY" );
        ponteiroMatrizRotacaoTelaZ = programa.getUniformLocation( "rotacaoTelaZ" );
        
        this.textura = textura;
        ponteiroParametroTextura = programa.getUniformLocation( "parametroTextura" );
        
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
    
    private static final int NUMERO_COORDENDAS = 3;
    
    public static final int
        X = 0,
        Y = 1,
        Z = 2;
    
    private static float[] getXYZ( float[] xyz ) {
        final float[] xyzFinal = new float[]{ 0.0f, 0.0f, 0.0f };
        
        if ( xyz == null )
            return xyzFinal;
        
        for ( int i = 0; i < xyz.length && i < xyzFinal.length; i++ )
            xyzFinal[i] = xyz[i];
        
        return xyzFinal;
    }
    
    private int getCoordenadaValida( int coord ) {
        if ( coord < X )
            coord = X;
        else if ( coord > Z )
            coord = Z;
        
        return coord;
    }
    
    private final float[]
        vetorEscala = new float[NUMERO_COORDENDAS],
        vetorRotacao = new float[NUMERO_COORDENDAS],
        vetorTranslacao = new float[NUMERO_COORDENDAS],
        vetorProjecao = new float[2],
        vetorTranslacaoTela = new float[4],
        vetorRotacaoTela = new float[NUMERO_COORDENDAS];
    
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
        matrizTrans = matrizId.clone(),
        matrizProjecao = matrizId.clone(),
        matrizTranslacaoTela = matrizId.clone(),
        matrizRotacaoTelaX = matrizId.clone(),
        matrizRotacaoTelaY = matrizId.clone(),
        matrizRotacaoTelaZ = matrizId.clone();
    
    public void setEscala( float x, float y, float z ) {
        vetorEscala[0] = x;
        vetorEscala[1] = y;
        vetorEscala[2] = z;
        
        matrizEscala[0] = x;    //                0                    0           0
        /*                0*/   matrizEscala[5] = y;    //             0           0
        /*                0                       0*/   matrizEscala[10] = z;   // 0
        //                0                       0                        0       1
    }
    
    public void setEscala( float[] xyz ) {
        if ( xyz == null )
            return;
        
        xyz = getXYZ( xyz );
        setEscala( xyz[0], xyz[1], xyz[2] );
    }
    
    public void setRotacao( float x, float y, float z ) {
        vetorRotacao[0] = x;
        vetorRotacao[1] = y;
        vetorRotacao[2] = z;
        
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
    
    public void setRotacao( float[] xyz ) {
        if ( xyz == null )
            return;
        
        xyz = getXYZ( xyz );
        setRotacao( xyz[0], xyz[1], xyz[2] );
    }
    
    public void setTranslacao( float x, float y, float z ) {
        vetorTranslacao[0] = x;
        vetorTranslacao[1] = y;
        vetorTranslacao[2] = z;
        
        /*                    1                    0                    0*/   matrizTrans[3] =  x;
        /*                    0                    1                    0*/   matrizTrans[7] =  y;
        /*                    0                    0                    1*/   matrizTrans[11] = z;
        //                    0                    0                    0                       1
    }
    
    public void setTranslacao( float[] xyz ) {
        if ( xyz == null )
            return;
        
        xyz = getXYZ( xyz );
        setTranslacao( xyz[0], xyz[1], xyz[2] );
    }
    
    public void setProjecao( float x, float y ) {
        vetorProjecao[0] = x;
        vetorProjecao[1] = y;
        
        matrizProjecao[0] = x;    //                  0                    0           0
        /*                  0*/   matrizProjecao[5] = y;    //             0           0
        /*                  0                         0*/   //             1           0
        //                  0                         0                    0           1
    }
    
    public void setProjecao( float[] xy ) {
        if ( xy == null || xy.length < 2 )
            return;
        
        setProjecao( xy[0], xy[1] );
    }
    
    public void setTranslacaoTela( float x, float y, float expoenteZx, float expoenteZy ) {
        vetorTranslacaoTela[0] = x;
        vetorTranslacaoTela[1] = y;
        vetorTranslacaoTela[2] = expoenteZx;
        vetorTranslacaoTela[3] = expoenteZy;
        
        /*        1        0*/  matrizTranslacaoTela[2] = expoenteZx;  matrizTranslacaoTela[3] =  x;
        /*        0        1*/  matrizTranslacaoTela[6] = expoenteZy;  matrizTranslacaoTela[7] =  y;
        //        0        0                                       1                              0
        //        0        0                                       0                              1
    }
    
    public void setTranslacaoTela( float[] xyZxZy ) {
        if ( xyZxZy == null || xyZxZy.length < 4 )
            return;
        
        setTranslacaoTela( xyZxZy[0], xyZxZy[1], xyZxZy[2], xyZxZy[3] );
    }
    
    public void setRotacaoTela( float x, float y, float z ) {
        vetorRotacaoTela[0] = x;
        vetorRotacaoTela[1] = y;
        vetorRotacaoTela[2] = z;
        
        float
            sinX = (float) Math.sin( x ), cosX = (float) Math.cos( x ),
            sinY = (float) Math.sin( y ), cosY = (float) Math.cos( y ),
            sinZ = (float) Math.sin( z ), cosZ = (float) Math.cos( z );
        
        // 1                                0                                  0        0
        /* 0*/  matrizRotacaoTelaX[5] =  cosX;    matrizRotacaoTelaX[6] =  -sinX;    // 0
        /* 0*/  matrizRotacaoTelaX[9] =  sinX;    matrizRotacaoTelaX[10] =  cosX;    // 0
        // 0                                0                                  0        1

        matrizRotacaoTelaY[0] =  cosY;    /* 0*/  matrizRotacaoTelaY[2] =   sinY;    // 0
        //                          0        1                                 0        0
        matrizRotacaoTelaY[8] = -sinY;    /* 0*/  matrizRotacaoTelaY[10] =  cosY;    // 0
        //                          0        0                                 0        1

        matrizRotacaoTelaZ[0] =  cosZ;    matrizRotacaoTelaZ[1] = -sinZ;    // 0    0
        matrizRotacaoTelaZ[4] =  sinZ;    matrizRotacaoTelaZ[5] =  cosZ;    // 0    0
        //                          0                                 0        1    0
        //                          0                                 0        0    1
    }
    
    public void setRotacaoTela( float[] xyz ) {
        if ( xyz == null )
            return;
        
        xyz = getXYZ( xyz );
        setRotacaoTela( xyz[0], xyz[1], xyz[2] );
    }
    
    public float getEscala( int coord ) {
        return vetorEscala[getCoordenadaValida( coord )];
    }
    
    public float getRotacao( int coord ) {
        return vetorRotacao[getCoordenadaValida( coord )];
    }
    
    public float getTranslacao( int coord ) {
        return vetorTranslacao[getCoordenadaValida( coord )];
    }
    
    public float getProjecao( int coord ) {
        if ( coord < 0 )
            coord = 0;
        else if ( coord > 1 )
            coord = 1;
        
        return vetorProjecao[coord];
    }
    
    public float getTranslacaoTela( int coord ) {
        if ( coord < 0 )
            coord = 0;
        else if ( coord > 3 )
            coord = 3;
        
        return vetorTranslacaoTela[coord];
    }
    
    public float getRotacaoTela( int coord ) {
        return vetorRotacaoTela[getCoordenadaValida( coord )];
    }
    
    public float[] getVetorEscala() {
        return vetorEscala.clone();
    }
    
    public float[] getVetorRotacao() {
        return vetorRotacao.clone();
    }
    
    public float[] getVetorTranslacao() {
        return vetorTranslacao.clone();
    }

    public float[] getVetorProjecao() {
        return vetorProjecao.clone();
    }
    
    public float[] getVetorTranslacaoTela() {
        return vetorTranslacaoTela.clone();
    }
    
    public float[] getVetorRotacaoTela() {
        return vetorRotacaoTela.clone();
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

    public float[] getMatrizProjecao() {
        return matrizProjecao.clone();
    }
    
    public float[] getMatrizTranslacaoTela() {
        return matrizTranslacaoTela.clone();
    }

    public float[] getMatrizRotacaoTelaX() {
        return matrizRotacaoTelaX.clone();
    }

    public float[] getMatrizRotacaoTelaY() {
        return matrizRotacaoTelaY.clone();
    }

    public float[] getMatrizRotacaoTelaZ() {
        return matrizRotacaoTelaZ.clone();
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
        if ( indiceParametro < 0 || indiceParametro >= parametroTextura.length )
            return;
        
        parametroTextura[indiceParametro] = valor;
    }
    
    public float getParametroTextura( int indiceParametro ) {
        return parametroTextura[getIndiceParametroValido( indiceParametro )];
    }
    
    public void draw() {
        if ( textura != null )
            textura.bind();
        
        programa.ativar();
        
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
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizProjecao, 1, true, matrizProjecao, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizTranslacaoTela, 1, true, matrizTranslacaoTela, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotacaoTelaX, 1, true, matrizRotacaoTelaX, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotacaoTelaY, 1, true, matrizRotacaoTelaY, 0
        );
        GLES32.glUniformMatrix4fv(
            ponteiroMatrizRotacaoTelaZ, 1, true, matrizRotacaoTelaZ, 0
        );
        
        GLES32.glUniform1fv(
            ponteiroParametroTextura, parametroTextura.length, parametroTextura, 0
        );
        
        GLES32.glBindVertexArray( vertexArrayObject );
        GLES32.glDrawElements( modoDesenho, numElementos, GLES32.GL_UNSIGNED_INT, 0 );
        GLES32.glBindVertexArray( 0 );
        
        programa.desativar();
        
        if ( textura != null )
            textura.unbind();
    }
    
    @Override
    public void close(){
        programa.close();
        GLES32.glDeleteVertexArrays( 1, new int[]{ vertexArrayObject }, 0 );
        GLES32.glDeleteBuffers( 2, new int[]{ elementBufferObject, vertexBufferObject }, 0 );
    }
}
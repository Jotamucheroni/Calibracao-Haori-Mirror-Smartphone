package com.unesp.calibracao_haori;

import android.opengl.GLES32;

public class RenderBuffer implements AutoCloseable {
    public static final int numCompCor = 3;
    
    private int largura, altura;

    private final int id;
    
    public RenderBuffer( int largura, int altura ) {
        setLargura( largura );
        setAltura( altura );
        
        int[] bufferId = new int[1];
        GLES32.glGenRenderbuffers( 1, bufferId, 0 );
        id = bufferId[0];
        alocar();
    }
    
    public RenderBuffer() {
        this( 1, 1 );
    }
    
    public void setLargura( int largura ) {
        if ( largura < 1 )
            largura = 1;
        
        this.largura = largura;
    }
    
    public void setAltura( int altura ) {
        if ( altura < 1 )
            altura = 1;
        
        this.altura = altura;
    }
    
    public int getLargura() {
        return largura;
    }
    
    public int getAltura() {
        return altura;
    }
    
    public int getId() {
        return id;
    }
    
    public int getNumPix() {
        return largura * altura;
    }
    
    public int getNumBytes() {
        return getNumPix() * RenderBuffer.numCompCor;
    }
    
    public void alocar() {
        GLES32.glBindRenderbuffer( GLES32.GL_RENDERBUFFER, id );
        GLES32.glRenderbufferStorage( GLES32.GL_RENDERBUFFER, GLES32.GL_RGB8, largura, altura );
    }
    
    @Override
    public void close() {
        GLES32.glDeleteRenderbuffers( 1, new int[]{ id }, 0 );
    }
}
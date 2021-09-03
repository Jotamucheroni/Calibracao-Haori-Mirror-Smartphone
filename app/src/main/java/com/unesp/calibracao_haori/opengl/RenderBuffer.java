package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

public class RenderBuffer implements AutoCloseable {
    public static final int numeroComponentesCor = 4; 
    
    private int largura, altura;
    
    private final int id;
    
    public RenderBuffer( int largura, int altura ) {
        setLargura( largura );
        setAltura( altura );
        
        int[] bufferId = new int[1];
        GLES32.glGenRenderbuffers( 1, bufferId, 0 );
        id = bufferId[0];
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
        return getNumPix() * RenderBuffer.numeroComponentesCor;
    }
    
    private boolean alocado = false;
    
    public void alocar() {
        GLES32.glBindRenderbuffer( GLES32.GL_RENDERBUFFER, id );
        GLES32.glRenderbufferStorage( GLES32.GL_RENDERBUFFER, GLES32.GL_RGBA8, largura, altura );
        
        alocado = true;
    }
    
    public boolean getAlocado() {
        return alocado;
    }
    
    @Override
    public void close() {
        GLES32.glDeleteRenderbuffers( 1, new int[]{ id }, 0 );
    }
}
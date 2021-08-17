package com.unesp.calibracao_haori;

import android.opengl.GLES32;

public class FrameBuffer implements AutoCloseable {
    public static final int numCompCor = 3;
    
    private int numRenderBuffer;
    private int largura, altura;
    
    private final int[] id;
    
    public FrameBuffer( int numRenderBuffer, int largura, int altura ) {
        setNumRenderBuffer( numRenderBuffer );
        setLargura( largura );
        setAltura( altura );
        id = new int[1];
        GLES32.glGenFramebuffers( 1, id, 0 );
        
        alocar();
    }
    
    public FrameBuffer( int largura, int altura ) {
        this( 1, largura, altura );
    }
    
    public FrameBuffer( int numRenderBuffer ) {
        this( numRenderBuffer, 1, 1 );
    }
    
    public FrameBuffer() {
        this( 1, 1, 1 );
    }
    
    public void setNumRenderBuffer( int numRenderBuffer ) {
        if ( numRenderBuffer < 1 )
            numRenderBuffer = 1;
        
        this.numRenderBuffer = numRenderBuffer;
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
    
    public int getNumRenderBuffer() {
        return numRenderBuffer;
    }
    
    public int getLargura() {
        return largura;
    }
    
    public int getAltura() {
        return altura;
    }
    
    public int getId() {
        return id[0];
    }
    
    public int getNumPix() {
        return largura * altura;
    }
    
    public int getNumBytes() {
        return getNumPix() * FrameBuffer.numCompCor;
    }

    private RenderBuffer[] rb;
    
    public void alocar() {
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, id[0] );
        rb = new RenderBuffer[getNumRenderBuffer()];
        for ( int i = 0; i < rb.length; i++ ) {
            rb[i] = new RenderBuffer( getLargura(), getAltura() );
            GLES32.glFramebufferRenderbuffer(
                GLES32.GL_DRAW_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0 + i,
                GLES32.GL_RENDERBUFFER, rb[i].getId()
            );
        }
    }
    
    @Override
    public void close() {
        for ( RenderBuffer r : rb )
            r.close();
        GLES32.glDeleteFramebuffers( 1, id, 0 );
    }
}
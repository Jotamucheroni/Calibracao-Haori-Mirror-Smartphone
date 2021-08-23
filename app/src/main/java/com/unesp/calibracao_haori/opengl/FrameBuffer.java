package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

public class FrameBuffer implements AutoCloseable {
    public static final int numCompCor = 3;
    
    private int numRenderBuffer;
    private int largura, altura;

    private final int id;
    
    public FrameBuffer( int numRenderBuffer, int largura, int altura ) {
        setNumRenderBuffer( numRenderBuffer );
        setLargura( largura );
        setAltura( altura );
        
        int[] bufferId = new int[1];
        GLES32.glGenFramebuffers( 1, bufferId, 0 );
        id = bufferId[0];
        alocar();
        
        int[] drawBuffers = new int[numRenderBuffer];
        for ( int i = 0; i < numRenderBuffer; i++ )
            drawBuffers[i] = GLES32.GL_COLOR_ATTACHMENT0 + i;
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, id );
        GLES32.glDrawBuffers( numRenderBuffer, drawBuffers, 0 );
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
        return id;
    }
    
    public int getNumPix() {
        return largura * altura;
    }
    
    public int getNumBytes() {
        return getNumPix() * FrameBuffer.numCompCor;
    }

    private RenderBuffer[] rb;
    
    private void alocar() {
        rb = new RenderBuffer[getNumRenderBuffer()];
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, id );
        for ( int i = 0; i < rb.length; i++ ) {
            rb[i] = new RenderBuffer( largura, altura );
            GLES32.glFramebufferRenderbuffer(
                GLES32.GL_DRAW_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0 + i,
                GLES32.GL_RENDERBUFFER, rb[i].getId()
            );
        }
    }
    
    public void exibir( int x, int y, int largura, int altura, int numColunas, int numLinhas ) {
        if( x < 0 )
            x = 0;
        
        if( y < 0 )
            y = 0;
        
        if( largura < 1 )
            largura = 1;
        
        if( altura < 1 )
            altura = 1;
        
        if( numColunas < 1 )
            numColunas = 1;
        
        if( numLinhas < 1 )
            numLinhas = 1;
        
        int
            numCelulas = numColunas * numLinhas,
            largColuna = largura / numColunas, altLinha = altura / numLinhas;
        GLES32.glBindFramebuffer( GLES32.GL_READ_FRAMEBUFFER, id );
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
        for ( int i = 0; i < numCelulas; i++ ) {
            int coluna = i % numColunas;
            int linha = ( numLinhas - 1 ) - ( i / numColunas );
            
            GLES32.glReadBuffer( GLES32.GL_COLOR_ATTACHMENT0 + i );
            GLES32.glBlitFramebuffer(
                0, 0, this.largura, this.altura,
                x + coluna * largColuna, y + linha * altLinha,
                x + ( coluna + 1 ) * largColuna, y + ( linha  + 1 ) * altLinha,
                GLES32.GL_COLOR_BUFFER_BIT, GLES32.GL_LINEAR
            );
        }
    }
    
    public void exibir( int largura, int altura, int numColunas, int numLinhas ) {
        exibir( 0, 0, largura, altura, numColunas, numLinhas );
    }
    
    public void exibir( int largura, int altura ) {
        exibir( 0, 0, largura, altura, 1, 1 );
    }
    
    @Override
    public void close() {
        for ( RenderBuffer r : rb )
            r.close();
        GLES32.glDeleteFramebuffers( 1, new int[]{ id }, 0 );
    }
}
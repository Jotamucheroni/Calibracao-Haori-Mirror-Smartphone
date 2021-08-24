package com.unesp.calibracao_haori.opengl.renderbuffer;

import android.opengl.GLES32;

import com.unesp.calibracao_haori.opengl.RenderBuffer;

public class FrameBufferObject extends FrameBuffer implements AutoCloseable {
    public static final int numCompCor = 3;
    
    private int numRenderBuffer;
    
    public FrameBufferObject(int numRenderBuffer, int largura, int altura ) {
        setNumRenderBuffer( numRenderBuffer );
        setLargura( largura );
        setAltura( altura );
        
        int[] bufferId = new int[1];
        GLES32.glGenFramebuffers( 1, bufferId, 0 );
        setId( bufferId[0] );
        alocar();
        
        int[] drawBuffers = new int[numRenderBuffer];
        for ( int i = 0; i < numRenderBuffer; i++ )
            drawBuffers[i] = GLES32.GL_COLOR_ATTACHMENT0 + i;
        bindDraw();
        GLES32.glDrawBuffers( numRenderBuffer, drawBuffers, 0 );
    }
    
    public FrameBufferObject(int largura, int altura ) {
        this( 1, largura, altura );
    }
    
    public FrameBufferObject(int numRenderBuffer ) {
        this( numRenderBuffer, 1, 1 );
    }
    
    public FrameBufferObject() {
        this( 1, 1, 1 );
    }
    
    public void setNumRenderBuffer( int numRenderBuffer ) {
        if ( numRenderBuffer < 1 )
            numRenderBuffer = 1;
        
        this.numRenderBuffer = numRenderBuffer;
    }
    
    public int getNumRenderBuffer() {
        return numRenderBuffer;
    }
    
    public int getNumPix() {
        return getLargura() * getAltura();
    }
    
    public int getNumBytes() {
        return getNumPix() * FrameBufferObject.numCompCor;
    }

    private RenderBuffer[] rb;
    
    private void alocar() {
        rb = new RenderBuffer[getNumRenderBuffer()];
        bindDraw();
        for ( int i = 0; i < rb.length; i++ ) {
            rb[i] = new RenderBuffer( getLargura(), getAltura() );
            GLES32.glFramebufferRenderbuffer(
                GLES32.GL_DRAW_FRAMEBUFFER, GLES32.GL_COLOR_ATTACHMENT0 + i,
                GLES32.GL_RENDERBUFFER, rb[i].getId()
            );
        }
    }

    public void copiar(
        FrameBuffer destino,
        int x, int y, int largura, int altura,
        int numColunas, int numLinhas
    ) {
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
        
        bindRead();
        destino.bindDraw();
        for ( int i = 0; i < numCelulas; i++ ) {
            int coluna = i % numColunas;
            int linha = ( numLinhas - 1 ) - ( i / numColunas );
            
            GLES32.glReadBuffer( GLES32.GL_COLOR_ATTACHMENT0 + i );
            GLES32.glBlitFramebuffer(
                0, 0, getLargura(), getAltura(),
                x + coluna * largColuna, y + linha * altLinha,
                x + ( coluna + 1 ) * largColuna, y + ( linha  + 1 ) * altLinha,
                GLES32.GL_COLOR_BUFFER_BIT, GLES32.GL_LINEAR
            );
        }
    }
    
    public void copiar(
        FrameBuffer destino,
        int largura, int altura,
        int numColunas, int numLinhas
    ) {
        copiar( destino, 0, 0, largura, altura, numColunas, numLinhas );
    }
    
    public void copiar(
        FrameBuffer destino,
        int largura, int altura
    ) {
        copiar( destino, 0, 0, largura, altura, 1, 1 );
    }
    
    @Override
    public void close() {
        for ( RenderBuffer r : rb )
            r.close();
        GLES32.glDeleteFramebuffers( 1, new int[]{ getId() }, 0 );
    }
}
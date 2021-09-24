package com.unesp.calibracao_haori.opengl.framebuffer;

import android.opengl.GLES32;

import com.unesp.calibracao_haori.opengl.Desenho;

public abstract class FrameBuffer {
    private int
        id,
        largura, altura;
    
    protected void setId( int id ) {
        if ( id < 0 )
            id = 0;
        
        this.id = id;
    }
    
    protected void setLargura( int largura ) {
        if ( largura < 1 )
            largura = 1;
        
        this.largura = largura;
    }
    
    protected void setAltura( int altura ) {
        if ( altura < 1 )
            altura = 1;
        
        this.altura = altura;
    }
    
    public int getId() {
        return id;
    }
    
    public int getLargura() {
        return largura;
    }
    
    public int getAltura() {
        return altura;
    }
    
    public int getNumeroPixeis() {
        return largura * altura;
    }
    
    public void bindDraw() {
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, id );
    }
    
    public void bindRead() {
        GLES32.glBindFramebuffer( GLES32.GL_READ_FRAMEBUFFER, id );
    }
    
    public void bind() {
        GLES32.glBindFramebuffer( GLES32.GL_FRAMEBUFFER, id );
    }
    
    public void unbindDraw() {
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, 0 );
    }
    
    public void unbindRead() {
        GLES32.glBindFramebuffer( GLES32.GL_READ_FRAMEBUFFER, 0 );
    }
    
    public void unbind() {
        GLES32.glBindFramebuffer( GLES32.GL_FRAMEBUFFER, 0 );
    }
    
    public void clear() {
        bindDraw();
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
        unbindDraw();
    }
    
    public void draw( int x, int y, int largura, int altura, Desenho desenho ) {
        if ( desenho == null )
            return;
        
        bindDraw();
        GLES32.glViewport( x, y, largura, altura );
        desenho.draw();
        unbindDraw();
    }
    
    public void draw( int largura, int altura, Desenho desenho ) {
        draw( 0, 0, largura, altura, desenho );
    }
    
    public void draw( Desenho desenho ) {
        draw( 0, 0, this.largura, this.altura, desenho );
    }
    
    public void draw( int x, int y, int largura, int altura, Desenho[] desenho ) {
        if ( desenho == null )
            return;
        
        bindDraw();
        GLES32.glViewport( x, y, largura, altura );
        
        for( Desenho des : desenho )
            des.draw();
        unbindDraw();
    }
    
    public void draw( int largura, int altura, Desenho[] desenho ) {
        draw( 0, 0, largura, altura, desenho );
    }
    
    public void draw( Desenho[] desenho ) {
        draw( 0, 0, this.largura, this.altura, desenho );
    }
}
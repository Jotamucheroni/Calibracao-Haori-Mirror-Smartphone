package com.unesp.calibracao_haori.opengl.framebuffer;

import android.opengl.GLES32;

import com.unesp.calibracao_haori.opengl.Objeto;

public abstract class FrameBuffer {
    private int largura, altura;
    
    private int id;
    
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
    
    protected void setId( int id ) {
        if ( id < 0 )
            id = 0;
        
        this.id = id;
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
    
    public void bindDraw() {
        GLES32.glBindFramebuffer( GLES32.GL_DRAW_FRAMEBUFFER, id );
    }
    
    public void bindRead() {
        GLES32.glBindFramebuffer( GLES32.GL_READ_FRAMEBUFFER, id );
    }
    
    public void bind() {
        GLES32.glBindFramebuffer( GLES32.GL_FRAMEBUFFER, id );
    }
    
    public void clear() {
        bindDraw();
        GLES32.glClear( GLES32.GL_COLOR_BUFFER_BIT );
    }
    
    public void draw( int x, int y, int largura, int altura, Objeto objeto ) {
        bindDraw();
        GLES32.glViewport( x, y, largura, altura );
        objeto.draw();
    }
    
    public void draw( int largura, int altura, Objeto objeto ) {
        draw( 0, 0, largura, altura, objeto );
    }
    
    public void draw( Objeto objeto ) {
        draw( 0, 0, this.largura, this.altura, objeto );
    }
    
    public void draw( int x, int y, int largura, int altura, Objeto[] objeto ) {
        bindDraw();
        GLES32.glViewport( x, y, largura, altura );

        for( Objeto obj : objeto )
            obj.draw();
    }
    
    public void draw( int largura, int altura, Objeto[] objeto ) {
        draw( 0, 0, largura, altura, objeto );
    }
    
    public void draw( Objeto[] objeto ) {
        draw( 0, 0, this.largura, this.altura, objeto );
    }
}
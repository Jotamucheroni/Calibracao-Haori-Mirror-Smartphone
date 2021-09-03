package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;

public class Textura implements AutoCloseable {
    private final int id;
    private int largura, altura;
    private boolean monocromatica;
    private int formatoImagem, formatoInterno;
    
    public Textura( int largura, int altura, boolean monocromatica ) {
        setLargura( largura );
        setAltura( altura );
        setMonocromatica( monocromatica );
        
        int[] bufferId = new int[1];
        GLES32.glGenTextures( 1, bufferId, 0 );
        id = bufferId[0];
        
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, id );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_S, GLES32.GL_CLAMP_TO_EDGE
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_WRAP_T, GLES32.GL_CLAMP_TO_EDGE
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MIN_FILTER, GLES32.GL_LINEAR
        );
        GLES32.glTexParameteri(
            GLES32.GL_TEXTURE_2D, GLES32.GL_TEXTURE_MAG_FILTER, GLES32.GL_LINEAR
        );
    }
    
    public Textura( int largura, int altura ) {
        this( largura, altura, false );
    }
    
    public Textura() {
        this( 1, 1, false );
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
    
    public void setMonocromatica( boolean monocromatica ) {
        this.monocromatica = monocromatica;

        formatoInterno = monocromatica ? GLES32.GL_R8 : GLES32.GL_RGBA8;
        formatoImagem = monocromatica ? GLES32.GL_RED : GLES32.GL_RGBA;
    }
    
    public int getLargura() {
        return largura;
    }
    
    public int getAltura() {
        return altura;
    }
    
    public boolean getMonocromatica() {
        return monocromatica;
    }
    
    public int getId() {
        return id;
    }
    
    private boolean alocado = false;
    
    public void alocar() {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, id );
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, formatoInterno,
            largura, altura, 0,
            formatoImagem, GLES32.GL_UNSIGNED_BYTE, null
        );
        
        alocado = true;
    }
    
    public boolean getAlocado() {
        return alocado;
    }
    
    public void carregarImagem( ByteBuffer imagem ) {
        if ( imagem == null || !alocado )
            return;
        
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, id );
        GLES32.glTexSubImage2D(
            GLES32.GL_TEXTURE_2D, 0,
            0, 0, largura, altura,
            formatoImagem, GLES32.GL_UNSIGNED_BYTE, imagem
        );
    }
    
    public void bind() {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, id );
    }
    
    @Override
    public void close() {
        GLES32.glDeleteTextures( 1, new int[]{ id }, 0 );
    }
}
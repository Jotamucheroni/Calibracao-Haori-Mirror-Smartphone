package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

import java.nio.ByteBuffer;

public class Textura implements AutoCloseable {
    private static final int[] formatoInterno = new int[]{
        GLES32.GL_R8, GLES32.GL_RG8, GLES32.GL_RGB8, GLES32.GL_RGBA8
    };
    private static final int[] formatoImagem = new int[]{
        GLES32.GL_RED, GLES32.GL_RG, GLES32.GL_RGB, GLES32.GL_RGBA
    };
    
    private final int id;
    private final int largura, altura;
    private final int numeroComponentesCor;
    
    public Textura( int largura, int altura, int numeroComponentesCor ) {
        int[] bufferId = new int[1];
        GLES32.glGenTextures( 1, bufferId, 0 );
        id = bufferId[0];
        
        bind();
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
        
        if ( largura < 1 )
            largura = 1;
        this.largura = largura;
        
        if ( altura < 1 )
            altura = 1;
        this.altura = altura;
        
        if ( numeroComponentesCor < 1 )
            numeroComponentesCor = 1;
        else if ( numeroComponentesCor > 4 )
            numeroComponentesCor = 4;
        this.numeroComponentesCor = numeroComponentesCor;
        
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, formatoInterno[this.numeroComponentesCor - 1],
            this.largura, this.altura, 0,
            formatoImagem[this.numeroComponentesCor - 1], GLES32.GL_UNSIGNED_BYTE, null
        );
        unbind();
    }
    
    public Textura( int largura, int altura ) {
        this( largura, altura, 4 );
    }
    
    public void bind() {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, id );
    }
    
    public void unbind() {
        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, 0 );
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
    
    public int getNumeroComponentesCor() {
        return numeroComponentesCor;
    }
    
    public int getNumeroPixeis() {
        return largura * altura;
    }
    
    public int getNumeroBytes() {
        return getNumeroPixeis() * numeroComponentesCor;
    }
    
    public void carregarImagem( ByteBuffer imagem ) {
        if ( imagem == null )
            return;
        
        bind();
        GLES32.glTexSubImage2D(
            GLES32.GL_TEXTURE_2D, 0,
            0, 0, largura, altura,
            formatoImagem[numeroComponentesCor - 1], GLES32.GL_UNSIGNED_BYTE, imagem
        );
        unbind();
    }
    
    @Override
    public void close() {
        GLES32.glDeleteTextures( 1, new int[]{ id }, 0 );
    }
}
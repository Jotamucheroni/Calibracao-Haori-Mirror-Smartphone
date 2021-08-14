package com.unesp.calibracao_haori;

import android.graphics.Bitmap;
import android.opengl.GLES32;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImagemOpenGL {
    private Bitmap imagem;
    private int textura;
    
    ImagemOpenGL( Bitmap imagem, int textura ) {
        setImagem( imagem );
        setTextura( textura );
    }
    
    ImagemOpenGL( Bitmap imagem ) {
        this( imagem, 0 );
    }
    
    public void setImagem( Bitmap imagem ) {
        if ( imagem == null )
            return;
        
        if ( imagem.isMutable() )
            imagem = imagem.copy( null, false );
        
        this.imagem = imagem;
    }
    
    public void setTextura( int textura ) {
        if ( textura < 0 ) {
            this.textura = 0;
            
            return;
        }
        
        this.textura = textura;
    }
    
    public Bitmap getImagem() {
        return imagem;
    }
    
    public int getTextura() {
        return textura;
    }
    
    public void carregar() {
        if ( imagem == null )
            return;
        
        ByteBuffer bb = ByteBuffer.allocateDirect( imagem.getByteCount() );
        bb.order( ByteOrder.nativeOrder() );
        imagem.copyPixelsToBuffer( bb );
        bb.position( 0 );

        GLES32.glBindTexture( GLES32.GL_TEXTURE_2D, textura );
        GLES32.glTexImage2D(
            GLES32.GL_TEXTURE_2D, 0, GLES32.GL_RGBA8,
            imagem.getWidth(), imagem.getHeight(), 0,
            GLES32.GL_RGBA, GLES32.GL_UNSIGNED_BYTE, bb
        );
    }
}
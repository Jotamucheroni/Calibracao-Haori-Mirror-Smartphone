package com.unesp.calibracao_haori;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImagemOpenGL extends TexturaOpenGL {
    private Bitmap imagem;
    
    public ImagemOpenGL( Bitmap imagem, boolean monocromatica ) {
        super();
        
        setImagem( imagem );
        setMonocromatica( monocromatica );
    }
    
    ImagemOpenGL( Bitmap imagem ) {
        this( imagem, false );
    }
    
    public void setImagem( Bitmap imagem ) {
        if ( imagem == null )
            return;
        
        if ( imagem.isMutable() )
            imagem = imagem.copy( null, false );
        
        this.imagem = imagem;
        setLargura( imagem.getWidth() );
        setAltura( imagem.getHeight() );
        alocar();
    }
    
    public Bitmap getImagem() {
        return imagem;
    }
    
    public void carregar() {
        if ( imagem == null )
            return;
        
        ByteBuffer bb = ByteBuffer.allocateDirect( imagem.getByteCount() );
        bb.order( ByteOrder.nativeOrder() );
        imagem.copyPixelsToBuffer( bb );
        bb.position( 0 );
        
        carregarImagem( bb );
    }
}
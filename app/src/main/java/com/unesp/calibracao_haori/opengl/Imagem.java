package com.unesp.calibracao_haori.opengl;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Imagem extends Textura {
    private Bitmap imagem;
    
    public Imagem(Bitmap imagem, boolean monocromatica ) {
        super();
        
        setImagem( imagem );
        setMonocromatica( monocromatica );
    }
    
    Imagem( Bitmap imagem ) {
        this( imagem, false );
    }
    
    public void setImagem( Bitmap imagem ) {
        if ( imagem == null )
            return;
        
        if ( imagem.isMutable() )
            imagem = imagem.copy( null, false );
        
        this.imagem = imagem;
        setLargura( this.imagem.getWidth() );
        setAltura( this.imagem.getHeight() );
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
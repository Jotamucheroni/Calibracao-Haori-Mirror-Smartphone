package com.unesp.calibracao_haori.opengl;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Imagem extends Textura {
    public Imagem( Bitmap imagem ) {
        super(
            imagem.getWidth(), imagem.getHeight(),
            4
        );
        
        carregar( imagem );
    }
    
    public void carregar( Bitmap imagem ) {
        if ( imagem == null )
            return;
        
        if (
            imagem.getWidth() != getLargura() ||
            imagem.getHeight() != getAltura() ||
            imagem.getConfig() != Bitmap.Config.ARGB_8888
        )
            return;
        
        ByteBuffer bb = ByteBuffer.allocateDirect( imagem.getByteCount() );
        bb.order( ByteOrder.nativeOrder() );
        imagem.copyPixelsToBuffer( bb );
        bb.rewind();
        
        carregarImagem( bb );
    }
}
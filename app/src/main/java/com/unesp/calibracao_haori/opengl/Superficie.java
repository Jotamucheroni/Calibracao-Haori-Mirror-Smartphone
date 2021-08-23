package com.unesp.calibracao_haori.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.unesp.calibracao_haori.MainActivity;

public class Superficie extends GLSurfaceView implements AutoCloseable {
    private Renderizador renderizador = null;
    
    public Superficie( Context context ) {
        super( context );
    }
    
    public Superficie(MainActivity activity ) {
        super( activity );
        
        setEGLContextClientVersion( 3 );
        renderizador =  new Renderizador( activity );
        setRenderer( renderizador );
    }
    
    @Override
    public void close() {
        if ( renderizador == null )
            return;
        
        renderizador.close();
    }
}
package com.unesp.calibracao_haori;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SuperficieOpenGL extends GLSurfaceView implements AutoCloseable {
    private RenderizadorOpenGL renderizador = null;
    
    public SuperficieOpenGL( Context context ) {
        super( context );
    }
    
    public SuperficieOpenGL( MainActivity activity ) {
        super( activity );
        
        setEGLContextClientVersion( 3 );
        renderizador =  new RenderizadorOpenGL( activity );
        setRenderer( renderizador );
    }
    
    @Override
    public void close() {
        if ( renderizador == null )
            return;
        
        renderizador.close();
    }
}
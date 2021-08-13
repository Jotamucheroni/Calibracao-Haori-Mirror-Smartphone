package com.unesp.calibracao_haori;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class SuperficieOpenGL extends GLSurfaceView {
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
    
    public void liberarRecursos() {
        if ( renderizador == null )
            return;
        
        renderizador.liberarRecursos();
    }
}
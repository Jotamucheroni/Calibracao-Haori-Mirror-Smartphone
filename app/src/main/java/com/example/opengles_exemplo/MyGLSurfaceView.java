package com.example.opengles_exemplo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.SurfaceHolder;

import androidx.appcompat.app.AppCompatActivity;

public class MyGLSurfaceView extends GLSurfaceView {
    private MyGLRenderer renderer = null;

    public MyGLSurfaceView( Context context ) {
        super( context );
    }

    public MyGLSurfaceView( MainActivity activity ) {
        super( activity );

        setEGLContextClientVersion( 3 );
        renderer =  new MyGLRenderer( activity );
        setRenderer( renderer );
    }

    public void liberarRecursos() {
        if ( renderer != null )
            renderer.liberarRecursos();
    }
}

package com.unesp.calibracao_haori.es;

import com.unesp.calibracao_haori.es.camera.Camera;
import com.unesp.calibracao_haori.opengl.DetectorPontos;
import com.unesp.calibracao_haori.opengl.Desenho;
import com.unesp.calibracao_haori.opengl.Programa;
import com.unesp.calibracao_haori.opengl.Textura;
import com.unesp.calibracao_haori.opengl.framebuffer.FrameBufferObject;

public class Dispositivo implements AutoCloseable {
    private String id;
    
    private Camera camera;
    private Textura textura;
    private FrameBufferObject frameBufferObject;
    private Desenho desenho;
    
    private DetectorPontos detectorPontos;
    
    public Dispositivo(
        String id,
        Camera camera, Textura textura, FrameBufferObject frameBufferObject, Desenho desenho,
        DetectorPontos detectorPontos
    ) {
        setId( id );
        setCamera( camera );
        setTextura( textura );
        setFrameBufferObject( frameBufferObject );
        setDesenho( desenho );
        setDetectorPontos( detectorPontos );
    }
    
    public Dispositivo(
        Camera camera, Textura textura, FrameBufferObject frameBufferObject, Desenho desenho,
        DetectorPontos detectorPontos
    ) {
        this( null, camera, textura, frameBufferObject, desenho, detectorPontos);
    }
    
    public Dispositivo(
        String id,
        Camera camera, Textura textura, FrameBufferObject frameBufferObject, Desenho desenho
    ) {
        this( id, camera, textura, frameBufferObject, desenho, null );
        
        setDetectorPontos();
    }
    
    public Dispositivo(
        Camera camera, Textura textura, FrameBufferObject frameBufferObject, Desenho desenho
    ) {
        this( null, camera, textura, frameBufferObject, desenho);
    }
    
    public Dispositivo(
        String id,
        Camera camera, Textura textura, FrameBufferObject frameBufferObject
    ) {
        this( id, camera, textura, frameBufferObject, null, null );
        
        setDesenho();
        setDetectorPontos();
    }
    
    public Dispositivo(
        Camera camera, Textura textura, FrameBufferObject frameBufferObject
    ) {
        this( null, camera, textura, frameBufferObject );
    }
    
    public Dispositivo(
        String id,
        Camera camera, Textura textura
    ) {
        this( id, camera, textura, null, null, null );
        
        setFrameBufferObject();
        setDesenho();
        setDetectorPontos();
    }
    
    public Dispositivo(
        Camera camera, Textura textura
    ) {
        this( null, camera, textura );
    }
    
    public Dispositivo(
        String id,
        Camera camera
    ) {
        this( id, camera, null, null, null, null );
        
        setTextura();
        setFrameBufferObject();
        setDesenho();
        setDetectorPontos();
    }
    
    public Dispositivo(
        Camera camera
    ) {
        this( null, camera );
    }
    
    public void setId( String id ) {
        if ( id == null ) {
            this.id = "Dispositivo";
            
            return;
        }
        
        this.id = id;
    }
    
    public void setCamera( Camera camera ) {
        this.camera = camera;
    }
    
    public void setTextura( Textura textura ) {
        this.textura = textura;
    }
    
    private void setTextura() {
        if ( camera == null )
            return;
        
        Textura inicializadorTextura = new Textura(
            camera.getLarguraImagem(), camera.getAlturaImagem(), true
        );
        
        if ( camera.getNumeroComponentesCorImagem() > 1 )
            inicializadorTextura.setMonocromatica( false );
        
        setTextura( inicializadorTextura );
    }
    
    public void setFrameBufferObject( FrameBufferObject frameBufferObject ) {
        this.frameBufferObject = frameBufferObject;
    }
    
    private void setFrameBufferObject() {
        setFrameBufferObject(
            new FrameBufferObject( Programa.MAXIMO_SAIDAS, 640, 480 )
        );
    }
    
    public void setDesenho( Desenho desenho ) {
        this.desenho = desenho;
    }
    
    private void setDesenho() {
        if ( textura == null )
            return;
        
        setDesenho(
            new Desenho(
                2, 2,
                Desenho.getRefQuad(), Desenho.getRefElementos(),
                textura
            )
        );
    }
    
    public void setDetectorPontos( DetectorPontos detectorPontos ) {
        this.detectorPontos = detectorPontos;
    }
    
    private void setDetectorPontos() {
        if ( frameBufferObject == null )
            return;
        
        DetectorPontos inicializadorDetectorPontos = new DetectorPontos(
            frameBufferObject.getNumBytes(), FrameBufferObject.numeroComponentesCor
        );
        
        setDetectorPontos(inicializadorDetectorPontos);
    }

    public String getId() {
        return id;
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public Textura getTextura() {
        return textura;
    }
    
    public FrameBufferObject getFrameBufferObject() {
        return frameBufferObject;
    }
    
    public Desenho getDesenho() {
        return desenho;
    }
    
    public DetectorPontos getDetectorPontos() {
        return detectorPontos;
    }
    
    public boolean getLigado() {
        if( camera == null )
            return false;
        
        return camera.getLigada();
    }
    
    public void ligar() {
        if ( camera == null )
            return;
        
        camera.ligar();
    }
    
    public void desligar() {
        if ( camera == null )
            return;
        
        camera.desligar();
    }
    
    public void alocar() {
        if ( textura != null )
            textura.alocar();
        
        if ( frameBufferObject != null )
            frameBufferObject.alocar();
        
        if ( detectorPontos != null )
            detectorPontos.alocar();
    }
    
    public void atualizarTextura() {
        if ( textura == null || camera == null )
            return;
        
        textura.carregarImagem( camera.getImagem() );
    }
    
    public void draw() {
        if ( frameBufferObject == null || desenho == null )
            return;
        
        frameBufferObject.clear();
        frameBufferObject.draw(desenho);
    }
    
    public void atualizarImagemDetector( int numeroRenderBuffer ) {
        if ( detectorPontos == null || frameBufferObject == null )
            return;
        
        if ( detectorPontos.pronto() ) {
            frameBufferObject.lerRenderBuffer( numeroRenderBuffer, detectorPontos.getImagem() );
            detectorPontos.executar();
        }
    }
    
    public void atualizarImagemDetector() {
        atualizarImagemDetector( 1 );
    }
    
    @Override
    public void close() {
        if ( detectorPontos != null )
            detectorPontos.close();
        
        if ( frameBufferObject != null )
            frameBufferObject.close();
        
        if ( textura != null )
            textura.close();
        
        if ( camera != null )
            camera.close();
    }
}
package com.unesp.calibracao_haori.opengl;

import java.nio.ByteBuffer;

public class DetectorBorda implements AutoCloseable {
    private int tamanhoImagem;
    private int numeroComponentesCor;
    
    private ByteBuffer imagem, visImagem;
    
    DetectorBorda( int tamanhoImagem, int numeroComponentesCor ) {
        setTamanhoImagem( tamanhoImagem );
        setNumeroComponentesCor( numeroComponentesCor );
    }
    
    DetectorBorda( int tamanhoImagem ) {
        this( tamanhoImagem, 4 );
    }
    
    public void setTamanhoImagem( int tamanhoImagem ) {
        if ( tamanhoImagem < 1 )
            tamanhoImagem = 1;
        
        synchronized ( this ) {
            this.tamanhoImagem = tamanhoImagem;
        }
    }
    
    public void setNumeroComponentesCor( int numeroComponentesCor ) {
        if ( numeroComponentesCor < 1 )
            numeroComponentesCor = 1;
        else if ( numeroComponentesCor > 4 )
            numeroComponentesCor = 4;
        
        synchronized ( this ) {
            this.numeroComponentesCor = numeroComponentesCor;
        }
    }
    
    public ByteBuffer getImagem() {
        imagem.rewind();
        
        return imagem;
    }
    
    public void alocar() {
        imagem = ByteBuffer.allocateDirect( tamanhoImagem );
        
        synchronized ( this ) {
            visImagem = imagem.asReadOnlyBuffer();
        }
    }
    
    private int saida = 0;
    private final Object sincronizador = new Object();
    
    private final Thread detector = new Thread(
        () ->
        {
            int
                contador,
                tamanhoImagem, numeroComponentesCor;
            ByteBuffer imagem;

            synchronized ( this ) {
                tamanhoImagem = this.tamanhoImagem;
                numeroComponentesCor = this.numeroComponentesCor;
                imagem = this.visImagem;
            }
            
            while ( true ) {
                contador = 0;
                for( int i = 0; i < tamanhoImagem; i += numeroComponentesCor ) {
                    imagem.position( i );
                    if ( Byte.toUnsignedInt( imagem.get() ) == 255 )
                        contador++;
                }
                synchronized ( this ) {
                    saida = contador;
                }
                
                synchronized( sincronizador ) {
                    try {
                        sincronizador.wait();
                    } catch ( InterruptedException e ) {
                        return;
                    }
                }
            }
        }
    );
    
    public int getSaida() {
        synchronized ( this ) {
            return saida;
        }
    }
    
    // Verifica se todos os parâmetros obrigatórios foram devidamente inicializados
    public boolean preparado() {
        /*ByteBuffer visImagem;
        
        synchronized ( this ) {
            visImagem = this.visImagem;
        }*/
        
        synchronized ( this ) {
            return imagem != null && visImagem != null;
        }
    }
    
    // Verifica se o objeto está preparado e se não há outra execução ainda em curso
    public boolean pronto() {
        return !detector.isAlive() || detector.getState() == Thread.State.WAITING;
    }
    
    // Realiza a detecção de borda
    public void executar() {
        if ( !preparado() || !pronto() )
            return;
        
        if ( !detector.isAlive() )
            detector.start();
        else
            synchronized( sincronizador ) {
                sincronizador.notify();
            }
    }
    
    @Override
    public void close() {
        if ( detector.isAlive() )
            detector.interrupt();
    }
}
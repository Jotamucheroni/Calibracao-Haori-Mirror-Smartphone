package com.unesp.calibracao_haori;

public class Recurso {
    private final Object resposta = new Object();
    
    public void esperar() {
        synchronized ( resposta ) {
            try {
                resposta.wait();
            } catch ( InterruptedException e ) {
                e.printStackTrace();
            }
        }
    }
    
    public void liberarEspera() {
        synchronized ( resposta ) {
            resposta.notify();
        }
    }
}
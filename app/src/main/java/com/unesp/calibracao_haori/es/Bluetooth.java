package com.unesp.calibracao_haori.es;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.unesp.calibracao_haori.MainActivity;

public class Bluetooth implements AutoCloseable {
    private final MainActivity atividade;
    private final ByteBuffer bufferSaida;
    
    private final BluetoothAdapter bluetoothAdapter;
    private final TreeSet<BluetoothDevice> remoteDevices;
    private final BroadcastReceiver receiver;
    
    private final Object travaPesquisa = new Object();
    private boolean pesquisando = false;
    
    public Bluetooth( MainActivity atividade, ByteBuffer bufferSaida ) {
        this.atividade = atividade;
        this.bufferSaida = bufferSaida;
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        remoteDevices = new TreeSet<>( Comparator.comparing( BluetoothDevice::getAddress ) );
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent ) {
                if ( BluetoothDevice.ACTION_FOUND.equals( intent.getAction() ) ) {
                    BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE
                    );
                    
                    remoteDevices.add( device );
                    /*if ( remoteDevices.add( device ) ) {
                        String nome = device.getName();
                        Log.i(
                            "Dispositivo bluetooth",
                            device.getAddress()
                                +   ": "
                                +   ( ( nome == null ) ? "Nome indisponível" : nome )
                        );
                    }*/
                } else if (
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( intent.getAction() )
                )
                    synchronized ( travaPesquisa ) {
                        if ( pesquisando )
                            bluetoothAdapter.startDiscovery();
                    }
            }
        };
    }
    
    public Bluetooth( MainActivity atividade ) {
        this( atividade, null );
    }
    
    public ByteBuffer getBufferSaida(){
        return bufferSaida;
    }
    
    public void pesquisarDispositivos() {
        if ( bluetoothAdapter == null || atividade == null )
            return;
        
        synchronized ( travaPesquisa ) {
            if ( pesquisando )
                return;
            
            if ( !bluetoothAdapter.isEnabled() )
                atividade.ativarBluetooth();
            
            atividade.registerReceiver(
                receiver, new IntentFilter( BluetoothDevice.ACTION_FOUND )
            );
            atividade.registerReceiver(
                receiver, new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED )
            );
            
//            Log.i( "Bluetooth", "Descoberta de dispositivos iniciada:" );
            bluetoothAdapter.startDiscovery();
            pesquisando = true;
        }
    }
    
    public void encerrarPesquisa() {
        if ( bluetoothAdapter == null || atividade == null )
            return;
        
        synchronized ( travaPesquisa ) {
            if ( !pesquisando )
                return;
            
            pesquisando = false;
            bluetoothAdapter.cancelDiscovery();
            atividade.unregisterReceiver( receiver );
//            Log.i( "Bluetooth", "Descoberta de dispositivos encerrada." );
        }
    }
    
    private BluetoothSocket soquete;
    private final Object travaSoquete = new Object();
    
    private void enviarDados() {
        if ( bufferSaida == null )
            return;
        
        OutputStream saida;
        synchronized ( travaSoquete ) {
            if ( soquete == null )
                return;
            
            try {
                saida = soquete.getOutputStream();
            } catch ( IOException e ) {
                e.printStackTrace();
                
                return;
            }
        }
        ByteBuffer visBufferSaida = bufferSaida.asReadOnlyBuffer();
        
        new Thread(
            () -> 
            {
                try {
                    byte[] vetorBytes = new byte[visBufferSaida.capacity()];
                    while( true ) {
                        visBufferSaida.rewind();
                        visBufferSaida.get( vetorBytes );
                        saida.write( vetorBytes );
                        Thread.sleep( 41 );
                    }
                } catch ( IOException | InterruptedException ignored ) {}
            }
        ).start();
    }

    private float[] dados;
    private boolean exibirQuadradoCalibracao = false;
    private boolean exibirQuadradoTeste = false;
    private final float[]
        dadosQuadradoCalibracao = new float[]{
            0.98f, 1.26f, 0.25f, 0.25f, 90.00f, 90.00f, 88.00f
        },
        dadosQuadradoTeste = new float[]{
            1.0f + 1000, 1.0f + 1000,
            0.0f + 1000, 0.0f + 1000,
            0.0f + 90 + 1000, 0.0f + 90 + 1000, 0.0f + 90 + 1000,
            1.0f + 1000, 1.0f + 10000,
            0.0f + 1000, 0.0f + 1000, 1.0f + 1000,
            0.0f + 1000, 0.0f + 1000
        };
    private float sinal = 0;
    
    private void receberDados() {
        DataInputStream entrada;
        
        synchronized ( travaSoquete ) {
            if ( soquete == null )
                return;
            
            try {
                entrada = new DataInputStream( soquete.getInputStream() );
            } catch ( IOException e ) {
                e.printStackTrace();
                
                return;
            }
        }
        
        new Thread(
            () ->
            {
                try {
                    float dado;
                    
                    while( true ) {
                        do {
                            sinal = entrada.readFloat();
                        } while( sinal >= 0 );
                        
                        switch ( (int) sinal ) {
                            case -1:
                                dados = dadosQuadradoCalibracao;
                                exibirQuadradoCalibracao = true;
                                
                                break;
                            
                            case -3:
                                dados = dadosQuadradoTeste;
                                exibirQuadradoTeste = true;
                                
                                break;
                            
                            default:
                                continue;
                        }
                        
                        for ( int i = 0; ; i++ ) {
                            dado = entrada.readFloat();
                            
                            if ( dado < 0 )
                                break;
                            
                            if ( i == dados.length )
                                i = 0;
                            
                            dados[i] = dado;
                        }
                        
                        switch ( (int) dado ) {
                            case -2:
                                exibirQuadradoCalibracao = false;
                                
                                break;
                            
                            case -4:
                                exibirQuadradoTeste = false;
                                
                                break;
                        }
                    }
                } catch ( IOException ignored ) {}
            }
        ).start();
    }
    
    public boolean exibirQuadradoCalibracao() {
        return exibirQuadradoCalibracao;
    }
    
    public float[] getParametrosQuadradoCalibracao() {
        return dadosQuadradoCalibracao.clone();
    }
    
    public boolean exibirQuadradoTeste() {
        return exibirQuadradoTeste;
    }
    
    public float[] getParametrosQuadradoTeste() {
        return dadosQuadradoTeste.clone();
    }
    
    private Thread servidor;
    private BluetoothServerSocket soqueteServidor;
    private final Object travaSoqueteServidor = new Object();
    
    public void abrirServidor() {
        if ( bluetoothAdapter == null || atividade == null )
            return;
        
        if ( servidor != null )
            if ( servidor.isAlive() )
                return;
        
        synchronized ( travaSoqueteServidor ) {
            try {
                soqueteServidor = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                    "Calibração",
                    UUID.fromString( "7427f3ad-d28e-4267-a5b5-f358165eac26" )
                );
            } catch ( IOException ignored ) {
                return;
            }
        }
        
        servidor = new Thread(
            () -> 
            {
                if ( !bluetoothAdapter.isEnabled() )
                    atividade.ativarBluetooth();
                atividade.tornarDispositivoVisivel( 30 );
                
                try {
                    BluetoothSocket soquete = soqueteServidor.accept();
                    synchronized ( travaSoqueteServidor ) {
                        soqueteServidor.close();
                    }
                    
                    synchronized ( travaSoquete ) {
                        fecharSoquete();
                        this.soquete = soquete;
                    }
                    enviarDados();
                    receberDados();
                } catch( IOException ignored ) {}
            }
        );
        servidor.start();
    }
    
    public void fecharServidor() {
        synchronized ( travaSoqueteServidor ) {
            if ( soqueteServidor == null )
                return;
            
            try {
                soqueteServidor.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    
    public void fecharSoquete() {
        synchronized ( travaSoquete ) {
            if ( soquete == null )
                return;
            
            try {
                soquete.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void close() {
        encerrarPesquisa();
        fecharSoquete();
        fecharServidor();
    }
}
package com.unesp.calibracao_haori.es;

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
import android.util.Log;

import androidx.annotation.NonNull;

import com.unesp.calibracao_haori.MainActivity;

public class Bluetooth implements AutoCloseable {
    private final BluetoothAdapter bluetoothAdapter;
    
    private MainActivity atividade;
    private int tamanhoBufferSaida;
    private ByteBuffer bufferSaida;
    
    private TreeSet<BluetoothDevice> remoteDevices;
    private BroadcastReceiver receiver;
    
    private BluetoothServerSocket soqueteServidor;
    
    private boolean pesquisando, servindo, ligado;
    private final Object
        travaPesquisa = new Object(),
        travaServidor = new Object(),
        travaLigado = new Object();
    
    public Bluetooth(
        MainActivity atividade, int tamanhoBufferSaida, ByteBuffer bufferSaida
    ) {
        pesquisando = false;
        servindo = false;
        ligado = false;
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( bluetoothAdapter == null )
            return;
        
        setAtividade( atividade );
        setTamanhoBufferSaida( tamanhoBufferSaida );
        setBufferSaida( bufferSaida );
        
        remoteDevices = new TreeSet<>(
            Comparator.comparing( BluetoothDevice::getAddress )
        );
        
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent ) {
                synchronized ( travaLigado ) {
                    if ( !ligado )
                        return;
                }
                
                synchronized ( travaPesquisa ) {
                    if ( !pesquisando )
                        return;
                    
                    if ( BluetoothDevice.ACTION_FOUND.equals( intent.getAction() ) ) {
                        BluetoothDevice device = intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE
                        );
                        
                        if ( remoteDevices.add( device ) ) {
                            String nome = device.getName();
                            Log.i(
                                "Dispositivo bluetooth",
                                device.getAddress()
                                    +   ": "
                                    +   ( ( nome == null ) ? "Nome indisponível" : nome )
                            );
                        }
                    } else if (
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( intent.getAction() )
                    )
                        bluetoothAdapter.startDiscovery();
                }
            }
        };
        
        try {
            soqueteServidor = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                "Calibração",
                UUID.fromString( "7427f3ad-d28e-4267-a5b5-f358165eac26" )
            );
        } catch ( IOException e ) {
            e.printStackTrace();
            
            return;
        }
        
        ligado = true;
    }
    
    public void setAtividade( MainActivity atividade ) {
        this.atividade = atividade;
        
        if ( atividade == null )
            return;
        
        if ( !bluetoothAdapter.isEnabled() )
            atividade.ativarBluetooth();
    }
    
    public void setTamanhoBufferSaida( int tamanhoBufferSaida ) {
        if ( tamanhoBufferSaida < 1 )
            tamanhoBufferSaida = 1;
        
        this.tamanhoBufferSaida = tamanhoBufferSaida;
    }
    
    public void setBufferSaida( ByteBuffer bufferSaida ) {
        if ( bufferSaida == null ) {
            this.bufferSaida = null;
            
            return;
        }
        
        this.bufferSaida = bufferSaida.asReadOnlyBuffer();
    }
    
    public void pesquisarDispositivos() {
        synchronized ( travaLigado ) {
            if ( !ligado )
                return;
        }
        
        synchronized ( travaPesquisa ) {
            if ( pesquisando )
                return;
            
            if ( bluetoothAdapter == null || atividade == null )
                return;
            
            atividade.registerReceiver(
                receiver, new IntentFilter( BluetoothDevice.ACTION_FOUND )
            );
            atividade.registerReceiver(
                receiver, new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED )
            );
            Log.i( "Bluetooth", "Descoberta de dispositivos iniciada:" );
            bluetoothAdapter.startDiscovery();
            
            pesquisando = true;
        }
    }
    
    public void encerrarPesquisa() {
        synchronized ( travaPesquisa ) {
            if ( !pesquisando )
                return;
            
            pesquisando = false;
            
            if ( bluetoothAdapter == null || atividade == null )
                return;
            
            bluetoothAdapter.cancelDiscovery();
            Log.i( "Bluetooth", "Descoberta de dispositivos encerrada." );
            atividade.unregisterReceiver( receiver );
        }
    }
    
    private void enviarDados( @NonNull BluetoothSocket soquete ) {
        new Thread(
            () -> 
            {
                try {
                    OutputStream output = soquete.getOutputStream();
                    
                    byte[] b = new byte[tamanhoBufferSaida];
                    while( true ) {
                        bufferSaida.rewind();
                        bufferSaida.get( b );
                        output.write( b );
                        output.flush();
                        Thread.sleep( 41 );
                    }
                } catch ( IOException | InterruptedException ignored ) {}
            }
        ).start();
    }
    
    private BluetoothSocket soquete;
    
    public void abrirServidor() {
        synchronized ( travaLigado ) {
            if ( !ligado )
                return;
        }
        
        new Thread(
            () -> 
            {
                synchronized ( travaServidor ) {
                    if ( servindo )
                        return;
                    
                    atividade.tornarDispositivoVisivel( 30 );
                    servindo = true;
                }
                
                try {
                    BluetoothSocket soquete = soqueteServidor.accept();
                    
                    synchronized ( travaServidor ) {
                        servindo = false;
                        soqueteServidor.close();
                        
                        if ( this.soquete != null )
                            this.soquete.close();
                        
                        this.soquete = soquete;
                        enviarDados( soquete );
                    }
                } catch( IOException ignored ) {}
            }
        ).start();
    }
    
    public void fecharServidor() {
        synchronized ( travaServidor ) {
            if ( !servindo )
                return;

            servindo = false;

            try {
                soqueteServidor.close();
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
    
    public void fecharSoquete() {
        synchronized ( travaServidor ) {
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
        synchronized ( travaLigado ) {
            if ( !ligado )
                return;
            
            ligado = false;
        }
        
        encerrarPesquisa();
        fecharServidor();
        fecharSoquete();
    }
}
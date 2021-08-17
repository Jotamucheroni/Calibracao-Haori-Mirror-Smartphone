package com.unesp.calibracao_haori.es;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
//import java.util.TreeSet;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
/*import android.content.BroadcastReceiver;
import android.content.Context;*/
import android.content.Intent;

import androidx.annotation.NonNull;

import com.unesp.calibracao_haori.MainActivity;
/*import android.content.IntentFilter;
import android.util.Log;*/

public class Bluetooth implements AutoCloseable {
    private final MainActivity activity;
    private final BluetoothAdapter bluetoothAdapter;
/*    private final TreeSet<BluetoothDevice> remoteDevices;
    
    private final BroadcastReceiver receiver;*/
    
    private final int tamBufferSaida;
    private final ByteBuffer bufferSaida;
    
    public Bluetooth(
            MainActivity activity, int tamBufferSaida, ByteBuffer bufferSaida
    ) throws Exception {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if ( bluetoothAdapter == null )
            throw new Exception( "O dispositivo não é compatível com Bluetooth" );
        if ( activity == null )
            throw new Exception( "A atividade relacionada é nula" );
        this.activity = activity;
        
        if ( !bluetoothAdapter.isEnabled() )
            activity.ativarBluetooth();
        
        /*remoteDevices = new TreeSet<BluetoothDevice>(
            ( o1, o2 ) -> o1.getAddress().compareTo( o2.getAddress() )
        );
        
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive( Context context, Intent intent ) {
                if ( BluetoothDevice.ACTION_FOUND.equals( intent.getAction() ) ) {
                    BluetoothDevice device = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE
                    );
                    
                    if ( remoteDevices.add( device ) ) {
                        String nome = device.getName();
                        Log.i(
                            "Dispositivo bluetooth",
                            device.getAddress()
                                + ": "
                                + ( ( nome == null ) ? "Nome indisponível" : nome )
                        );
                    }
                }
                else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( intent.getAction() ) )
                    bluetoothAdapter.startDiscovery();
            }
        };*/
        
        this.tamBufferSaida = tamBufferSaida;
        this.bufferSaida = bufferSaida.asReadOnlyBuffer();
    }
    
    public Bluetooth( MainActivity activity ) throws Exception {
        this( activity, 1, ByteBuffer.allocateDirect( 1 ) );
    }
    
    /*public void pesquisarDispositivos() {
        activity.registerReceiver( receiver, new IntentFilter( BluetoothDevice.ACTION_FOUND ) );
        activity.registerReceiver(
            receiver, new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED )
        );
        Log.i( "Bluetooth", "Descoberta de dispositivos iniciada:" );
        bluetoothAdapter.startDiscovery();
    }
    
    public void encerrarPesquisa() {
        bluetoothAdapter.cancelDiscovery();
        Log.i( "Bluetooth", "Descoberta de dispositivos encerrada." );
        activity.unregisterReceiver( receiver );
    }*/
    
    private Thread enviarDados( @NonNull BluetoothSocket socket ) {
        Thread thread = new Thread(
            () -> {
                try {
                    OutputStream output = socket.getOutputStream();
                    
                    byte[] b = new byte[tamBufferSaida];
                    while( true ) {
                        bufferSaida.rewind();
                        bufferSaida.get( b );
                        output.write( b );
                        output.flush();
                    }
                } catch ( IOException ignored ) {}
            }
        );
        thread.start();
        
        return thread;
    }
    
    /*private final int numElem = 10;
    private final int numBytes = numElem * Integer.BYTES;
    
    private Thread receberDados( BluetoothSocket socket ) {
        Thread thread = new Thread(
            () ->
            {
                try {
                    InputStream input = socket.getInputStream();
                    
                    ByteBuffer bb = ByteBuffer.allocateDirect( numBytes );
                    byte[] b = new byte[numBytes];
                    while( true ) {
                        if ( input.read( b ) == -1 )
                            return;
                        bb.rewind();
                        bb.put( b );
                        bb.rewind();
                        System.out.print( bb.getInt() );
                        for ( int i = 1; i < numElem; i++ )
                            System.out.print( ", " + bb.getInt() );
                        System.out.println();
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        );
        thread.start();
        
        return thread;
    }*/
    
    private BluetoothServerSocket serverSocket;
    
    public void abrirServidor() {
        new Thread(
            () -> {
                try {
                    serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                        "Calibração",
                        UUID.fromString( "7427f3ad-d28e-4267-a5b5-f358165eac26" )
                    );
                    activity.tornarDispositivoVisivel( 30 );
                    BluetoothSocket socket = serverSocket.accept();
                    serverSocket.close();
                    
                    if ( socket != null ) {
                        Thread
//                            tReceber = receberDados( socket ),
                            tEnviar = enviarDados( socket );
                        
//                        tReceber.join();
                        tEnviar.join();
                        socket.close();
                    }
                } catch ( IOException | InterruptedException e ) {
                    e.printStackTrace();
                }
            }
        ).start();
    }
    
    public void fecharServidor() {
        if ( serverSocket == null )
            return;
        
        try {
            serverSocket.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void close() {
//        encerrarPesquisa();
        fecharServidor();
    }
}
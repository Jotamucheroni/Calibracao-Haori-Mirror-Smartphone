package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Config {
    public static void imprimir( @NonNull int[] configs ) {
        final int[] valConfig =  new int[1];
        
        for ( int config : configs ) {
            GLES32.glGetIntegerv( config, valConfig, 0 );
            Log.i( "OpenGL Config.", Integer.toString( valConfig[0] ) );
        }
    }

    public static void imprimir( @NonNull int[] configs, @NonNull String[] configNames ) {
        final int[] valConfig =  new int[1];
        
        for ( int i = 0; i < configs.length; i++ ) {
            GLES32.glGetIntegerv( configs[i], valConfig, 0 );
            Log.i( "OpenGL Config.", configNames[i] + ": " + valConfig[0] );
        }
    }
    
    public static void imprimir( @NonNull int[] configs, @NonNull int[] configNumComp ) {
        final int[] valConfig =  new int[Arrays.stream( configNumComp ).max().getAsInt()];
        
        for ( int i = 0; i < configs.length; i++ ) {
            GLES32.glGetIntegerv( configs[i], valConfig, 0 );

            StringBuilder info = new StringBuilder( Integer.toString( valConfig[0] ) );
            for ( int j = 1; j < configNumComp[i]; j++ )
                info.append(", ").append( valConfig[j] );
            Log.i( "OpenGL Config.", String.valueOf( info ) );
        }
    }

    public static void imprimir(
        @NonNull int[] configs, @NonNull String[] configNames, @NonNull int[] configNumComp
    ) {
        final int[] valConfig =  new int[Arrays.stream( configNumComp ).max().getAsInt()];
        
        for ( int i = 0; i < configs.length; i++ ) {
            GLES32.glGetIntegerv( configs[i], valConfig, 0 );
            
            StringBuilder info = new StringBuilder( Integer.toString( valConfig[0] ) );
            for ( int j = 1; j < configNumComp[i]; j++ )
                info.append(", ").append( valConfig[j] );
            Log.i( "OpenGL Config.", configNames[i] + ": " + String.valueOf( info ) );
        }
    }

    public static void imprimirConfigBuffer( int tipo, int buffer ) {
        final int[] bufferConfig =  new int[10];
        
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, bufferConfig, 0
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, bufferConfig, 1
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE, bufferConfig, 2
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE, bufferConfig, 3
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE, bufferConfig, 4
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE, bufferConfig, 5
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE, bufferConfig, 6
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE, bufferConfig, 7
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_COMPONENT_TYPE, bufferConfig, 8
        );
        GLES32.glGetFramebufferAttachmentParameteriv(
                tipo, buffer, GLES32.GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING, bufferConfig, 9
        );
        
        String tipoAlvo;
        switch ( bufferConfig[0] ) {
            case GLES32.GL_NONE:
                tipoAlvo = "nenhum";
                break;
            case GLES32.GL_FRAMEBUFFER_DEFAULT:
                tipoAlvo = "default framebuffer";
                break;
            case GLES32.GL_TEXTURE:
                tipoAlvo = "textura";
                break;
            case GLES32.GL_RENDERBUFFER:
                tipoAlvo = "renderbuffer";
                break;
            default:
                tipoAlvo = "indeterminado";
        }
        Log.i( "Buffer Config - Tipo", tipoAlvo );
        
        Log.i( "Buffer Config - Nome", Integer.toString( bufferConfig[1] ) );
        
        Log.i( "Buffer Config - Red", bufferConfig[2] + " bits" );
        Log.i( "Buffer Config - Green", bufferConfig[3] + " bits" );
        Log.i( "Buffer Config - Blue", bufferConfig[4] + " bits" );
        Log.i( "Buffer Config - Alpha", bufferConfig[5] + " bits" );
        Log.i( "Buffer Config - Depth", bufferConfig[6] + " bits" );
        Log.i( "Buffer Config - Stencil", bufferConfig[7] + " bits" );
        
        String formatoInterno;
        switch ( bufferConfig[8] ) {
            case GLES32.GL_FLOAT:
                formatoInterno = "float";
                break;
            case GLES32.GL_INT:
                formatoInterno = "int";
                break;
            case GLES32.GL_UNSIGNED_INT:
                formatoInterno = "unsigned int";
                break;
            case GLES32.GL_SIGNED_NORMALIZED:
                formatoInterno = "signed normalized";
                break;
            case GLES32.GL_UNSIGNED_NORMALIZED:
                formatoInterno = "unsigned normalized";
                break;
            default:
                formatoInterno = "indeterminado";
        }
        Log.i( "Buffer Config - Formato interno", formatoInterno );
        
        String espacoCor;
        switch ( bufferConfig[9] ) {
            case GLES32.GL_LINEAR:
                espacoCor = "linear";
                break;
            case GLES32.GL_SRGB:
                espacoCor = "sRGB";
                break;
            default:
                espacoCor = "indeterminado";
        }
        Log.i( "Buffer Config - Espaço de cores", espacoCor );
    }
}
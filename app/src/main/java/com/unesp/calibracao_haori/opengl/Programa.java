package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

public class Programa {
    public static int loadShader( int type, String shaderCode ) {
        int shader = GLES32.glCreateShader( type );
        
        GLES32.glShaderSource( shader, shaderCode );
        GLES32.glCompileShader( shader );
        int[] compilado = new int[1];
        GLES32.glGetShaderiv( shader, GLES32.GL_COMPILE_STATUS, compilado, 0 );
        
        // Verifica se houve erro de compilação
        if ( compilado[0] == 0 ) {
            System.out.println( "Um programa não pôde ser compilado:\n" + "Código fonte:" );
            
            int i = 1;
            for( String linha: shaderCode.split( "\n" ) ) {
                System.out.println( i + "\t" + linha );
                i++;
            }
        }
        
        return shader;
    }
    
    public static int gerarPrograma(
        @NonNull String vertexShaderCode,
        @NonNull String fragmentShaderCode
    ) {
        int program = GLES32.glCreateProgram();
        
        int vertexShader = loadShader( GLES32.GL_VERTEX_SHADER, vertexShaderCode );
        int fragmentShader = loadShader( GLES32.GL_FRAGMENT_SHADER, fragmentShaderCode );
        
        GLES32.glAttachShader( program, vertexShader );
        GLES32.glAttachShader( program, fragmentShader );
        GLES32.glLinkProgram( program );
        
        return program;
    }
    
    @NonNull
    public static String gerarCodigoVertexShader( boolean cor, boolean textura ) {
        StringBuilder codigo = new StringBuilder(
                "#version 320 es\n"
            +   "\n"
            +   "uniform mat4 escala;\n"
            +   "uniform mat4 rotX;\n"
            +   "uniform mat4 rotY;\n"
            +   "uniform mat4 rotZ;\n"
            +   "uniform mat4 trans;\n"
            +   "\n"
            +   "in vec4 pos;\n"
            +   "in vec4 cor;\n"
            +   "in vec2 tex;\n"
            +   "\n"
        );
        
        if ( cor )
            codigo.append( "out vec4 corFrag;\n" );
        
        if ( textura )
            codigo.append( "out vec2 texFrag;\n" );
        
        codigo
            .append( "\n" )
            .append(
                "void main() {\n"
            +   "    gl_Position = trans * rotZ * rotY * rotX * escala * pos;\n"
            +   "\n"
            );
        
        if ( cor )
            codigo.append( "corFrag = cor;\n" );
        
        if ( textura )
            codigo.append( "texFrag = tex;\n" );
        
        codigo.append( "}" );
        
        return codigo.toString();
    }
    
    private static final int numSaidas = 2;
    
    @NonNull
    public static String gerarCodigoFragmentShader(
        boolean cor, boolean textura, boolean texturaMonocromatica
    ) {
        StringBuilder codigo = new StringBuilder( 
                "#version 320 es\n"
            +   "\n"
            +   "precision mediump float;\n"
            +   "\n"
        );
        
        if ( cor )
            codigo.append( "in vec4 corFrag;\n" );
        
        if ( textura )
            codigo.append(
                    "in vec2 texFrag;\n"
                +   "\n"
                +   "layout(binding = 0) uniform sampler2D imagem;\n"
                +   "\n"
            );
        
        codigo
            .append( "out vec4 saida[" ).append( numSaidas ).append( "];\n" )
            .append( "\n" )
            .append( "void main() {\n" );
        
        if ( !textura ) {
            for( int i = numSaidas; i > 0; i-- )
                codigo.append( "saida[" ).append( i - 1 ).append( "] = " );
            
            if ( cor )
                codigo.append( "corFrag" );
            else
                codigo.append( "vec4( 1.0, 1.0, 1.0, 1.0 )" );
            
            codigo.append( ";\n}" );
            
            return codigo.toString();
        }
        
        codigo.append( "vec4 pixelCentral = texture( imagem, vec2( texFrag.x, texFrag.y ) );\n" );
        
        if ( texturaMonocromatica )
            codigo.append( "pixelCentral.b = pixelCentral.g = pixelCentral.r;\n" );
        
        if ( cor )
            codigo.append( "pixelCentral = 0.5 * corFrag + 0.5 * pixelCentral;\n" );
        
        codigo.append(
                "\n"
            +   "ivec2 tamanho = textureSize( imagem, 0 );\n"
            +   "vec2 dist = vec2( 1.0 / float( tamanho.x ), 1.0 / float( tamanho.y ) );\n"
            +   "\n"
            +   "float gx[3][3] = float[3][3](\n"
            +   "    float[3]( -1.0,  0.0,  1.0 ),\n"
            +   "    float[3]( -2.0,  0.0,  2.0 ),\n"
            +   "    float[3]( -1.0,  0.0,  1.0 )\n"
            +   ");\n"
            +   "float gy[3][3] = float[3][3](\n"
            +   "    float[3](  1.0,  2.0,  1.0 ),\n"
            +   "    float[3](  0.0,  0.0,  0.0 ),\n"
            +   "    float[3]( -1.0, -2.0, -1.0 )\n"
            +   ");\n"
            +   "\n"
            +   "vec4 corTex;\n"
            +   "\n"            
            +   "vec4 dx = vec4( 0.0 );\n"
            +   "vec4 dy = vec4( 0.0 );\n"
            +   "\n"
            +   "int i, j;\n"
            +   "for ( int y = -1; y <= 1; y++ )\n"
            +   "   for ( int x = -1; x <= 1; x++ ) {\n"
            +   "       i = y + 1;\n"
            +   "       j = x + 1;\n"
            +   "\n"
            +   "       corTex = texture(\n"
            +   "           imagem,\n"
            +   "           vec2( texFrag.x + float(x) * dist.x, texFrag.y + float(y) * dist.y )\n"
            +   "       );\n"
        );
        
        if ( texturaMonocromatica )
            codigo.append( "corTex.b = corTex.g = corTex.r;\n" );
        
        if ( cor )
            codigo.append( "corTex = 0.5 * corFrag + 0.5 * corTex;\n" );
        
        codigo.append(
                "\n"
            +   "        dx += gx[i][j] * corTex;\n"
            +   "        dy += gy[i][j] * corTex;\n"
            +   "    }"
            +   "\n"
            +   "    vec4 sobel = sqrt( dx * dx + dy * dy );\n"
            +   "\n"
            +   "    saida[0] = pixelCentral;\n"
            +   "    saida[1] = sobel;\n"
            +   "}"
        );
        
        return codigo.toString();
    }
    
    public static int gerarPrograma( boolean cor, boolean textura, boolean texturaMonocromatica ) {
        return gerarPrograma(
                gerarCodigoVertexShader( cor, textura ),
                gerarCodigoFragmentShader( cor, textura, texturaMonocromatica )
        );
    }
}
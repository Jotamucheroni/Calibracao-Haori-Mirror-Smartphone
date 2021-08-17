package com.unesp.calibracao_haori;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

public class ProgramaOpenGL {
    public static int loadShader( int type, String shaderCode ) {
        int shader = GLES32.glCreateShader( type );
        
        GLES32.glShaderSource( shader, shaderCode );
        GLES32.glCompileShader( shader );
        int[] compilado = new int[1];
        GLES32.glGetShaderiv( shader, GLES32.GL_COMPILE_STATUS, compilado, 0 );
        
        // Verifica se houve erro de compilação
        if ( compilado[0] == 0 ) {
            // Imprime o código onde está localizado o erro
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
    
    private static final String vertexShaderCabRef =
            "#version 320 es\n"
        
        +   "uniform mat4 escala;\n"
        +   "uniform mat4 rotX;\n"
        +   "uniform mat4 rotY;\n"
        +   "uniform mat4 rotZ;\n"
        +   "uniform mat4 trans;\n"
        
        +   "in vec4 pos;\n"
        +   "in vec4 cor;\n"
        +   "in vec2 tex;\n"
        ;
    
    private static final String vertexShaderMainRef =
            "void main() {\n"
        +   "  gl_Position = trans * rotZ * rotY * rotX * escala * pos;\n"
        ;
    
    private static final String fragmentCabRef =
            "#version 320 es\n"
        
        +   "precision mediump float;\n"
        
        +   "layout(binding = 0) uniform sampler2D imagem;\n"
        
        /*+   "vec4 getInv( in vec4 corOrig ) {\n"
        
        +   "    return vec4( 1.0 ) - corOrig;\n"
        +   "}\n"
        
        +   "vec4[3][3] getInv( in vec4 janela[3][3] ) {\n"
        
        +   "    for ( int i = 0; i < 9; i++ )\n"
        +   "        janela[ i / 3 ][ i % 3 ] = getInv( janela[ i / 3 ][ i % 3 ] );\n"
        
        +   "    return janela;\n"
        +   "}\n"
        
        +   "vec4 getCinzaMed( in vec4 corOrig ) {\n"
        +   "    float cinza = ( corOrig.r + corOrig.g + corOrig.b ) / 3.0;\n"
        
        +   "    return vec4( cinza, cinza, cinza, corOrig.a );\n"
        +   "}\n"
        
        +   "vec4[3][3] getCinzaMed( in vec4 janela[3][3] ) {\n"
        
        +   "    for ( int i = 0; i < 9; i++ )\n"
        +   "        janela[ i / 3 ][ i % 3 ] = getCinzaMed( janela[ i / 3 ][ i % 3 ] );\n"
        
        +   "    return janela;\n"
        +   "}\n"
        
        +   "vec4 getCinzaPond( in vec4 corOrig ) {\n"
        +   "    float cinza = 0.2126 * corOrig.r + 0.7152 * corOrig.g + 0.0722 * corOrig.b;\n"
        
        +   "    return vec4( cinza, cinza, cinza, corOrig.a );\n"
        +   "}\n"
        
        +   "vec4[3][3] getCinzaPond( in vec4 janela[3][3] ) {\n"
        
        +   "    for ( int i = 0; i < 9; i++ )\n"
        +   "        janela[ i / 3 ][ i % 3 ] = getCinzaPond( janela[ i / 3 ][ i % 3 ] );\n"
        
        +   "    return janela;\n"
        +   "}\n"
        
        +   "vec4 getBor( in vec4 janela[3][3] ) {\n"
        +   "    vec4 soma = vec4( 0.0 );\n"
        
        +   "    for ( int i = 0; i < 9; i++ )\n"
        +   "        soma += janela[ i / 3 ][ i % 3 ];\n"
        
        +   "    return soma / 9.0;\n"
        +   "}\n"
        
        +   "vec4 getSobel( in vec4 janela[3][3] ) {\n"
        +   "    vec4 dx = - ( janela[0][0] + 2.0 * janela[1][0] + janela[2][0] ) + ( janela[0][2] + 2.0 * janela[1][2] + janela[2][2] );\n"
        +   "    vec4 dy =   ( janela[0][0] + 2.0 * janela[0][1] + janela[0][2] ) - ( janela[2][0] + 2.0 * janela[2][1] + janela[2][2] );\n"
        
        +   "    return sqrt( dx * dx + dy * dy );\n"
        +   "}\n"*/
        ;
    
    private static final String fragmentCabMainRef =
            "void main() {\n"
        ;
    
    private static final String corFragRef =
            "saida[1] = saida[0] = corFrag;\n"
        /*    "saida[0] = saida[4] = corFrag;\n"
        +   "saida[1] = saida[5] = getInv( saida[0] );\n"
        +   "saida[2] = saida[6] = getCinzaMed( saida[0] );\n"
        +   "saida[3] = saida[7] = getCinzaPond( saida[0] );\n"*/
        ;
    
    private static final int numSaidas = 2;
    
    private static final int[][][] programas = new int[][][] {
        { { 0, 0 }, { 0, 0 } }, { { 0, 0 }, { 0, 0 } }
    };
    
    public static int gerarPrograma( int numCompCor, int numCompTex, boolean texPb ) {
        int cor = ( numCompCor > 0 ) ? 1 : 0, tex = ( numCompTex > 0 ) ? 1 : 0, pb = texPb ? 1 : 0;
        
        if ( programas[cor][tex][pb] != 0 )
            return programas[cor][tex][pb];
        
        StringBuilder vertexShaderCode = new StringBuilder( vertexShaderCabRef );
        StringBuilder fragmentShaderCode = new StringBuilder( fragmentCabRef );
        
        if ( numCompCor > 0 ) {
            vertexShaderCode.append( "out vec4 corFrag;\n" );
            fragmentShaderCode.append( "in vec4 corFrag;\n" );
        }
        if ( numCompTex > 0 ) {
            vertexShaderCode.append( "out vec2 texFrag;\n" );
            fragmentShaderCode.append( "in vec2 texFrag;\n" );
        }
        
        vertexShaderCode.append( vertexShaderMainRef );
        fragmentShaderCode.append( "out vec4 saida[" )
            .append( numSaidas )
            .append( "];\n" )
            .append( fragmentCabMainRef );
        
        if ( ! ( numCompCor > 0 || numCompTex > 0 ) ) {
            fragmentShaderCode.append( "vec4 corFrag = vec4( 1.0, 1.0, 1.0, 1.0 );\n" );
            fragmentShaderCode.append( corFragRef );
        }
        else {
            if ( numCompCor > 0 ) {
                vertexShaderCode.append( "corFrag = cor;\n" );
                
                if ( ! ( numCompTex > 0 ) )
                    fragmentShaderCode.append( corFragRef );
            }
            if (numCompTex > 0) {
                vertexShaderCode.append( "texFrag = tex;\n" );
                fragmentShaderCode.append(
                        "ivec2 tamanho = textureSize( imagem, 0 );\n"
                    
                    +   "vec2 dist = vec2( 1.0 / float( tamanho.x ), 1.0 / float( tamanho.y ) );\n"
                    
                    +   "float gx[3][3] = float[3][3]( float[3]( -1.0,  0.0,  1.0 ), float[3]( -2.0,  0.0,  2.0 ), float[3]( -1.0,  0.0,  1.0 ) );\n"
                    +   "float gy[3][3] = float[3][3]( float[3](  1.0,  2.0,  1.0 ), float[3](  0.0,  0.0,  0.0 ), float[3]( -1.0, -2.0, -1.0 ) );\n"
                    
                    +   "vec4 janela[3][3];\n"
                    
                    +   "vec4 dx = vec4( 0.0 );\n"
                    +   "vec4 dy = vec4( 0.0 );\n"
                    
                    +   "for ( int y = -1; y <= 1; y++ )\n"
                    +   "    for ( int x = -1; x <= 1; x++ ) {\n"
                    +   "         int i = y + 1, j = x + 1;\n"
                    +   "         vec4 corTex = texture( imagem, vec2( texFrag.x + float(x) * dist.x, texFrag.y + float(y) * dist.y ) );\n"
                );
                
                if ( pb > 0 ) {
                    fragmentShaderCode.append(
                            "     corTex.b = corTex.g = corTex.r;\n"
                    );
                }
                
                if ( numCompCor > 0 ) {
                    fragmentShaderCode.append(
                            "     janela[i][j] = corTex = 0.5 * corFrag + 0.5 * corTex;\n"
                    );
                }
                else {
                    fragmentShaderCode.append(
                            "     janela[i][j] = corTex;\n"
                    );
                }
                
                fragmentShaderCode.append(
                        "         dx += gx[i][j] * corTex;\n"
                    +   "         dy += gy[i][j] * corTex;\n"
                    +   "}\n"

                    +   "vec4 sobel = sqrt( dx * dx + dy * dy );\n"

                    +   "saida[0] = janela[1][1];\n"
                    +   "saida[1] = sobel;\n"
                    /*+   "saida[2] = ( sobel.r > 0.5 ) ? sobel : vec4( 0.0 );\n"
                    +   "saida[3] = ( sobel.r > 0.7 ) ? sobel : vec4( 0.0 );\n"
                    +   "saida[4] = ( sobel.r > 0.9 ) ? sobel : vec4( 0.0 );\n"
                    +   "saida[5] = ( sobel.r > 1.1 ) ? sobel : vec4( 0.0 );\n"
                    +   "saida[6] = ( sobel.r > 1.3 ) ? sobel : vec4( 0.0 );\n"
                    +   "saida[7] = ( sobel.r > 1.5 ) ? sobel : vec4( 0.0 );\n"*/
                );
            }
        }
        
        vertexShaderCode.append( "}" );
        fragmentShaderCode.append( "}" );
        
        programas[cor][tex][pb] = gerarPrograma(
                vertexShaderCode.toString(), fragmentShaderCode.toString()
        );
        
        return programas[cor][tex][pb];
    }
    
    public static void liberarRecursos() {
        for ( int[][] matProg : programas )
            for ( int[] vetProg : matProg )
                for ( int programa: vetProg )
                    GLES32.glDeleteProgram( programa );
    }
}
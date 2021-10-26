package com.unesp.calibracao_haori.opengl;

import android.opengl.GLES32;

import androidx.annotation.NonNull;

public class Programa implements AutoCloseable {
    private final int id;
    
    public Programa( boolean cor, boolean textura, boolean texturaMonocromatica ) {
        id = GLES32.glCreateProgram();
        
        int vertexShader = compilarShader(
                GLES32.GL_VERTEX_SHADER, gerarCodigoVertexShader( cor, textura )
        );
        int fragmentShader = compilarShader(
                GLES32.GL_FRAGMENT_SHADER, gerarCodigoFragmentShader( cor, textura, texturaMonocromatica )
        );
        
        GLES32.glAttachShader( id, vertexShader );
        GLES32.glAttachShader( id, fragmentShader );
        GLES32.glLinkProgram( id );
    }
    
    public Programa( boolean cor, boolean textura ) {
        this( cor, textura, false );
    }
    
    public Programa( boolean cor ) {
        this( cor, false, false );
    }
    
    public Programa() {
        this( false, false, false );
    }
    
    private static String gerarCodigoVertexShader( boolean cor, boolean textura ) {
        StringBuilder codigo = new StringBuilder(
                "#version 320 es\n"
            +   "\n"
            +   "uniform mat4 escala;\n"
            +   "uniform mat4 rotX;\n"
            +   "uniform mat4 rotY;\n"
            +   "uniform mat4 rotZ;\n"
            +   "uniform mat4 trans;\n"
            +   "uniform mat4 projecao;\n"
            +   "uniform mat4 translacaoTela;\n"
            +   "uniform mat4 rotacaoTelaX;\n"
            +   "uniform mat4 rotacaoTelaY;\n"
            +   "uniform mat4 rotacaoTelaZ;\n"
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
                +   "    if ( gl_Position.z > 0.0 ) {\n"
                +   "       float z = gl_Position.z, tx, ty;\n"
                            
                +   "       gl_Position = projecao * gl_Position;\n"
                +   "       gl_Position.x /= gl_Position.z;\n"
                +   "       gl_Position.y /= gl_Position.z;\n"
                +   "       gl_Position.z = 0.0;\n"
                +   "       gl_Position = rotacaoTelaZ * rotacaoTelaY * rotacaoTelaX * gl_Position;\n"
                +   "       tx = translacaoTela[3][0] / ( z * z );\n"
                +   "       ty = translacaoTela[3][1] / ( z * z );\n"
                +   "       gl_Position.x += tx;\n"
                +   "       gl_Position.y += ty;\n"
                +   "    }\n"
                +   "    gl_Position.z = 1.0;\n"
                +   "\n"
            );
        
        if ( cor )
            codigo.append( "corFrag = cor;\n" );
        
        if ( textura )
            codigo.append( "texFrag = tex;\n" );
        
        codigo.append( "}" );
        
        return codigo.toString();
    }
    
    public static final int MAXIMO_PARAMETROS_TEXTURA = 2;
    public static final int MAXIMO_SAIDAS = 8;
    
    private static String gerarCodigoFragmentShader(
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
        
        if ( textura ) {
            codigo.append(
                    "in vec2 texFrag;\n"
                +   "\n"
                +   "layout(binding = 0) uniform sampler2D imagem;\n"
                +   "\n"
                +   "float matrizSobelGx[3][3] = float[3][3](\n"
                +   "   float[3]( -1.0,  0.0,  1.0 ),\n"
                +   "   float[3]( -2.0,  0.0,  2.0 ),\n"
                +   "   float[3]( -1.0,  0.0,  1.0 )\n"
                +   ");\n"
                +   "float matrizSobelGy[3][3] = float[3][3](\n"
                +   "   float[3](  1.0,  2.0,  1.0 ),\n"
                +   "   float[3](  0.0,  0.0,  0.0 ),\n"
                +   "   float[3]( -1.0, -2.0, -1.0 )\n"
                +   ");\n"
                +   "\n"
            );
            codigo
                .append( "uniform float parametroTextura[" )
                .append( MAXIMO_PARAMETROS_TEXTURA )
                .append( "];\n" )
                .append( "\n" );
        }
        else
            codigo.append( "\n" );
        
        codigo
            .append( "out vec4 saida[" ).append( MAXIMO_SAIDAS ).append( "];\n" )
            .append( "\n" )
            .append( "void main() {\n" );
        
        if ( !textura ) {
            for( int i = MAXIMO_SAIDAS; i > 0; i-- )
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
            +   "vec4 corTex;\n"
            +   "\n"
            +   "vec4 sobelDx = vec4( 0.0 );\n"
            +   "vec4 sobelDy = vec4( 0.0 );\n"
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
            +   "        sobelDx += matrizSobelGx[i][j] * corTex;\n"
            +   "        sobelDy += matrizSobelGy[i][j] * corTex;\n"
            +   "    }"
            +   "\n"
            +   "sobelDx = abs( sobelDx ) / 4.0;\n"
            +   "sobelDy = abs( sobelDy ) / 4.0;\n"
            +   "\n"
            +   "vec4 sobel = sqrt( sobelDx * sobelDx + sobelDy * sobelDy );\n"
            +   "vec4 anguloSobel = vec4( 0.0 );\n"
            +   "\n"
            +   "if ( sobelDx.r >= sobelDy.r && sobelDx.r != 0.0 )\n"
            +   "   anguloSobel = vec4( sobelDy.r / sobelDx.r );\n"
            +   "else if ( sobelDy.r != 0.0 )\n"
            +   "    anguloSobel = vec4( sobelDx.r / sobelDy.r );\n"
            +   "\n"
            +   "bool condicaoIntensidadeSobel = sobel.r >= parametroTextura[0];\n"
            +   "bool condicaoAnguloSobel = anguloSobel.r >= parametroTextura[1];\n"
            +   "\n"
            +   "saida[0] = pixelCentral;\n"
            +   "saida[1] = sobel;\n"
            +   "saida[2] = condicaoIntensidadeSobel && condicaoAnguloSobel\n"
            +   "   ? vec4( 1.0 ) : vec4( 0.0 );\n"
            +   "}"
        );
        
        return codigo.toString();
    }
    
    private static int compilarShader( int tipo, String codigoShader ) {
        int shader = GLES32.glCreateShader( tipo );
        
        GLES32.glShaderSource( shader, codigoShader );
        GLES32.glCompileShader( shader );
        int[] compilado = new int[1];
        GLES32.glGetShaderiv( shader, GLES32.GL_COMPILE_STATUS, compilado, 0 );
        
        // Verifica se houve erro de compilação
        if ( compilado[0] == 0 ) {
            System.out.println( "Um programa não pôde ser compilado:\n" + "Código fonte:" );
            
            int i = 1;
            for( String linha: codigoShader.split( "\n" ) ) {
                System.out.println( i + "\t" + linha );
                i++;
            }
        }
        
        return shader;
    }
    
    public int getId() {
        return id;
    }
    
    public int getAttribLocation( String nomeAtributo ) {
        return GLES32.glGetAttribLocation( id, nomeAtributo );
    }
    
    public int getUniformLocation( String nomeUniforme ) {
        return GLES32.glGetUniformLocation( id, nomeUniforme );
    }
    
    public void ativar() {
        GLES32.glUseProgram( id );
    }
    
    public void desativar() {
        GLES32.glUseProgram( 0 );
    }
    
    @Override
    public void close(){
        GLES32.glDeleteProgram( id );
    }
}
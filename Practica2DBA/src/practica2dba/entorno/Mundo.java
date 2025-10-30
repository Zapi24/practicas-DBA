/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.entorno;

import practica2dba.utils.Coordenada;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *
 * @author zapi24
 */
public class Mundo{ //Clase que cartga el mundo y almacena syu estado 
    
    private int filas;
    private int columnas;
    private int[][] mapa; // -1 = Obstáculo, 0 = Libre

    public Mundo(String rutaFichero) throws IOException {
        System.out.println("[Mundo] Cargando mapa desde: " + rutaFichero);
        cargarDesdeFichero(rutaFichero);
        System.out.println("[Mundo] Mapa de " + filas + "x" + columnas + " cargado.");
    }

    private void cargarDesdeFichero(String rutaFichero) throws IOException {
        File fichero = new File(rutaFichero);
        if (!fichero.exists()) {
            throw new IOException("El fichero del mapa no existe en: " + fichero.getAbsolutePath());
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(fichero))) {
            this.filas = Integer.parseInt(br.readLine());
            this.columnas = Integer.parseInt(br.readLine());
            this.mapa = new int[filas][columnas];

            String linea;
            for (int f = 0; f < filas; f++) {
                linea = br.readLine();
                if (linea == null) throw new IOException("Formato de mapa incorrecto: Faltan filas.");
                
                StringTokenizer tokenizer = new StringTokenizer(linea); 
                for (int c = 0; c < columnas; c++) {
                    if (!tokenizer.hasMoreTokens()) throw new IOException("Formato de mapa incorrecto: Faltan columnas en fila " + f);
                    this.mapa[f][c] = Integer.parseInt(tokenizer.nextToken());
                }
            }
        } catch (NumberFormatException e) {
            throw new IOException("Error de formato numérico en el fichero del mapa.", e);
        }
    }

    public boolean isDentroDeLimites(int x, int y) {
        return (y >= 0 && y < filas && x >= 0 && x < columnas);
    }

    public int getValorCelda(int x, int y) {
        if (!isDentroDeLimites(x, y)) {
            return -1; // Fuera de límites es obstáculo
        }
        return mapa[y][x]; // [fila][columna] -> [y][x]
    }

    public boolean isCeldaTransitable(int x, int y){
        
        return isDentroDeLimites(x, y) && getValorCelda(x, y) == 0;
    }
    
    public boolean isCeldaMuro(int x, int y){
        
        return getValorCelda(x,y) == -1;
    }

    public void imprimirMundo(Coordenada posAgente) {
        final String ANSI_RESET = "\u001B[0m";
        final String ANSI_RED_BG = "\u001B[41m";
        final String ANSI_BLACK_BG = "\u001B[40m";
        final String ANSI_WHITE = "\u001B[37m";

        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                if (posAgente != null && f == posAgente.getY() && c == posAgente.getX()) {
                    System.out.print(ANSI_RED_BG + ANSI_WHITE + " R " + ANSI_RESET); // Agente
                } else if (mapa[f][c] == -1) {
                    System.out.print(ANSI_BLACK_BG + "   " + ANSI_RESET); // Obstáculo
                } else {
                    System.out.print(" . "); // Libre
                }
            }
            System.out.println();
        }
    }
    
}

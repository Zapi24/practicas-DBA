package interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class GridMapPanel extends JPanel {

    private int[][] mapa;
    private Coordenada agente;
    private Set<Coordenada> renos = new HashSet<>();
    private Coordenada posicionSanta;
    private Set<Coordenada> rastro = new HashSet<>();

    // imagenes
    private Image homerImage;
    private Image donutImage;
    private Image santaImage; 
    private Image groundImage;
    private Image obstacleImage;
    
    // Colores
    private final Color COLOR_MURO = new Color(50, 50, 60);
    private final Color COLOR_SUELO = new Color(245, 245, 245);
    private final Color COLOR_RASTRO = new Color(255, 240, 150);
    private final Color COLOR_SANTA = new Color(46, 204, 113); 
    private final Color COLOR_GRID = new Color(225, 225, 225);

    private boolean showGrid = true;

    public GridMapPanel(int[][] mapa, Coordenada agente, Set<Coordenada> renos, Coordenada santa) {
        this.mapa = mapa;
        this.agente = agente;
        this.renos = (renos != null) ? renos : new HashSet<>();
        this.posicionSanta = santa;
        setBackground(Color.WHITE);
        if (agente != null) rastro.add(agente);
        
        // Carga de imágenes
        cargarImagenes();
    }
    
    // cargamos las imagenes
    private void cargarImagenes() {
        try {
            homerImage = new ImageIcon("resources/homer.png").getImage();
            donutImage = new ImageIcon("resources/donut.png").getImage();
            santaImage = new ImageIcon("resources/santa_simpson.png").getImage(); 
            groundImage = new ImageIcon("resources/cesped.png").getImage();
            obstacleImage = new ImageIcon("resources/arbol.png").getImage();
            
            // Comprobacion de carga
            if (homerImage.getWidth(this) == -1 || donutImage.getWidth(this) == -1 || groundImage.getWidth(this) == -1 || obstacleImage.getWidth(this) == -1){
                System.err.println("[GUI] Advertencia: Una o más imágenes no se cargaron correctamente. Usando colores por defecto.");
            }
        } catch (Exception e) {
            System.err.println("[GUI] Error al cargar imágenes. Usando colores por defecto. Cree la carpeta 'resources' y añada 'homer.png', 'donut.png'.");
            e.printStackTrace();
        }
    }


    public void setAgente(Coordenada nuevaPosicion) {
        this.agente = nuevaPosicion;
        if (nuevaPosicion != null) this.rastro.add(nuevaPosicion);
        repaint();
    }

    public void setRenos(Set<Coordenada> nuevosRenos) {
        this.renos = nuevosRenos;
        repaint();
    }
    
    public void setPosicionSanta(Coordenada santa) {
        this.posicionSanta = santa;
        repaint();
    }

    public void setMapa(int[][] mapa) {
        this.mapa = mapa;
        this.rastro.clear();
        if (this.agente != null) rastro.add(this.agente);
        repaint();
    }
    
    public void removerReno(Coordenada renoEncontrado) {
        this.renos.remove(renoEncontrado);
        repaint();
    }
    
    public int[][] getMapa() {
        return mapa;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapa == null) return;

        Graphics2D g2d = (Graphics2D) g;

        int filas = mapa.length;
        int columnas = mapa[0].length;

        int cellWidth = getWidth() / columnas;
        int cellHeight = getHeight() / filas;
        int cellSize = Math.min(cellWidth, cellHeight);

        int offsetX = (getWidth() - (cellSize * columnas)) / 2;
        int offsetY = (getHeight() - (cellSize * filas)) / 2;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Dibujar mapa, rastro y fondo (Cesped/Muros)
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                int valor = mapa[y][x];
                int px = offsetX + x * cellSize;
                int py = offsetY + y * cellSize;

                if (valor == -1) {
                    // Muro
                    if (obstacleImage != null && obstacleImage.getWidth(this) != -1) {
                        // Dibujar la imagen del arbol
                        g2d.drawImage(obstacleImage, px, py, cellSize, cellSize, this);
                    } else {
                        // si no carga la img pintamos un color
                        g2d.setColor(COLOR_MURO);
                        g2d.fillRect(px, py, cellSize, cellSize); 
                    } 
                } else {
                    // Cesped
                    
                    if (rastro.contains(new Coordenada(x, y))) {
                        // Camino recorrido
                        g2d.setColor(COLOR_RASTRO);
                        g2d.fillRect(px, py, cellSize, cellSize);
                        
                    } else if (groundImage != null && groundImage.getWidth(this) != -1) {
                        // Dibujar el cesoed
                        g2d.drawImage(groundImage, px, py, cellSize, cellSize, this);
                        
                    } else {
                        // si no carga la img pintamos un color
                        g2d.setColor(COLOR_SUELO);
                        g2d.fillRect(px, py, cellSize, cellSize);
                    }
                }
                
                // Dibujar el grid
                if (showGrid && cellSize > 5) { 
                    g2d.setColor(COLOR_GRID);
                    g2d.drawRect(px, py, cellSize, cellSize);
                }
            }
        }

        // 2. Dibujar la Posición Final de Santa
        if (posicionSanta != null) {
            int gx = offsetX + posicionSanta.getX() * cellSize;
            int gy = offsetY + posicionSanta.getY() * cellSize;

            if (santaImage != null && santaImage.getWidth(this) != -1) {
                 g2d.drawImage(santaImage, gx, gy, cellSize, cellSize, this);
            } else {
                 // si no carga la img pintamos un color
                 g2d.setColor(COLOR_SANTA);
                 g2d.fillRect(gx, gy, cellSize, cellSize);
            }
        }

        // 3. Dibujar los Renos (Rosquillas)
        if (donutImage != null && donutImage.getWidth(this) != -1) {
            for(Coordenada reno : renos){
                int rx = offsetX + reno.getX() * cellSize;
                int ry = offsetY + reno.getY() * cellSize;
                g2d.drawImage(donutImage, rx, ry, cellSize, cellSize, this);
            }
        } else {
            // si no carga la img pintamos un color
            for(Coordenada reno : renos){
                int rx = offsetX + reno.getX() * cellSize;
                int ry = offsetY + reno.getY() * cellSize;
                g2d.setColor(Color.RED); 
                g2d.fillOval(rx + cellSize/6, ry + cellSize/6, cellSize - cellSize/3, cellSize - cellSize/3);
            }
        }
        
        // 4. Dibujar el Agente (Homer)
        if (agente != null) {
            int ax = offsetX + agente.getX() * cellSize;
            int ay = offsetY + agente.getY() * cellSize;

            if (homerImage != null && homerImage.getWidth(this) != -1) {
                g2d.drawImage(homerImage, ax, ay, cellSize, cellSize, this);
            } else {
                // si no carga la img pintamos un color
                g2d.setColor(Color.BLUE);
                int padding = cellSize / 6;
                int size = cellSize - (padding * 2);
                g2d.fillOval(ax + padding, ay + padding, size, size);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(ax + padding, ay + padding, size, size);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (mapa == null) return new Dimension(600, 600);
        int filas = mapa.length;
        int columnas = mapa[0].length;
        int maxDim = Math.max(filas, columnas);
        
        int desiredSize = Math.max(600, maxDim * 10); 
        return new Dimension(desiredSize, desiredSize);
    }
}
/*
    public int[][] getMapa() {
        return mapa;
    }
}
*/
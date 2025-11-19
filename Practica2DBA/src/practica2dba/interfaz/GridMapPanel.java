package practica2dba.interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class GridMapPanel extends JPanel {

    private int[][] mapa;
    private Coordenada agente;
    private Coordenada objetivo;
    private Set<Coordenada> rastro = new HashSet<>();

    // Colores
    private final Color COLOR_MURO = new Color(50, 50, 60);
    private final Color COLOR_SUELO = new Color(245, 245, 245);
    private final Color COLOR_RASTRO = new Color(255, 240, 150);
    private final Color COLOR_OBJETIVO = new Color(46, 204, 113);
    private final Color COLOR_AGENTE = new Color(52, 152, 219);
    private final Color COLOR_GRID = new Color(225, 225, 225);

    private boolean showGrid = true;

    public GridMapPanel(int[][] mapa, Coordenada agente, Coordenada objetivo) {
        this.mapa = mapa;
        this.agente = agente;
        this.objetivo = objetivo;
        setBackground(Color.WHITE);
        if (agente != null) rastro.add(agente);
    }

    public void setAgente(Coordenada nuevaPosicion) {
        this.agente = nuevaPosicion;
        if (nuevaPosicion != null) this.rastro.add(nuevaPosicion);
        repaint();
    }

    public void setObjetivo(Coordenada objetivo) {
        this.objetivo = objetivo;
        repaint();
    }

    public void setMapa(int[][] mapa) {
        this.mapa = mapa;
        this.rastro.clear();
        if (this.agente != null) rastro.add(this.agente);
        repaint();
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

        boolean modoSimple = cellSize < 15;

        //dibujar mapa
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                int valor = mapa[y][x];
                int px = offsetX + x * cellSize;
                int py = offsetY + y * cellSize;

                if (valor == -1) {
                    g2d.setColor(COLOR_MURO);
                } else {
                    if (rastro.contains(new Coordenada(x, y))) {
                        g2d.setColor(COLOR_RASTRO);
                    } else {
                        g2d.setColor(COLOR_SUELO);
                    }
                }
                g2d.fillRect(px, py, cellSize, cellSize);

                if (showGrid && cellSize > 5) { 
                    g2d.setColor(COLOR_GRID);
                    g2d.drawRect(px, py, cellSize, cellSize);
                }
            }
        }

        if (!modoSimple) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        //dibuja objetivo
        if (objetivo != null) {
            int gx = offsetX + objetivo.getX() * cellSize;
            int gy = offsetY + objetivo.getY() * cellSize;

            g2d.setColor(COLOR_OBJETIVO);
            
            if (modoSimple) {
                g2d.fillRect(gx, gy, cellSize, cellSize); //simple para mapas grandes
            } else {

                g2d.fillRect(gx, gy, cellSize, cellSize); //detallado para mapas pequeños
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(gx + 2, gy + 2, cellSize - 4, cellSize - 4);
                
                // Cruz decorativa pero en mapas grandes no se ve bien
                // g2d.drawLine(gx, gy, gx + cellSize, gy + cellSize);
                // g2d.drawLine(gx + cellSize, gy, gx, gy + cellSize);
            }
        }

        //dibujar agente
        if (agente != null) {
            g2d.setColor(COLOR_AGENTE);

            if (modoSimple) {
                g2d.fillOval(offsetX + agente.getX() * cellSize + 1, 
                             offsetY + agente.getY() * cellSize + 1, 
                             cellSize - 2, cellSize - 2);
            } else {
                int padding = cellSize / 6;
                int ax = offsetX + agente.getX() * cellSize + padding;
                int ay = offsetY + agente.getY() * cellSize + padding;
                int size = cellSize - (padding * 2);

                g2d.fillOval(ax, ay, size, size);

                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval(ax, ay, size, size);
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
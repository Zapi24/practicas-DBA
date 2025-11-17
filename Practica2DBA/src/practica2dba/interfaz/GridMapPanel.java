package practica2dba.interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;

/**
 * Panel que muestra visualmente el mapa, el agente y el objetivo.
 * Se adapta automáticamente al tamaño de la ventana (responsive).
 */
public class GridMapPanel extends JPanel {

    private int[][] mapa;
    private Coordenada agente;
    private Coordenada objetivo;
    private int padding = 2;
    private boolean showGrid = true;

    public GridMapPanel(int[][] mapa, Coordenada agente, Coordenada objetivo) {
        this.mapa = mapa;
        this.agente = agente;
        this.objetivo = objetivo;
        setBackground(Color.WHITE);
    }

    // ---- Setters dinámicos ----
    public void setAgente(Coordenada agente) {
        this.agente = agente;
        repaint();
    }

    public void setObjetivo(Coordenada objetivo) {
        this.objetivo = objetivo;
        repaint();
    }

    public void setMapa(int[][] mapa) {
        this.mapa = mapa;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    // ---- Recalcula tamaño según el espacio disponible ----
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (mapa == null) return;

        int filas = mapa.length;
        int columnas = mapa[0].length;

        // 🔹 Cálculo del tamaño de celda según el tamaño actual del panel
        int cellWidth = getWidth() / columnas;
        int cellHeight = getHeight() / filas;
        int cellSize = Math.min(cellWidth, cellHeight);

        // 🔹 Centramos el mapa si sobra espacio
        int offsetX = (getWidth() - (cellSize * columnas)) / 2;
        int offsetY = (getHeight() - (cellSize * filas)) / 2;

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                int valor = mapa[y][x];
                int px = offsetX + x * cellSize + padding;
                int py = offsetY + y * cellSize + padding;

                // Color según tipo de celda
                if (valor == -1) {
                    g.setColor(Color.DARK_GRAY); // obstáculo
                } else {
                    g.setColor(Color.LIGHT_GRAY); // libre
                }

                g.fillRect(px, py, cellSize - padding * 2, cellSize - padding * 2);

                if (showGrid) {
                    g.setColor(Color.GRAY);
                    g.drawRect(px, py, cellSize - padding * 2, cellSize - padding * 2);
                }
            }
        }

        // 🔹 Dibuja el objetivo
        if (objetivo != null) {
            int gx = offsetX + objetivo.getX() * cellSize + cellSize / 4;
            int gy = offsetY + objetivo.getY() * cellSize + cellSize / 4;
            int size = cellSize / 2;

            g.setColor(Color.RED);
            g.fillOval(gx, gy, size, size);
        }

        // 🔹 Dibuja el agente
        if (agente != null) {
            int ax = offsetX + agente.getX() * cellSize + cellSize / 4;
            int ay = offsetY + agente.getY() * cellSize + cellSize / 4;
            int size = cellSize / 2;

            g.setColor(Color.BLUE);
            g.fillOval(ax, ay, size, size);
        }
    }

    // ---- Tamaño preferido adaptable ----
    @Override
    public Dimension getPreferredSize() {
        if (mapa == null) return new Dimension(600, 600);

        int filas = mapa.length;
        int columnas = mapa[0].length;

        // 🔹 Tamaño base entre 400 y 800 px, adaptable al nº de celdas
        int baseSize = Math.min(800, Math.max(400, Math.max(filas, columnas) * 15));

        // Mantiene proporción cuadrada (importante para que no se deforme)
        return new Dimension(baseSize, baseSize);
    }
}

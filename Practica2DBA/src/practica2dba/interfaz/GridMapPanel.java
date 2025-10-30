package practica2dba.interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GridMapPanel extends JPanel {

    private int[][] mapa;
    private Coordenada agente;
    private Coordenada objetivo;
    private int cellSize = 40; // tamaño de cada celda
    private int padding = 2;
    private boolean showGrid = true;

    public GridMapPanel(int[][] mapa, Coordenada agente, Coordenada objetivo) {
        this.mapa = mapa;
        this.agente = agente;
        this.objetivo = objetivo;
        setPreferredSize(new Dimension(mapa[0].length * cellSize, mapa.length * cellSize));
        setBackground(Color.WHITE);
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mapa == null) return;

        for (int y = 0; y < mapa.length; y++) {
            for (int x = 0; x < mapa[0].length; x++) {
                int valor = mapa[y][x];
                int px = x * cellSize + padding;
                int py = y * cellSize + padding;

                if (valor == -1) {
                    g.setColor(Color.DARK_GRAY); // obstáculo
                } else {
                    g.setColor(Color.LIGHT_GRAY); // libre
                }

                g.fillRect(px, py, cellSize - padding * 2, cellSize - padding * 2);

                // Dibuja rejilla si está activada
                if (showGrid) {
                    g.setColor(Color.GRAY);
                    g.drawRect(px, py, cellSize - padding * 2, cellSize - padding * 2);
                }
            }
        }

        // Dibuja el objetivo
        if (objetivo != null) {
            g.setColor(Color.GREEN);
            g.fillOval(objetivo.getX() * cellSize + cellSize / 4,
                       objetivo.getY() * cellSize + cellSize / 4,
                       cellSize / 2, cellSize / 2);
        }

        // Dibuja el agente
        if (agente != null) {
            g.setColor(Color.RED);
            g.fillOval(agente.getX() * cellSize + cellSize / 4,
                       agente.getY() * cellSize + cellSize / 4,
                       cellSize / 2, cellSize / 2);
        }
    }
}

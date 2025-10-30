package practica2dba.interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private GridMapPanel panel;

    public VentanaPrincipal(int[][] mapa, Coordenada agente, Coordenada objetivo) {
        setTitle("Agent Rumba - Visualización");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new GridMapPanel(mapa, agente, objetivo);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(800, 600));
        add(scroll);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actualizar(Coordenada nuevaPosicion) {
        panel.setAgente(nuevaPosicion);
    }
}

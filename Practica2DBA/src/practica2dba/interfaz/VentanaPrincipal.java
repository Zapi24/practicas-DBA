package practica2dba.interfaz;

import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    private GridMapPanel panel;
    private JLabel lblBateria; //Para el texto de la bateria
    private int contadorBateria = 0; //Contador de batería

    public VentanaPrincipal(int[][] mapa, Coordenada agente, Coordenada objetivo) {
        setTitle("Agent Rumba - Visualización");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setLayout(new BorderLayout());

        //Panel para el mapa
        panel = new GridMapPanel(mapa, agente, objetivo);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(800, 600));
        add(scroll, BorderLayout.CENTER); // Lo añadimos al centro

        //Panel para el contador de baterí
        lblBateria = new JLabel("Batería actual: 0", SwingConstants.CENTER);
        lblBateria.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); //Le mete un poco de margen
        add(lblBateria, BorderLayout.SOUTH); //Lo ponemos abajo

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actualizar(Coordenada nuevaPosicion) {

        panel.setAgente(nuevaPosicion);

        contadorBateria++;
        lblBateria.setText("Batería actual: " + contadorBateria);
    }
   
}
package practica2dba.interfaz;

import practica2dba.entorno.Mundo;
import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// JADE Imports
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


 //Configura los parámetros (mapa, coordenadas) y visualiza la ejecución.
 
public class VentanaPrincipal extends JFrame {

    // Componentes visuales
    private GridMapPanel panel;
    private JLabel lblEstado; //batería
    
    // posiciones y mapas
    private JComboBox<String> cmbMapas;
    private JTextField txtXIni, txtYIni, txtXObj, txtYObj;
    private JButton btnLanzar;
    private ContainerController containerRef;
    
    
    private int contadorBateria = 0;
    private boolean agenteLanzado = false; // Para evitar lanzar dos veces


    public VentanaPrincipal() {
        setTitle("Agent Rumba - Configuración de Misión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // controles de arriba
        JPanel controlPanel = crearPanelControl();
        add(controlPanel, BorderLayout.NORTH);

        // panel del mapa
        panel = new GridMapPanel(null, new Coordenada(0,0), new Coordenada(0,0));
        
        // si el mapa es muy grande
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(800, 600));
        scroll.getVerticalScrollBar().setUnitIncrement(16); 
        add(scroll, BorderLayout.CENTER);

        // controlesa abajo y bateria
        lblEstado = new JLabel("Estado: Selecciona un mapa y lanza el agente.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblEstado, BorderLayout.SOUTH);

        // por defecto
        cargarPrevisualizacion();

        // Ajustar tamaño y mostrar
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
        setVisible(true);
    }

    private JPanel crearPanelControl() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Configuración"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Selección de Mapa
        c.gridx = 0; c.gridy = 0; 
        p.add(new JLabel("Mapa:"), c);
        
        String[] mapasDisponibles = listarMapas();
        cmbMapas = new JComboBox<>(mapasDisponibles);
        c.gridx = 1; c.gridy = 0; c.gridwidth = 3; 
        p.add(cmbMapas, c);
        
        cmbMapas.addActionListener(e -> cargarPrevisualizacion());

        //Coordenadas Agente
        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Inicio X:"), c);
        txtXIni = new JTextField("0", 3); // Valor por defecto
        c.gridx = 1; c.gridy = 1; p.add(txtXIni, c);

        c.gridx = 2; c.gridy = 1; p.add(new JLabel("Inicio Y:"), c);
        txtYIni = new JTextField("0", 3); 
        c.gridx = 3; c.gridy = 1; p.add(txtYIni, c);

        //Coordenadas Objetivo
        c.gridx = 0; c.gridy = 2; p.add(new JLabel("Objetivo X:"), c);
        txtXObj = new JTextField("9", 3);
        c.gridx = 1; c.gridy = 2; p.add(txtXObj, c);

        c.gridx = 2; c.gridy = 2; p.add(new JLabel("Objetivo Y:"), c);
        txtYObj = new JTextField("9", 3); 
        c.gridx = 3; c.gridy = 2; p.add(txtYObj, c);

        //Botón Lanzar 
        btnLanzar = new JButton("LANZAR AGENTE");
        btnLanzar.setBackground(new Color(46, 204, 113));
        btnLanzar.setForeground(Color.WHITE);
        btnLanzar.setFont(new Font("Arial", Font.BOLD, 12));
        btnLanzar.setFocusPainted(false);
        c.gridx = 4; c.gridy = 0; c.gridheight = 3; c.fill = GridBagConstraints.BOTH;
        p.add(btnLanzar, c);
        btnLanzar.addActionListener(e -> lanzarJade());

        return p;
    }

    private String[] listarMapas() {
        List<String> lista = new ArrayList<>();
        
        agregarMapasDeCarpeta("maps", lista);
        agregarMapasDeCarpeta("mapsDefensa", lista);

        if (lista.isEmpty()) {
            return new String[]{"No se encontraron mapas"};
        }
        return lista.toArray(new String[0]);
    }

    private void agregarMapasDeCarpeta(String carpeta, List<String> lista) {
        File dir = new File(carpeta);
        if (dir.exists() && dir.isDirectory()) {
            File[] archivos = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
            if (archivos != null) {
                for (File f : archivos) {
                    lista.add(carpeta + "/" + f.getName());
                }
            }
        }
    }

    private void cargarPrevisualizacion() {
        String rutaMapa = (String) cmbMapas.getSelectedItem();
        if (rutaMapa == null || rutaMapa.equals("No se encontraron mapas")) return;

        try {
            Mundo m = new Mundo(rutaMapa);
            panel.setMapa(m.getMapa()); // reseta la anterior ejecucion
            
            try {
                int ax = Integer.parseInt(txtXIni.getText());
                int ay = Integer.parseInt(txtYIni.getText());
                int ox = Integer.parseInt(txtXObj.getText());
                int oy = Integer.parseInt(txtYObj.getText());
                
                panel.setAgente(new Coordenada(ax, ay));
                panel.setObjetivo(new Coordenada(ox, oy));
            } catch (NumberFormatException ex) { }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer mapa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lanzarJade() {
        try {

            String rutaMapa = (String) cmbMapas.getSelectedItem();
            int xIni = Integer.parseInt(txtXIni.getText());
            int yIni = Integer.parseInt(txtYIni.getText());
            int xObj = Integer.parseInt(txtXObj.getText());
            int yObj = Integer.parseInt(txtYObj.getText());

            Mundo m = new Mundo(rutaMapa);
            panel.setMapa(m.getMapa());
            
            if (!m.isDentroDeLimites(xIni, yIni) || !m.isDentroDeLimites(xObj, yObj)) {
                JOptionPane.showMessageDialog(this, "Coordenadas fuera de límites.");
                return;
            }

            panel.setAgente(new Coordenada(xIni, yIni));
            panel.setObjetivo(new Coordenada(xObj, yObj));
            contadorBateria = 0;
            
            // Bloquea botón mientras que se ejecuta
            btnLanzar.setEnabled(false);
            btnLanzar.setText("EJECUTANDO...");
            btnLanzar.setBackground(Color.GRAY);

            if (containerRef == null) {
                System.out.println("Iniciando Servidor JADE por primera vez...");
                Runtime rt = Runtime.instance();
                Profile p = new ProfileImpl();
                containerRef = rt.createAgentContainer(p);
            }
            
            Object[] agentArgs = new Object[] {
                xIni, yIni, xObj, yObj, rutaMapa, this 
            };

            String nombreAgente = "Rumba_" + System.currentTimeMillis();
            
            AgentController ac = containerRef.createNewAgent(nombreAgente, "practica2dba.agente.AgenteRumba", agentArgs);
            ac.start();
            
            lblEstado.setText("Agente " + nombreAgente + " lanzado.");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
            habilitarControles();
        }
    }

    //cuando el agente termina
    public void habilitarControles() {
        SwingUtilities.invokeLater(() -> {
            btnLanzar.setEnabled(true);
            btnLanzar.setText("LANZAR DE NUEVO");
            btnLanzar.setBackground(new Color(46, 204, 113));
            lblEstado.setText("Fin de la simulación. Batería total: " + contadorBateria);
        });
    }

    public void actualizar(Coordenada nuevaPosicion) {
        //pinta el rastro
        panel.setAgente(nuevaPosicion);
        
        // Actualizamos contador
        contadorBateria++;
        lblEstado.setText("Batería consumida: " + contadorBateria);
        
        panel.scrollRectToVisible(new Rectangle(
            panel.getGraphics().getClipBounds()
        ));
    }
}
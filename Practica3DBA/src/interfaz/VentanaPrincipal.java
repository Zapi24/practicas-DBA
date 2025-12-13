package interfaz;

import practica2dba.entorno.Mundo;
import practica2dba.utils.Coordenada;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// JADE Imports
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;


public class VentanaPrincipal extends JFrame {

    // Componentes visuales
    private GridMapPanel panel;
    private JLabel lblEstado; 
    
    // posiciones y mapas
    private JComboBox<String> cmbMapas;
    private JTextField txtXIni, txtYIni;
    private JButton btnLanzar;
    private ContainerController containerRef;
    
    // Posiciones
    private final Coordenada POSICION_INICIAL_DEFECTO = new Coordenada(19, 19);
    private final Coordenada POSICION_SANTA_DEFECTO = null; 
    
    // mapa 100x100 posis renos para la visualizacion
    //private final String[] COORDS_RENOS_STR = {"11,12", "19,41", "41,60", "52,33", "63,34", "66,56", "92,74", "95,10"};
    
    //mapa 20x20 posis renos para la visualizacion
    private final String[] COORDS_RENOS_STR = {"1,1", "18,2", "11,4", "7,8", "15,10", "3,14", "10,17", "17,18"};
    
    public VentanaPrincipal() {
        setTitle("Práctica 3 DBA: Rescatando a los Renos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Controles de arriba
        JPanel controlPanel = crearPanelControl();
        add(controlPanel, BorderLayout.NORTH);

        // Panel del mapa
        Set<Coordenada> renosIniciales = getRenosCoords(COORDS_RENOS_STR);
        panel = new GridMapPanel(null, POSICION_INICIAL_DEFECTO, renosIniciales, POSICION_SANTA_DEFECTO);
        
        // Scroll para mapas grandes
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(800, 600));
        scroll.getVerticalScrollBar().setUnitIncrement(16); 
        add(scroll, BorderLayout.CENTER);

        lblEstado = new JLabel("Estado: Configura el mapa y lanza la misión.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Arial", Font.BOLD, 14));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(lblEstado, BorderLayout.SOUTH);

        cargarPrevisualizacion();

        // Ajustar tamaño y mostrar
        pack();
        setLocationRelativeTo(null); 
        setVisible(true);
        
        // Lanzamos los agentes permanentes (Santa, Elfo, Rudolph)
        iniciarAgentesBase();
    }
    
    // coordenadas de renos para la previsualización
    private Set<Coordenada> getRenosCoords(String[] coordsStr) {
        Set<Coordenada> coords = new HashSet<>();
        for (String s : coordsStr) {
            try {
                String[] parts = s.split(",");
                int x = Integer.parseInt(parts[0].trim());
                int y = Integer.parseInt(parts[1].trim());
                coords.add(new Coordenada(x, y));
            } catch (Exception ignored) {}
        }
        return coords;
    }
    
    private void iniciarAgentesBase() {
        try {
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);

            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            containerRef = rt.createMainContainer(p);

            // Agentes 
            containerRef.createNewAgent("santa", "Agentes.SantaClaus", null).start();
            containerRef.createNewAgent("elf", "Agentes.Elfo", null).start();
            containerRef.createNewAgent("rudolph", "Agentes.Rudolph", null).start();
            
            System.out.println("Agentes Santa, Elfo y Rudolph iniciados correctamente.");
            lblEstado.setText("Agentes permanentes listos. Introduce la posición inicial del Buscador.");

        } catch (Exception e){
            System.err.println("Error al iniciar agentes base: " + e.getMessage());
            lblEstado.setText("ERROR CRÍTICO: No se pudo iniciar JADE.");
            btnLanzar.setEnabled(false);
        }
    }


    private JPanel crearPanelControl() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Configuración Inicial del Buscador"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Selección de Mapa
        c.gridx = 0; c.gridy = 0; 
        p.add(new JLabel("Mapa:"), c);
        
        String[] mapasDisponibles = listarMapas();
        cmbMapas = new JComboBox<>(mapasDisponibles);
        cmbMapas.setSelectedItem("mapas-pr3/mapa-20x20.txt"); 
        c.gridx = 1; c.gridy = 0; c.gridwidth = 3; 
        p.add(cmbMapas, c);
        
        cmbMapas.addActionListener(e -> cargarPrevisualizacion());

        c.gridwidth = 1;
        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Inicio X (Buscador):"), c);
        txtXIni = new JTextField(String.valueOf(POSICION_INICIAL_DEFECTO.getX()), 3); 
        c.gridx = 1; c.gridy = 1; p.add(txtXIni, c);

        c.gridx = 2; c.gridy = 1; p.add(new JLabel("Inicio Y (Buscador):"), c);
        txtYIni = new JTextField(String.valueOf(POSICION_INICIAL_DEFECTO.getY()), 3); 
        c.gridx = 3; c.gridy = 1; p.add(txtYIni, c);


        //Botón Lanzar 
        btnLanzar = new JButton("LANZAR MISIÓN DE RESCATE");
        btnLanzar.setBackground(new Color(46, 204, 113));
        btnLanzar.setForeground(Color.WHITE);
        btnLanzar.setFont(new Font("Arial", Font.BOLD, 12));
        btnLanzar.setFocusPainted(false);
        c.gridx = 4; c.gridy = 0; c.gridheight = 2; c.fill = GridBagConstraints.BOTH;
        p.add(btnLanzar, c);
        btnLanzar.addActionListener(e -> lanzarBuscador());

        return p;
    }

    //los mapas de la carpeta
    private String[] listarMapas() {
        List<String> lista = new ArrayList<>();
        agregarMapasDeCarpeta("maps", lista);
        agregarMapasDeCarpeta("mapas-pr3", lista); 
        
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
        if (rutaMapa == null || rutaMapa.contains("No se encontraron mapas")) return;

        try {
            Mundo m = new Mundo(rutaMapa);
            panel.setMapa(m.getMapa()); // reseta el mapa y el rastro
            panel.setRenos(getRenosCoords(COORDS_RENOS_STR)); // Pinta todos los renos
            panel.setPosicionSanta(POSICION_SANTA_DEFECTO); 
            
            try {
                int ax = Integer.parseInt(txtXIni.getText());
                int ay = Integer.parseInt(txtYIni.getText());
                panel.setAgente(new Coordenada(ax, ay));
            } catch (NumberFormatException ex) { }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al leer mapa: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void lanzarBuscador() {
        if (containerRef == null) {
            JOptionPane.showMessageDialog(this, "El contenedor JADE no está iniciado. Revisa la consola.");
            return;
        }
        
        try {
            String rutaMapa = (String) cmbMapas.getSelectedItem();
            int xIni = Integer.parseInt(txtXIni.getText());
            int yIni = Integer.parseInt(txtYIni.getText());

            Mundo m = new Mundo(rutaMapa);
            
            if (!m.isDentroDeLimites(xIni, yIni)) {
                JOptionPane.showMessageDialog(this, "Coordenadas iniciales fuera de límites.");
                return;
            }

            // 1. Configurar la GUI antes de lanzar el agente
            panel.setMapa(m.getMapa());
            panel.setAgente(new Coordenada(xIni, yIni));
            panel.setRenos(getRenosCoords(COORDS_RENOS_STR));
            panel.setPosicionSanta(POSICION_SANTA_DEFECTO); 
            
            // 2. Bloquear botón
            btnLanzar.setEnabled(false);
            btnLanzar.setText("BUSCADOR EJECUTANDO...");
            btnLanzar.setBackground(Color.GRAY);
            
            // 3. Argumentos para el Agente
            Object[] agentArgs = new Object[] {
                rutaMapa, xIni, yIni, this 
            };

            String nombreAgente = "buscador"; // El nombre debe ser 'buscador' para que Elfo lo encuentre
            
            AgentController ac = containerRef.createNewAgent(nombreAgente, "Agentes.AgenteNuestro", agentArgs);
            ac.start();
            
            lblEstado.setText("Agente Buscador lanzado. Esperando respuesta de Santa...");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            ex.printStackTrace();
            habilitarControles(0, "ERROR: Revisa la consola.");
        }
    }

    // Cuando el agente termina 
    public void habilitarControles(int pasosTotales, String mensajeFinal) {
        SwingUtilities.invokeLater(() -> {
            btnLanzar.setEnabled(true);
            btnLanzar.setText("LANZAR NUEVA MISIÓN");
            btnLanzar.setBackground(new Color(46, 204, 113));
            lblEstado.setText(mensajeFinal + " Pasos totales de movimiento: " + pasosTotales);
        });
    }
    
    // Método para que el Agente llame al moverse
    public void actualizarPosicion(Coordenada nuevaPosicion) {
        SwingUtilities.invokeLater(() -> {
            panel.setAgente(nuevaPosicion);
            
            // scroll para mapas grandes
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, panel);
            if (scrollPane != null && panel.getMapa() != null) {
                int[][] currentMap = panel.getMapa();
                
                // Calculamos y ejecutamos el scroll
                panel.scrollRectToVisible(new Rectangle(
                    nuevaPosicion.getX() * panel.getPreferredSize().width / currentMap[0].length,
                    nuevaPosicion.getY() * panel.getPreferredSize().height / currentMap.length,
                    10, 10
                ));
            }
        });
    }
    
    // Agente borre un reno al rescatarlo
    public void renoRescatado(Coordenada posReno) {
        SwingUtilities.invokeLater(() -> {
            panel.removerReno(posReno);
        });
    }
    
    // Agente actualice la posición final de Santa
    public void actualizarPosicionFinalSanta(Coordenada posSanta) {
        SwingUtilities.invokeLater(() -> {
            panel.setPosicionSanta(posSanta);
        });
    }
}
package practica2dba;

import practica2dba.interfaz.VentanaPrincipal;
import javax.swing.SwingUtilities;

// Lanza la aplicacion
public class Practica2DBA {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO INTERFAZ AGENTE RUMBA ---");

        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal();
        });
    }
}
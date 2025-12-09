package practica2dba.agente;

import practica2dba.entorno.Entorno;
import practica2dba.estrategia.*;
import practica2dba.interfaz.VentanaPrincipal;
import practica2dba.utils.*;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;


public class AgenteRumba extends Agent{
    
    private Entorno entorno;
    private Coordenada objetivo;
    private EstrategiaMovimiento estrategia;
    private VentanaPrincipal ventana;

    
    @Override
    protected void setup(){
        
        System.out.println("Iniciando agente: " + getAID().getLocalName());

        try{
            Object[] args = getArguments();
            if (args != null && args.length == 6){  
                
                //Pos inicial
                int x_ini = (int) args[0];
                int y_ini = (int) args[1];
                
                //Pos objetivo
                int x_obj = (int) args[2];
                int y_obj = (int) args[3];
                
                //Ruta del archivo del mapa
                String rutaMapa = (String) args[4];

                // La ventana creada por el main
                this.ventana = (VentanaPrincipal) args[5];

                this.objetivo = new Coordenada(x_obj, y_obj);
                this.entorno = new Entorno(rutaMapa, new Coordenada(x_ini, y_ini));
                
                // Estratedia elegida
                this.estrategia = new EstrategiaNat();

                System.out.println("Configuración: Inicio (" + x_ini + "," + y_ini + "), Objetivo (" + x_obj + "," + y_obj + ")");
                System.out.println("Estrategia usada: " + estrategia.getClass().getSimpleName());

                //consola
                System.out.println("\n--- MAPA INICIAL ---");
                entorno.imprimirMundoActual();

                addBehaviour(new TickerBehaviour(this, 200){ 
                    @Override
                    protected void onTick() {
                        ejecutarCiclo();
                    }
                });

            }else{
                System.err.println("Error: Argumentos incorrectos. (Se esperaban 6: x, y, ox, oy, mapa, ventana)");
                doDelete();
            }
        }catch(Exception e){
            System.err.println("Error en setup(): " + e.getMessage());
            e.printStackTrace();
            doDelete();
        }
    }
    
    private void ejecutarCiclo(){
        
        //percepcion
        Percepcion percepcion = entorno.getPercepcionActual();
        
        System.out.println("-------------------------------------");
        System.out.println("Posición actual: " + percepcion.getPosicionActual());

        //decide
        Movimiento proximoMovimiento = estrategia.decidirMovimiento(percepcion, objetivo);

        //actua
        System.out.println("DECISIÓN: Mover -> " + proximoMovimiento);
        ResultadoAccion resultado = entorno.ejecutarAccion(proximoMovimiento);
        
        // actualiza el UI
        if (this.ventana != null) {
            // Usamos invokeLater para asegurar que tocamos la GUI desde el hilo correcto
            javax.swing.SwingUtilities.invokeLater(() -> {
                this.ventana.actualizar(entorno.getPosicionActual());
            });
        }


        //comprueba
        switch (resultado){
            
            case VICTORIA:
                if (percepcion.getPosicionActual().equals(objetivo)) {
                    System.out.println("¡VICTORIA! Objetivo alcanzado en " + percepcion.getPosicionActual());
                    stopTicker(); // Detener el TickerBehaviour
                }
                break;
            case OBSTACULO:
                
                System.err.println("El agente intentó un movimiento inválido (obstáculo o límite).");
                stopTicker();
                break;

            case MOVIMIENTO_VALIDO:
                
                System.out.println("Acción ejecutada. Nueva posición: " + entorno.getPosicionActual());
                //Comprobar victoria después de un movimiento válido
                if (entorno.getPosicionActual().equals(objetivo)){
                    
                    System.out.println("¡VICTORIA! Objetivo alcanzado en " + entorno.getPosicionActual());
                    stopTicker();
                }
                break;
        }

    }

    //para detener al agente
    private void stopTicker(){
        
        System.out.println("Deteniendo el ciclo del agente...");
        doDelete(); 
    }

    @Override
    protected void takeDown(){
        System.out.println("\n--- MAPA FINAL ---");
        
        if (entorno != null){
            entorno.imprimirMundoActual();
        }
        
        //para poder ejecutar varias veces desde la UI
        if (this.ventana != null) {
            this.ventana.habilitarControles();
        }
        
        System.out.println("Agente " + getAID().getLocalName() + " terminando.");
    }
}
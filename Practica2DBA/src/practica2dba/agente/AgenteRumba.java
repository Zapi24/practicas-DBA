/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.agente;

/**
 *
 * @author zapi24
 */

import practica2dba.entorno.Entorno;
import practica2dba.estrategia.EstrategiaManhattan;
import practica2dba.estrategia.EstrategiaMovimiento;
import practica2dba.interfaz.VentanaPrincipal;
import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;
import practica2dba.utils.ResultadoAccion;

import javax.swing.SwingUtilities;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;


public class AgenteRumba extends Agent{
    
    private Entorno entorno;
    private Coordenada objetivo;
    private EstrategiaMovimiento estrategia;
    private practica2dba.interfaz.VentanaPrincipal ventana;

    
    @Override
    protected void setup(){
        
        System.out.println("Iniciando agente: " + getAID().getLocalName());

        try{
            Object[] args = getArguments();
            if (args != null && args.length == 6){  //Obtiene los argumentos asociados al agente
                
                //Pos inicial
                int x_ini = (int) args[0];
                int y_ini = (int) args[1];
                
                //Pos objetivo
                int x_obj = (int) args[2];
                int y_obj = (int) args[3];
                
                //Bateria max
                int bateriaMax = (int) args[4];
                
                //Ruta del archivo del mapa
                String rutaMapa = (String) args[5];

                //Una vez obtenido todos los datos, define las variables del agente
                this.objetivo = new Coordenada(x_obj, y_obj);
                this.entorno = new Entorno(rutaMapa, new Coordenada(x_ini, y_ini), bateriaMax);
                this.estrategia = new EstrategiaManhattan();

                // Crear interfaz gráfica
                javax.swing.SwingUtilities.invokeLater(() -> {
                    ventana = new VentanaPrincipal(
                        entorno.getMundo().getMapa(),
                        entorno.getPosicionActual(),
                        objetivo
                    );
                });


                System.out.println("Configuración: Inicio (" + x_ini + "," + y_ini + "), Objetivo (" + x_obj + "," + y_obj + ")");
                System.out.println("Batería Máxima: " + bateriaMax);
                System.out.println("Estrategia usada: " + estrategia.getClass().getSimpleName());

                //Imrpimimos el estado icicial del mundo
                System.out.println("\n--- MAPA INICIAL ---");
                entorno.imprimirMundoActual();

                //Vamos ejecutando el ciclo
                addBehaviour(new TickerBehaviour(this, 500) {
                    @Override
                    protected void onTick() {
                        ejecutarCiclo();
                    }
                });

            }else{
                
                System.err.println("Error: Argumentos incorrectos. (Se esperaban 6)");
                doDelete();
            }
        }catch(Exception e){
            
            System.err.println("Error en setup(): " + e.getMessage());
            e.printStackTrace();
            doDelete();
        }
    }
    
    private void ejecutarCiclo(){
        
        //1. PERCEPCIÓN
        Percepcion percepcion = entorno.getPercepcionActual();
        
        System.out.println("-------------------------------------");
        System.out.println("Ciclo. Batería restante: " + entorno.getBateriaRestante());
        System.out.println("Posición actual: " + percepcion.getPosicionActual());

        //2. DECISIÓN (delegada a la Estrategia)
        Movimiento proximoMovimiento = estrategia.decidirMovimiento(percepcion, objetivo);

        //3. ACCIÓN (delegada al Entorno)
        System.out.println("DECISIÓN: Mover -> " + proximoMovimiento);
        ResultadoAccion resultado = entorno.ejecutarAccion(proximoMovimiento);
        // Actualizar interfaz con la nueva posición
        if (ventana != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                ventana.actualizar(entorno.getPosicionActual());
            });
        }


        //4. VALIDACIÓN
        switch (resultado){
            
            case VICTORIA:
                //Comprobación de victoria (delegada al agente)
                if (percepcion.getPosicionActual().equals(objetivo)) {
                    System.out.println("¡VICTORIA! Objetivo alcanzado en " + percepcion.getPosicionActual());
                    stopTicker(); // Detener el TickerBehaviour
                }
                break;
            case OBSTACULO:
                
                System.err.println("¡DERROTA! El agente intentó un movimiento inválido (obstáculo o límite).");
                stopTicker();
                break;
            case DERROTA_BATERIA:
                
                System.err.println("¡DERROTA! Batería agotada.");
                stopTicker();
                break;
            case MOVIMIENTO_VALIDO:
                
                System.out.println("Acción ejecutada. Nueva posición: " + entorno.getPosicionActual());
                //Comprobar victoria después de un movimiento válido para que no se piense el siguiente movimiento si ya ha ganado
                if (entorno.getPosicionActual().equals(objetivo)){
                    
                    System.out.println("¡VICTORIA! Objetivo alcanzado en " + entorno.getPosicionActual());
                    stopTicker();
                }
                break;
        }

    }

    //Metodo para detener al agente
    private void stopTicker(){
        
        System.out.println("Deteniendo el ciclo del agente...");
        doDelete(); 
    }

    @Override
    protected void takeDown(){
        System.out.println("\n--- MAPA FINAL ---"); //Imprimimos el mapa final
        
        if (entorno != null){
            entorno.imprimirMundoActual();
        }
        
        System.out.println("Agente " + getAID().getLocalName() + " terminando.");
    }
}

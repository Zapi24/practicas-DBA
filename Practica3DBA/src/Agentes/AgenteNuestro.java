package Agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.Behaviour;

//Para la logica del movimiento
import practica2dba.entorno.Entorno;
import practica2dba.estrategia.EstrategiaNat;
import practica2dba.estrategia.EstrategiaMovimiento;
import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;
import practica2dba.utils.ResultadoAccion;

// Importar la interfaz
import interfaz.VentanaPrincipal; 

import java.io.IOException;

public class AgenteNuestro extends Agent {

    private int secretCode = -1;
    private boolean abortMission = false;
    private int pasosTotales = 0; // Contador de pasos
    
    //--- VARIABLES DE NAVEGACIÓN ---
    private Entorno entorno;
    private EstrategiaMovimiento estrategia;
    
    // Referencia a la GUI
    private VentanaPrincipal gui; 
    
    // Valores por defecto (serán reemplazados por los argumentos de la GUI)
    private String RUTA_MAPA = "mapas-pr3/100x100-conObstaculos.txt";
    private Coordenada INICIO_AGENTE = new Coordenada(99, 99);
    private Coordenada POSICION_SANTA = new Coordenada(0, 0); 

    @Override
    protected void setup(){
        
        // argumentos de la GUI
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            this.RUTA_MAPA = (String) args[0];
            int xIni = (Integer) args[1];
            int yIni = (Integer) args[2];
            this.INICIO_AGENTE = new Coordenada(xIni, yIni);
            this.gui = (VentanaPrincipal) args[3];
        } else {
            System.err.println("Agente lanzado sin argumentos de GUI. Usando valores por defecto.");
        }
        
        System.out.println("Agente Buscador (" + getLocalName() + ") está listo.");
        
        //Inicializamos el ENTORNO (Carga el mapa)
        try{
            this.entorno = new Entorno(RUTA_MAPA, INICIO_AGENTE);
            this.estrategia = new EstrategiaNat(); 
            System.out.println("[NAVEGACION] Mapa cargado y estrategia lista.");
            
            if (gui != null) gui.actualizarPosicion(INICIO_AGENTE);
            
        }catch (IOException e){
            
            System.err.println("ERROR : No se pudo cargar el mapa: " + RUTA_MAPA);
            e.printStackTrace();
            doDelete();
            return;
        }
        
        //Usamos un SequentialBehaviour para ejecutar los pasos en orden
        SequentialBehaviour secuencial = new SequentialBehaviour(this);
        
        secuencial.addSubBehaviour(new PresentToSanta());
        secuencial.addSubBehaviour(new ContactRudolph());
        secuencial.addSubBehaviour(new CollectReindeer());
        secuencial.addSubBehaviour(new GoToSanta());
        
        addBehaviour(secuencial);
    }
    
    @Override
    protected void takeDown() {
        if (gui != null && !abortMission) {
            gui.habilitarControles(pasosTotales, "Misión completada. ¡HoHoHo!");
        } else if (gui != null && abortMission) {
            gui.habilitarControles(pasosTotales, "Misión abortada.");
        }
        System.out.println("Agente Buscador (" + getLocalName() + ") finalizado.");
    }


    //-----------------------------------------------------------------------
    //PASO 1: Negociar con Santa (A través del Elfo)
    //-----------------------------------------------------------------------
    private class PresentToSanta extends OneShotBehaviour{
        
        @Override
        public void action(){
            if (abortMission) return;
            
            if(gui != null) gui.agregarMensajeChat("Buscador", "Enviando solicitud de misión al Elfo (para Santa).");

            System.out.println("--- PASO 1: Solicitando audiencia con Santa ---");
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("elf", AID.ISLOCALNAME));
            // Protocolo GenZ para el Elfo
            msg.setContent("Bro PEDIR_MISION En Plan");
            send(msg);

            ACLMessage reply = blockingReceive();
            
            if (reply != null) {
                //Limpiamos la traducción del Elfo (Bro...En Plan)
                String inner = reply.getContent().replace("Bro", "").replace("En Plan", "").trim();

                if(inner.contains("RECHAZADO")){
                    System.out.println("[BUSCADOR] Santa me ha rechazado. Misión cancelada.");
                    abortMission = true;
                    myAgent.doDelete();
                    return;
                }

                if(inner.contains("CODIGO_SECRETO")){
                    //Formato esperado: CODIGO_SECRETO:1234
                    try{
                        
                        String[] parts = inner.split(":");
                        secretCode = Integer.parseInt(parts[1].trim());
                        System.out.println("[BUSCADOR] ¡Aceptado! Tengo el código secreto: " + secretCode);          
                    }catch(Exception e){
                        
                        System.out.println("[ERROR] No pude leer el código: " + inner);
                        abortMission = true;
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    // PASO 2: Autenticarse con Rudolph (Directamente)
    //-----------------------------------------------------------------------
    private class ContactRudolph extends OneShotBehaviour{
        @Override
        public void action(){
            
            if (abortMission) return;
            
            if(gui != null) gui.agregarMensajeChat("Buscador", "Iniciando protocolo de seguridad con Rudolph (Código: " + secretCode + ")");

            System.out.println("--- PASO 2: Conectando con Rudolph ---");
            ACLMessage init = new ACLMessage(ACLMessage.REQUEST);
            init.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            // Enviamos el código que nos dio Santa
            init.setContent("INIT:" + secretCode);
            send(init);

            ACLMessage reply = blockingReceive();
            
            //Comprobamos si Rudolph aceptó o rechazó
            if(reply != null && reply.getPerformative() == ACLMessage.AGREE){
                
                System.out.println("[BUSCADOR] Rudolph ha validado mis credenciales. Comenzando búsqueda.");
            }else{
                
                System.out.println("[BUSCADOR] CRÍTICO: Rudolph dice que el código es inválido o no responde.");
                abortMission = true;
                myAgent.doDelete();
            }
        }
    }

    //-----------------------------------------------------------------------
    //PASO 3: Bucle de búsqueda de renos
    //-----------------------------------------------------------------------
    private class CollectReindeer extends Behaviour {
        boolean doneFlag = false;

        @Override
        public void action(){
            
            if (abortMission){ 
                doneFlag = true; 
                return; 
            }
            
            if(gui != null) gui.agregarMensajeChat("Buscador", "Solicitando ubicación del siguiente reno a Rudolph.");

            //1. Pedir coordenada
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            req.setContent("GET_NEXT");
            send(req);

            ACLMessage reply = blockingReceive();
            if (reply == null) {
                System.err.println("[BUSCADOR] Error: No hay respuesta de Rudolph.");
                abortMission = true;
                doneFlag = true;
                return;
            }
            
            String content = reply.getContent();

            if(content.equals("FIN")){
                doneFlag = true;
                return;
            }

            //2. Obtener respuesta: "Dasher_11,12"
            String[] parts = content.split("_");
            String nombreReno = parts[0];
            String[] coordsStr = parts[1].split(",");
            int targetX = Integer.parseInt(coordsStr[0]);
            int targetY = Integer.parseInt(coordsStr[1]);

            Coordenada objetivoReno = new Coordenada(targetX, targetY);
            System.out.println("\n[BUSCADOR] Objetivo: " + nombreReno + " en " + objetivoReno);

            // 3. --- BUCLE DE MOVIMIENTO REAL ---
            navegarHacia(objetivoReno);

            // 4. Informar a Santa
            System.out.println("[BUSCADOR] ¡" + nombreReno + " encontrado!");
            
            // Actualizar GUI para borrar el reno del mapa
            if (gui != null) {
                gui.renoRescatado(objetivoReno); 
                gui.agregarMensajeChat("Buscador", "¡He rescatado a " + nombreReno + "! Informando al Elfo.");
            }

            ACLMessage msgSanta = new ACLMessage(ACLMessage.INFORM);
            msgSanta.addReceiver(new AID("elf", AID.ISLOCALNAME));
            msgSanta.setContent("Bro ENCONTRE A " + nombreReno + " En Plan");
            send(msgSanta);
        }

        @Override
        public boolean done(){ 
            return doneFlag; 
        }
    }

    //-----------------------------------------------------------------------
    //PASO 4: Volver a casa (Santa)
    //-----------------------------------------------------------------------
    private class GoToSanta extends OneShotBehaviour {
        @Override
        public void action(){
            if (abortMission) return;

            if(gui != null) gui.agregarMensajeChat("Buscador", "Misión cumplida. Solicitando coordenadas de regreso al Elfo.");
            
            System.out.println("\n--- PASO 4: Regreso a casa ---");
            ACLMessage ask = new ACLMessage(ACLMessage.REQUEST);
            ask.addReceiver(new AID("elf", AID.ISLOCALNAME));
            ask.setContent("Bro PEDIR_LOCALIZACION_SANTA_CLAUS En Plan");
            send(ask);
            
            ACLMessage reply = blockingReceive(); //Esperamos respuesta
            if (reply == null) {
                System.err.println("[BUSCADOR] Error: No hay respuesta de Santa. Usando default (0,0).");
            } else {
                String content = reply.getContent().replace("Bro", "").replace("En Plan", "").trim();
                
                // Lógica para parsear la respuesta de Santa 
                try {
                    String xStr = content.split(",")[0].split(":")[1];
                    String yStr = content.split(",")[1].split(":")[1];
                    int santaX = Integer.parseInt(xStr);
                    int santaY = Integer.parseInt(yStr);
                    POSICION_SANTA = new Coordenada(santaX, santaY);
                    
                    System.out.println("[BUSCADOR] Recibida ubicación de Santa: " + POSICION_SANTA);
                    if (gui != null) gui.actualizarPosicionFinalSanta(POSICION_SANTA);
                    
                } catch (Exception e) {
                     System.err.println("[ERROR] No pude leer la ubicación de Santa (" + content + "). Usando default (0,0).");
                     POSICION_SANTA = new Coordenada(0, 0); 
                }
            }
            
            //Navegacion a casa
            navegarHacia(POSICION_SANTA);

            //Saludo final
            ACLMessage arrived = new ACLMessage(ACLMessage.INFORM);
            arrived.addReceiver(new AID("elf", AID.ISLOCALNAME));
            arrived.setContent("Bro LLEGO En Plan");
            
            if(gui != null) gui.agregarMensajeChat("Buscador", "He llegado a la base. Enviando saludo final.");
            send(arrived);
            
            ACLMessage finalReply = blockingReceive();
            if (finalReply != null) {
                System.out.println("FIN: " + finalReply.getContent());
            } else {
                 System.out.println("FIN: No se recibió saludo final de Santa.");
            }
            
            myAgent.doDelete(); 
        }
    }
    
    //--- MÉTODO AUXILIAR PARA MOVER AL AGENTE ---
    private void navegarHacia(Coordenada destino) {
        
        while (!entorno.getPosicionActual().equals(destino)) {
            
            //1. Percibir
            Percepcion p = entorno.getPercepcionActual();
            
            //2. Decidir con la estrategia
            Movimiento mov = estrategia.decidirMovimiento(p, destino);
            
            //3. Ejecutamos la accion
            ResultadoAccion res = entorno.ejecutarAccion(mov);
            
            pasosTotales++; // Contamos el paso
            
            // ACTUALIZACIÓN DE LA GUI
            if (gui != null) {
                gui.actualizarPosicion(entorno.getPosicionActual());
                // Pequeña pausa para poder ver el movimiento en la GUI
                try {
                    Thread.sleep(100); 
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Nota: Se ha quitado la impresión de movimiento para no saturar la consola
            // System.out.println("    Moviendo " + mov + " -> " + entorno.getPosicionActual());
            
            if(res == ResultadoAccion.OBSTACULO){
                System.err.println("    [ERROR] ¡Choque contra muro! Revisa la estrategia.");
                break;
            }
        }
    }
}
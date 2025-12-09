package Agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.Behaviour;

//Para la logica del movimientoó

import practica2dba.entorno.Entorno;
import practica2dba.estrategia.EstrategiaNat;
import practica2dba.estrategia.EstrategiaMovimiento;
import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;
import practica2dba.utils.ResultadoAccion;

import java.io.IOException;

public class AgenteNuestro extends Agent {

    private int secretCode = -1;
    private boolean abortMission = false; //Bandera para abortar si algo sale mal
    
    //--- VARIABLES DE NAVEGACIÓN ---
    private Entorno entorno;
    private EstrategiaMovimiento estrategia;
    
    // Configuración inicial según tu TXT
    private final String RUTA_MAPA = "mapas-pr3/100x100-conObstaculos.txt"; // Asegúrate de crear este archivo
    private final Coordenada INICIO_AGENTE = new Coordenada(99, 99);
    private final Coordenada POSICION_SANTA = new Coordenada(0, 0);

    @Override
    protected void setup(){
        System.out.println("Agente Buscador (" + getLocalName() + ") está listo.");
        
        //Inicializamos el ENTORNO (Carga el mapa)
        try{
            
            this.entorno = new Entorno(RUTA_MAPA, INICIO_AGENTE);
            this.estrategia = new EstrategiaNat(); 
            System.out.println("[NAVEGACION] Mapa cargado y estrategia lista.");
        }catch (IOException e){
            
            System.err.println("ERROR : No se pudo cargar el mapa.");
            e.printStackTrace();
            doDelete();
            return;
        }
        
        //Usamos un SequentialBehaviour para ejecutar los pasos en orden
        SequentialBehaviour secuencial = new SequentialBehaviour();
        
        secuencial.addSubBehaviour(new PresentToSanta());
        secuencial.addSubBehaviour(new ContactRudolph());
        secuencial.addSubBehaviour(new CollectReindeer());
        secuencial.addSubBehaviour(new GoToSanta());
        
        addBehaviour(secuencial);
    }

    //-----------------------------------------------------------------------
    //PASO 1: Negociar con Santa (A través del Elfo)
    //-----------------------------------------------------------------------
    private class PresentToSanta extends OneShotBehaviour{
        
        @Override
        public void action(){
            if (abortMission) return;

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

            System.out.println("--- PASO 2: Conectando con Rudolph ---");
            ACLMessage init = new ACLMessage(ACLMessage.REQUEST);
            init.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            // Enviamos el código que nos dio Santa
            init.setContent("INIT:" + secretCode);
            send(init);

            ACLMessage reply = blockingReceive();
            
            //Comprobamos si Rudolph aceptó o rechazó
            if(reply.getPerformative() == ACLMessage.AGREE){
                
                System.out.println("[BUSCADOR] Rudolph ha validado mis credenciales. Comenzando búsqueda.");
            }else if(reply.getPerformative() == ACLMessage.REFUSE){
                
                System.out.println("[BUSCADOR] CRÍTICO: Rudolph dice que el código es inválido.");
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

            //1. Pedir coordenada
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            req.setContent("GET_NEXT");
            send(req);

            ACLMessage reply = blockingReceive();
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
        public void action() {
            if (abortMission) return;

            System.out.println("\n--- PASO 4: Regreso a casa ---");
            // Pedimos permiso/ubi (aunque ya sabemos que es 0,0 por el txt, seguimos protocolo)
            ACLMessage ask = new ACLMessage(ACLMessage.REQUEST);
            ask.addReceiver(new AID("elf", AID.ISLOCALNAME));
            ask.setContent("Bro PEDIR_LOCALIZACION_SANTA_CLAUS En Plan");
            send(ask);
            blockingReceive(); // Esperamos respuesta formal

            System.out.println("[BUSCADOR] Volviendo al origen (0,0)...");
            
            // --- NAVEGACIÓN REAL A CASA ---
            navegarHacia(POSICION_SANTA);

            // Saludo final
            ACLMessage arrived = new ACLMessage(ACLMessage.INFORM);
            arrived.addReceiver(new AID("elf", AID.ISLOCALNAME));
            arrived.setContent("Bro LLEGO En Plan");
            send(arrived);
            
            ACLMessage finalReply = blockingReceive();
            System.out.println("FIN: " + finalReply.getContent());
            myAgent.doDelete();
        }
    }
    
    //--- MÉTODO AUXILIAR PARA MOVER AL AGENTE ---
    private void navegarHacia(Coordenada destino) {
        
        //Mientras no estemos en la casilla destino
        while (!entorno.getPosicionActual().equals(destino)) {
            
            //1. Percibir
            Percepcion p = entorno.getPercepcionActual();
            
            //2. Decidir con la estrategia
            Movimiento mov = estrategia.decidirMovimiento(p, destino);
            
            //3. Ejecutamos la accion
            ResultadoAccion res = entorno.ejecutarAccion(mov);
            
            System.out.println("   Moviendo " + mov + " -> " + entorno.getPosicionActual());
            
            if(res == ResultadoAccion.OBSTACULO){
                System.err.println("   [ERROR] ¡Choque contra muro! Revisa la estrategia.");
                break; //Evitar bucle infinito si se atasca
            }
            
            //Pequeña pausa para simular tiempo de viaje y no saturar CPU
            //try { Thread.sleep(20); } catch (InterruptedException e) {}
        }
    }
}
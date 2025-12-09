package Agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.Behaviour;

public class AgenteNuestro extends Agent {

    private int secretCode = -1;
    private boolean abortMission = false; //Bandera para abortar si algo sale mal

    @Override
    protected void setup(){
        System.out.println("Agente Buscador (" + getLocalName() + ") está listo.");
        
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
    private class CollectReindeer extends Behaviour{
        boolean doneFlag = false;

        @Override
        public void action(){
            
            if (abortMission){
                doneFlag = true;
                return;
            }

            //Pedir siguiente reno
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            req.setContent("GET_NEXT");
            send(req);

            ACLMessage reply = blockingReceive();
            String content = reply.getContent();

            //Condición de salida actualizada a "FIN"
            if (content.equals("FIN")) {
                System.out.println("[BUSCADOR] Rudolph indica que no quedan más renos.");
                doneFlag = true;
                return;
            }

            //Simulación de movimiento
            //Formato esperado: "Dasher_X:10,Y:5"
            System.out.println("[BUSCADOR] Objetivo recibido: " + content);
            try {
                System.out.println("   >> Viajando hacia el reno...");
                Thread.sleep(1000); // SIMULAMOS EL VIAJE, ESTE DEBERIA REALIZARSE AQUIÍ
                System.out.println("   >> ¡Reno rescatado y enviado al establo!");
                
                //Informamos a Santa
                
                //1. Extraemos el nombre limpio del reno (Rudolph envía "Dasher_X:10...")
                String nombreReno = content.split("_")[0]; //Cortamos por el guion bajo "_" para quedarnos solo con el nombre

                // 2. Preparamos el mensaje para el Elfo
                ACLMessage msgSanta = new ACLMessage(ACLMessage.INFORM);
                msgSanta.addReceiver(new AID("elf", AID.ISLOCALNAME));

                //3. Contenido en jerga GenZ: "Bro ENCONTRE A [Nombre] En Plan"
                msgSanta.setContent("Bro ENCONTRE A " + nombreReno + " En Plan");

                //4. Enviamos el mensaje
                myAgent.send(msgSanta);

                System.out.println("[BUSCADOR] Informe de progreso enviado a Santa: " + nombreReno + " rescatado.");
                
                //!!No esperamos ninguna respuesta
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
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

            System.out.println("--- PASO 4: Regresando a la base ---");
            
            //1. Pedir ubicación
            ACLMessage ask = new ACLMessage(ACLMessage.REQUEST);
            ask.addReceiver(new AID("elf", AID.ISLOCALNAME));
            ask.setContent("Bro PEDIR_LOCALIZACION_SANTA_CLAUS En Plan");
            send(ask);

            ACLMessage reply = blockingReceive();
            // La respuesta viene sucia del Elfo (Bro ... En Plan), pero para viajar no la parseamos estricto
            System.out.println("[BUSCADOR] Coordenadas de Santa recibidas: " + reply.getContent());
            
            try{
                System.out.println("   >> Viajando hacia Santa Claus...");
                Thread.sleep(1500); // Viaje final
            }catch (InterruptedException e) {
            
                 e.printStackTrace();
            }

            //2. Avisar que llegamos
            ACLMessage arrived = new ACLMessage(ACLMessage.INFORM);
            arrived.addReceiver(new AID("elf", AID.ISLOCALNAME));
            arrived.setContent("Bro LLEGO En Plan");
            send(arrived);

            //3. Esperar el HoHoHo final
            ACLMessage finalReply = blockingReceive();
            String cleanReply = finalReply.getContent().replace("Bro", "").replace("En Plan", "").trim();
            System.out.println("------------------------------------------------");
            System.out.println("MENSAJE FINAL DE SANTA: " + cleanReply);
            System.out.println("------------------------------------------------");
            
            // Terminamos el agente
            myAgent.doDelete();
        }
    }
}
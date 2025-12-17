package Agentes;

import jade.core.Agent;
import jade.core.AID; //LIBRERIA necesaria para el paso de mensajes
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

import interfaz.VentanaPrincipal;

public class Elfo extends Agent {
    
    //Variable para guardar la ventana
    private VentanaPrincipal gui;

    @Override
    protected void setup() {
        
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof VentanaPrincipal) {
            this.gui = (VentanaPrincipal) args[0];
        }
        
        System.out.println("El elfo traductor está listo y está escuchandoá...");

        //Implementamos un comportamiento cíclico.
        addBehaviour(new CyclicBehaviour(this){
            
            @Override
            public void action(){
                
                //1. Intentamos recibir un mensaje
                ACLMessage msg = myAgent.receive();
                
                ///Si no hay ningñun mensaje vloqueamos el comportamiento hasta que haya uno
                if (msg == null) {
                    block();
                    return;
                }

                //Obtenemos el sender y el contenido
                String sender = msg.getSender().getLocalName();
                String content = msg.getContent();

                //---------------------------------------------------------
                //CASO A: Mensaje de NUESTRO AGENTE ("buscador") hacia Santa
                //---------------------------------------------------------
                if(sender.equals("buscador")){ // De Genz a Finlandes
                    
                    System.out.println("[ELFO] Traduciendo de GenZ a Finlandés...");

                    //1. Limpiamos del String el Bro y el En Plan
                    String contenidoMsg = content.replace("Bro", "").replace("En Plan", "").trim();

                    //2. Creamos el mensaje para Santa
                    ACLMessage forward = new ACLMessage(msg.getPerformative());
                    forward.addReceiver(new AID("santa", AID.ISLOCALNAME));
                    
                    //3. Añadimos el el contenido en Finlandes, que sigficia: "Querido Santa ... Gracias"
                    forward.setContent("Rakas Joulupukki " + contenidoMsg + " Kiitos");
                    
                    if (gui != null) {
                        gui.agregarMensajeChat("Elfo", "Traducido de Buscador a Santa: \"" + contenidoMsg + "\"");
                    }
                    
                    send(forward);
                    return;
                }

                //---------------------------------------------------------
                //CASO B: Mensaje de SANTA CLAUS ("santa") hacia nuestro agente
                //---------------------------------------------------------
                if(sender.equals("santa")){ //De finlandes a Genz
                    System.out.println("[ELFO] Traduciendo de Finlandés a GenZ...");

                    // 1. Limpiamos del String el finlandes que significa: "Feliz Navidad" y "Nos vemos"
                    String contenidoMsg = content.replace("Hyvää joulua", "").replace("Nähdään pian", "").trim();

                    // 2. Creamos el mensaje para el Buscador
                    ACLMessage back = new ACLMessage(msg.getPerformative());
                    back.addReceiver(new AID("buscador", AID.ISLOCALNAME));
                    
                    // 3. Añadimos la jerga juvenil: "Bro ... En Plan"
                    back.setContent("Bro " + contenidoMsg + " En Plan");
                    
                    if (gui != null) {
                        gui.agregarMensajeChat("Elfo", "Traducido de Santa a Buscador: \"" + contenidoMsg + "\"");
                    }
                    
                    send(back);
                    return;
                }
            }
        });
    }
}
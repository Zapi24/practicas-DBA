package Agentes;

import jade.core.Agent;
import jade.core.AID; //LIBRERIA necesaria para el paso de mensajes (con Rudoplh)
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import java.util.Random;


import interfaz.VentanaPrincipal;

public class SantaClaus extends Agent {

    private int codigo;
    private Random rand = new Random(); //Para el numero aleatorio
    private VentanaPrincipal gui;

    @Override
    protected void setup(){
        
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof VentanaPrincipal) {
            this.gui = (VentanaPrincipal) args[0];
        }
        
        System.out.println("Santa Claus está listo y esperando en su casa.");
        addBehaviour(new SantaBehaviour(this));
    }

    //Implementamos un comportamiento ciclico
    private class SantaBehaviour extends CyclicBehaviour{
        
        public SantaBehaviour(Agent a){
            super(a);
        }

        @Override
        public void action() {
            ACLMessage msg = myAgent.receive();

            if (msg == null) {
                block();
                return;
            }

            String content = msg.getContent();
            String sender = msg.getSender().getLocalName();

            //1. Validar protocolo Finlandés (El mensaje viene del Elfo traducido)
            //El mensajde debe empezar con "Rakas Joulupukki" y terminar con "Kiitos"
            if(!content.startsWith("Rakas Joulupukki") || !content.endsWith("Kiitos")){ //!!Caso de error
                System.out.println("Santa Claus recibió un mensaje mal formateado de " + sender);
                return; //Ignoramos mensajes que no respeten el protocolo
            }

            //2. Extraer el contenido real (Quitamos el saludo y despedida finlandesa)
            String contenidoMsg = content.replace("Rakas Joulupukki", "").replace("Kiitos", "").trim();

            //Preparamos la respuesta (que irá al Elfo)
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            //3. Lógica de SantaClaus
            switch (contenidoMsg) {
                case "PEDIR_MISION" -> {
                    
                    //1º Probabilidad del 80% de ser aceptado
                    boolean trustworthy = rand.nextDouble() < 0.8;

                    //RECHAZADO: Enviamos respuesta negativa envuelta en finlandés
                    if(!trustworthy){
                        reply.setContent("Hyvää joulua RECHAZADO Nähdään pian");
                        
                        if(gui != null) gui.agregarMensajeChat("Santa", "Has sido RECHAZADO (No fiable). Enviando respuesta a Elfo.");
                        
                        myAgent.send(reply);
                        System.out.println("Santa Claus ha decidido que el voluntario NO es digno.");
                        return;
                    }

                    //ACEPTADO: Generamos código
                    codigo = 1000 + rand.nextInt(9000); //Codigo aleatorio de 4 cifras (el valor min sera 1000+0 y el valor maximo 1000+8999)

                    //-----------------------------------------------------------
                    //CONFIRMACION DEL CODIGO: Avisar a Rudolph del código válido 
                    //-----------------------------------------------------------
                    
                    ACLMessage msgToRudolph = new ACLMessage(ACLMessage.INFORM);    //Establecemos un inform
                    msgToRudolph.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
                    msgToRudolph.setContent("CODIGO_SECRETO:" + codigo);
                    myAgent.send(msgToRudolph);
                    
                    if(gui != null) gui.agregarMensajeChat("Santa", "Enviando COPIA DE SEGURIDAD del código (" + codigo + ") a Rudolph.");
                    System.out.println("Santa Claus avisa a Rudolph: El código de hoy es " + codigo);

                    //Respondemos al voluntario con el código
                    reply.setContent("Hyvää joulua CODIGO_SECRETO:" + codigo + " Nähdään pian");
                    
                    if(gui != null) gui.agregarMensajeChat("Santa", "Voluntario aceptado. Enviando código a Elfo.");
                    
                    myAgent.send(reply);
                    System.out.println("Santa Claus acepta al voluntario. Código enviado.");
                }

                case "PEDIR_LOCALIZACION_SANTA_CLAUS" -> {
                    
                    // El buscador pide coordenadas para volver
                    //Coordenadas fijas o aleatorias, según prefieras. Aquí fijas para simplificar.
                    
                    reply.setContent("Hyvää joulua SANTA_X:10,SANTA_Y:5 Nähdään pian");
                    if(gui != null) gui.agregarMensajeChat("Santa", "Enviando mi ubicación GPS al Elfo.");
                    myAgent.send(reply);
                    System.out.println("Santa Claus envía su ubicación para el regreso.");
                }

                case "LLEGO" -> {
                    //El buscador ha llegado con todos los renos
                    
                    reply.setContent("Hyvää joulua HoHoHo! Nähdään pian");
                    if(gui != null) gui.agregarMensajeChat("Santa", "¡Misión Cumplida! HoHoHo!");
                    myAgent.send(reply);
                    System.out.println("Santa Claus: ¡HoHoHo! Misión cumplida.");
                    
                    //!!!Terminar el agente Santa aquí si la simulación acaba
                    //myAgent.doDelete(); 
                }
                
                case String s when s.startsWith("ENCONTRE A") -> {
                    //El mensaje interno será algo como "ENCONTRE A Dasher"
                    String reno = s.replace("ENCONTRE A", "").trim();

                    //No respondemos de ninguna manera, solo pintamos por pantalla 
                    if(gui != null) gui.agregarMensajeChat("Santa", "¡HoHoHo! Me alegra saber que han encontrado a " + reno);
                    System.out.println("Santa Claus sonríe: ¡Excelente! Han recuperado a " + reno + ".");
                }
                
                default -> {
                    
                     System.out.println("Santa no entiende el mensaje interno: " + contenidoMsg);
                }
            }
        }
    }
}
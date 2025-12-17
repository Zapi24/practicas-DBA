package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

import interfaz.VentanaPrincipal;

public class Rudolph extends Agent {

    //Lista de los nombres de los renos a rescatar
    private String[] renos = {"Dasher", "Dancer", "Vixen", "Prancer", "Cupid", "Comet", "Blitzen", "Donner"};

    
    //Posiciones de los renos para el mapa 100x100
    //private String[] coordsRenos = {"11,12", "19,41", "41,60", "52,33", "63,34", "66,56", "92,74", "95,10"};
    
    // Posiciones de los renos para el mapa 20x20
    private String[] coordsRenos = {"1,1", "18,2", "11,4", "7,8", "15,10", "3,14", "10,17", "17,18"}; 
    
    private int index = 0;
    
    //Guardamos el código de la sesión actual
    private String codigo = null;
    
    private VentanaPrincipal gui;

    @Override
    protected void setup(){
        
        Object[] args = getArguments();
        if (args != null && args.length > 0 && args[0] instanceof VentanaPrincipal) {
            this.gui = (VentanaPrincipal) args[0];
        }
        
        System.out.println("Rudolph está listo en el establo.");

        //Le implementamos un comportamineto ciclico
        addBehaviour(new CyclicBehaviour(this){
            @Override
            public void action(){
                
                //Intentamos recibir un mensaje
                ACLMessage msg = receive();
                
                ///Si no hay ningñun mensaje vloqueamos el comportamiento hasta que haya uno
                if (msg == null){
                    block();
                    return;
                }

                String sender = msg.getSender().getLocalName();
                String content = msg.getContent();

                //---------------------------------------------------------
                //CASO 1: MENSAJE DE SANTA (Nos da la clave buena)
                //---------------------------------------------------------
                if (sender.equals("santa") && content.startsWith("VALID_CODE:")){
                    
                    codigo = content.split(":")[1];
                    System.out.println("[RUDOLPH] Recibido código maestro de Santa: " + codigo);
                    return;
                }
                
                //---------------------------------------------------------
                //CASO 2: MENSAJE DEL BUSCADOR (Intenta conectarse)
                //---------------------------------------------------------
                if (content.startsWith("INIT")){
                    
                    String codigo = content.split(":")[1];

                    // Comprobamos si el código coincide con el que nos dio Santa
                    if(codigo != null && codigo.equals(codigo)){
                        index = 0;
                        System.out.println("[RUDOLPH] Código correcto. Aceptando misión.");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setPerformative(ACLMessage.AGREE);    //Performativa de agree
                        respuesta.setContent("ACCEPT");
                        
                        if(gui != null) gui.agregarMensajeChat("Rudolph", "Código correcto. Aceptando conexión segura con Buscador.");
                        send(respuesta);
                        
                    }else{
                        
                        System.out.println("[RUDOLPH] ¡ALERTA! Código incorrecto o Santa aún no me avisó.");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setPerformative(ACLMessage.REFUSE);   //Performatica de Refuse
                        respuesta.setContent("CODIGO_INVALIDO");
                        
                        if(gui != null) gui.agregarMensajeChat("Rudolph", "¡ALERTA! Código incorrecto. Rechazando conexión.");
                        send(respuesta);
                    }
                    return;
                }

                //---------------------------------------------------------
                //CASO 3: DAR COORDENADAS
                //---------------------------------------------------------
                if (content.equals("GET_NEXT")){
                    
                     ACLMessage reply = msg.createReply();
                     reply.setPerformative(ACLMessage.INFORM);
                     
                    if(index < renos.length){
                        
                        //Formato: NOMBRE_X,Y (Para que el agente lo parseé fácil). Ejemplo: Dasher_11,12
                        String payload = renos[index] + "_" + coordsRenos[index];
                        reply.setContent(payload);
                        
                        if(gui != null) gui.agregarMensajeChat("Rudolph", "Enviando coordenadas de " + renos[index] + " al Buscador.");
                        index++;
                     }else{
                        
                        reply.setContent("FIN");
                        
                        if(gui != null) gui.agregarMensajeChat("Rudolph", "No quedan más renos. Enviando señal FIN.");
                     }
                     send(reply);
                }
            }
        });
    }
}
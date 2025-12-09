package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class Rudolph extends Agent {

    //Lista de los nombres de los renos a rescatar
    private String[] renos = {"Dasher", "Dancer", "Vixen", "Prancer", "Cupid", "Comet", "Blitzen", "Donner"};

    private int index = 0;
    
    //Guardamos el código de la sesión actual
    private String codigo = null;

    @Override
    protected void setup(){
        
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
                        
                        System.out.println("[RUDOLPH] Código correcto. Aceptando misión.");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setPerformative(ACLMessage.AGREE);    //Performativa de agree
                        respuesta.setContent("ACCEPT");
                        send(respuesta);
                        
                    }else{
                        
                        System.out.println("[RUDOLPH] ¡ALERTA! Código incorrecto o Santa aún no me avisó.");
                        ACLMessage respuesta = msg.createReply();
                        respuesta.setPerformative(ACLMessage.REFUSE);   //Performatica de Refuse
                        respuesta.setContent("CODIGO_INVALIDO");
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
                         
                        reply.setContent(renos[index++] + "_X:" + (index*10) + ",Y:" + (index*5));
                     }else{
                         
                        reply.setContent("FIN");
                     }
                     send(reply);
                }
            }
        });
    }
}
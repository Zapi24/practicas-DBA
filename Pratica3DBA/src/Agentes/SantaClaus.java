package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.util.Random;

public class SantaClaus extends Agent {

    private int secretCode;
    private Random rand = new Random();

    @Override
    protected void setup() {
        System.out.println("Santa Claus ready!");
        addBehaviour(new SantaBehaviour(this));
    }

    private class SantaBehaviour extends jade.core.behaviours.CyclicBehaviour {
        public SantaBehaviour(Agent a) {
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

            //la mensaje que recibe debe ser: Rakas Joulupukki ... Kiitos
            if (!content.startsWith("Rakas Joulupukki") || !content.endsWith("Kiitos")) {
                System.out.println("Santa received malformed message from " + sender);
                return;
            }

            String inner = content.replace("Rakas Joulupukki", "")
                    .replace("Kiitos", "").trim();

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);

            switch (inner) {
                case "PEDIR_MISION" -> {
                    boolean trustworthy = rand.nextDouble() < 0.8;

                    if (!trustworthy) {
                        reply.setContent("Hyvää joulua REJECT Nähdään pian");
                        myAgent.send(reply);
                        System.out.println("Santa rechazou el voluntário.");
                        return;
                    }

                    //cria codigo secreto random para enviar
                    secretCode = 1000 + rand.nextInt(9000);
                    reply.setContent("Hyvää joulua SECRET_CODE:" + secretCode + " Nähdään pian");
                    myAgent.send(reply);
                    System.out.println("Santa Claus confirma que eres bueno! Este es el código secreto: " + secretCode);
                    return;
                }
                case "PEDIR_LOCALIZACION_SANTA_CLAUS" -> {
                    reply.setContent("Hyvää joulua SANTA_X:10,SANTA_Y:5 Nähdään pian");
                    myAgent.send(reply);
                    return;
                }
                case "LLEGO" -> {
                    reply.setContent("Hyvää joulua HoHoHo! Nähdään pian");
                    myAgent.send(reply);
                    return;
                }
            }

        }
    }
}

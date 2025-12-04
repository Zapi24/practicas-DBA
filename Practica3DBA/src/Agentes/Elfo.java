package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Elfo extends Agent {

    @Override
    protected void setup() {
        System.out.println("Translator ready!");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive();
                if (msg == null) {
                    block();
                    return;
                }

                String sender = msg.getSender().getLocalName();
                String content = msg.getContent();

                //mensaje del agente nuestro a santa
                if (sender.equals("busqueda")) {
                    String inner = content.replace("Bro", "")
                            .replace("En Plan", "").trim();

                    ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                    forward.addReceiver(getAID("santa"));
                    forward.setContent("Rakas Joulupukki " + inner + " Kiitos");
                    send(forward);
                    return;
                }

                //de santa claus a agente nuestro
                if (sender.equals("santa")) {
                    String inner = content.replace("Hyvää joulua", "")
                            .replace("Nähdään pian", "")
                            .trim();

                    ACLMessage back = new ACLMessage(ACLMessage.INFORM);
                    back.addReceiver(getAID("bursqueda"));
                    back.setContent("Bro " + inner + " En Plan");
                    send(back);
                    return;
                }
            }
        });
    }
}


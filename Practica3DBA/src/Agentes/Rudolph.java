package Agentes;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Rudolph extends Agent {

    private String[] renos = {
            "Dasher", "Dancer", "Vixen", "Prancer", "Cupid", "Comet", "Blitzen", "Donner"
    };

    private int index = 0;
    private int validCode = 0;

    @Override
    protected void setup() {
        System.out.println("Rudolph esta pronto!");

        addBehaviour(new jade.core.behaviours.CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) {
                    block();
                    return;
                }

                String content = msg.getContent();

                if (content.startsWith("INIT")) {
                    int codigo = Integer.parseInt(content.split(":")[1]);
                    validCode = codigo;

                    ACLMessage reply = msg.createReply();
                    reply.setContent("ACCEPT");
                    send(reply);
                    return;
                }

                if (!content.equals("GET_NEXT")) return;

                ACLMessage reply = msg.createReply();
                //review
                if (index < renos.length) {
                    reply.setContent(renos[index++] + "_X:" + (index*3) + ",Y:" + (index*2));
                } else {
                    reply.setContent("Ja encontramos todos los renos!");
                }

                send(reply);
            }
        });
    }
}

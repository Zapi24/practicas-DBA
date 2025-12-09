package Agentes;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

public class AgenteNuestro extends Agent {

    private int secretCode;

    @Override
    protected void setup() {
        System.out.println("Agente pronto!");
        addBehaviour(new MainBehaviour());
    }

    private class MainBehaviour extends jade.core.behaviours.SequentialBehaviour {
        public MainBehaviour() {
            addSubBehaviour(new PresentToSanta());
            addSubBehaviour(new ContactRudolph());
            addSubBehaviour(new CollectReindeer());
            addSubBehaviour(new GoToSanta());
        }
    }

    private class PresentToSanta extends jade.core.behaviours.OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("elf", AID.ISLOCALNAME));
            msg.setContent("Bro PEDIR_MISION En Plan");
            send(msg);

            ACLMessage reply = blockingReceive();
            String inner = reply.getContent().replace("Bro", "").replace("En Plan", "").trim();

            if (inner.startsWith("RECHAZADO")) {
                System.out.println("Santa nos rechazou. A salir...");
                myAgent.doDelete();
                return;
            }

            if (inner.startsWith("SECRET_CODE")) {
                secretCode = Integer.parseInt(inner.split(":")[1]);
                System.out.println("Recibio codigo secreto: " + secretCode);
            }
        }
    }

    private class ContactRudolph extends jade.core.behaviours.OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage init = new ACLMessage(ACLMessage.REQUEST);
            init.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            init.setContent("INIT:" + secretCode);
            send(init);

            ACLMessage reply = blockingReceive();
            System.out.println("Rudolph acepto coneccion");
        }
    }

    //5 y 6
    private class CollectReindeer extends jade.core.behaviours.Behaviour {

        @Override
        public void action() {
            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(new AID("rudolph", AID.ISLOCALNAME));
            req.setContent("GET_NEXT");
            send(req);

            ACLMessage reply = blockingReceive();
            String content = reply.getContent();

            if (content.equals("Ja encontramos todos los renos!")) {
                System.out.println("Todos los renos fueron encontrados!");
                doneFlag = true;
                return;
            }

            System.out.println("Encontrou reno: " + content);
        }

        boolean doneFlag = false;

        @Override
        public boolean done() {
            return doneFlag;
        }
    }

    //8 pedir ubicacion
    private class GoToSanta extends jade.core.behaviours.OneShotBehaviour {
        @Override
        public void action() {
            ACLMessage ask = new ACLMessage(ACLMessage.REQUEST);
            ask.addReceiver(new AID("elf", AID.ISLOCALNAME));
            ask.setContent("Bro PEDIR_LOCALIZACION_SANTA_CLAUS En Plan");
            send(ask);

            ACLMessage reply = blockingReceive();
            System.out.println("Cambiar para Santa Claus...");

            ACLMessage arrived = new ACLMessage(ACLMessage.INFORM);
            arrived.addReceiver(new AID("elf", AID.ISLOCALNAME));
            arrived.setContent("Bro LLEGO En Plan");
            send(arrived);

            ACLMessage finalReply = blockingReceive();
            System.out.println("Santa Claus dice: " + finalReply.getContent());
        }
    }
}

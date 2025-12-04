import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    public static void main(String[] args) {

        try {

            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);

            //create a main container with GUI
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            ContainerController mainContainer = rt.createMainContainer(p);

            //launch Santa
            AgentController santa = mainContainer.createNewAgent(
                    "santa", "SantaClaus", null);
            santa.start();

            //launch the Translator
            AgentController elf = mainContainer.createNewAgent(
                    "elf", "Elfo", null);
            elf.start();

            //launch Rudolph
            AgentController rudolph = mainContainer.createNewAgent(
                    "rudolph", "Rudolph", null);
            rudolph.start();

            //launch  our Agent
            AgentController searcher = mainContainer.createNewAgent(
                    "procurador", "AgenteNuestro", null);
            searcher.start();

            //launch GUI agent
            AgentController ui = mainContainer.createNewAgent(
                    "ui",
                    "GUIAgent",
                    null
            );
            ui.start();

            System.out.println("All agents started successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    public static void main(String[] args) {

        try {
            // Start JADE runtime
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);

            // Create a main container with GUI
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            ContainerController mainContainer = rt.createMainContainer(p);

            // Launch Santa
            AgentController santa = mainContainer.createNewAgent(
                    "santa", "SantaClausAgent", null);
            santa.start();

            // Launch the Translator
            AgentController elf = mainContainer.createNewAgent(
                    "elf", "ElfTranslatorAgent", null);
            elf.start();

            // Launch Rudolph
            AgentController rudolph = mainContainer.createNewAgent(
                    "rudolph", "Rudolph", null);
            rudolph.start();

            // Launch Searcher agent
            AgentController searcher = mainContainer.createNewAgent(
                    "procurador", "AgenteNuestro", null);
            searcher.start();

            // Launch GUI agent (optional)
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

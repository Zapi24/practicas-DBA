import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class Main {
    public static void main(String[] args) {

        try{
            //Creamos el runtime para ir llamando a los agentes
            
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);

            //Llamamos a la interfaz de JADE
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");
            ContainerController mainContainer = rt.createMainContainer(p);

            
            // 1. SANTA:
            AgentController santa = mainContainer.createNewAgent(
                    "santa", "Agentes.SantaClaus", null);
            santa.start();

            //2. ELFO 
            AgentController elf = mainContainer.createNewAgent(
                    "elf", "Agentes.Elfo", null);
            elf.start();

            // 3. RUDOLPH (Añadido paquete Agentes.)
            // Opcional: Aquí podrías pasar las coordenadas como argumentos en lugar de null
            // si quisieras quitar el array hardcodeado de Rudolph.
            AgentController rudolph = mainContainer.createNewAgent(
                    "rudolph", "Agentes.Rudolph", null);
            rudolph.start();

            // 4. NUESTRO AGENTE 
            AgentController searcher = mainContainer.createNewAgent(
                    "buscador", "Agentes.AgenteNuestro", null);
            searcher.start();


            System.out.println("Todos los agentes iniciados correctamente.");

        } catch (Exception e){
            
            e.printStackTrace();
        }
    }
}

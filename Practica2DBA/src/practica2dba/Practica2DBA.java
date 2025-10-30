/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package practica2dba;

import practica2dba.agente.AgenteRumba;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 *
 * @author zapi24
 */
public class Practica2DBA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
        
        System.out.println("--- LANZADOR PRÁCTICA 2: AGENTE RUMBA ---");

        Runtime rt = Runtime.instance();
        Profile p = new ProfileImpl();
        
        try{
            ContainerController cc = rt.createAgentContainer(p);
            
            // Argumentos del Agente:
            // (x_ini, y_ini, x_obj, y_obj, bateria_max, ruta_mapa)
            Object[] agentArgs = new Object[] {
                0,                          //x_ini
                0,                          //y_ini
                9,                          //x_obj
                9,                          //y_obj
                100,                        //Batería máxima
                "maps/mapWithHorizontalWall.txt"     //Ruta del mapa
            };
            
            //Lanzamos el AgenteRumba desde su nuevo paquete
            AgentController ac = cc.createNewAgent(
                "Rumba",
                "practica2dba.agente.AgenteRumba", 
                agentArgs
            );
            
            ac.start();
            System.out.println("Agente 'Rumba' lanzado. Revisar la consola del agente...");

        }catch (Exception e){
            
            System.err.println("ERROR: No se pudo lanzar el agente. Puede que el main container no este activo");
            e.printStackTrace();
        }
    }
    
}

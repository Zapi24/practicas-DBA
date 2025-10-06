/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package practica1dba;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;   //Para obtener la instancia del entorno de ejecución de Jade
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.Scanner;

/**
 * Clase principal que actúa como lanzador para el sistema de agentes JADE.
 * Se conecta al Main Container (servidor principal) y despliega los distintos agentes
 * * @author zapi24
 */
public class Practica1DBA{

    /**
     * Punto de entrada para la ejecución del sistema de agentes.
     * @param args los argumentos de la línea de comandos
     */
    public static void main(String[] args){
        
        //1. Obtener la instancia del entorno de ejecución de JADE.
        Runtime rt = Runtime.instance();

        // 2.Crear un Profile para el Contenedor de Agentes.
        // Usamos un ProfileImpl vacío para conectar al Main Container por defecto (localhost:1099).
        Profile p = new ProfileImpl();
        
        Scanner sc = new Scanner(System.in);
        
        //Configuraciones Opcionales (puedes descomentar si es necesario)
        //Para conectar a un host diferente:
        //p.setParameter(Profile.MAIN_HOST, "192.168.1.10"); 
        //Para dar un nombre específico al nuevo contenedor:
        //p.setParameter(Profile.CONTAINER_NAME, "ContenedorSecundario");

        System.out.println("--- Lanzador JADE ---");

        try{    //Se ejecutará si el contenedor esta abierto, o no ha habido algun otro error
            //3. Crear el Contenedor de Agentes obteniendo el contenedor principal activoá.
            ContainerController cc = rt.createAgentContainer(p);
            
            
            
            //Ejecutamos la interfaz para ver que ejercicio queremos ejecutar
            
            System.out.println("*******************************************");
            System.out.println("*** Que ejercicio te gustaría ejecutar: ***");
            System.out.println("*******************************************");
            
            System.out.println("1. Mensaje basico.");
            System.out.println("2. Mensaje una sola vez.");
            System.out.println("3. Mensaje indefinido cada 2 segundos.");
            System.out.println("4. Muestra 'eco' 5 veces.");
            System.out.println("5. Muestra la media de los numeros dados.");

            int opcion = sc.nextInt(); //leemos la opciones
            
            
            
            try{
                String agentClassName = "";
                String agentName = "";
                
                switch(opcion){
                    case 1:
                        agentClassName = "practica1dba.ejercicio1";
                        agentName = "AgenteEjercicio1";
                        break;
                    case 2:
                        agentClassName = "practica1dba.ejercicio2";
                        agentName = "AgenteEjercicio2";
                        break;
                    case 3:
                        agentClassName = "practica1dba.ejercicio3";
                        agentName = "AgenteEjercicio3";
                        break;
                    case 4:
                        agentClassName = "practica1dba.ejercicio4";
                        agentName = "AgenteEjercicio4";
                        break;
                    case 5:
                        //Pendiente
                        System.out.println("Ejercicio 5 pendiente de implementación.");
                        return;
                    default:
                        System.out.println("Por favor, ingresa un valor válido entre 1 y 5.");
                        return; // Salir si la opción no es válida
                }  
                
                if (!agentClassName.isEmpty()){ // Si se ha completado con éxito
                    
                    //4. Crear un nuevo Agente en el contenedor.
                    AgentController ac = cc.createNewAgent(
                        agentName,              // Nombre del agente
                        agentClassName,         // Clase completa del agente
                        null                    // Argumentos (none)
                    );

                    // 5. Iniciar la ejecución del agente.
                    ac.start();

                    System.out.println("Agente '" + agentName + "' (clase " + agentClassName + ") lanzado exitosamente.");
                    System.out.println("El contenedor permanecerá activo hasta que el agente termine o se cierre manualmente." + "\n");
                }
            } catch(Exception e){
                
                System.out.println("Por favor, ingresa un valor válido");
            }
            
       
        }catch (Exception e){
            
            System.err.println("ERROR: No se pudo lanzar el agente. Asegúrate de que el contenedor principal está activo.");
            System.err.println(e.getMessage());
        }
    }
}

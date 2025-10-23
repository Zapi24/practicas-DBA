/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 *
 * @author zapi24
 */
public class ejercicio2 extends Agent{

    @Override
    protected void setup(){
        
        System.out.println("Hola soy el agente del ejercicio 2: " + getAID().getLocalName());

        //Añadimos el comportamiento 
        addBehaviour(new OneShotBehaviour(this){
            
            //Los OneShot deben definir el metodo action (su done siempre devuelve true, haciendo que se ejecute solo 1 vez)
            @Override
            public void action(){
                
                System.out.println("Este mensaje se ejecuta UNA SOLA VEZ.");
                
                myAgent.doDelete(); 
            }
        });
    }

    @Override 
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
}

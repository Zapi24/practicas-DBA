/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;

/**
 *
 * @author zapi24
 */
public class ejercicio4 extends Agent{
    
    public static final int REPETICIONES = 5;   //Se repetira hasta un max de 5 veces
    
    
    @Override
    protected void setup(){
        System.out.println("Hola soy el agente del ejercicio 4: " + getAID().getLocalName());

        addBehaviour(new Behaviour(this){
            
            private int contador=0;
        
            //El action se ejecuta hasta que el done devuelva true
            @Override
            public void action(){
                
                contador++;
                System.out.println("ecco, por " + contador + "º vez.");
            }
            
            //Done devuelve true al alcanzar el limite
            
            @Override
            public boolean done(){
                
                boolean terminado = contador>=REPETICIONES;
                
                if(terminado){
                    
                    myAgent.doDelete();
                }
                
                return terminado;
            }  
        });  
    }
    
    @Override
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
}

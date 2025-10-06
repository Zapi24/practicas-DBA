/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;

/**
 *
 * @author zapi24
 */
public class ejercicio3 extends Agent{
    
    private static final long PERIODO = 2000; //Se ejecutará cada 2000 ms
    
    @Override
    protected void setup(){
        
        System.out.println("Hola soy el agente del ejercicio 3: " + getAID().getLocalName());
        
        //Añadimos el comportamiento 
        addBehaviour(new TickerBehaviour(this, PERIODO){
            
            //Los tickets deben definir el metodo onTick
            @Override
            protected void onTick(){
                
                System.out.println("Este mensaje se ejecuta cada 2000ms");
            }
        });
    }
    
    //En este caso como el agente ejecuta indefinidamente el onTick, no se llamara al takeDown en ningun momento
    
    @Override
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
}

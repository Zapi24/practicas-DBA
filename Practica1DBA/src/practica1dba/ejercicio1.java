/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
/**
 *
 * @author zapi24
 */
public class ejercicio1 extends Agent{
    
    @Override
    protected void setup(){
        
        System.out.println("Hola soy el primer Agente: " + getAID().getLocalName());
        
        doDelete(); //Llama al takeDown
    }
    
    //Se ejecuta justo antes de que el agente sea destruido
    @Override 
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
}

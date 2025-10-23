/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.OneShotBehaviour;

import java.util.Scanner;



/**
 *
 * @author zapi24
 */
public class ejercicio5 extends Agent{
    
    
    private int numElementos = 0;
    private float suma = 0;
    
    private Scanner sc = new Scanner(System.in);
    
    
    @Override
    protected void setup(){
        
        System.out.println("Hola soy el agente del ejercicio 5: " + getAID().getLocalName());        
        SequentialBehaviour secuencia = new SequentialBehaviour(this);  //Para concatenar comportamientos
        
        //1º Para pedir el número de elementos
        secuencia.addSubBehaviour(new OneShotBehaviour(this){
        
            @Override 
            public void action(){   //El action del OneShotBehaviour
                
                System.out.println("Ingresa el numero N de elementos para hacer media: ");
                try{
                    
                    ejercicio5.this.numElementos = ejercicio5.this.sc.nextInt();

                    if(ejercicio5.this.numElementos <= 0){

                        System.out.println("Por favor, ingresa un valor válido (mayor a cero)");
                        myAgent.doDelete(); //Si ha habido algun error, cerramos el agente
                    } 
                }catch(Exception e){

                    System.out.println("Por favor, ingresa un valor válido");
                    myAgent.doDelete(); //Si ha habido algun error, cerramos el agente
                }
            }
        });  
        
        
        //2º Para solicitar los números para hacer media
        secuencia.addSubBehaviour(new OneShotBehaviour(this){
            
            @Override
            public void action(){
                
                for(int i = 0; i<ejercicio5.this.numElementos;i++){
                    
                    System.out.println("Ingresa el " + (i+1) + "º elemento a sumar: ");
                    try{
                        
                        float numero = sc.nextFloat();
                        ejercicio5.this.suma = ejercicio5.this.suma + numero;   //Vamos calculando la suma
                       
                    }catch(Exception e){

                        System.out.println("Por favor, ingresa un valor válido");
                        myAgent.doDelete(); //Si ha habido algun error, cerramos el agente
                    }
                }
                
            }
        });  
        
        //3º Para calcular la media y devolver el resultado
        
        secuencia.addSubBehaviour(new OneShotBehaviour(this){
            
           @Override 
           public void action(){
           
               float media = ejercicio5.this.suma / ejercicio5.this.numElementos;
          
               System.out.println("\n" + "El valor final de la media es: " + media);
               
               myAgent.doDelete(); //Se ha acabado la ejecución del agente
           } 
        });
        
        
        // !!IMPORTANTE
        addBehaviour(secuencia);    //Una vez definidos los comportamientos se los pasamos al agente
    }
    
    @Override
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
    
}

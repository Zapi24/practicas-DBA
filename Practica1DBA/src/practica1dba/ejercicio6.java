/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica1dba;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.OneShotBehaviour;

/**
 *
 * @author zapi24
 */

/*

    Entendimiento teorico sobre el desfase:

Por lo general, entendemos que el desfase es: 
        desfase= Tiempo real de Ejecucion - Tiempo previsto

El tiempo ideal previsto es el tiempo que tardaria el agente en condiciones ideales
sin retrasos ni interferencias. Para calcularlo tenemos que calcular el tiempo de inicio
en el que el agente empezo a ejecutar cosas. Y ese tiempo sumarlo al periodo del ticket.
Por ejemplo si el agente tardo un segundo en empezar en ejecutar, para el 5 tick tendremos
lo siguiente: 1000ms + (500ms * 5tick) = 3500ms en condiciones ideales

Sin embargo al someterle al estres, este tiempo que este ejecutando la carga de trabajo pesada, 
dejara en pausa la ejecucion del ticket en este caso. 

Para obtener el tiempo real vamos a someter uno comportamiento en el que estara haciendo
calculos o simplemente esperando durante un segundo y medio. Esto hara que el ticket se ejecute
cada segundo en vez de cada segundo y medio. Ya que:

Empiezan a ejectutarse ambos concurrentemente
---- 500 ms ---- ---- 500 ms ---- ---- 500 ms ---- 
---- 500 ms ----

1500 ms menos 500 ms Nos da un retraso de 1000 ms  

*/
public class ejercicio6 extends Agent{
    
    private static final long PERIODO = 500; //Se ejecutará cada medio segundo
    private static final long PERIODO_ESPERA = 1500;
    private static final int MAX = 5;
    
    private long tiempoInicio;
    private int contador;
    
    @Override
    protected void setup(){
        
        System.out.println("Hola soy el agente del ejercicio 6: " + getAID().getLocalName());        

        this.tiempoInicio = System.currentTimeMillis(); //Para inicializar el tiempo de inicio
        
        //Comportamiento periodico ideal cada 500 ms
        addBehaviour(new TickerBehaviour(this,PERIODO){
           
            @Override
            protected void onTick(){
                
                long tiempoReal = System.currentTimeMillis();   //Vemos en que momento entra realemnte
                contador++; //Actualizamos el contador
                long tiempoIdeal = ejercicio6.this.tiempoInicio + /*(long)*/contador * PERIODO;
                
                long diferencia = tiempoReal - tiempoIdeal;
                
                System.out.println("Tiempo real:" + tiempoReal);
                System.out.println("Tiempo ideal:" + tiempoIdeal);
                System.out.println("Diferencia:" + diferencia);
                
                
                if(contador>=MAX){
                    
                    myAgent.doDelete(); //Paramos la ejecucion del agente a llegar al tope
                }
            }
        });
        
        //Comportamiento de "carga pesada"
        addBehaviour(new OneShotBehaviour(this){
            
            @Override
            public void action(){
                
                System.out.println("Empiezo a generar carga pesada en: "+System.currentTimeMillis());
                //!!NO FUNCIONA
                try{
                    
                    Thread.sleep(PERIODO_ESPERA);
                }catch (InterruptedException ex){ //Netbeans me hizo esto
                    System.getLogger(ejercicio6.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
                
                //Codigo arreglado por el molegon:
                /* inicioCarga = System.currentTimeMillis();
                while (System.currentTimeMillis() - inicioCarga < PERIODO_ESPERA){  //Se queda esperando en el while el segundo y medio 
                    // Simular trabajo intenso que no cede el control (operaciones matemáticas)
                    double dummy = Math.sqrt(Math.random()); 
                    dummy++; 
                }*/
                System.out.println("Acabo de generar carga pesada en: "+System.currentTimeMillis());

            }
        });
        
    }
    
    
    @Override
    protected void takeDown(){
        
        System.out.println("Terminando el Agente: " + getAID().getLocalName());
    }
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.utils;


import java.util.HashMap;   //Como un map pero mas eficiente
/**
 *
 * @author zapi24
 */
public class Percepcion{    //Clase que encapsula toda la informacion del entorno que el agente puede conocer
    
    private Coordenada posicionActual;
    private int bateriaRestante;    //Valor limite para que el programa tenga un final si se queda atascadoí
    
    //Para que el agente sepa si sus casillas adyacentes son libres
    HashMap<Movimiento,Boolean> sensorLibre = new HashMap<>();  
    
    //Para identificar si el agente tiene muros en sus casillas adyacentes
    HashMap<Movimiento,Boolean> sensorMuro = new HashMap<>();
    

    public Percepcion(Coordenada pos, int bat, HashMap<Movimiento,Boolean> libre, HashMap<Movimiento,Boolean> muro){
        
        this.posicionActual = pos;
        this.bateriaRestante = bat;
                
        //Inicializamos los sensores
        this.sensorLibre = libre;
        this.sensorMuro = muro;
        
        
    }

    //Aqui estan los getters
    public Coordenada getPosicionActual() { return posicionActual; }
    public int getBateriaRestante() { return bateriaRestante; }
    
    public HashMap<Movimiento,Boolean> getSensorLibre(){return sensorLibre;}
    public HashMap<Movimiento,Boolean> getSensorMuro(){return sensorMuro;}
      
}

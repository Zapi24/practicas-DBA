/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.entorno;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;
import practica2dba.utils.ResultadoAccion;

import java.util.HashMap; 

import java.io.IOException;

/**
 *
 * @author zapi24
 */
public class Entorno{   //Clase que se encarga de mediador entre el agente y el mundo
    
    private Mundo mundo;
    private Coordenada posicionAgente;
    private int bateriaRestante;

    public Entorno(String rutaMapa, Coordenada posInicial, int bateriaMax) throws IOException{
        
        this.mundo = new Mundo(rutaMapa);   //Carga el mapa
        this.posicionAgente = posInicial;   //Establece la posicion inicial del agente
        this.bateriaRestante = bateriaMax;  //Establece la bateria del agente
        
        //Por si la casilla inicial la hemos puesto mal
        if(!mundo.isCeldaTransitable(posInicial.getX(), posInicial.getY())){
            
            throw new IllegalArgumentException("La posición inicial (" + posInicial + ") es un obstáculo o está fuera de límites.");
        }
    }


    //Simula los sensores del agente, esta es la informacion que obtiene el agente
    public Percepcion getPercepcionActual(){
        
        //Su posicion
        int x = posicionAgente.getX();
        int y = posicionAgente.getY();

        //Lo que tiene arriba, abajo,. izquierda y derecha
        HashMap<Movimiento,Boolean> sensorLibre = new HashMap<>();
        HashMap<Movimiento,Boolean> sensorMuro = new HashMap<>();
        
        //Para ver si la celda es transitable
        sensorLibre.put(Movimiento.ABAJO,mundo.isCeldaTransitable(x, y + 1));
        sensorLibre.put(Movimiento.ARRIBA,mundo.isCeldaTransitable(x, y - 1));
        sensorLibre.put(Movimiento.IZQUIERDA,mundo.isCeldaTransitable(x - 1 , y));
        sensorLibre.put(Movimiento.DERECHA,mundo.isCeldaTransitable(x + 1 , y));
        
        //Para ver si es un muro
        sensorMuro.put(Movimiento.ABAJO,mundo.isCeldaMuro(x, y + 1));
        sensorMuro.put(Movimiento.ARRIBA,mundo.isCeldaMuro(x, y - 1));
        sensorMuro.put(Movimiento.IZQUIERDA,mundo.isCeldaMuro(x - 1 , y));
        sensorMuro.put(Movimiento.DERECHA,mundo.isCeldaMuro(x + 1 , y));
        

        return new Percepcion(new Coordenada(x, y), bateriaRestante, sensorLibre, sensorMuro);
    }

    //Se encarga de ejecutar el movimiento deseado
    public ResultadoAccion ejecutarAccion(Movimiento mov){
        
        //Marcamos como final si hemos consumido toda la bateria
        if(this.bateriaRestante <= 0){
            
            return ResultadoAccion.DERROTA_BATERIA;
        }
        
        //Actualizamos al valor de la bateri
        this.bateriaRestante--;

        //Obtenemos la posicion nueva del agente
        Coordenada nuevaPos = new Coordenada(posicionAgente.getX(), posicionAgente.getY());
        
        switch (mov){
            
            case ARRIBA:    nuevaPos.setY(nuevaPos.getY() - 1); break;
            case ABAJO:  nuevaPos.setY(nuevaPos.getY() + 1); break;
            case IZQUIERDA:  nuevaPos.setX(nuevaPos.getX() - 1); break;
            case DERECHA: nuevaPos.setX(nuevaPos.getX() + 1); break;
            
            //No deberia llegar nunca a esta situacion, esta puesto por si acaso
            case QUEDARSE:  return ResultadoAccion.MOVIMIENTO_VALIDO; 
        }
        
        //Validamos que ese movimiento sea hacia una celda transitable por si acaso
        if(mundo.isCeldaTransitable(nuevaPos.getX(), nuevaPos.getY())){
            
            this.posicionAgente = nuevaPos;
            return ResultadoAccion.MOVIMIENTO_VALIDO;
        }else{
            
            return ResultadoAccion.OBSTACULO;
        }
    }
    
    //Para opbtener la posicion actual del agente
    public Coordenada getPosicionActual(){return this.posicionAgente;}
    
    //Para opbtener la bateria actual del agente
    public int getBateriaRestante(){return this.bateriaRestante;}

    //Para ir imprimiento el mundo
    public void imprimirMundoActual(){
        
        mundo.imprimirMundo(posicionAgente);
    }
    
}

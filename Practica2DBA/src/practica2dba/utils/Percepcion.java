/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.utils;

/**
 *
 * @author zapi24
 */
public class Percepcion{    //Clase que encapsula toda la informacion del entorno que el agente puede conocer
    
    private Coordenada posicionActual;
    private int bateriaRestante;    //Valor limite para que el programa tenga un final si se queda atascadoí
    
    //Almacena si los sensores son casillas transitables
    private boolean sensorArribaLibre;
    private boolean sensorAbajoLibre;
    private boolean sensorIzquierdaLibre;
    private boolean sensorDerechaLibre;

    public Percepcion(Coordenada pos, int bat, boolean arriba, boolean abajo, boolean izq, boolean der){
        
        this.posicionActual = pos;
        this.bateriaRestante = bat;
        this.sensorArribaLibre = arriba;
        this.sensorAbajoLibre = abajo;
        this.sensorIzquierdaLibre = izq;
        this.sensorDerechaLibre = der;
    }

    //Aqui estan los getters
    public Coordenada getPosicionActual() { return posicionActual; }
    public int getBateriaRestante() { return bateriaRestante; }
    
    public boolean isSensorArribaLibre() { return sensorArribaLibre; }
    public boolean isSensorAbajoLibre() { return sensorAbajoLibre; }
    public boolean isSensorIzquierdaLibre() { return sensorIzquierdaLibre; }
    public boolean isSensorDerechaLibre() { return sensorDerechaLibre; }
  
}

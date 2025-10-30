/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

/**
 *
 * @author zapi24
 */
public class EstrategiaManhattan implements EstrategiaMovimiento{
    
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo){
        
        Coordenada actual = p.getPosicionActual();
        //Calculamos los valores x e y que usa la estrategia manhattan para decidir los movimientos. 
        //Calcula la diferencia de la pos actual con la objetivo
        int valorX = objetivo.getX() - actual.getX();
        int valorY = objetivo.getY() - actual.getY();
        
        // --- Lógica movimiento (simple) ---
        //Mira la distancia vertical, y si es mayor a la distancia horizontal se mueve verticalmente, si no se mueve horizontalmente

        //Vemos cual de los dos ejes es el que tiene mayor distancia
        if (Math.abs(valorY) > Math.abs(valorX)){
            
            if (valorY > 0 && p.isSensorAbajoLibre()) return Movimiento.ABAJO;
            if (valorY < 0 && p.isSensorArribaLibre()) return Movimiento.ARRIBA;
        }

        //Lo mismo pero con el movimiento horizontal
        if (valorX > 0 && p.isSensorDerechaLibre()) return Movimiento.DERECHA;
        if (valorX < 0 && p.isSensorIzquierdaLibre()) return Movimiento.IZQUIERDA;
        
        // --- Lógica anti-atasco (simple) ---
        // Si la ruta ideal está bloqueada, toma la primera salida libre
        // que no sea "hacia atrás" (si es posible).
        
        if (p.isSensorAbajoLibre() && valorY <= 0) return Movimiento.ABAJO;
        if (p.isSensorDerechaLibre() && valorX <= 0) return Movimiento.DERECHA;
        if (p.isSensorArribaLibre() && valorY >= 0) return Movimiento.ARRIBA;
        if (p.isSensorIzquierdaLibre() && valorX >= 0) return Movimiento.IZQUIERDA;

        //Si está totalmente rodeado (excepto por donde vino), vuelve.`l
        if (p.isSensorAbajoLibre()) return Movimiento.ARRIBA;
        if (p.isSensorArribaLibre()) return Movimiento.ABAJO;
        if (p.isSensorDerechaLibre()) return Movimiento.DERECHA;
        if (p.isSensorIzquierdaLibre()) return Movimiento.IZQUIERDA;
        
        return Movimiento.QUEDARSE; //Si esta completamente rodeado por que su posicion inicial es esa, pues no se puede mover 
    }
}

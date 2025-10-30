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
public class EstrategiaZapi implements EstrategiaMovimiento{
    
    
       public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo){
           
           /* La estrategia base que voy a cojer es la manhattan, pero  voy a 
           * intentar añadir una locia de escapar de las paredes. En el caso en
           * el que el colega se encuentre con una pared dentro de sus sensores
           * se encargara de seguirla (intentando bordearla) y cuando se escape
           * de ella, continuara con la manhattan.
           */
           
           //Coordenada actual = p.getPosicionActual();
           
           
           
           
           
           return Movimiento.QUEDARSE;  //Si llega hasta aqui es que ha echo algo mal
       }
       
       /*public Movimiento escaparParedes(Percepcion p, Coordenada objetivo){
           
           
       }*/
       
}

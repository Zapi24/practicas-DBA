/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;


/**
 *
 * @author zapi24
 */
public interface EstrategiaMovimiento{  //Interfaz de las distintas estrategias que implementaremos
    
    Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo);
}

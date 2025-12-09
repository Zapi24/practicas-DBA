package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

import java.util.HashSet;
import java.util.Set;

/**
 * @author natag
 */
public class EstrategiaNat implements EstrategiaMovimiento {

    private enum Modo {
        BUSQUEDA_DIRECTA,
        RODEO_OBSTACULO
    }

    private Modo modoActual = Modo.BUSQUEDA_DIRECTA;
    private Movimiento orientacion = Movimiento.ABAJO;

    //Para obstaculos en forma de U
    private Coordenada coordenadaAtasco = null;
    private int distanciaAlObjetivoEnAtasco = Integer.MAX_VALUE;

    //Memoria de las casillas que ha visitado en el el atasco
    private final Set<Coordenada> celulasVisitadasEnEsteRodeo = new HashSet<>();
    

    // ------------------------------------

    @Override
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo) {
        
        Coordenada actual = p.getPosicionActual();
        int valorX = objetivo.getX() - actual.getX();
        int valorY = objetivo.getY() - actual.getY();

        if (valorX == 0 && valorY == 0) {
            return Movimiento.QUEDARSE;
        }

        Movimiento movimientoIdealPrio = getMovimientoPrioritario(valorX, valorY);
        Movimiento movimientoIdealSec = getMovimientoSecundario(valorX, valorY);
        Movimiento proximoMovimiento;

        if (modoActual == Modo.RODEO_OBSTACULO) {
            
            //Añadimos la posición actual a la memoria del atasco
            this.celulasVisitadasEnEsteRodeo.add(actual);

            //LÓGICA MODO RODEO
            int distanciaActual = distanciaManhattan(actual, objetivo);
            boolean hemosSuperadoObstaculo = (distanciaActual < this.distanciaAlObjetivoEnAtasco);

            if (hemosSuperadoObstaculo) {
                if (p.getSensorLibre().get(movimientoIdealPrio)) {
                    proximoMovimiento = transicionABusqueda(movimientoIdealPrio);
                } else if (movimientoIdealSec != null && p.getSensorLibre().get(movimientoIdealSec)) {
                    proximoMovimiento = transicionABusqueda(movimientoIdealSec);
                } else {
                    proximoMovimiento = rodearObstaculo(p, actual);
                }
            } else {
                proximoMovimiento = rodearObstaculo(p, actual);
            }
            
        } else { // modoActual == Modo.BUSQUEDA_DIRECTA
            
            //LÓGICA MODO BUSQUEDA
            if (p.getSensorLibre().get(movimientoIdealPrio)) {
                proximoMovimiento = movimientoIdealPrio;
            } 
            else if (movimientoIdealSec != null && p.getSensorLibre().get(movimientoIdealSec)) {
                proximoMovimiento = movimientoIdealSec;
            } 
            else {  // Cambio de estado a rodeo
                proximoMovimiento = transicionARodeo(actual, objetivo, movimientoIdealPrio, p);
            }
        }
        
        // actualizamos
        if (proximoMovimiento != null && proximoMovimiento != Movimiento.QUEDARSE) {
            this.orientacion = proximoMovimiento;
        } 
        else if (proximoMovimiento == null) {
            proximoMovimiento = buscarSalidaEmergencia(p);
        }

        return proximoMovimiento;
    }

    // Modo busqueda y resetamos la memoria de atasco
    private Movimiento transicionABusqueda(Movimiento movimientoElegido) {
        modoActual = Modo.BUSQUEDA_DIRECTA;
        coordenadaAtasco = null;
        distanciaAlObjetivoEnAtasco = Integer.MAX_VALUE;
        celulasVisitadasEnEsteRodeo.clear(); 
        return movimientoElegido;
    }
    
    // Modo rodeo, guardando en memoria atasco
    private Movimiento transicionARodeo(Coordenada actual, Coordenada objetivo, Movimiento movPrioritario, Percepcion p) {
        modoActual = Modo.RODEO_OBSTACULO;
        coordenadaAtasco = actual;
        distanciaAlObjetivoEnAtasco = distanciaManhattan(actual, objetivo);
        this.orientacion = movPrioritario;
        celulasVisitadasEnEsteRodeo.clear();
        
        return rodearObstaculo(p, actual);
    }

    // Estrategia Manhattan
    private int distanciaManhattan(Coordenada a, Coordenada b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    // Rodeamos siempre por la izquierda
    private Movimiento rodearObstaculo(Percepcion p, Coordenada actual) {
        Movimiento izq = girarIzquierda(this.orientacion);
        Movimiento der = girarDerecha(this.orientacion);
        Movimiento atras = girarAtras(this.orientacion);
        Movimiento recto = this.orientacion;

        // posibles movimientos
        Coordenada c_izq = getCoordenadaSiguiente(actual, izq);
        Coordenada c_recto = getCoordenadaSiguiente(actual, recto);
        Coordenada c_der = getCoordenadaSiguiente(actual, der);
        Coordenada c_atras = getCoordenadaSiguiente(actual, atras);

        // Prioriza casillas NO VISITADAS en este rodeo 
        if (p.getSensorLibre().get(izq) && !celulasVisitadasEnEsteRodeo.contains(c_izq)) {
            return izq;
        }
        if (p.getSensorLibre().get(recto) && !celulasVisitadasEnEsteRodeo.contains(c_recto)) {
            return recto;
        }
        if (p.getSensorLibre().get(der) && !celulasVisitadasEnEsteRodeo.contains(c_der)) {
            return der;
        }
        if (p.getSensorLibre().get(atras) && !celulasVisitadasEnEsteRodeo.contains(c_atras)) {
            return atras;
        }
        
        //Si hya bucle bordeamos tocando el muro hasta salir
        if (p.getSensorLibre().get(izq)) {
            return izq;
        }
        if (p.getSensorLibre().get(recto)) {
            return recto;
        }
        if (p.getSensorLibre().get(der)) {
            return der;
        }
        if (p.getSensorLibre().get(atras)) {
            return atras;
        }

        return Movimiento.QUEDARSE; // Totalmente atascado
    }

    private Coordenada getCoordenadaSiguiente(Coordenada c, Movimiento m) {
        switch (m) {
            case ARRIBA:    return new Coordenada(c.getX(), c.getY() - 1);
            case ABAJO:     return new Coordenada(c.getX(), c.getY() + 1);
            case IZQUIERDA: return new Coordenada(c.getX() - 1, c.getY());
            case DERECHA:   return new Coordenada(c.getX() + 1, c.getY());
            default:        return c;
        }
    }

    private Movimiento getMovimientoPrioritario(int valorX, int valorY) {
        if (Math.abs(valorY) > Math.abs(valorX)) {
            return (valorY > 0) ? Movimiento.ABAJO : Movimiento.ARRIBA;
        } 
        else if (valorX != 0) {
            return (valorX > 0) ? Movimiento.DERECHA : Movimiento.IZQUIERDA;
        }
        else if (valorY != 0) {
             return (valorY > 0) ? Movimiento.ABAJO : Movimiento.ARRIBA;
        }
        return Movimiento.QUEDARSE;
    }
    
    private Movimiento getMovimientoSecundario(int valorX, int valorY) {
        if (Math.abs(valorY) > Math.abs(valorX)) {
            if (valorX == 0) return null;
            return (valorX > 0) ? Movimiento.DERECHA : Movimiento.IZQUIERDA;
        } 
        else {
            if (valorY == 0) return null;
            return (valorY > 0) ? Movimiento.ABAJO : Movimiento.ARRIBA;
        }
    }
    
    private Movimiento buscarSalidaEmergencia(Percepcion p) {
        if (p.getSensorLibre().get(Movimiento.ARRIBA)) return Movimiento.ARRIBA;
        if (p.getSensorLibre().get(Movimiento.ABAJO)) return Movimiento.ABAJO;
        if (p.getSensorLibre().get(Movimiento.DERECHA)) return Movimiento.DERECHA;
        if (p.getSensorLibre().get(Movimiento.IZQUIERDA)) return Movimiento.IZQUIERDA;
        return Movimiento.QUEDARSE;
    }
    
    private Movimiento girarIzquierda(Movimiento m) {
        switch(m) {
            case ARRIBA: return Movimiento.IZQUIERDA;
            case IZQUIERDA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.ARRIBA;
            default: return Movimiento.ARRIBA; 
        }
    }
    
    private Movimiento girarDerecha(Movimiento m) {
        switch(m) {
            case ARRIBA: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.IZQUIERDA;
            case IZQUIERDA: return Movimiento.ARRIBA;
            default: return Movimiento.ARRIBA;
        }
    }
    
    private Movimiento girarAtras(Movimiento m) {
        switch(m) {
            case ARRIBA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.ARRIBA;
            case IZQUIERDA: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.IZQUIERDA;
            default: return Movimiento.ARRIBA;
        }
    }
}
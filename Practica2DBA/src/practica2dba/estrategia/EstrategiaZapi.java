package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * @author zapi24
 */
public class EstrategiaZapi implements EstrategiaMovimiento{

    
    /*Esta estrategia sigue una una memoria de las casillas por las que ha pasado el agente.
    Este almacena las coordenadas de sus casillas, y para cada una de ellas guardamos un entero
    almacenadno las veces que ha pasado el colega por esa casilla.*/
    private HashMap<Coordenada, Integer> mapaDeVisitas = new HashMap<>();


    @Override
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo){

        Coordenada actual = p.getPosicionActual();  //Obtenemos la casilla actual

        //1º Aumentamos el contador dentro del mapa de visitas
        if(!mapaDeVisitas.containsKey(actual)){  //Si no contiene la key actual, es decir es la primea vez que pasa por esa casilla, la inicializamos
            
            mapaDeVisitas.put(actual,1);    //Le marcamos uno como que la acabamos de visitar
        }
        else{   //Si ya ha pasado por esa casilla, aumentamos su valor en 1
            
            mapaDeVisitas.put(actual, mapaDeVisitas.get(actual)+1);
        }

        //2º Vemos que movimientos son accesibles para el agente
        List<Movimiento> movimientosPosibles = new ArrayList<>();
        if(p.getSensorLibre().get(Movimiento.ARRIBA)) movimientosPosibles.add(Movimiento.ARRIBA);
        if(p.getSensorLibre().get(Movimiento.ABAJO)) movimientosPosibles.add(Movimiento.ABAJO);
        if(p.getSensorLibre().get(Movimiento.DERECHA)) movimientosPosibles.add(Movimiento.DERECHA);
        if(p.getSensorLibre().get(Movimiento.IZQUIERDA)) movimientosPosibles.add(Movimiento.IZQUIERDA);

        //3º Si no hay movimientos posibles (deberia ser una situacion imposible)
        if (movimientosPosibles.isEmpty()){

            return Movimiento.QUEDARSE; //El colega no se mueve y se queda bloqueado hasta que se quede sin bateria
        }

        //4º Basado en las prioridades de mapaDeVisitas y la distancia minima de manhattan, calculamos nuestro movimiento
        Movimiento mejorMovimiento = null;
        int minVisitas = Integer.MAX_VALUE;         //Prioridad 1: Menor número de visitas
        double minManhattan = Double.MAX_VALUE;     //Prioridad 2: Menor distancia Manhattan

        for (Movimiento mov : movimientosPosibles){
            
            //Calcular la coordenada futura para este movimiento
            Coordenada futuraCord = new Coordenada(actual.getX(), actual.getY());    //Almacenara la futura coordenada a la que nos moveremos
            
            switch(mov){
                case ARRIBA:    futuraCord.setY(futuraCord.getY() - 1); break;
                case ABAJO:     futuraCord.setY(futuraCord.getY() + 1); break;
                case IZQUIERDA: futuraCord.setX(futuraCord.getX() - 1); break;
                case DERECHA:   futuraCord.setX(futuraCord.getX() + 1); break;
                default: break; 
            }

            //Vemos cuantas veces hemos visitado esa coordenada
            int visitas = 0;    //Lo inicializo en cero, para que en el caso que sea una casilla que nunca se haya visitado, ya tenga su valor adquirido.
            if(mapaDeVisitas.containsKey(futuraCord)){ 
                
                visitas = mapaDeVisitas.get(futuraCord);    //Obtenemos el valor de esa casilla si ya la hemos visitado
            }
            
            //Vemos cuanta distancia manhattan tenemos desde esa coordenada hasta nuestra coordenada objetivo
            double distManhattan = calcularDistanciaManhattan(futuraCord, objetivo);

            //EMPIEZA LA LOGICA DE DECISION A PARTIR DE LOS DATOS CALCULADOS:
            
            //Hasta este punto, este movimiento es el menos visitado, así que elegimos ese. No tenemos en cuenta la distancia Manhattan
            if(visitas < minVisitas){

                minVisitas = visitas;
                minManhattan = distManhattan;
                mejorMovimiento = mov;  //Guardamos el mejor movimiento hasta el momento
                
                //Si hubiese un empate, es decir tenemos dos coordenadas con la misma prioridad de visita (Ej: dos casillas que no hemos visitado tdvia con 0 en cada una)
                
            }else if(visitas == minVisitas){
                
                if (distManhattan < minManhattan){ //Desempatamos utilizando la distancia Manhattan
                    
                    minManhattan = distManhattan;
                    mejorMovimiento = mov; //Guardamos el mejor movimiento hasta el momento
                }
            } 
        }
        
        return mejorMovimiento; //Devolvemos el mejor movimiento
    }

    //Funcion de la formula de distancia Manhattan
    private double calcularDistanciaManhattan(Coordenada a, Coordenada b){
        
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}


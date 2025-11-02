/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package practica2dba.utils;

/**
 *
 * @author zapi24
 */
public class Coordenada{
    
    private int x; 
    private int y; 

    //Constrcutor
    public Coordenada(int x, int y){
        
        this.x = x;
        this.y = y;
    }

    //Gettters y setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    //Para poder mostrar por pantalla las coordenadas
    @Override
    public String toString(){
        return "(" + x + ", " + y + ")";
    }

    //Para poder comparar la entradas
    @Override
    public boolean equals(Object obj){ 
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordenada that = (Coordenada) obj;
        return x == that.x && y == that.y;
    }
   
    /*
        He tenido que añadir esto por que por lo visto cuando tenemos un HashMap con <Coordenada,..>
        Para que java trate como unicas las claves y no cree una nueva direccion de memoria
        cada vez que consultamos el HashMap con la misma clave tenemos que añadirlo. Si no lo que 
        hara java es tener una direccion de memoria diferente para el mismo objeto cada vez que 
        consulte o tratemos al objeto del HashMap con coordenada (2,5) por ejemplo.
    */
    @Override
    public int hashCode(){
        // java.util.Objects.hash es la forma moderna y segura de generar un hashCode
        return java.util.Objects.hash(x, y);
    }
}




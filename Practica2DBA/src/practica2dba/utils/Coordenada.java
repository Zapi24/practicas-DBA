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
    public boolean equals(Object obj){ //(Tengo que revisar que hace, esta funcion me la hizo el colegon)
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordenada that = (Coordenada) obj;
        return x == that.x && y == that.y;
    }
}




# practicas-DBA
Repositorio con la resolución del las prácticas de la asignatura de DBA. Prácticas realizas en Java utilizando la libreria Jade basada en agentes.


**Anotaciones importantes:**

Para abrir el contenedor principal y el servidor debemos ejecutar la siguiente linea de comando desde la terminal:

java -cp dist/lib/jade.jar jade.Boot -name dba_server -gui

---

# Memoria – Práctica 2: Movimiento de un Agente en un Mundo Bidimensional

**Diseño Basado en Agentes – Curso 2025/2026**
**Integrantes:**

* Natalia García Ortega
* Samuel Sánchez Cantero
* Mehul Micul Hasmuklal
* Maria Néu Seelow

---

## 📑 Índice

* [Introducción](#introducción)
* [Arquitectura del sistema](#arquitectura-del-sistema)
* [Diseño e implementación de estrategias](#diseño-e-implementación-de-las-estrategias)
* [Estrategias implementadas](#estrategias-implementadas)
* [Análisis de resultados](#análisis-de-resultados)
* [Manual del usuario](#manual-del-usuario)
* [Conclusiones y mejoras futuras](#conclusiones-y-mejoras-futuras)

---

## Introducción

En esta práctica se desarrolla un agente capaz de navegar por un mapa con obstáculos para llegar a un objetivo.
El agente únicamente ve las casillas adyacentes (arriba, abajo, derecha e izquierda).

Se implementan múltiples estrategias de movimiento para comparar su eficiencia en términos de energía consumida (número de movimientos).

---

## Arquitectura del sistema

La estructura del proyecto se divide en varios paquetes:

* **utils**: encapsula objetos comunes para mejorar claridad y evitar errores.
* **interfaz**: contiene el `JFrame` y toda la interfaz gráfica.
* **estrategia**: incluye las distintas estrategias de movimiento implementadas.
* **entorno**: contiene la clase `Mundo` y controla el intercambio de información con el agente.
* **agente**: selecciona la estrategia y gestiona el ciclo de decisión del agente.

Todo gestionado por un `main` (`Practica2DBA`) que establece posición del agente, objetivo y mapa a usar.

---

## Diseño e implementación de las Estrategias

Todas las estrategias heredan de `EstrategiaMovimiento`.
Se desarrollaron varias para determinar cuál era la más eficiente.
A continuación se describen las tres principales.

---

## Estrategias implementadas

### 🌀 EstrategiaZapi

* Usa un `HashMap` para contar visitas a cada casilla.
* El agente siempre elige la casilla accesible con menor número de visitas.
* En caso de empate, se usa distancia Manhattan.
* **Problema:** puede quedar atrapado serpenteando y consumir energía excesiva.
* **Resultado:** estrategia descartada.

---

### 🎯 EstrategiaNat (Estrategia seleccionada)

Estrategia híbrida basada en:

* **Búsqueda directa:** distancia Manhattan modificada.
* **Rodeo de obstáculos:** siguiendo el muro por la izquierda.
* **Memoria interna:** guarda casillas visitadas durante el rodeo para evitar bucles.

El agente tiene dos modos:

1. **BUSQUEDA_DIRECTA:** se intenta ir de forma óptima al objetivo.
2. **RODEO_OBSTACULO:** si ambos ejes están bloqueados.

Sale del modo de rodeo cuando:

* La distancia al objetivo es menor que cuando se atascó.
* El camino directo está libre.

**Resultado:** estable, eficiente y sin bucles en la mayoría de mapas.

---

### 🧠 MiHa_estrategia6

Estrategia avanzada con dos modos según distancia al objetivo:

#### 1. **Modo BÚSQUEDA (distancia > 5)**

* Coste = Manhattan + Penalización de visitas + Penalización de pared.
* "Pared pegajosa": si toca obstáculo, se mantiene 15 pasos para evitar oscilaciones.
* Memoria dual global.

#### 2. **Modo FINAL (distancia ≤ 5)**

* Se ignora Manhattan y se aplica lógica de resolución de laberintos.
* Prioridades estrictas:
  directo → rodeo izq. → rodeo der. → retroceso.
* Penalización masiva a movimientos repetidos en esta fase.
* Fail-safe si un lado está bloqueado.

**Resultado:** muy potente en estructuras complejas pero menos estable en general que EstrategiaNat.

---

## Análisis de resultados

### 📍 Posiciones iniciales y energías esperadas

| Tipo           | Fichero                | Ax | Ay | Gx | Gy | Energía |
| -------------- | ---------------------- | -- | -- | -- | -- | ------- |
| Sin obstáculos | mapWithoutObstacle.txt | 49 | 49 | 0  | 0  | 98      |
| Horizontal     | mapHorizontal.txt      | 30 | 49 | 30 | 0  | 85      |
| Vertical       | mapVertical.txt        | 0  | 25 | 49 | 25 | 103     |
| Diagonal       | mapTriangleBig.txt     | 30 | 49 | 40 | 0  | 193     |
| Convexo        | mapComplex1.txt        | 30 | 49 | 40 | 0  | 381     |
| Cóncavo        | mapComplex2.txt        | 30 | 49 | 40 | 0  | 107     |
| Complejo 1     | mapComplex3.txt        | 49 | 17 | 3  | 15 | 162     |
| Complejo 2     | mapComplex4.txt        | 49 | 27 | 15 | 39 | 178     |
| Sorpresa 1     | mapComplex5.txt        | 25 | 49 | 25 | 37 | 102     |
| Sorpresa 2     | mapComplex6.txt        | 24 | 31 | 25 | 34 | 176     |

### 📊 Comparativa de estrategias

| Mapa               | EstrategiaZapi | EstrategiaNat | EstrategiaMiha6 |
| ------------------ | -------------- | ------------- | --------------- |
| mapWithoutObstacle | 98             | 98            | 98              |
| mapHorizontal      | 251            | 232           | 88              |
| mapVertical        | 207            | 487           | 244             |
| mapTriangleBig     | +3000          | 226           | 674             |
| mapComplex1        | 689            | 199           | 850             |
| mapComplex2        | 349            | 99            | 75              |
| mapComplex3        | 70             | 166           | 83              |
| mapComplex4        | 664            | 174           | 509             |
| mapComplex5        | 802            | 101           | 157             |
| mapComplex6        | 290            | 290           | 219             |

👉 **Conclusión:** EstrategiaNat es la más equilibrada, estable y de buen rendimiento general.

---

## Manual del usuario

### ▶️ Ejecución

1. Iniciar el servidor

   ```bash
   java -cp dist/lib/jade.jar jade.Boot -name dba_server -gui
   ```
2. Ejecutar el programa.
3. Se mostrará la interfaz principal con:

   * Agente (círculo azul)
   * Objetivo (cuadrado verde)
   * Obstáculos (cuadrados negros)
   * Casillas visitadas (cuadrados amarillos)

### 🎛️ Configuración

En el menú superior podrás seleccionar:

* El mapa
* Posición inicial del agente
* Posición del objetivo

Pulsa **LANZAR AGENTE** para empezar.

Al terminar, se mostrará:

* Energía consumida
* Camino recorrido






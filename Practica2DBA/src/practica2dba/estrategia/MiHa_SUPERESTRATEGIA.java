package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.PriorityQueue; // Fila de prioridade (para A*)
import java.util.HashSet;
import java.util.Collections;

/**
 * @author zapi24 (Modificado com IA)
 *
 * Nome da Estratégia: EstrategiaAEstrela (A*)
 *
 * Esta estratégia é um AGENTE PLANEADOR.
 * 1. Mantém um mapa interno (mapaConhecido) de todas as células
 * que já observou.
 * 2. A cada passo, usa o algoritmo A* para calcular o caminho
 * mais curto conhecido do ponto atual até o objetivo.
 * 3. Executa o primeiro movimento desse caminho.
 * 4. Se encontrar um obstáculo inesperado, o mapa é atualizado
 * e o A* recalcula um novo caminho no próximo passo.
 */
public class MiHa_SUPERESTRATEGIA implements EstrategiaMovimiento {

    // --- Mapa interno do agente ---
    private enum TipoCelula { LIVRE, OBSTACULO, DESCONHECIDO }
    private HashMap<Coordenada, TipoCelula> mapaConhecido = new HashMap<>();

    // --- Estruturas de dados para o A* ---
    
    /**
     * Classe interna para representar um "Nó" (Node) no A*
     * Contém a coordenada, os custos e o "pai" (de onde viemos).
     */
    private class Node implements Comparable<Node> {
        Coordenada coord;
        double g; // Custo real desde o início
        double h; // Heurística (Manhattan) até o objetivo
        double f; // Custo total (g + h)
        Node parent;
        Movimiento movParaChegarAqui; // Movimento que levou até este nó

        Node(Coordenada coord, double g, double h, Node parent, Movimiento mov) {
            this.coord = coord;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
            this.movParaChegarAqui = mov;
        }

        // Permite que a PriorityQueue ordene pelo menor custo 'f'
        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }
    }

    // --- O Método de Decisão Principal ---

    @Override
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo) {
        
        // 1. Atualizar o nosso mapa interno com o que estamos a ver
        atualizarMapaConhecido(p);

        // 2. Calcular o melhor caminho com A*
        List<Movimiento> caminho = calcularCaminhoAEstrela(p.getPosicionActual(), objetivo);

        // 3. Executar o primeiro passo
        if (caminho != null && !caminho.isEmpty()) {
            return caminho.get(0); // Devolve o primeiro movimento do plano
        }

        // Failsafe: Se o A* falhar (sem caminho), não te mexas.
        return Movimiento.QUEDARSE;
    }

    /**
     * Usa os sensores da percepção para atualizar o mapa interno.
     */
    private void atualizarMapaConhecido(Percepcion p) {
        Coordenada actual = p.getPosicionActual();
        mapaConhecido.put(actual, TipoCelula.LIVRE); // Onde estamos é livre

        // Mapear vizinhos
        mapearVizinho(p, Movimiento.ARRIBA, new Coordenada(actual.getX(), actual.getY() - 1));
        mapearVizinho(p, Movimiento.ABAJO, new Coordenada(actual.getX(), actual.getY() + 1));
        mapearVizinho(p, Movimiento.IZQUIERDA, new Coordenada(actual.getX() - 1, actual.getY()));
        mapearVizinho(p, Movimiento.DERECHA, new Coordenada(actual.getX() + 1, actual.getY()));
    }

    private void mapearVizinho(Percepcion p, Movimiento mov, Coordenada coordVizinho) {
        // Não sobrescreve informação que já temos
        if (!mapaConhecido.containsKey(coordVizinho)) {
            if (p.getSensorMuro().get(mov)) {
                mapaConhecido.put(coordVizinho, TipoCelula.OBSTACULO);
            } else if (p.getSensorLibre().get(mov)) {
                mapaConhecido.put(coordVizinho, TipoCelula.LIVRE);
            }
            // Se ambos forem falsos (limite do mapa que não é muro),
            // a estratégia 'isCeldaTransitable' do Entorno trata disso,
            // mas aqui podemos assumir que se não é muro, é livre.
            // Para ser mais seguro, tratamos 'DESCONHECIDO'.
        }
    }

    /**
     * O algoritmo A* principal.
     */
    private List<Movimiento> calcularCaminhoAEstrela(Coordenada inicio, Coordenada objetivo) {
        
        // Fila de prioridade (Open Set), ordenada pelo menor 'f'
        PriorityQueue<Node> openList = new PriorityQueue<>();
        
        // Set de coordenadas já visitadas (Closed Set)
        HashSet<Coordenada> closedList = new HashSet<>();
        
        // Mapa para guardar os nós e os seus custos
        HashMap<Coordenada, Node> allNodes = new HashMap<>();

        // 1. Nó inicial
        double hInicial = calcularDistanciaManhattan(inicio, objetivo);
        Node startNode = new Node(inicio, 0, hInicial, null, null);
        openList.add(startNode);
        allNodes.put(inicio, startNode);

        while (!openList.isEmpty()) {
            // 2. Pega no nó com menor custo 'f'
            Node currentNode = openList.poll();

            // 3. Chegámos ao objetivo?
            if (currentNode.coord.equals(objetivo)) {
                return reconstruirCaminho(currentNode);
            }

            // 4. Já o explorámos, adiciona ao closedList
            closedList.add(currentNode.coord);

            // 5. Vê todos os vizinhos
            for (Movimiento mov : Movimiento.values()) {
                if (mov == Movimiento.QUEDARSE) continue;

                Coordenada coordVizinho = calcularCoordenadaFutura(currentNode.coord, mov);
                
                // Se já o explorámos, ignora
                if (closedList.contains(coordVizinho)) {
                    continue;
                }

                // Verifica se é uma célula transitável (LIVRE ou DESCONHECIDO)
                TipoCelula tipo = mapaConhecido.getOrDefault(coordVizinho, TipoCelula.DESCONHECIDO);
                if (tipo == TipoCelula.OBSTACULO) {
                    continue; // É um muro que conhecemos, ignora
                }
                
                // 6. Calcula os custos para este vizinho
                double gVizinho = currentNode.g + 1; // Custo para chegar aqui é +1
                double hVizinho = calcularDistanciaManhattan(coordVizinho, objetivo);
                
                Node vizinhoNode = allNodes.get(coordVizinho);

                if (vizinhoNode == null) {
                    // Nó novo, nunca visto nesta busca A*
                    vizinhoNode = new Node(coordVizinho, gVizinho, hVizinho, currentNode, mov);
                    allNodes.put(coordVizinho, vizinhoNode);
                    openList.add(vizinhoNode);
                } else if (gVizinho < vizinhoNode.g) {
                    // Encontrámos um caminho *melhor* (g mais baixo) para este nó
                    vizinhoNode.g = gVizinho;
                    vizinhoNode.f = gVizinho + hVizinho;
                    vizinhoNode.parent = currentNode;
                    vizinhoNode.movParaChegarAqui = mov;
                    
                    // Re-adiciona à fila para reavaliar (PriorityQueue trata duplicados)
                    openList.remove(vizinhoNode);
                    openList.add(vizinhoNode);
                }
            }
        }

        // 7. Se sairmos do loop, não há caminho
        return null;
    }

    /**
     * Reconstrói a lista de movimentos a partir dos "pais" (parent).
     */
    private List<Movimiento> reconstruirCaminho(Node noFinal) {
        List<Movimiento> caminho = new ArrayList<>();
        Node current = noFinal;

        // Volta para trás do objetivo até ao início
        while (current.parent != null) {
            caminho.add(current.movParaChegarAqui);
            current = current.parent;
        }

        // O caminho está ao contrário (Objetivo -> Início), inverte
        Collections.reverse(caminho);
        return caminho;
    }


    // --- Funções de Ajuda (copiadas de estratégias anteriores) ---

    private Coordenada calcularCoordenadaFutura(Coordenada actual, Movimiento mov) {
        Coordenada futura = new Coordenada(actual.getX(), actual.getY());
        switch(mov){
            case ARRIBA:    futura.setY(futura.getY() - 1); break;
            case ABAJO:     futura.setY(futura.getY() + 1); break;
            case IZQUIERDA: futura.setX(futura.getX() - 1); break;
            case DERECHA:   futura.setX(futura.getX() + 1); break;
            default: break; 
        }
        return futura;
    }

    private double calcularDistanciaManhattan(Coordenada a, Coordenada b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
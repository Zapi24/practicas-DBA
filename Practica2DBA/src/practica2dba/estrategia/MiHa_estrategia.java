package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

import java.util.*;

/**
 * A versão definitiva da estratégia. Combina o A* com uma exploração heurística avançada
 * e uma memória de curto prazo (lista tabu) para evitar loops e tomar decisões de exploração
 * muito mais inteligentes. Esta é a estratégia mais robusta para resolver qualquer labirinto.
 *
 * @author Gemini Assistant (Versão Final para MiHa_estrategia)
 */
public class MiHa_estrategia implements EstrategiaMovimiento {

    // Memória de longo prazo do mapa
    private final Map<Coordenada, Boolean> mapaConhecido = new HashMap<>();
    // Memória de todas as células que o agente já pisou
    private final Set<Coordenada> celulasVisitadas = new HashSet<>();
    // Memória de curto prazo para evitar loops simples (A->B, B->A)
    private final LinkedList<Coordenada> historicoRecente = new LinkedList<>();
    private static final int TAMANHO_HISTORICO = 4; // Guarda os últimos 4 passos

    private LinkedList<Movimiento> caminhoCalculado = new LinkedList<>();

    @Override
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo) {
        Coordenada atual = p.getPosicionActual();
        atualizarMemoria(atual, p);

        if (caminhoCalculado.isEmpty() || caminhoEstaBloqueado(p)) {
            recalcularCaminho(atual, objetivo);
        }

        if (!caminhoCalculado.isEmpty()) {
            return caminhoCalculado.poll();
        }

        System.out.println("[Último Recurso] Não foi possível calcular um caminho. A tentar desatascar.");
        return usarUltimoRecurso(p);
    }

    private void atualizarMemoria(Coordenada atual, Percepcion p) {
        // Atualiza o mapa conhecido com os sensores
        mapaConhecido.put(atual, true);
        mapaConhecido.put(new Coordenada(atual.getX(), atual.getY() - 1), p.getSensorLibre().get(Movimiento.ARRIBA));
        mapaConhecido.put(new Coordenada(atual.getX(), atual.getY() + 1), p.getSensorLibre().get(Movimiento.ABAJO));
        mapaConhecido.put(new Coordenada(atual.getX() - 1, atual.getY()), p.getSensorLibre().get(Movimiento.IZQUIERDA));
        mapaConhecido.put(new Coordenada(atual.getX() + 1, atual.getY()), p.getSensorLibre().get(Movimiento.DERECHA));
        
        // Adiciona a posição atual às visitadas e ao histórico recente
        celulasVisitadas.add(atual);
        historicoRecente.addFirst(atual);
        if (historicoRecente.size() > TAMANHO_HISTORICO) {
            historicoRecente.removeLast();
        }
    }

    private void recalcularCaminho(Coordenada atual, Coordenada objetivo) {
        System.out.println("[Cérebro] A recalcular rota a partir de " + atual);
        
        // 1. Tentar encontrar o caminho ideal para o objetivo final
        List<Coordenada> caminhoParaObjetivo = calcularCaminhoAEstrela(atual, objetivo, objetivo);
        if (caminhoParaObjetivo != null) {
            System.out.println("[Cérebro] Encontrei um caminho direto para o objetivo! A executar.");
            this.caminhoCalculado = converterCoordenadasParaMovimentos(caminhoParaObjetivo, atual);
            return;
        }

        // 2. Se não houver caminho direto, encontrar o melhor ponto para explorar
        System.out.println("[Explorador] Caminho direto desconhecido. A procurar o melhor ponto de exploração...");
        Coordenada pontoDeExploracao = encontrarMelhorPontoDeExploracao(atual, objetivo);
        if (pontoDeExploracao != null) {
            System.out.println("[Explorador] Ponto de exploração escolhido: " + pontoDeExploracao + ". A planear rota para lá.");
            List<Coordenada> caminhoParaExplorar = calcularCaminhoAEstrela(atual, pontoDeExploracao, objetivo);
            if (caminhoParaExplorar != null) {
                this.caminhoCalculado = converterCoordenadasParaMovimentos(caminhoParaExplorar, atual);
            }
        }
    }

    /**
     * O núcleo da exploração inteligente.
     * Encontra o ponto não visitado que tem a melhor pontuação combinada de
     * (distância desde o agente) + (distância até ao objetivo).
     * Isto prioriza a exploração de fronteiras que parecem ser "atalhos".
     */
    private Coordenada encontrarMelhorPontoDeExploracao(Coordenada atual, Coordenada objetivo) {
        return mapaConhecido.entrySet().stream()
                .filter(entry -> entry.getValue() && !celulasVisitadas.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .min(Comparator.comparingInt(c -> {
                    // Pontuação = Custo para chegar (g) + Heurística para o fim (h)
                    int pontuacao = distanciaManhattan(atual, c) + distanciaManhattan(c, objetivo);
                    // Penaliza pontos que estão na memória recente para evitar loops
                    if (historicoRecente.contains(c)) {
                        pontuacao += 20; 
                    }
                    return pontuacao;
                }))
                .orElse(null);
    }
    
    private List<Coordenada> calcularCaminhoAEstrela(Coordenada inicio, Coordenada fim, Coordenada objetivoFinal) {
        PriorityQueue<Nodo> openSet = new PriorityQueue<>();
        Map<Nodo, Nodo> cameFrom = new HashMap<>();
        Map<Nodo, Integer> gScore = new HashMap<>();
        
        Nodo nodoInicial = new Nodo(inicio, 0, distanciaManhattan(inicio, fim));
        openSet.add(nodoInicial);
        gScore.put(nodoInicial, 0);

        while (!openSet.isEmpty()) {
            Nodo atual = openSet.poll();
            if (atual.getCoord().equals(fim)) {
                return reconstruirCaminho(cameFrom, atual);
            }
            for (Coordenada vizinhoCoord : getVizinhos(atual.getCoord())) {
                if (mapaConhecido.getOrDefault(vizinhoCoord, false)) {
                    int gScoreTentativo = gScore.getOrDefault(atual, Integer.MAX_VALUE) + 1;
                    Nodo vizinhoNodo = new Nodo(vizinhoCoord);
                    if (gScoreTentativo < gScore.getOrDefault(vizinhoNodo, Integer.MAX_VALUE)) {
                        cameFrom.put(vizinhoNodo, atual);
                        gScore.put(vizinhoNodo, gScoreTentativo);
                        vizinhoNodo.setG(gScoreTentativo);
                        // A heurística é sempre em relação ao objetivo FINAL, não ao ponto de exploração
                        vizinhoNodo.setH(distanciaManhattan(vizinhoCoord, objetivoFinal)); 
                        if (!openSet.contains(vizinhoNodo)) {
                            openSet.add(vizinhoNodo);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    // --- Funções Auxiliares ---

    private boolean caminhoEstaBloqueado(Percepcion p) {
        if (caminhoCalculado.isEmpty()) return false;
        Movimiento proximoMovimento = caminhoCalculado.peek();
        switch (proximoMovimento) {
            case ARRIBA: return !p.getSensorLibre().get(Movimiento.ARRIBA);
            case ABAJO: return !p.getSensorLibre().get(Movimiento.ABAJO);
            case IZQUIERDA: return !p.getSensorLibre().get(Movimiento.IZQUIERDA);
            case DERECHA: return !p.getSensorLibre().get(Movimiento.DERECHA);
            default: return false;
        }
    }

    private Movimiento usarUltimoRecurso(Percepcion p) {
        Coordenada atual = p.getPosicionActual();
        Coordenada posAnterior = historicoRecente.size() > 1 ? historicoRecente.get(1) : null;
        
        if (p.getSensorLibre().get(Movimiento.ARRIBA) && !new Coordenada(atual.getX(), atual.getY() - 1).equals(posAnterior)) return Movimiento.ARRIBA;
        if (p.getSensorLibre().get(Movimiento.DERECHA) && !new Coordenada(atual.getX() + 1, atual.getY()).equals(posAnterior)) return Movimiento.DERECHA;
        if (p.getSensorLibre().get(Movimiento.ABAJO) && !new Coordenada(atual.getX(), atual.getY() + 1).equals(posAnterior)) return Movimiento.ABAJO;
        if (p.getSensorLibre().get(Movimiento.IZQUIERDA) && !new Coordenada(atual.getX() - 1, atual.getY()).equals(posAnterior)) return Movimiento.IZQUIERDA;
        
        if (p.getSensorLibre().get(Movimiento.ARRIBA)) return Movimiento.ARRIBA;
        if (p.getSensorLibre().get(Movimiento.DERECHA)) return Movimiento.DERECHA;
        if (p.getSensorLibre().get(Movimiento.ABAJO)) return Movimiento.ABAJO;
        if (p.getSensorLibre().get(Movimiento.IZQUIERDA)) return Movimiento.IZQUIERDA;
        
        return Movimiento.QUEDARSE;
    }

    private int distanciaManhattan(Coordenada a, Coordenada b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private List<Coordenada> getVizinhos(Coordenada coord) {
        return Arrays.asList(
                new Coordenada(coord.getX(), coord.getY() - 1), new Coordenada(coord.getX(), coord.getY() + 1),
                new Coordenada(coord.getX() - 1, coord.getY()), new Coordenada(coord.getX() + 1, coord.getY())
        );
    }
    
    private List<Coordenada> reconstruirCaminho(Map<Nodo, Nodo> cameFrom, Nodo atual) {
        List<Coordenada> caminhoTotal = new LinkedList<>();
        caminhoTotal.add(atual.getCoord());
        while (cameFrom.containsKey(atual)) {
            atual = cameFrom.get(atual);
            caminhoTotal.add(atual.getCoord());
        }
        Collections.reverse(caminhoTotal);
        if (!caminhoTotal.isEmpty()) {
            caminhoTotal.remove(0);
        }
        return caminhoTotal;
    }

    private LinkedList<Movimiento> converterCoordenadasParaMovimentos(List<Coordenada> caminho, Coordenada posInicial) {
        LinkedList<Movimiento> movimentos = new LinkedList<>();
        Coordenada anterior = posInicial;
        for (Coordenada proximo : caminho) {
            if (proximo.getY() < anterior.getY()) movimentos.add(Movimiento.ARRIBA);
            else if (proximo.getY() > anterior.getY()) movimentos.add(Movimiento.ABAJO);
            else if (proximo.getX() < anterior.getX()) movimentos.add(Movimiento.IZQUIERDA);
            else if (proximo.getX() > anterior.getX()) movimentos.add(Movimiento.DERECHA);
            anterior = proximo;
        }
        return movimentos;
    }

    private class Nodo implements Comparable<Nodo> {
        private final Coordenada coord;
        private int g; private int h;
        public Nodo(Coordenada coord, int g, int h) { this.coord = coord; this.g = g; this.h = h; }
        public Nodo(Coordenada coord) { this(coord, Integer.MAX_VALUE, Integer.MAX_VALUE); }
        public Coordenada getCoord() { return coord; }
        public int getF() { return g + h; }
        public void setG(int g) { this.g = g; }
        public void setH(int h) { this.h = h; }
        @Override public int compareTo(Nodo other) { return Integer.compare(this.getF(), other.getF()); }
        @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; Nodo nodo = (Nodo) o; return Objects.equals(coord, nodo.coord); }
        @Override public int hashCode() { return Objects.hash(coord.getX(), coord.getY()); }
    }
}
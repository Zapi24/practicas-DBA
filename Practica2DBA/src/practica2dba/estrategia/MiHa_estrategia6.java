package practica2dba.estrategia;

import practica2dba.utils.Coordenada;
import practica2dba.utils.Movimiento;
import practica2dba.utils.Percepcion;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MiHa_estrategia6 implements EstrategiaMovimiento {

    // --- Penalizações ---
    private static final int PENALIDADE_CELULA = 5; 
    private static final int PENALIDADE_ACAO = 10; 
    private static final int PENALIDADE_AFASTAR_PAREDE_MODOBUSCA = 1000;
    private static final int PENALIDADE_AFASTAR_PAREDE_MODOFINAL = 1000;
    private static final int PENALIDADE_JA_PISADO_MODOFINAL = 200; 
    private static final int PENALIDADE_LOOP_MODOFINAL = 20; 
    
    private static final int BONUS_CURVA_ESQUINA = -5000;

    // --- Variáveis de Estado ---
    private static final int JOGADAS_NA_PAREDE_TOTAL = 15; 
    private int contadorJogadasNaParede = 0;
    
    private static final int DISTANCIA_RAIO_FINAL = 5;
    private boolean modoFinalAtivado = false;
    private Movimiento direcaoGeralObjetivo = null;
    
    private int contadorJogadasSemParede_Modo2 = 0; 
    private boolean pertoDaParede_Modo2 = false; 

    // --- Checks Modo Final ---
    private boolean esquerdaCheck = false;
    private boolean direitaCheck = false;
    private int contadorOpostoEsquerda = 0;
    private int contadorOpostoDireita = 0;
    private HashMap<Coordenada, Integer> mapaVisitas_ModoFinal = new HashMap<>();

    // --- Memórias ---
    private HashMap<Coordenada, Integer> mapaDeVisitas_Celula = new HashMap<>();
    private HashMap<Coordenada, HashMap<Movimiento, Integer>> mapaDeVisitas_Acao = new HashMap<>();
    private Coordenada ultimaPosicao = null;
    private Movimiento ultimoMovimientoDecidido = null;


    @Override
    public Movimiento decidirMovimiento(Percepcion p, Coordenada objetivo) {

        Coordenada actual = p.getPosicionActual();
        double distAtual = calcularDistanciaManhattan(actual, objetivo);

        // 1. Gatilho Modo Final
        boolean raioAux = (distAtual <= DISTANCIA_RAIO_FINAL);
        if (!modoFinalAtivado && raioAux) {
            Movimiento direcao = calcularDirecaoGeral(actual, objetivo, p);
            boolean paredeEmFrente = false;
            if (direcao == Movimiento.ARRIBA && p.getSensorMuro().get(Movimiento.ARRIBA)) paredeEmFrente = true;
            else if (direcao == Movimiento.ABAJO && p.getSensorMuro().get(Movimiento.ABAJO)) paredeEmFrente = true;
            else if (direcao == Movimiento.IZQUIERDA && p.getSensorMuro().get(Movimiento.IZQUIERDA)) paredeEmFrente = true;
            else if (direcao == Movimiento.DERECHA && p.getSensorMuro().get(Movimiento.DERECHA)) paredeEmFrente = true;
            
            if (paredeEmFrente) {
                this.modoFinalAtivado = true;
                this.direcaoGeralObjetivo = direcao;
                this.mapaVisitas_ModoFinal.clear();
            }
        }
        
        // 2. Atualizar Memórias
        mapaDeVisitas_Celula.put(actual, mapaDeVisitas_Celula.getOrDefault(actual, 0) + 1);
        if (ultimoMovimientoDecidido != null && ultimaPosicao != null) { 
            HashMap<Movimiento, Integer> acoesDaUltimaPosicao = 
                mapaDeVisitas_Acao.getOrDefault(ultimaPosicao, new HashMap<>());
            int contagemAcao = acoesDaUltimaPosicao.getOrDefault(ultimoMovimientoDecidido, 0);
            acoesDaUltimaPosicao.put(ultimoMovimientoDecidido, contagemAcao + 1);
            mapaDeVisitas_Acao.put(ultimaPosicao, acoesDaUltimaPosicao);
        }

        // 3. Escolher Cérebro
        Movimiento mejorMovimiento;
        System.out.println("  > Raio Final Ativo: " + this.modoFinalAtivado);
        
        if (modoFinalAtivado) {
            mejorMovimiento = decidirMovimento_ModoFinal(p, objetivo, actual);
        } else {
            mejorMovimiento = decidirMovimento_ModoBusca(p, objetivo, actual);
        }

        // 4. Atualizar Estado
        this.ultimaPosicao = actual;
        this.ultimoMovimientoDecidido = mejorMovimiento;
        return mejorMovimiento;
    }


    // --- MODO BUSCA ---
    private Movimiento decidirMovimento_ModoBusca(Percepcion p, Coordenada objetivo, Coordenada actual) {
        List<Movimiento> movimentosPossores = getMovimentosPossiveis(p);
        if (movimentosPossores.isEmpty()) return Movimiento.QUEDARSE;

        boolean haParedes = haParedes(p);
        Movimiento mejorMovimiento = null;
        double minCustoTotal = Double.MAX_VALUE;
        boolean podeSairDaParede = (contadorJogadasNaParede > JOGADAS_NA_PAREDE_TOTAL);

        System.out.println("  > [BUSCA] Timer Parede: " + contadorJogadasNaParede + "/" + JOGADAS_NA_PAREDE_TOTAL + " (Pode Sair: " + podeSairDaParede + ")");

        for (Movimiento mov : movimentosPossores) {
            Coordenada futuraCord = calcularCoordenadaFutura(actual, mov);
            double custoManhattan = calcularDistanciaManhattan(futuraCord, objetivo);
            double custoParede = 0;
            double custoVisitas = getCustoVisitas_ModoBusca(actual, futuraCord, mov);

            boolean isLargarParede = isLargarParede(p, mov);
            if (haParedes && isLargarParede) {
                if (!podeSairDaParede) {
                    custoParede = PENALIDADE_AFASTAR_PAREDE_MODOBUSCA;
                }
            }
            
            double custoTotal = custoManhattan + custoVisitas + custoParede;
            if (custoTotal < minCustoTotal) {
                minCustoTotal = custoTotal;
                mejorMovimiento = mov;
            }
        }
        
        atualizarTimer(haParedes);
        return mejorMovimiento;
    }


    // --- MODO FINAL ---
    private Movimiento decidirMovimento_ModoFinal(Percepcion p, Coordenada objetivo, Coordenada actual) {
        
        boolean haParedes = haParedes(p);
        atualizarContadorParede_Modo2(haParedes); // <-- MÉTODO RESTAURADO AQUI
        boolean ignoraManhattan = this.pertoDaParede_Modo2;
        
        boolean forcarCurvaDireita = false;
        if (haParedes) {
            this.contadorJogadasSemParede_Modo2 = 0; 
        } else {
            this.contadorJogadasSemParede_Modo2++; 
        }
        if (this.contadorJogadasSemParede_Modo2 == 1) {
            forcarCurvaDireita = true; 
            System.out.println("    > [MODO FINAL] CANTO DETETADO! A tentar curvar à direita relativa.");
        }
        
        if (forcarCurvaDireita) ignoraManhattan = true;
        
        Movimiento target = this.direcaoGeralObjetivo;
        Movimiento contornoA = getContornoAntiHorario(target); 
        Movimiento contornoB = getContornoHorario(target);     
        Movimiento oposto = getOposto(target);
        
        List<Movimiento> ordemPrioridade = new ArrayList<>();
        if (!esquerdaCheck) {
            ordemPrioridade.add(target);
            ordemPrioridade.add(contornoA);
            ordemPrioridade.add(contornoB);
            ordemPrioridade.add(oposto);
        } else if (!direitaCheck) {
            ordemPrioridade.add(target);
            ordemPrioridade.add(contornoB);
            ordemPrioridade.add(contornoA);
            ordemPrioridade.add(oposto);
        } else {
            System.out.println("--- FALHA TOTAL. RESETANDO. ---");
            this.esquerdaCheck = false;
            this.direitaCheck = false;
            this.contadorOpostoEsquerda = 0;
            this.contadorOpostoDireita = 0;
            this.mapaVisitas_ModoFinal.clear();
            ordemPrioridade.add(target);
            ordemPrioridade.add(contornoA);
            ordemPrioridade.add(contornoB);
            ordemPrioridade.add(oposto);
        }
        
        System.out.println("  > [FINAL] IgnoraManhattan: " + ignoraManhattan + " | Forçar Curva: " + forcarCurvaDireita);
        
        Movimiento mejorMovimiento = null;
        double minCustoTotal = Double.MAX_VALUE;
        HashMap<Movimiento, Integer> acoesDaPosicaoAtual = mapaDeVisitas_Acao.getOrDefault(actual, new HashMap<>());
        this.mapaVisitas_ModoFinal.put(actual, 1);

        for (Movimiento mov : ordemPrioridade) {
            if (!p.getSensorLibre().get(mov)) continue;

            Coordenada futuraCord = calcularCoordenadaFutura(actual, mov);

            double custoManhattan = ignoraManhattan ? 0 : calcularDistanciaManhattan(futuraCord, objetivo);
            int contagemPisado = mapaVisitas_ModoFinal.getOrDefault(futuraCord, 0);
            double custoVisitas = (contagemPisado > 0) ? PENALIDADE_JA_PISADO_MODOFINAL : 0;
            int contagemAcao = acoesDaPosicaoAtual.getOrDefault(mov, 0);
            double custoAcao = contagemAcao * PENALIDADE_LOOP_MODOFINAL;
            double custoPrioridade = (ordemPrioridade.indexOf(mov) * 0.1); 
            
            double custoParede = 0;
            boolean isLargarParede = isLargarParede(p, mov);
            if (haParedes && isLargarParede) {
                custoParede = PENALIDADE_AFASTAR_PAREDE_MODOFINAL;
            }

            double custoCurva = 0;
            if (forcarCurvaDireita) {
                Movimiento direitaRelativa = getDireitaRelativa(this.ultimoMovimientoDecidido);
                if (mov == direitaRelativa) {
                    custoCurva = BONUS_CURVA_ESQUINA; 
                }
            }

            double custoTotal = custoManhattan + custoVisitas + custoAcao + custoPrioridade + custoParede + custoCurva;
            
            if (custoTotal < minCustoTotal) {
                minCustoTotal = custoTotal;
                mejorMovimiento = mov;
            }
        }
        
        if (mejorMovimiento == null) return Movimiento.QUEDARSE;

        if (mejorMovimiento == oposto) {
            if (!esquerdaCheck) {
                this.contadorOpostoEsquerda++;
                if (this.contadorOpostoEsquerda > 10) {
                    this.esquerdaCheck = true;
                    this.mapaVisitas_ModoFinal.clear(); 
                }
            } else if (!direitaCheck) {
                this.contadorOpostoDireita++;
                if (this.contadorOpostoDireita > 10) {
                    this.direitaCheck = true;
                    this.mapaVisitas_ModoFinal.clear(); 
                }
            }
        } else {
            if (!esquerdaCheck) this.contadorOpostoEsquerda = 0;
            if (!direitaCheck) this.contadorOpostoDireita = 0;
        }

        return mejorMovimiento;
    }


    // --- FUNÇÕES AUXILIARES ---

    private List<Movimiento> getMovimentosPossiveis(Percepcion p) {
        List<Movimiento> movimentosPossores = new ArrayList<>();
        if (p.getSensorLibre().get(Movimiento.ARRIBA))    movimentosPossores.add(Movimiento.ARRIBA);
        if (p.getSensorLibre().get(Movimiento.ABAJO))     movimentosPossores.add(Movimiento.ABAJO);
        if (p.getSensorLibre().get(Movimiento.IZQUIERDA)) movimentosPossores.add(Movimiento.IZQUIERDA);
        if (p.getSensorLibre().get(Movimiento.DERECHA))   movimentosPossores.add(Movimiento.DERECHA);
        return movimentosPossores;
    }
    
    private boolean haParedes(Percepcion p) {
        return p.getSensorMuro().get(Movimiento.ARRIBA) || 
               p.getSensorMuro().get(Movimiento.ABAJO) || 
               p.getSensorMuro().get(Movimiento.IZQUIERDA) || 
               p.getSensorMuro().get(Movimiento.DERECHA);
    }
    
    private boolean isLargarParede(Percepcion p, Movimiento mov) {
        return (p.getSensorMuro().get(Movimiento.ARRIBA) && mov == Movimiento.ABAJO) ||
               (p.getSensorMuro().get(Movimiento.ABAJO) && mov == Movimiento.ARRIBA) ||
               (p.getSensorMuro().get(Movimiento.IZQUIERDA) && mov == Movimiento.DERECHA) ||
               (p.getSensorMuro().get(Movimiento.DERECHA) && mov == Movimiento.IZQUIERDA);
    }

    private double getCustoVisitas_ModoBusca(Coordenada actual, Coordenada futuraCord, Movimiento mov) {
        int contagemCelula = mapaDeVisitas_Celula.getOrDefault(futuraCord, 0);
        HashMap<Movimiento, Integer> acoesDaPosicaoAtual = 
            mapaDeVisitas_Acao.getOrDefault(actual, new HashMap<>());
        int contagemAcao = acoesDaPosicaoAtual.getOrDefault(mov, 0);
        return (contagemCelula * PENALIDADE_CELULA) + (contagemAcao * PENALIDADE_ACAO);
    }
    
    private void atualizarTimer(boolean haParedes) {
        if (!haParedes) {
            this.contadorJogadasNaParede = 0;
        } else {
            this.contadorJogadasNaParede++;
            if (this.contadorJogadasNaParede > JOGADAS_NA_PAREDE_TOTAL) {
                 this.contadorJogadasNaParede = JOGADAS_NA_PAREDE_TOTAL + 1;
            }
        }
    }
    
    // --- MÉTODO RESTAURADO ---
    private void atualizarContadorParede_Modo2(boolean haParedes) {
        this.pertoDaParede_Modo2 = haParedes;
    }
    
    private Movimiento getDireitaRelativa(Movimiento anterior) {
        if (anterior == null) return Movimiento.QUEDARSE;
        switch (anterior) {
            case ARRIBA: return Movimiento.DERECHA;
            case ABAJO: return Movimiento.IZQUIERDA;
            case IZQUIERDA: return Movimiento.ARRIBA;
            case DERECHA: return Movimiento.ABAJO;
        }
        return Movimiento.QUEDARSE;
    }

    private Movimiento calcularDirecaoGeral(Coordenada actual, Coordenada objetivo, Percepcion p) {
        int valorX = objetivo.getX() - actual.getX();
        int valorY = objetivo.getY() - actual.getY();
        if (Math.abs(valorY) > Math.abs(valorX)) {
            return (valorY < 0) ? Movimiento.ARRIBA : Movimiento.ABAJO;
        } else {
            return (valorX < 0) ? Movimiento.IZQUIERDA : Movimiento.DERECHA;
        }
    }
    
    private Movimiento getContornoAntiHorario(Movimiento target) {
        switch (target) {
            case ARRIBA: return Movimiento.IZQUIERDA;
            case IZQUIERDA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.ARRIBA;
        }
        return Movimiento.QUEDARSE;
    }
    
    private Movimiento getContornoHorario(Movimiento target) {
        switch (target) {
            case ARRIBA: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.IZQUIERDA;
            case IZQUIERDA: return Movimiento.ARRIBA;
        }
        return Movimiento.QUEDARSE;
    }
    
    private Movimiento getOposto(Movimiento target) {
        switch (target) {
            case ARRIBA: return Movimiento.ABAJO;
            case ABAJO: return Movimiento.ARRIBA;
            case IZQUIERDA: return Movimiento.DERECHA;
            case DERECHA: return Movimiento.IZQUIERDA;
        }
        return Movimiento.QUEDARSE;
    }

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

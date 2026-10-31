package com.a2.backend.arvore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Algoritmos sobre a árvore de fórmula (Java puro, sem Spring).
 *
 * Regras:
 *  - quantidade é por unidade do pai;
 *  - o valor de uma folha é o preço de compra;
 *  - o valor de um item composto é a soma de (valor × quantidade) dos filhos.
 */
public final class ArvoreGenerica {

    private static final double TOLERANCIA = 0.005;

    private ArvoreGenerica() {
    }

    // ------------------------------------------------------------------
    // Construção
    // ------------------------------------------------------------------

    /**
     * Monta a árvore a partir da lista de itens (o filho aponta para o NOME do pai).
     * Lança IllegalArgumentException se a fórmula for inválida.
     */
    public static TreeNode<ItemFormula> buildTree(List<ItemFormula> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("A fórmula não possui itens");
        }

        Map<String, List<ItemFormula>> filhosPorPai = new HashMap<>();
        Map<String, Integer> ocorrencias = new HashMap<>();
        ItemFormula itemRaiz = null;

        for (ItemFormula item : itens) {
            validarItem(item);
            ocorrencias.merge(item.getNome(), 1, Integer::sum);

            if (item.getPai() == null) {
                if (itemRaiz != null) {
                    throw new IllegalArgumentException("A fórmula deve ter exatamente um item raiz (sem pai)");
                }
                itemRaiz = item;
            } else {
                filhosPorPai.computeIfAbsent(item.getPai(), k -> new ArrayList<>()).add(item);
            }
        }

        if (itemRaiz == null) {
            throw new IllegalArgumentException("A fórmula deve ter exatamente um item raiz (sem pai)");
        }

        for (String nomePai : filhosPorPai.keySet()) {
            int qtd = ocorrencias.getOrDefault(nomePai, 0);
            if (qtd == 0) {
                throw new IllegalArgumentException("O item pai \"" + nomePai + "\" não existe");
            }
            if (qtd > 1) {
                throw new IllegalArgumentException(
                        "O nome \"" + nomePai + "\" é usado por mais de um item e não pode ser pai");
            }
        }

        TreeNode<ItemFormula> raiz = new TreeNode<>(itemRaiz);
        adicionarFilhos(raiz, filhosPorPai);

        if (raiz.preOrder().size() != itens.size()) {
            throw new IllegalArgumentException("A fórmula contém um ciclo ou itens desconectados da raiz");
        }
        return raiz;
    }

    private static void validarItem(ItemFormula item) {
        if (item == null || item.getNome() == null || item.getNome().isBlank()) {
            throw new IllegalArgumentException("Todos os itens precisam de um nome");
        }
        if (item.getQuantidade() <= 0) {
            throw new IllegalArgumentException("Quantidade inválida em \"" + item.getNome() + "\"");
        }
        if (item.getValor() < 0 || Double.isNaN(item.getValor())) {
            throw new IllegalArgumentException("Valor inválido em \"" + item.getNome() + "\"");
        }
    }

    private static void adicionarFilhos(TreeNode<ItemFormula> pai, Map<String, List<ItemFormula>> filhosPorPai) {
        List<ItemFormula> filhos = filhosPorPai.get(pai.getData().getNome());
        if (filhos == null) {
            return;
        }
        for (ItemFormula item : filhos) {
            adicionarFilhos(pai.addChild(new TreeNode<>(item)), filhosPorPai);
        }
    }

    // ------------------------------------------------------------------
    // Exibição
    // ------------------------------------------------------------------

    public static void printTree(TreeNode<ItemFormula> no, int nivel) {
        if (no == null) {
            return;
        }
        ItemFormula item = no.getData();
        System.out.printf("%s%s (qtd %d, R$ %.2f)%n", "  ".repeat(nivel), item.getNome(),
                item.getQuantidade(), item.getValor());
        for (TreeNode<ItemFormula> filho : no.getChildren()) {
            printTree(filho, nivel + 1);
        }
    }

    // ------------------------------------------------------------------
    // Cálculo do custo
    // ------------------------------------------------------------------

    /** Custo de UMA unidade do produto, somando as folhas (ignora o valor gravado nos itens compostos). */
    public static double calcularTotal(TreeNode<ItemFormula> no) {
        if (no == null) {
            return 0;
        }
        if (no.isLeaf()) {
            return no.getData().getValor();
        }
        double total = 0;
        for (TreeNode<ItemFormula> filho : no.getChildren()) {
            total += calcularTotal(filho) * filho.getData().getQuantidade();
        }
        return arredondar(total);
    }

    // ------------------------------------------------------------------
    // Explosão
    // ------------------------------------------------------------------

    public static NoExplosao explodir(TreeNode<ItemFormula> no) {
        ItemFormula item = no.getData();
        List<NoExplosao> filhos = new ArrayList<>();
        for (TreeNode<ItemFormula> filho : no.getChildren()) {
            filhos.add(explodir(filho));
        }
        return new NoExplosao(item.getId(), item.getNome(), item.getQuantidade(), item.getValor(),
                arredondar(item.getValor() * item.getQuantidade()), filhos);
    }

    // ------------------------------------------------------------------
    // Necessidade de componentes para produzir N unidades
    // ------------------------------------------------------------------

    /**
     * Quantidade de cada componente básico (folha) e valor de aquisição para produzir
     * {@code quantidade} unidades. Folhas com o mesmo nome são somadas e o valor
     * unitário exibido é a média ponderada.
     */
    public static Necessidade calcularNecessidade(TreeNode<ItemFormula> raiz, int quantidade) {
        if (raiz == null || quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }

        Map<String, double[]> porNome = new LinkedHashMap<>(); // [quantidade, valorTotal]
        acumularFolhas(raiz, quantidade, porNome);

        List<ComponenteNecessario> componentes = new ArrayList<>();
        double valorTotal = 0;
        for (Map.Entry<String, double[]> e : porNome.entrySet()) {
            double qtd = e.getValue()[0];
            double total = e.getValue()[1];
            componentes.add(new ComponenteNecessario(e.getKey(), (int) qtd, arredondar(total / qtd), arredondar(total)));
            valorTotal += total;
        }
        return new Necessidade(quantidade, componentes, arredondar(valorTotal));
    }

    private static void acumularFolhas(TreeNode<ItemFormula> no, long multiplicador, Map<String, double[]> acc) {
        if (no.isLeaf()) {
            double[] a = acc.computeIfAbsent(no.getData().getNome(), k -> new double[2]);
            a[0] += multiplicador;
            a[1] += multiplicador * no.getData().getValor();
            return;
        }
        for (TreeNode<ItemFormula> filho : no.getChildren()) {
            acumularFolhas(filho, multiplicador * filho.getData().getQuantidade(), acc);
        }
    }

    // ------------------------------------------------------------------
    // Atualização de valor e implosão
    // ------------------------------------------------------------------

    public static TreeNode<ItemFormula> buscarPorId(TreeNode<ItemFormula> raiz, Integer id) {
        if (raiz == null || id == null) {
            return null;
        }
        for (TreeNode<ItemFormula> no : raiz.preOrder()) {
            if (id.equals(no.getData().getId())) {
                return no;
            }
        }
        return null;
    }

    /** Atualiza o valor de um item pelo id. Retorna false se o item não existir. */
    public static boolean atualizarValor(TreeNode<ItemFormula> raiz, Integer id, double novoValor) {
        if (novoValor < 0 || Double.isNaN(novoValor)) {
            throw new IllegalArgumentException("O valor deve ser maior ou igual a zero");
        }
        TreeNode<ItemFormula> no = buscarPorId(raiz, id);
        if (no == null) {
            return false;
        }
        no.getData().setValor(arredondar(novoValor));
        return true;
    }

    /**
     * Implosão: recalcula, de baixo para cima, o valor de cada item composto como a soma de
     * (valor × quantidade) dos filhos. Retorna só os itens cujo valor mudou.
     */
    public static List<AlteracaoValor> implosao(TreeNode<ItemFormula> raiz) {
        List<AlteracaoValor> alteracoes = new ArrayList<>();
        if (raiz == null) {
            return alteracoes;
        }
        for (TreeNode<ItemFormula> no : raiz.postOrder()) {
            if (no.isLeaf()) {
                continue;
            }
            double novo = 0;
            for (TreeNode<ItemFormula> filho : no.getChildren()) {
                novo += filho.getData().getValor() * filho.getData().getQuantidade();
            }
            novo = arredondar(novo);

            ItemFormula item = no.getData();
            if (Math.abs(novo - item.getValor()) >= TOLERANCIA) {
                alteracoes.add(new AlteracaoValor(item.getId(), item.getNome(), item.getValor(), novo));
                item.setValor(novo);
            }
        }
        return alteracoes;
    }

    static double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}

package com.a2.backend.arvore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/** Testes de Java puro (não precisam de banco nem de Spring). */
class ArvoreGenericaTest {

    private static List<ItemFormula> caneta() {
        return numerar(Arrays.asList(
                new ItemFormula("Caneta", null, 1, 2.50),
                new ItemFormula("Tinta", "Caneta", 1, 0.80),
                new ItemFormula("Pigmento", "Tinta", 1, 0.30),
                new ItemFormula("Solvente", "Tinta", 1, 0.50),
                new ItemFormula("Corpo", "Caneta", 1, 1.00),
                new ItemFormula("Plástico", "Corpo", 1, 0.70),
                new ItemFormula("Clip", "Corpo", 1, 0.30),
                new ItemFormula("Tampa", "Caneta", 1, 0.70),
                new ItemFormula("Plástico", "Tampa", 1, 0.50),
                new ItemFormula("Vedação", "Tampa", 1, 0.20)));
    }

    private static List<ItemFormula> computador() {
        return numerar(Arrays.asList(
                new ItemFormula("Computador", null, 1, 2500.00),
                new ItemFormula("Gabinete", "Computador", 1, 800.00),
                new ItemFormula("Placa-Mãe", "Gabinete", 1, 500.00),
                new ItemFormula("CPU", "Placa-Mãe", 1, 1000.00),
                new ItemFormula("Memória RAM", "Placa-Mãe", 2, 200.00),
                new ItemFormula("Chipset", "Placa-Mãe", 1, 150.00),
                new ItemFormula("Fonte de Alimentação", "Gabinete", 1, 300.00),
                new ItemFormula("Armazenamento", "Gabinete", 1, 0.00),
                new ItemFormula("HDD", "Armazenamento", 1, 250.00),
                new ItemFormula("SSD", "Armazenamento", 1, 400.00),
                new ItemFormula("Monitor", "Computador", 1, 900.00),
                new ItemFormula("Tela LCD", "Monitor", 1, 600.00),
                new ItemFormula("Carcaça", "Monitor", 1, 100.00),
                new ItemFormula("Periféricos", "Computador", 1, 0.00),
                new ItemFormula("Teclado", "Periféricos", 1, 100.00),
                new ItemFormula("Mouse", "Periféricos", 1, 80.00)));
    }

    /** Simula os ids que o banco geraria. */
    private static List<ItemFormula> numerar(List<ItemFormula> itens) {
        int id = 1;
        for (ItemFormula item : itens) {
            item.setId(id++);
        }
        return itens;
    }

    private static NoExplosao achar(NoExplosao no, String nome) {
        if (no.nome().equals(nome)) {
            return no;
        }
        for (NoExplosao filho : no.filhos()) {
            NoExplosao r = achar(filho, nome);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    // ---------------- Teste 1: caneta ----------------

    @Test
    void canetaTotal() {
        assertEquals(2.50, ArvoreGenerica.calcularTotal(ArvoreGenerica.buildTree(caneta())), 0.001);
    }

    @Test
    void canetaEstruturaDaArvore() {
        TreeNode<ItemFormula> raiz = ArvoreGenerica.buildTree(caneta());
        assertEquals(10, raiz.preOrder().size());
        assertEquals(3, raiz.getChildren().size());
        assertEquals("Tinta", raiz.getChildren().get(0).getData().getNome());
    }

    @Test
    void canetaExplosao() {
        NoExplosao e = ArvoreGenerica.explodir(ArvoreGenerica.buildTree(caneta()));
        assertEquals("Caneta", e.nome());
        assertEquals(3, e.filhos().size());
        assertEquals(0.80, achar(e, "Tinta").valorTotal(), 0.001);
    }

    @Test
    void canetaProduzir10() {
        Necessidade n = ArvoreGenerica.calcularNecessidade(ArvoreGenerica.buildTree(caneta()), 10);
        assertEquals(25.00, n.valorTotal(), 0.001);
        assertEquals(5, n.componentes().size());

        ComponenteNecessario plastico = n.componentes().stream()
                .filter(c -> c.nome().equals("Plástico")).findFirst().orElseThrow();
        assertEquals(20, plastico.quantidade());
        assertEquals(0.60, plastico.valorUnitario(), 0.001); // média ponderada de 0,70 e 0,50
        assertEquals(12.00, plastico.valorTotal(), 0.001);

        for (String nome : List.of("Pigmento", "Solvente", "Clip", "Vedação")) {
            assertEquals(10, n.componentes().stream().filter(c -> c.nome().equals(nome))
                    .findFirst().orElseThrow().quantidade());
        }
    }

    @Test
    void canetaAtualizarPigmentoEImplodir() {
        List<ItemFormula> itens = caneta();
        TreeNode<ItemFormula> raiz = ArvoreGenerica.buildTree(itens);

        assertTrue(ArvoreGenerica.atualizarValor(raiz, 3, 0.50)); // id 3 = Pigmento
        assertEquals(2.70, ArvoreGenerica.calcularTotal(raiz), 0.001);

        List<AlteracaoValor> alteracoes = ArvoreGenerica.implosao(raiz);
        assertEquals(2, alteracoes.size()); // Tinta e Caneta
        assertEquals("Tinta", alteracoes.get(0).nome());
        assertEquals(1.00, alteracoes.get(0).valorNovo(), 0.001);
        assertEquals("Caneta", alteracoes.get(1).nome());
        assertEquals(2.70, raiz.getData().getValor(), 0.001);
    }

    @Test
    void atualizarItemInexistenteRetornaFalse() {
        assertFalse(ArvoreGenerica.atualizarValor(ArvoreGenerica.buildTree(caneta()), 999, 1.0));
    }

    @Test
    void atualizarPorIdNaoMexeNoOutroPlastico() {
        TreeNode<ItemFormula> raiz = ArvoreGenerica.buildTree(caneta());
        ArvoreGenerica.atualizarValor(raiz, 6, 1.00); // Plástico do Corpo
        NoExplosao e = ArvoreGenerica.explodir(raiz);
        assertEquals(1.00, achar(achar(e, "Corpo"), "Plástico").valorUnitario(), 0.001);
        assertEquals(0.50, achar(achar(e, "Tampa"), "Plástico").valorUnitario(), 0.001);
    }

    // ---------------- Teste 2: computador ----------------

    @Test
    void computadorTotal() {
        assertEquals(3380.00, ArvoreGenerica.calcularTotal(ArvoreGenerica.buildTree(computador())), 0.001);
    }

    @Test
    void computadorImplosaoCorrigeValoresInconsistentes() {
        TreeNode<ItemFormula> raiz = ArvoreGenerica.buildTree(computador());
        List<AlteracaoValor> alteracoes = ArvoreGenerica.implosao(raiz);
        NoExplosao e = ArvoreGenerica.explodir(raiz);

        assertEquals(1550.00, achar(e, "Placa-Mãe").valorUnitario(), 0.001);
        assertEquals(650.00, achar(e, "Armazenamento").valorUnitario(), 0.001);
        assertEquals(2500.00, achar(e, "Gabinete").valorUnitario(), 0.001);
        assertEquals(700.00, achar(e, "Monitor").valorUnitario(), 0.001);
        assertEquals(180.00, achar(e, "Periféricos").valorUnitario(), 0.001);
        assertEquals(3380.00, achar(e, "Computador").valorUnitario(), 0.001);
        assertEquals(6, alteracoes.size());

        // idempotente: rodar de novo não altera nada
        assertTrue(ArvoreGenerica.implosao(raiz).isEmpty());
    }

    @Test
    void computadorMemoriaRamUsaQuantidadeDoPai() {
        Necessidade n = ArvoreGenerica.calcularNecessidade(ArvoreGenerica.buildTree(computador()), 5);
        assertEquals(10, n.componentes().stream().filter(c -> c.nome().equals("Memória RAM"))
                .findFirst().orElseThrow().quantidade());
        assertEquals(5 * 3380.00, n.valorTotal(), 0.001);
    }

    // ---------------- Validações ----------------

    @Test
    void rejeitaFormulaSemRaiz() {
        List<ItemFormula> itens = List.of(new ItemFormula("A", "B", 1, 1), new ItemFormula("B", "A", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ArvoreGenerica.buildTree(itens));
    }

    @Test
    void rejeitaDuasRaizes() {
        List<ItemFormula> itens = List.of(new ItemFormula("A", null, 1, 1), new ItemFormula("B", null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ArvoreGenerica.buildTree(itens));
    }

    @Test
    void rejeitaPaiInexistente() {
        List<ItemFormula> itens = List.of(new ItemFormula("A", null, 1, 1), new ItemFormula("B", "X", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ArvoreGenerica.buildTree(itens));
    }

    @Test
    void rejeitaPaiComNomeRepetido() {
        List<ItemFormula> itens = List.of(
                new ItemFormula("A", null, 1, 1),
                new ItemFormula("B", "A", 1, 1),
                new ItemFormula("B", "A", 1, 1),
                new ItemFormula("C", "B", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> ArvoreGenerica.buildTree(itens));
    }

    @Test
    void rejeitaCicloDesconectadoDaRaiz() {
        List<ItemFormula> itens = new ArrayList<>(List.of(
                new ItemFormula("A", null, 1, 1),
                new ItemFormula("B", "C", 1, 1),
                new ItemFormula("C", "B", 1, 1)));
        assertThrows(IllegalArgumentException.class, () -> ArvoreGenerica.buildTree(itens));
    }

    @Test
    void rejeitaQuantidadeEValorInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> ArvoreGenerica.buildTree(List.of(new ItemFormula("A", null, 0, 1))));
        assertThrows(IllegalArgumentException.class,
                () -> ArvoreGenerica.buildTree(List.of(new ItemFormula("A", null, 1, -1))));
        assertThrows(IllegalArgumentException.class,
                () -> ArvoreGenerica.calcularNecessidade(ArvoreGenerica.buildTree(caneta()), 0));
    }
}

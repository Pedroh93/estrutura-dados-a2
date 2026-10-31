package com.a2.backend.arvore;

import static com.a2.backend.arvore.ArvoreGenerica.buildTree;
import static com.a2.backend.arvore.ArvoreGenerica.calcularTotal;
import static com.a2.backend.arvore.ArvoreGenerica.printTree;

import java.util.Arrays;
import java.util.List;

/** TESTE 1 do enunciado. */
public class TesteCaneta {

    public static void main(String[] args) {
        // Lista de itens da fórmula da caneta
        List<ItemFormula> itens = Arrays.asList(
                new ItemFormula("Caneta", null, 1, 2.50),
                new ItemFormula("Tinta", "Caneta", 1, 0.80),
                new ItemFormula("Pigmento", "Tinta", 1, 0.30),
                new ItemFormula("Solvente", "Tinta", 1, 0.50),
                new ItemFormula("Corpo", "Caneta", 1, 1.00),
                new ItemFormula("Plástico", "Corpo", 1, 0.70),
                new ItemFormula("Clip", "Corpo", 1, 0.30),
                new ItemFormula("Tampa", "Caneta", 1, 0.70),
                new ItemFormula("Plástico", "Tampa", 1, 0.50),
                new ItemFormula("Vedação", "Tampa", 1, 0.20));

        TreeNode<ItemFormula> raiz = buildTree(itens);

        System.out.println("Árvore da fórmula da caneta:");
        printTree(raiz, 0);

        double total = calcularTotal(raiz);
        System.out.printf("%nValor total da caneta: R$ %.2f%n", total);
    }
}

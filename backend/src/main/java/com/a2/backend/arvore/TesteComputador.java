package com.a2.backend.arvore;

import static com.a2.backend.arvore.ArvoreGenerica.buildTree;
import static com.a2.backend.arvore.ArvoreGenerica.calcularTotal;
import static com.a2.backend.arvore.ArvoreGenerica.printTree;

import java.util.Arrays;
import java.util.List;

/** TESTE 2 do enunciado. */
public class TesteComputador {

    public static void main(String[] args) {
        // Lista de itens da fórmula do computador
        List<ItemFormula> itensPC = Arrays.asList(
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
                new ItemFormula("Mouse", "Periféricos", 1, 80.00));

        TreeNode<ItemFormula> raiz = buildTree(itensPC);

        System.out.println("Árvore da fórmula do computador:");
        printTree(raiz, 0);

        double total = calcularTotal(raiz);
        System.out.printf("%nValor total do computador: R$ %.2f%n", total);
    }
}

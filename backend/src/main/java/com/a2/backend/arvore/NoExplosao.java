package com.a2.backend.arvore;

import java.util.List;

/** Nó da explosão (formato do contrato da API). valorTotal = valorUnitario × quantidade. */
public record NoExplosao(
        Integer id,
        String nome,
        int quantidade,
        double valorUnitario,
        double valorTotal,
        List<NoExplosao> filhos) {
}

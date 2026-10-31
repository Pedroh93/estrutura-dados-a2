package com.a2.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.a2.backend.arvore.AlteracaoValor;
import com.a2.backend.arvore.ArvoreGenerica;
import com.a2.backend.arvore.ItemFormula;
import com.a2.backend.arvore.Necessidade;
import com.a2.backend.arvore.NoExplosao;
import com.a2.backend.arvore.TreeNode;
import com.a2.backend.dto.ItemFormulaRequest;
import com.a2.backend.dto.NovoProdutoRequest;
import com.a2.backend.dto.ProdutoResumo;
import com.a2.backend.dto.ResultadoImplosao;
import com.a2.backend.entity.ItemFormulaEntity;
import com.a2.backend.entity.Produto;
import com.a2.backend.exception.RecursoNaoEncontradoException;
import com.a2.backend.repository.ItemFormulaRepository;
import com.a2.backend.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ItemFormulaRepository itemRepository;

    public ProdutoService(ProdutoRepository produtoRepository, ItemFormulaRepository itemRepository) {
        this.produtoRepository = produtoRepository;
        this.itemRepository = itemRepository;
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ProdutoResumo> listar() {
        List<ProdutoResumo> resumos = new ArrayList<>();
        for (Produto produto : produtoRepository.findAll()) {
            resumos.add(resumir(produto));
        }
        resumos.sort((a, b) -> Integer.compare(a.id(), b.id()));
        return resumos;
    }

    @Transactional
    public ProdutoResumo criar(NovoProdutoRequest request) {
        if (request == null || request.nome() == null || request.nome().isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto");
        }
        if (request.itens() == null || request.itens().isEmpty()) {
            throw new IllegalArgumentException("A fórmula não possui itens");
        }

        // Valida e monta a árvore ANTES de gravar qualquer coisa
        TreeNode<ItemFormula> raiz = ArvoreGenerica.buildTree(converter(request.itens()));

        Produto produto = new Produto();
        produto.setNome(request.nome().trim());
        produto = produtoRepository.save(produto);

        // Pré-ordem garante que o pai é gravado antes dos filhos
        Map<TreeNode<ItemFormula>, ItemFormulaEntity> gravados = new IdentityHashMap<>();
        for (TreeNode<ItemFormula> no : raiz.preOrder()) {
            ItemFormula item = no.getData();
            ItemFormulaEntity entity = new ItemFormulaEntity();
            entity.setProduto(produto);
            entity.setPai(no.getParent() == null ? null : gravados.get(no.getParent()));
            entity.setNome(item.getNome());
            entity.setQuantidade(item.getQuantidade());
            entity.setValor(dinheiro(item.getValor()));
            gravados.put(no, itemRepository.save(entity));
        }

        return new ProdutoResumo(produto.getId(), produto.getNome(), gravados.size(), raiz.getData().getValor());
    }

    @Transactional
    public void excluir(Integer produtoId) {
        produtoRepository.delete(buscarProduto(produtoId)); // itens saem por ON DELETE CASCADE
    }

    // ------------------------------------------------------------------
    // Explosão, necessidade, atualização e implosão
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public NoExplosao explosao(Integer produtoId) {
        buscarProduto(produtoId);
        return ArvoreGenerica.explodir(montarArvore(itemRepository.buscarDoProduto(produtoId)));
    }

    @Transactional(readOnly = true)
    public Necessidade necessidade(Integer produtoId, int quantidade) {
        buscarProduto(produtoId);
        return ArvoreGenerica.calcularNecessidade(montarArvore(itemRepository.buscarDoProduto(produtoId)), quantidade);
    }

    @Transactional
    public NoExplosao atualizarValor(Integer produtoId, Integer itemId, Double valor) {
        if (valor == null || valor < 0 || valor.isNaN()) {
            throw new IllegalArgumentException("O valor deve ser maior ou igual a zero");
        }
        buscarProduto(produtoId);

        List<ItemFormulaEntity> itens = itemRepository.buscarDoProduto(produtoId);
        ItemFormulaEntity alvo = itens.stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item não encontrado"));

        alvo.setValor(dinheiro(valor)); // gravado ao fim da transação (dirty checking)
        return ArvoreGenerica.explodir(montarArvore(itens));
    }

    @Transactional
    public ResultadoImplosao implodir(Integer produtoId) {
        buscarProduto(produtoId);

        List<ItemFormulaEntity> itens = itemRepository.buscarDoProduto(produtoId);
        TreeNode<ItemFormula> raiz = montarArvore(itens);
        List<AlteracaoValor> alteracoes = ArvoreGenerica.implosao(raiz);

        for (AlteracaoValor alteracao : alteracoes) {
            itens.stream().filter(i -> i.getId().equals(alteracao.id())).findFirst()
                    .ifPresent(i -> i.setValor(dinheiro(alteracao.valorNovo())));
        }
        return new ResultadoImplosao(ArvoreGenerica.explodir(raiz), alteracoes);
    }

    /** Custo calculado direto de uma lista de itens, sem gravar (endpoint auxiliar). */
    public double calcularCusto(List<ItemFormula> itens) {
        return ArvoreGenerica.calcularTotal(ArvoreGenerica.buildTree(itens));
    }

    // ------------------------------------------------------------------
    // Auxiliares
    // ------------------------------------------------------------------

    private Produto buscarProduto(Integer produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    private ProdutoResumo resumir(Produto produto) {
        double valorTotal = itemRepository.findByProdutoIdAndPaiIsNull(produto.getId())
                .map(raiz -> raiz.getValor().doubleValue()).orElse(0.0);
        return new ProdutoResumo(produto.getId(), produto.getNome(),
                itemRepository.countByProdutoId(produto.getId()), valorTotal);
    }

    private TreeNode<ItemFormula> montarArvore(List<ItemFormulaEntity> entities) {
        List<ItemFormula> itens = new ArrayList<>();
        for (ItemFormulaEntity e : entities) {
            itens.add(new ItemFormula(e.getId(), e.getNome(), e.getPai() == null ? null : e.getPai().getNome(),
                    e.getQuantidade(), e.getValor().doubleValue()));
        }
        return ArvoreGenerica.buildTree(itens);
    }

    private List<ItemFormula> converter(List<ItemFormulaRequest> requests) {
        List<ItemFormula> itens = new ArrayList<>();
        for (ItemFormulaRequest r : requests) {
            if (r == null || r.nome() == null || r.nome().isBlank()) {
                throw new IllegalArgumentException("Todos os itens precisam de um nome");
            }
            String nome = r.nome().trim();
            if (r.quantidade() == null || r.quantidade() <= 0) {
                throw new IllegalArgumentException("Quantidade inválida em \"" + nome + "\"");
            }
            if (r.valor() == null || r.valor() < 0 || r.valor().isNaN()) {
                throw new IllegalArgumentException("Valor inválido em \"" + nome + "\"");
            }
            String pai = (r.pai() == null || r.pai().isBlank()) ? null : r.pai().trim();
            itens.add(new ItemFormula(nome, pai, r.quantidade(), r.valor()));
        }
        return itens;
    }

    private static BigDecimal dinheiro(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
    }
}

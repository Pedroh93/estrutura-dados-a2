package com.a2.backend.arvore;

/**
 * Linha da fórmula de um produto: um item, o nome do item pai (null na raiz),
 * a quantidade necessária POR UNIDADE DO PAI e o valor unitário.
 * O id é opcional: só existe depois que o item é gravado no banco.
 */
public class ItemFormula {

    private Integer id;
    private String nome;
    private String pai;
    private int quantidade;
    private double valor;

    public ItemFormula() {
    }

    public ItemFormula(String nome, String pai, int quantidade, double valor) {
        this.nome = nome;
        this.pai = pai;
        this.quantidade = quantidade;
        this.valor = valor;
    }

    public ItemFormula(Integer id, String nome, String pai, int quantidade, double valor) {
        this(nome, pai, quantidade, valor);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPai() { return pai; }
    public void setPai(String pai) { this.pai = pai; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}

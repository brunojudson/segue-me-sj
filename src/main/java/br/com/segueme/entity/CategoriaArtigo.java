package br.com.segueme.entity;

/**
 * Enumeração das categorias de artigos disponíveis para venda
 */
public enum CategoriaArtigo {
    
    ACESSORIOS("Acessórios"),
    ALIMENTACAO("Alimentação"),
    ARTESANATO("Artesanato"),
    ARTIGO_RELIGIOSO("Artigo Religioso"),
    BEBIDAS("Bebidas"),
    CAMISETAS("Camisetas"),
    HIGIENE_PESSOAL("Higiene Pessoal"),
    LEBRANCINHAS("Lembrancinhas"),
    LIVROS_MIDIAS("Livros e Mídias"),
    VESTUARIO("Vestuário"),
    OUTROS("Outros");

    private final String descricao;

    CategoriaArtigo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

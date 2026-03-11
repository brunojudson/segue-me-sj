package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.VendaArtigo;

/**
 * Repository para operações de persistência da entidade VendaArtigo
 */
public interface VendaArtigoRepository {
    
    /**
     * Salva um artigo no banco de dados
     * @param artigo Artigo a ser salvo
     * @return Artigo salvo com ID gerado
     */
    VendaArtigo save(VendaArtigo artigo);
    
    /**
     * Atualiza um artigo existente
     * @param artigo Artigo a ser atualizado
     * @return Artigo atualizado
     */
    VendaArtigo update(VendaArtigo artigo);
    
    /**
     * Busca um artigo pelo ID
     * @param id ID do artigo
     * @return Optional contendo o artigo, se encontrado
     */
    Optional<VendaArtigo> findById(Long id);
    
    /**
     * Busca um artigo pelo código
     * @param codigo Código único do artigo
     * @return Optional contendo o artigo, se encontrado
     */
    Optional<VendaArtigo> findByCodigo(String codigo);
    
    /**
     * Busca todos os artigos
     * @return Lista de todos os artigos
     */
    List<VendaArtigo> findAll();
    
    /**
     * Busca artigos ativos
     * @return Lista de artigos ativos
     */
    List<VendaArtigo> findAtivos();
    
    /**
     * Busca artigos por categoria
     * @param categoria Categoria do artigo
     * @return Lista de artigos da categoria
     */
    List<VendaArtigo> findByCategoria(String categoria);
    
    /**
     * Busca artigos com estoque baixo (abaixo do mínimo)
     * @return Lista de artigos com estoque abaixo do mínimo
     */
    List<VendaArtigo> findComEstoqueBaixo();
    
    /**
     * Busca artigos por nome (usando LIKE)
     * @param nome Nome ou parte do nome do artigo
     * @return Lista de artigos que correspondem ao critério
     */
    List<VendaArtigo> findByNome(String nome);
    
    /**
     * Remove um artigo
     * @param id ID do artigo a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    boolean delete(Long id);
    
    /**
     * Retorna todas as categorias distintas de artigos
     * @return Lista de categorias
     */
    List<String> findAllCategorias();

    /**
     * Retorna o maior sufixo numérico já usado para códigos que começam com o prefixo informado.
     * Ex.: para prefixo 'CAT' retorna 12 se já existirem CAT001..CAT012.
     * Retorna null se não houver nenhum registro.
     */
    Integer findMaxSuffixForPrefix(String prefix);
}

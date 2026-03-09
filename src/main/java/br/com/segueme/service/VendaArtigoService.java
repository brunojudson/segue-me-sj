package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.VendaArtigo;
import br.com.segueme.repository.VendaArtigoRepository;

/**
 * Service para gerenciar artigos de venda
 */
@ApplicationScoped
public class VendaArtigoService implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Inject
    private VendaArtigoRepository artigoRepository;
    
    /**
     * Salva um novo artigo
     */
    public VendaArtigo salvar(VendaArtigo artigo) {
        // Validações
        if (artigo == null) {
            throw new IllegalArgumentException("Artigo não pode ser nulo");
        }
        
        if (artigo.getNome() == null || artigo.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do artigo é obrigatório");
        }
        
        if (artigo.getPrecoBase() == null || artigo.getPrecoBase().doubleValue() < 0) {
            throw new IllegalArgumentException("Preço base do artigo é obrigatório e não pode ser negativo");
        }
        
        // Verificar se código já existe
        if (artigo.getCodigo() != null && !artigo.getCodigo().trim().isEmpty()) {
            Optional<VendaArtigo> existente = artigoRepository.findByCodigo(artigo.getCodigo());
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Já existe um artigo com o código: " + artigo.getCodigo());
            }
        }
        
        return artigoRepository.save(artigo);
    }
    
    /**
     * Atualiza um artigo existente
     */
    public VendaArtigo atualizar(VendaArtigo artigo) {
        if (artigo == null || artigo.getId() == null) {
            throw new IllegalArgumentException("Artigo e ID são obrigatórios para atualização");
        }
        
        // Verificar se artigo existe
        Optional<VendaArtigo> existente = artigoRepository.findById(artigo.getId());
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com o ID: " + artigo.getId());
        }
        
        // Verificar se código não está duplicado
        if (artigo.getCodigo() != null && !artigo.getCodigo().trim().isEmpty()) {
            Optional<VendaArtigo> porCodigo = artigoRepository.findByCodigo(artigo.getCodigo());
            if (porCodigo.isPresent() && !porCodigo.get().getId().equals(artigo.getId())) {
                throw new IllegalArgumentException("Já existe outro artigo com o código: " + artigo.getCodigo());
            }
        }
        
        return artigoRepository.update(artigo);
    }
    
    /**
     * Busca um artigo pelo ID
     */
    public Optional<VendaArtigo> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do artigo não pode ser nulo");
        }
        return artigoRepository.findById(id);
    }
    
    /**
     * Busca todos os artigos
     */
    public List<VendaArtigo> buscarTodos() {
        return artigoRepository.findAll();
    }
    
    /**
     * Busca apenas artigos ativos
     */
    public List<VendaArtigo> buscarAtivos() {
        return artigoRepository.findAtivos();
    }
    
    /**
     * Busca artigos por categoria
     */
    public List<VendaArtigo> buscarPorCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("Categoria não pode ser vazia");
        }
        return artigoRepository.findByCategoria(categoria);
    }
    
    /**
     * Busca artigos com estoque abaixo do mínimo
     */
    public List<VendaArtigo> buscarComEstoqueBaixo() {
        return artigoRepository.findComEstoqueBaixo();
    }
    
    /**
     * Busca artigos por nome (parcial)
     */
    public List<VendaArtigo> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return buscarTodos();
        }
        return artigoRepository.findByNome(nome);
    }
    
    /**
     * Retorna todas as categorias disponíveis
     */
    public List<String> listarCategorias() {
        return artigoRepository.findAllCategorias();
    }
    
    /**
     * Ativa um artigo
     */
    public void ativar(Long artigoId) {
        Optional<VendaArtigo> opt = artigoRepository.findById(artigoId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com ID: " + artigoId);
        }
        
        VendaArtigo artigo = opt.get();
        artigo.setAtivo(true);
        artigoRepository.update(artigo);
    }
    
    /**
     * Inativa um artigo
     */
    public void inativar(Long artigoId) {
        Optional<VendaArtigo> opt = artigoRepository.findById(artigoId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com ID: " + artigoId);
        }
        
        VendaArtigo artigo = opt.get();
        artigo.setAtivo(false);
        artigoRepository.update(artigo);
    }
    
    /**
     * Ajusta o estoque de um artigo
     */
    public void ajustarEstoque(Long artigoId, Integer novoEstoque) {
        if (novoEstoque == null || novoEstoque < 0) {
            throw new IllegalArgumentException("Estoque não pode ser negativo");
        }
        
        Optional<VendaArtigo> opt = artigoRepository.findById(artigoId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com ID: " + artigoId);
        }
        
        VendaArtigo artigo = opt.get();
        artigo.setEstoqueAtual(novoEstoque);
        artigoRepository.update(artigo);
    }
    
    /**
     * Remove um artigo
     */
    public void remover(Long artigoId) {
        if (artigoId == null) {
            throw new IllegalArgumentException("ID do artigo não pode ser nulo");
        }
        
        Optional<VendaArtigo> opt = artigoRepository.findById(artigoId);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Artigo não encontrado com ID: " + artigoId);
        }
        
        // Verificar se o artigo pode ser removido (não tem vendas associadas)
        // Essa verificação será feita pelo banco devido às constraints de FK
        
        artigoRepository.delete(artigoId);
    }
}

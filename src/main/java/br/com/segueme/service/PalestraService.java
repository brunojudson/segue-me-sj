package br.com.segueme.service;

import br.com.segueme.entity.Palestra;
import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.PalestraRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class PalestraService {

    @Inject
    private PalestraRepository palestraRepository;

    public Palestra buscarPorId(Long id) {
        return palestraRepository.findById(id);
    }

    public List<Palestra> buscarTodos() {
        return palestraRepository.findAll();
    }

    public List<Palestra> buscarPorEncontro(Encontro encontro) {
        return palestraRepository.findByEncontro(encontro);
    }

    @Transactional
    public void salvar(Palestra palestra) {
        // Adicionar validações de negócio se necessário
        palestraRepository.save(palestra);
    }

    @Transactional
    public void atualizar(Palestra palestra) {
        // Adicionar validações de negócio se necessário
        palestraRepository.update(palestra);
    }

    @Transactional
    public void excluir(Long id) {
        Palestra palestra = buscarPorId(id);
        if (palestra == null) {
            throw new IllegalArgumentException("Palestra não encontrada.");
        }
        
        // Verificar se a palestra está associada a um encontro finalizado
        if (palestra.getEncontro() != null && palestra.getEncontro().getAtivo() == false) {
            throw new IllegalArgumentException("Não é possível excluir a palestra pois está vinculada ao encontro '" + 
                                       palestra.getEncontro().getNome() + "', que já foi finalizado.");
        }
        
        palestraRepository.delete(id);
    }
}


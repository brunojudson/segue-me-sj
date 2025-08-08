package br.com.segueme.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Palestrante;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.repository.PalestranteRepository;

@ApplicationScoped
public class PalestranteService {

    @Inject
    private PalestranteRepository palestranteRepository;

    public Palestrante buscarPorId(Long id) {
        return palestranteRepository.findById(id);
    }

    public List<Palestrante> buscarTodos() {
        return palestranteRepository.findAll();
    }

    public List<Palestrante> buscarPorPessoa(Pessoa pessoa) {
        return palestranteRepository.findByPessoa(pessoa);
    }

    public List<Palestrante> buscarPorCasal(Casal casal) {
        return palestranteRepository.findByCasal(casal);
    }

    @Transactional
    public void salvar(Palestrante palestrante) {
        // Adicionar validações de negócio
        validarPalestrante(palestrante, true);
        palestranteRepository.save(palestrante);
    }

    @Transactional
    public void atualizar(Palestrante palestrante) {
        // Adicionar validações de negócio
        validarPalestrante(palestrante, false);
        palestranteRepository.update(palestrante);
    }

    @Transactional
    public void excluir(Long id) {
        Palestrante palestrante = palestranteRepository.findById(id);
        if (palestrante == null) {
            throw new IllegalArgumentException("Palestrante não encontrado.");
        }
        
        // Verificar se o palestrante está associado a alguma palestra
        if (palestrante.getPalestras() != null && !palestrante.getPalestras().isEmpty()) {
            throw new IllegalArgumentException("Não é possível excluir o palestrante pois está associado a " + 
                                        palestrante.getPalestras().size() + " palestra(s).");
        }
        
        palestranteRepository.delete(id);
    }

    private void validarPalestrante(Palestrante palestrante, boolean isNew) {
        if (palestrante.getTipoPalestrante() == null) {
            throw new IllegalArgumentException("Tipo do palestrante não pode ser nulo.");
        }

        switch (palestrante.getTipoPalestrante()) {
            case INDIVIDUAL:
                if (palestrante.getPessoaIndividual() == null || palestrante.getPessoaIndividual().getId() == null) {
                    throw new IllegalArgumentException("Palestrante individual deve ter uma pessoa com ID válido associada.");
                }
                // Validação simplificada: apenas para novos palestrantes, verifica se o pessoa_individual_id já existe
                if (isNew) {
                    if (palestranteRepository.existsByPessoaIndividualId(palestrante.getPessoaIndividual().getId())) {
                        throw new IllegalArgumentException("Esta pessoa (ID: " + palestrante.getPessoaIndividual().getId() + ") já está cadastrada como palestrante.");
                    }
                }
                palestrante.setCasal(null);
                if (palestrante.getMembrosGrupo() != null) {
                    palestrante.getMembrosGrupo().clear();
                }
                break;
            case CASAL:
                if (palestrante.getCasal() == null || palestrante.getCasal().getId() == null) {
                    throw new IllegalArgumentException("Palestrante do tipo casal deve ter um casal com ID válido associado.");
                }
                if (isNew) {
                    if (palestranteRepository.existsByCasalId(palestrante.getCasal().getId())) {
                        throw new IllegalArgumentException("Este casal (ID: " + palestrante.getCasal().getId() + ") já está cadastrado como palestrante.");
                    }
                }
                palestrante.setPessoaIndividual(null);
                if (palestrante.getMembrosGrupo() != null) {
                    palestrante.getMembrosGrupo().clear();
                }
                break;
            /*
             * case GRUPO: 
             *   Set<Pessoa> membros = palestrante.getMembrosGrupo(); 
             *   if (membros == null || membros.size() < 2) { 
             *     throw new IllegalArgumentException("Palestrante do tipo grupo deve ter pelo menos dois membros.");
             *   } 
             *   palestrante.setPessoaIndividual(null); 
             *   palestrante.setCasal(null);
             *   break;
             */
        }
    }
}


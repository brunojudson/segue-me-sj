package br.com.segueme.repository;

import br.com.segueme.entity.Palestrante;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Casal;
import java.util.List;

public interface PalestranteRepository {

    Palestrante findById(Long id);

    List<Palestrante> findAll();

    List<Palestrante> findByPessoa(Pessoa pessoa);

    List<Palestrante> findByCasal(Casal casal);

    void save(Palestrante palestrante);

    void update(Palestrante palestrante);

    void delete(Long id);
    
    boolean hasAssociations(Long id);
    
    boolean existsByCasalId(Long casalId);
    
    boolean existsByPessoaIndividualId(Long pessoaIndividualId);
}


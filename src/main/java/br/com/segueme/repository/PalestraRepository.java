package br.com.segueme.repository;

import br.com.segueme.entity.Palestra;
import br.com.segueme.entity.Encontro;
import java.util.List;

public interface PalestraRepository {

    Palestra findById(Long id);

    List<Palestra> findAll();

    List<Palestra> findByEncontro(Encontro encontro);

    void save(Palestra palestra);

    void update(Palestra palestra);

    void delete(Long id);
    
    boolean hasAssociations(Long id);
}


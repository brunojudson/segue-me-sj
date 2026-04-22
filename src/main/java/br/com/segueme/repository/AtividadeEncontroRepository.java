package br.com.segueme.repository;

import br.com.segueme.entity.AtividadeEncontro;
import br.com.segueme.entity.Encontro;

import java.util.List;

public interface AtividadeEncontroRepository {

    AtividadeEncontro findById(Long id);

    List<AtividadeEncontro> findByEncontro(Encontro encontro);

    void save(AtividadeEncontro atividade);

    void update(AtividadeEncontro atividade);

    void delete(Long id);
}

package br.com.segueme.repository;

import java.util.List;
import java.util.Optional;

import br.com.segueme.entity.Transferencia;
import br.com.segueme.enums.StatusTransferencia;

public interface TransferenciaRepository {

    Transferencia save(Transferencia transferencia);

    Transferencia update(Transferencia transferencia);

    Optional<Transferencia> findById(Long id);

    List<Transferencia> findAll();

    List<Transferencia> findByPessoa(Long pessoaId);

    List<Transferencia> findByTipo(StatusTransferencia tipo);
}

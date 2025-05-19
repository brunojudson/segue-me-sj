package br.com.segueme.repository;

import java.util.Optional;

import br.com.segueme.entity.Permissao;

public interface PermissaoRepository {

    Optional<Permissao> findByNome(String nome);
}

package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Casal;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.repository.CasalRepository;
import br.com.segueme.repository.PessoaRepository;

@ApplicationScoped
public class CasalService implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private CasalRepository casalRepository;

    @Inject
    private PessoaRepository pessoaRepository;

    /**
     * Salva um novo casal
     * 
     * @param casal Objeto casal a ser salvo
     * @return Casal salvo com ID gerado
     */
    public Casal salvar(Casal casal) {
        // Validações
        if (casal == null) {
            throw new IllegalArgumentException("Casal não pode ser nulo");
        }

        if (casal.getPessoa1() == null || casal.getPessoa1().getId() == null) {
            throw new IllegalArgumentException("Esposo pessoa do casal é obrigatória");
        }

        if (casal.getPessoa2() == null || casal.getPessoa2().getId() == null) {
            throw new IllegalArgumentException("Esposa pessoa do casal é obrigatória");
        }

        // Verificar se as pessoas são diferentes
        if (casal.getPessoa1().getId().equals(casal.getPessoa2().getId())) {
            throw new IllegalArgumentException("As duas pessoas do casal devem ser diferentes");
        }

        // Verificar se as pessoas existem
        Optional<Pessoa> pessoa1Existente = pessoaRepository.findById(casal.getPessoa1().getId());
        if (!pessoa1Existente.isPresent()) {
            throw new IllegalArgumentException("Esposo não encontrado com o ID: " + casal.getPessoa1().getId());
        }

        Optional<Pessoa> pessoa2Existente = pessoaRepository.findById(casal.getPessoa2().getId());
        if (!pessoa2Existente.isPresent()) {
            throw new IllegalArgumentException("Esposa não encontrada com o ID: " + casal.getPessoa2().getId());
        }

        // Verificar se já existe um casal com estas pessoas
        Optional<Casal> casalExistente = casalRepository.findByPessoas(
                casal.getPessoa1().getId(), casal.getPessoa2().getId());
        if (casalExistente.isPresent()) {
            throw new IllegalArgumentException("Já existe um casal cadastrado com estas pessoas");
        }
        // Verificar se alguma das pessoas já está em outro casal
        List<Casal> casaisPessoa1 = casalRepository.findByPessoa(casal.getPessoa1().getId());
        if (!casaisPessoa1.isEmpty()) {
            throw new IllegalArgumentException("Esposo já está cadastrado em outro casal.");
        }
        List<Casal> casaisPessoa2 = casalRepository.findByPessoa(casal.getPessoa2().getId());
        if (!casaisPessoa2.isEmpty()) {
            throw new IllegalArgumentException("Esposa já está cadastrada em outro casal.");
        }

        // Salvar casal
        return casalRepository.save(casal);
    }

    /**
     * Atualiza um casal existente
     * 
     * @param casal Objeto casal a ser atualizado
     * @return Casal atualizado
     */
    public Casal atualizar(Casal casal) {
        // Validações
        if (casal == null) {
            throw new IllegalArgumentException("Casal não pode ser nulo");
        }

        if (casal.getId() == null) {
            throw new IllegalArgumentException("ID do casal é obrigatório para atualização");
        }

        // Verificar se casal existe
        Optional<Casal> casalExistente = casalRepository.findById(casal.getId());
        if (!casalExistente.isPresent()) {
            throw new IllegalArgumentException("Casal não encontrado com o ID: " + casal.getId());
        }

        // Verificar se alguma das pessoas já está em outro casal (exceto o próprio
        // casal que está sendo atualizado)
        List<Casal> casaisPessoa1 = casalRepository.findByPessoa(casal.getPessoa1().getId());
        for (Casal c : casaisPessoa1) {
            if (!c.getId().equals(casal.getId())) {
                throw new IllegalArgumentException("Esposo já está cadastrado em outro casal.");
            }
        }
        List<Casal> casaisPessoa2 = casalRepository.findByPessoa(casal.getPessoa2().getId());
        for (Casal c : casaisPessoa2) {
            if (!c.getId().equals(casal.getId())) {
                throw new IllegalArgumentException("Esposa já está cadastrada em outro casal.");
            }
        }

        // Atualizar casal
        return casalRepository.update(casal);
    }

    /**
     * Busca um casal pelo ID
     * 
     * @param id ID do casal
     * @return Optional contendo o casal, se encontrado
     */
    public Optional<Casal> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do casal não pode ser nulo");
        }

        return casalRepository.findById(id);
    }

    /**
     * Busca todos os casais cadastrados
     * 
     * @return Lista de casais
     */
    public List<Casal> buscarTodos() {
        return casalRepository.findAll();
    }

    /**
     * Busca casais por pessoa (busca por qualquer um dos cônjuges)
     * 
     * @param pessoaId ID da pessoa
     * @return Lista de casais que contêm a pessoa
     */
    public List<Casal> buscarPorPessoa(Long pessoaId) {
        if (pessoaId == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }

        return casalRepository.findByPessoa(pessoaId);
    }

    /**
     * Busca casal por par de pessoas
     * 
     * @param pessoa1Id ID da primeira pessoa
     * @param pessoa2Id ID da segunda pessoa
     * @return Optional contendo o casal, se encontrado
     */
    public Optional<Casal> buscarPorPessoas(Long pessoa1Id, Long pessoa2Id) {
        if (pessoa1Id == null || pessoa2Id == null) {
            throw new IllegalArgumentException("IDs das pessoas são obrigatórios");
        }

        return casalRepository.findByPessoas(pessoa1Id, pessoa2Id);
    }

    /**
     * Remove um casal do banco de dados
     * 
     * @param id ID do casal a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do casal não pode ser nulo");
        }

        // Verificar se casal existe
        Optional<Casal> casalExistente = casalRepository.findById(id);
        if (!casalExistente.isPresent()) {
            throw new IllegalArgumentException("Casal não encontrado com o ID: " + id);
        }

        return casalRepository.delete(id);
    }

    public boolean verificarSePessoaEhCasal(Long pessoaId) {
        return !casalRepository.findByPessoa(pessoaId).isEmpty();
    }
}

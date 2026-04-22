package br.com.segueme.service;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.AtividadeEncontro;
import br.com.segueme.entity.Encontro;
import br.com.segueme.repository.AtividadeEncontroRepository;

@ApplicationScoped
public class AtividadeEncontroService {

    @Inject
    private AtividadeEncontroRepository repository;

    public AtividadeEncontro buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<AtividadeEncontro> buscarPorEncontro(Encontro encontro) {
        return repository.findByEncontro(encontro);
    }

    public void salvar(AtividadeEncontro atividade) {
        validar(atividade);
        repository.save(atividade);
    }

    public void atualizar(AtividadeEncontro atividade) {
        validar(atividade);
        repository.update(atividade);
    }

    public void excluir(Long id) {
        AtividadeEncontro atividade = repository.findById(id);
        if (atividade == null) {
            throw new IllegalArgumentException("Atividade não encontrada.");
        }
        repository.delete(id);
    }

    private void validar(AtividadeEncontro a) {
        if (a.getEncontro() != null
                && (a.getEncontro().getAtivo() == null || !a.getEncontro().getAtivo())) {
            throw new IllegalArgumentException("Não é possível cadastrar ou editar atividades em um encontro finalizado.");
        }
        if (a.getDataHoraFim() != null && a.getDataHoraInicio() != null
                && a.getDataHoraFim().isBefore(a.getDataHoraInicio())) {
            throw new IllegalArgumentException("O horário de término deve ser posterior ao horário de início.");
        }
    }
}

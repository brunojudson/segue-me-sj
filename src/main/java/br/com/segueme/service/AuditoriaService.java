package br.com.segueme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import br.com.segueme.entity.Auditoria;
import br.com.segueme.repository.AuditoriaRepository;

@ApplicationScoped
public class AuditoriaService {

    @Inject
    private AuditoriaRepository auditoriaRepository;

    @Transactional
    public void registrar(String entidade, Long entidadeId, String acao, String usuario, String detalhes) {
        Auditoria audit = new Auditoria();
        audit.setEntidade(entidade);
        audit.setEntidadeId(entidadeId);
        audit.setAcao(acao);
        audit.setUsuario(usuario);
        audit.setDataHora(java.time.LocalDateTime.now());
        audit.setDetalhes(detalhes);
        auditoriaRepository.salvar(audit);
    }
}
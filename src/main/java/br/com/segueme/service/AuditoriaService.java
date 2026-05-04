package br.com.segueme.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Auditavel;
import br.com.segueme.entity.Auditoria;
import br.com.segueme.repository.AuditoriaRepository;

@ApplicationScoped
public class AuditoriaService {

    @Inject
    private AuditoriaRepository auditoriaRepository;

    /**
     * Registra um evento de auditoria com detalhes em texto livre.
     */
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

    /**
     * Registra um evento de auditoria usando a interface {@link Auditavel}
     * para gerar os detalhes automaticamente.
     */
    public void registrar(String entidade, Long entidadeId, String acao, String usuario, Auditavel objeto) {
        String detalhes = objeto != null ? objeto.toAuditString() : "(sem dados)";
        registrar(entidade, entidadeId, acao, usuario, detalhes);
    }
}

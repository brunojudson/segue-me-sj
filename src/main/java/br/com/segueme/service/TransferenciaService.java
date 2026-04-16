package br.com.segueme.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Transferencia;
import br.com.segueme.enums.StatusTransferencia;
import br.com.segueme.repository.PessoaRepository;
import br.com.segueme.repository.TransferenciaRepository;

@ApplicationScoped
public class TransferenciaService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private TransferenciaRepository transferenciaRepository;

    @Inject
    private PessoaRepository pessoaRepository;

    /**
     * Registra a saída de um seguidor desta paróquia para outra.
     * O seguidor passa a status TRANSFERIDO_SAIDA e não poderá trabalhar em encontros.
     */
    public void registrarSaida(Pessoa pessoa, String paroquiaDestino, String observacoes, String registradoPor) {
        validarPessoa(pessoa);
        if (paroquiaDestino == null || paroquiaDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("A paróquia de destino é obrigatória para transferência de saída.");
        }
        if (pessoa.getStatusTransferencia() == StatusTransferencia.TRANSFERIDO_SAIDA) {
            throw new IllegalStateException("Este seguidor já está com transferência de saída registrada.");
        }

        Transferencia t = new Transferencia();
        t.setPessoa(pessoa);
        t.setParoquiaOrigem(resolverParoquiaOrigemAtual(pessoa));
        t.setParoquiaDestino(paroquiaDestino.trim());
        t.setTipoTransferencia(StatusTransferencia.TRANSFERIDO_SAIDA);
        t.setDataSolicitacao(LocalDate.now());
        t.setDataEfetivacao(LocalDate.now());
        t.setObservacoes(observacoes);
        t.setRegistradoPor(registradoPor);

        pessoa.setStatusTransferencia(StatusTransferencia.TRANSFERIDO_SAIDA);
        pessoa.setAtivo(false); // Pessoa inativa ao sair desta paróquia
        pessoaRepository.update(pessoa);
        transferenciaRepository.save(t);
    }

    /**
     * Registra o aceite de um seguidor vindo de outra paróquia.
     * O seguidor passa a status TRANSFERIDO_ENTRADA e pode trabalhar em encontros.
     */
    public void registrarEntrada(Pessoa pessoa, String paroquiaOrigem, String observacoes, String registradoPor) {
        validarPessoa(pessoa);
        if (paroquiaOrigem == null || paroquiaOrigem.trim().isEmpty()) {
            throw new IllegalArgumentException("A paróquia de origem é obrigatória para transferência de entrada.");
        }

        Transferencia t = new Transferencia();
        t.setPessoa(pessoa);
        t.setParoquiaOrigem(paroquiaOrigem.trim());
        t.setParoquiaDestino("Esta Paróquia");
        t.setTipoTransferencia(StatusTransferencia.TRANSFERIDO_ENTRADA);
        t.setDataSolicitacao(LocalDate.now());
        t.setDataEfetivacao(LocalDate.now());
        t.setObservacoes(observacoes);
        t.setRegistradoPor(registradoPor);

        pessoa.setStatusTransferencia(StatusTransferencia.TRANSFERIDO_ENTRADA);
        pessoa.setParoquiaOrigem(paroquiaOrigem.trim());
        pessoa.setAtivo(true); // Pessoa ativa ao entrar nesta paróquia
        pessoaRepository.update(pessoa);
        transferenciaRepository.save(t);
    }

    /**
     * Registra o retorno de um seguidor que havia saído para esta paróquia de origem.
     * O seguidor volta ao status RETORNADO e pode trabalhar em encontros novamente.
     */
    public void registrarRetorno(Pessoa pessoa, String observacoes, String registradoPor) {
        validarPessoa(pessoa);
        if (pessoa.getStatusTransferencia() != StatusTransferencia.TRANSFERIDO_SAIDA) {
            throw new IllegalStateException("Somente seguidores com saída registrada podem retornar.");
        }
        
        // Descobre a paróquia de onde o seguidor está retornando:
        // DEVE ser o destino (paroquiaDestino) do último registro de SAÍDA.
        // Usa ID como critério de desempate quando datas são iguais.
        List<Transferencia> historicoCompleto = transferenciaRepository.findByPessoa(pessoa.getId());
        
        String paroquiaDeRetorno = historicoCompleto.stream()
                .filter(t -> t.getTipoTransferencia() == StatusTransferencia.TRANSFERIDO_SAIDA)
                .filter(t -> t.getDataSolicitacao() != null)
                .filter(t -> t.getId() != null)
                .sorted(java.util.Comparator
                        .comparing(Transferencia::getDataSolicitacao).reversed()
                        .thenComparing(Transferencia::getId).reversed())
                .findFirst()
                .map(Transferencia::getParoquiaDestino)
                .orElseThrow(() -> new IllegalStateException(
                    "Não foi possível identificar a paróquia de retorno. Histórico de transferência inconsistente."));

        Transferencia t = new Transferencia();
        t.setPessoa(pessoa);
        t.setParoquiaOrigem(paroquiaDeRetorno);
        t.setParoquiaDestino("Esta Paróquia (retorno)");
        t.setTipoTransferencia(StatusTransferencia.RETORNADO);
        t.setDataSolicitacao(LocalDate.now());
        t.setDataEfetivacao(LocalDate.now());
        t.setObservacoes(observacoes);
        t.setRegistradoPor(registradoPor);

        pessoa.setStatusTransferencia(StatusTransferencia.RETORNADO);
        pessoa.setAtivo(true); // Pessoa ativa ao retornar para esta paróquia
        pessoaRepository.update(pessoa);
        transferenciaRepository.save(t);
    }

    public List<Transferencia> buscarPorPessoa(Long pessoaId) {
        return transferenciaRepository.findByPessoa(pessoaId);
    }

    // -------------------------------------------------------------------------

    private void validarPessoa(Pessoa pessoa) {
        if (pessoa == null || pessoa.getId() == null) {
            throw new IllegalArgumentException("Seguidor inválido para registrar transferência.");
        }
    }

    private String resolverParoquiaOrigemAtual(Pessoa pessoa) {
        // Se veio de outra paróquia, usa essa como origem; caso contrário "Esta Paróquia"
        if (pessoa.getParoquiaOrigem() != null && !pessoa.getParoquiaOrigem().trim().isEmpty()) {
            return pessoa.getParoquiaOrigem();
        }
        return "Esta Paróquia";
    }
}

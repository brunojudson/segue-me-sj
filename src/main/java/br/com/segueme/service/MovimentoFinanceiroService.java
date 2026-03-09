package br.com.segueme.service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.MovimentoFinanceiro;
import br.com.segueme.enums.TipoMovimento;
import br.com.segueme.repository.MovimentoFinanceiroRepository;

/**
 * Serviço para gestão financeira dos encontros.
 */
@ApplicationScoped
public class MovimentoFinanceiroService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private MovimentoFinanceiroRepository movimentoRepository;

    @Inject
    private AuditoriaService auditoriaService;

    @Inject
    private UsuarioService usuarioService;

    /**
     * Salva um novo movimento financeiro
     */
    public MovimentoFinanceiro salvar(MovimentoFinanceiro movimento) {
        validar(movimento);

        MovimentoFinanceiro salvo = movimentoRepository.save(movimento);

        auditoriaService.registrar("MovimentoFinanceiro", salvo.getId(), "INCLUÍDO",
                usuarioService.getUsuarioLogadoNome(),
                "Dados Salvo: " + salvo.toString());

        return salvo;
    }

    /**
     * Atualiza um movimento financeiro existente
     */
    public MovimentoFinanceiro atualizar(MovimentoFinanceiro movimento) {
        if (movimento == null || movimento.getId() == null) {
            throw new IllegalArgumentException("Movimento financeiro e ID são obrigatórios para atualização");
        }

        Optional<MovimentoFinanceiro> existente = movimentoRepository.findById(movimento.getId());
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("Movimento financeiro não encontrado com o ID: " + movimento.getId());
        }

        validar(movimento);

        auditoriaService.registrar("MovimentoFinanceiro", movimento.getId(), "ATUALIZADO",
                usuarioService.getUsuarioLogadoNome(),
                "Dados atualizados: " + movimento.toString());

        return movimentoRepository.update(movimento);
    }

    /**
     * Busca um movimento pelo ID
     */
    public Optional<MovimentoFinanceiro> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do movimento não pode ser nulo");
        }
        return movimentoRepository.findById(id);
    }

    /**
     * Busca todos os movimentos financeiros
     */
    public List<MovimentoFinanceiro> buscarTodos() {
        return movimentoRepository.findAll();
    }

    /**
     * Busca movimentos por encontro
     */
    public List<MovimentoFinanceiro> buscarPorEncontro(Long encontroId) {
        if (encontroId == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }
        return movimentoRepository.findByEncontro(encontroId);
    }

    /**
     * Busca movimentos por tipo e encontro
     */
    public List<MovimentoFinanceiro> buscarPorTipoEEncontro(TipoMovimento tipo, Long encontroId) {
        if (tipo == null || encontroId == null) {
            throw new IllegalArgumentException("Tipo e ID do encontro são obrigatórios");
        }
        return movimentoRepository.findByTipoAndEncontro(tipo, encontroId);
    }

    /**
     * Remove um movimento financeiro
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do movimento não pode ser nulo");
        }

        Optional<MovimentoFinanceiro> existente = movimentoRepository.findById(id);
        if (!existente.isPresent()) {
            throw new IllegalArgumentException("Movimento financeiro não encontrado com o ID: " + id);
        }

        auditoriaService.registrar("MovimentoFinanceiro", id, "EXCLUÍDO",
                usuarioService.getUsuarioLogadoNome(),
                "Dados Excluído: " + existente.toString());

        return movimentoRepository.delete(id);
    }

    /**
     * Calcula o total de receitas de um encontro
     */
    public BigDecimal calcularTotalReceitas(Long encontroId) {
        if (encontroId == null) {
            return BigDecimal.ZERO;
        }
        return movimentoRepository.somarReceitasPorEncontro(encontroId);
    }

    /**
     * Calcula o total de despesas de um encontro
     */
    public BigDecimal calcularTotalDespesas(Long encontroId) {
        if (encontroId == null) {
            return BigDecimal.ZERO;
        }
        return movimentoRepository.somarDespesasPorEncontro(encontroId);
    }

    /**
     * Calcula o saldo (receitas - despesas) de um encontro
     */
    public BigDecimal calcularSaldo(Long encontroId) {
        BigDecimal receitas = calcularTotalReceitas(encontroId);
        BigDecimal despesas = calcularTotalDespesas(encontroId);
        return receitas.subtract(despesas);
    }

    /**
     * Conta o total de movimentos
     */
    public long contarTotal() {
        return movimentoRepository.count();
    }

    private void validar(MovimentoFinanceiro movimento) {
        if (movimento == null) {
            throw new IllegalArgumentException("Movimento financeiro não pode ser nulo");
        }
        if (movimento.getTipo() == null) {
            throw new IllegalArgumentException("Tipo de movimento é obrigatório");
        }
        if (movimento.getCategoria() == null) {
            throw new IllegalArgumentException("Categoria é obrigatória");
        }
        if (movimento.getValor() == null || movimento.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
        if (movimento.getEncontro() == null || movimento.getEncontro().getId() == null) {
            throw new IllegalArgumentException("Encontro é obrigatório");
        }
        if (movimento.getDataMovimento() == null) {
            throw new IllegalArgumentException("Data do movimento é obrigatória");
        }
    }
}

package br.com.segueme.controller;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.enums.Sacramento;
import br.com.segueme.service.EncontristaService;

/**
 * Controller para a Ficha Digital do Seguimista.
 * Acesso por ID do encontrista (requer autenticação).
 */
@Named
@ViewScoped
public class FichaDigitalController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EncontristaService encontristaService;

    private Long id;
    private Encontrista encontrista;
    private boolean fichaEncontrada;
    private String mensagemErro;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Carrega os dados do encontrista a partir do ID informado via viewParam.
     */
    public void carregarFicha() {
        if (id == null) {
            fichaEncontrada = false;
            mensagemErro = "ID não informado.";
            return;
        }

        encontristaService.buscarPorId(id).ifPresentOrElse(
            enc -> {
                this.encontrista = enc;
                this.fichaEncontrada = true;
            },
            () -> {
                this.fichaEncontrada = false;
                this.mensagemErro = "Seguimista não encontrado. Verifique o ID informado.";
            }
        );
    }

    // ================================
    // Dados Pessoais
    // ================================

    public String getNomeCompleto() {
        Pessoa p = getPessoa();
        return p != null ? p.getNome() : "";
    }

    public String getCpf() {
        Pessoa p = getPessoa();
        return p != null && p.getCpf() != null ? p.getCpf() : "Não informado";
    }

    public String getDataNascimentoFormatada() {
        Pessoa p = getPessoa();
        if (p != null && p.getDataNascimento() != null) {
            return p.getDataNascimento().format(DATE_FORMATTER);
        }
        return "Não informada";
    }

    public String getSexo() {
        Pessoa p = getPessoa();
        if (p != null && p.getSexo() != null) {
            return p.getSexo().equals('F') ? "Feminino" : "Masculino";
        }
        return "Não informado";
    }

    public String getNaturalidade() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getNaturalidade() : null);
    }

    public String getIdadeTexto() {
        if (encontrista != null && encontrista.getIdade() != null) {
            return encontrista.getIdade() + " anos";
        }
        return "Não informada";
    }

    // ================================
    // Contato e Endereço
    // ================================

    public String getEndereco() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getEndereco() : null);
    }

    public String getTelefone() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getTelefone() : null);
    }

    public String getEmail() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getEmail() : null);
    }

    // ================================
    // Filiação
    // ================================

    public String getFiliacaoPai() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getFiliacaoPai() : null);
    }

    public String getFiliacaoMae() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getFiliacaoMae() : null);
    }

    // ================================
    // Formação e Religião
    // ================================

    public String getEscolaridade() {
        Pessoa p = getPessoa();
        if (p != null && p.getEscolaridade() != null) {
            return p.getEscolaridade().getDescricao();
        }
        return "Não informada";
    }

    public String getCurso() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getCurso() : null);
    }

    public String getInstituicaoEnsino() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getInstituicaoEnsino() : null);
    }

    public String getReligiao() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getReligiao() : null);
    }

    public String getIgrejaFrequenta() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getIgrejaFrequenta() : null);
    }

    public String getMovimentoParticipou() {
        Pessoa p = getPessoa();
        return valorOuNaoInformado(p != null ? p.getMovimentoParticipou() : null);
    }

    public String getSacramentosTexto() {
        Pessoa p = getPessoa();
        if (p != null && p.getSacramentos() != null && !p.getSacramentos().isEmpty()) {
            return p.getSacramentos().stream()
                    .map(Sacramento::getDescricao)
                    .collect(Collectors.joining(", "));
        }
        return "Nenhum sacramento informado";
    }

    public List<Sacramento> getSacramentos() {
        Pessoa p = getPessoa();
        if (p != null && p.getSacramentos() != null) {
            return p.getSacramentos();
        }
        return List.of();
    }

    // ================================
    // Dados do Encontro
    // ================================

    public String getNomeEncontro() {
        if (encontrista != null && encontrista.getEncontro() != null) {
            return encontrista.getEncontro().getNome();
        }
        return "";
    }

    public String getDataInscricaoFormatada() {
        if (encontrista != null && encontrista.getDataInscricao() != null) {
            return encontrista.getDataInscricao().format(DATETIME_FORMATTER);
        }
        return "Não informada";
    }

    public String getStatus() {
        if (encontrista != null && encontrista.getAtivo() != null) {
            return encontrista.getAtivo() ? "Ativo" : "Inativo";
        }
        return "Indefinido";
    }

    public String getStatusClass() {
        if (encontrista != null && encontrista.getAtivo() != null) {
            return encontrista.getAtivo() ? "status-ativo" : "status-inativo";
        }
        return "status-indefinido";
    }

    public String getCirculo() {
        if (encontrista != null && encontrista.getCirculo() != null) {
            String nome = encontrista.getCirculo().name();
            return nome.charAt(0) + nome.substring(1).toLowerCase();
        }
        return "Não definido";
    }

    public String getFormaPagamento() {
        if (encontrista != null && encontrista.getFormaPagamento() != null) {
            return encontrista.getFormaPagamento();
        }
        return "Não informada";
    }

    public String getObservacoes() {
        if (encontrista != null && encontrista.getObservacoes() != null
                && !encontrista.getObservacoes().isEmpty()) {
            return encontrista.getObservacoes();
        }
        return null;
    }

    // ================================
    // Foto
    // ================================

    public String getFotoUrl() {
        Pessoa p = getPessoa();
        if (p != null && p.getFoto() != null && !p.getFoto().isEmpty()) {
            return "/fotos/" + p.getFoto();
        }
        return "/resources/images/default_avatar.png";
    }

    // ================================
    // Helpers
    // ================================

    private Pessoa getPessoa() {
        return (encontrista != null) ? encontrista.getPessoa() : null;
    }

    private String valorOuNaoInformado(String valor) {
        return (valor != null && !valor.trim().isEmpty()) ? valor : "Não informado";
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Encontrista getEncontrista() {
        return encontrista;
    }

    public boolean isFichaEncontrada() {
        return fichaEncontrada;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }
}

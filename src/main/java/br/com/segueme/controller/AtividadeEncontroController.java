package br.com.segueme.controller;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.AtividadeEncontro;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Palestra;
import br.com.segueme.enums.TipoAtividade;
import br.com.segueme.service.AtividadeEncontroService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.PalestraService;

@Named
@ViewScoped
public class AtividadeEncontroController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private AtividadeEncontroService atividadeService;

    @Inject
    private EncontroService encontroService;

    @Inject
    private PalestraService palestraService;

    private List<Encontro> encontros;
    private Encontro encontroFiltro;
    private List<AtividadeEncontro> atividades;
    private Map<LocalDate, List<AtividadeEncontro>> atividadesPorDia;
    private List<Palestra> palestrasDoEncontro;

    private AtividadeEncontro atividade;
    private AtividadeEncontro atividadeSelecionada;

    private static final DateTimeFormatter DATE_LABEL = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new java.util.Locale("pt", "BR"));

    @PostConstruct
    public void init() {
        encontros = encontroService.buscarTodos();
        limpar();
    }

    public void limpar() {
        atividade = new AtividadeEncontro();
        atividadesPorDia = new LinkedHashMap<>();
        atividades = List.of();
        palestrasDoEncontro = List.of();
    }

    public void carregarPorEncontro() {
        // Tenta resolver encontroFiltro via parâmetro da URL se ainda não foi definido
        if (encontroFiltro == null) {
            String param = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap().get("encontroId");
            if (param != null && !param.isEmpty()) {
                encontroService.buscarPorId(Long.valueOf(param))
                        .ifPresent(e -> this.encontroFiltro = e);
            }
        }
        if (encontroFiltro == null) {
            atividadesPorDia = new LinkedHashMap<>();
            atividades = List.of();
            palestrasDoEncontro = List.of();
            return;
        }
        atividades = atividadeService.buscarPorEncontro(encontroFiltro);
        atividadesPorDia = agruparPorDia(atividades);
        palestrasDoEncontro = palestraService.buscarPorEncontro(encontroFiltro);
    }

    /** Retorna nomes dos palestrantes da palestra vinculada à atividade, separados por vírgula. */
    public String getNomesPalestrantes(AtividadeEncontro atv) {
        if (atv == null || atv.getPalestra() == null
                || atv.getPalestra().getPalestrantes() == null
                || atv.getPalestra().getPalestrantes().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (br.com.segueme.entity.Palestrante p : atv.getPalestra().getPalestrantes()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getNomeDisplay());
        }
        return sb.toString();
    }

    private Map<LocalDate, List<AtividadeEncontro>> agruparPorDia(List<AtividadeEncontro> lista) {
        Map<LocalDate, List<AtividadeEncontro>> mapa = new LinkedHashMap<>();
        for (AtividadeEncontro a : lista) {
            LocalDate dia = a.getDataHoraInicio().toLocalDate();
            mapa.computeIfAbsent(dia, k -> new java.util.ArrayList<>()).add(a);
        }
        return mapa;
    }

    public void carregarAtividade() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            AtividadeEncontro encontrado = atividadeService.buscarPorId(Long.valueOf(idParam));
            if (encontrado != null) {
                this.atividade = encontrado;
                this.encontroFiltro = encontrado.getEncontro();
            }
        }
        String encontroIdParam = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("encontroId");
        if (encontroIdParam != null && !encontroIdParam.isEmpty() && atividade.getId() == null) {
            encontroService.buscarPorId(Long.valueOf(encontroIdParam))
                    .ifPresent(e -> {
                        atividade.setEncontro(e);
                        this.encontroFiltro = e;
                    });
        }
        // Carrega palestras do encontro para o seletor no formulário
        if (this.encontroFiltro != null) {
            palestrasDoEncontro = palestraService.buscarPorEncontro(this.encontroFiltro);
        }
    }

    public String salvar() {
        try {
            if (atividade.getId() == null) {
                atividadeService.salvar(atividade);
                addInfo("Atividade cadastrada com sucesso!");
            } else {
                atividadeService.atualizar(atividade);
                addInfo("Atividade atualizada com sucesso!");
            }
            return "lista?faces-redirect=true" +
                   (encontroFiltro != null ? "&encontroId=" + encontroFiltro.getId() : "");
        } catch (Exception e) {
            addError(e.getMessage());
            return null;
        }
    }

    public void prepararExclusao(AtividadeEncontro a) {
        this.atividadeSelecionada = a;
    }

    public void excluir() {
        try {
            atividadeService.excluir(atividadeSelecionada.getId());
            addInfo("Atividade excluída com sucesso!");
            carregarPorEncontro();
        } catch (Exception e) {
            addError(e.getMessage());
        }
    }

    public String rotuloDia(LocalDate dia) {
        return dia.format(DATE_LABEL);
    }

    public TipoAtividade[] getTiposAtividade() {
        return TipoAtividade.values();
    }

    // ===== Helpers =====

    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", msg));
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", msg));
    }

    // ===== Getters e Setters =====

    public String formatarHora(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public List<Encontro> getEncontros() { return encontros; }
    public Encontro getEncontroFiltro() { return encontroFiltro; }
    public void setEncontroFiltro(Encontro encontroFiltro) { this.encontroFiltro = encontroFiltro; }
    public List<AtividadeEncontro> getAtividades() { return atividades; }
    public Map<LocalDate, List<AtividadeEncontro>> getAtividadesPorDia() { return atividadesPorDia; }
    public List<Palestra> getPalestrasDoEncontro() { return palestrasDoEncontro; }
    public AtividadeEncontro getAtividade() { return atividade; }
    public void setAtividade(AtividadeEncontro atividade) { this.atividade = atividade; }
    public AtividadeEncontro getAtividadeSelecionada() { return atividadeSelecionada; }
    public void setAtividadeSelecionada(AtividadeEncontro atividadeSelecionada) { this.atividadeSelecionada = atividadeSelecionada; }
}

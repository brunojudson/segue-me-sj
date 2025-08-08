package br.com.segueme.controller;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Versiculo;
import br.com.segueme.service.ContribuicaoService;
import br.com.segueme.service.EncontristaService;
import br.com.segueme.service.EncontroService;
import br.com.segueme.service.EquipeService;
import br.com.segueme.service.TrabalhadorService;
import br.com.segueme.service.VersiculoService;

@Named
@ViewScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private EncontroService encontroService;

    @Inject
    private EncontristaService encontristaService;

    @Inject
    private TrabalhadorService trabalhadorService;

    @Inject
    private EquipeService equipeService;

    @Inject
    private ContribuicaoService contribuicaoService;

    @Inject
    private VersiculoService versiculoService;

    private Versiculo versiculoDoDia;

    /*@Inject
    private ContribuicaoService contribuicaoService;*/

    private List<Encontro> proximosEncontros;
    private Long totalEncontristas;
    private Long totalTrabalhadores;
    private Long totalEquipes;
    private BigDecimal totalContribuicoes;

    @PostConstruct
    public void init() {
        carregarProximosEncontros();
        carregarEstatisticas();
        carregarVersiculoDoDia();
    }

    private void carregarProximosEncontros() {
        // Buscar todos os encontros ativos
        //List<Encontro> encontrosAtivos = encontroService.buscaUltimosDois();
        proximosEncontros = encontroService.buscaUltimosDois();
        // Filtrar apenas os encontros futuros ou em andamento
		/*
		 * LocalDate hoje = LocalDate.now(); proximosEncontros =
		 * encontrosAtivos.stream() .filter(e -> e.getDataFim().isAfter(hoje) ||
		 * e.getDataFim().isEqual(hoje)) .collect(Collectors.toList());
		 */
    }

    private void carregarEstatisticas() {
        // Estas implementações são simplificadas e devem ser ajustadas conforme a lógica real do sistema
        totalEncontristas = (long) encontristaService.buscarTodos().size();
        totalTrabalhadores = (long) trabalhadorService.buscarTodos().size();
        totalEquipes = (long) equipeService.buscarTodos().size();

        // Exemplo simplificado para o total de contribuições
        // totalContribuicoes = new BigDecimal("0.00");
        // Na implementação real, seria algo como:
         totalContribuicoes = contribuicaoService.calcularTotalGeral();
    }

    private void carregarVersiculoDoDia() {
        this.versiculoDoDia = versiculoService.buscarAleatorio();
    }

    // Getters e Setters
    public Versiculo getVersiculoDoDia() {
        return versiculoDoDia;
    }

    public List<Encontro> getProximosEncontros() {
        return proximosEncontros;
    }

    public void setProximosEncontros(List<Encontro> proximosEncontros) {
        this.proximosEncontros = proximosEncontros;
    }

    public Long getTotalEncontristas() {
        return totalEncontristas;
    }

    public void setTotalEncontristas(Long totalEncontristas) {
        this.totalEncontristas = totalEncontristas;
    }

    public Long getTotalTrabalhadores() {
        return totalTrabalhadores;
    }

    public void setTotalTrabalhadores(Long totalTrabalhadores) {
        this.totalTrabalhadores = totalTrabalhadores;
    }

    public Long getTotalEquipes() {
        return totalEquipes;
    }

    public void setTotalEquipes(Long totalEquipes) {
        this.totalEquipes = totalEquipes;
    }

    public BigDecimal getTotalContribuicoes() {
        return totalContribuicoes;
    }

    public void setTotalContribuicoes(BigDecimal totalContribuicoes) {
        this.totalContribuicoes = totalContribuicoes;
    }
}

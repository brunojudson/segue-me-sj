package br.com.segueme.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria", schema = "public")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidade;
    private Long entidadeId;
    private String acao;
    private String usuario;
    private LocalDateTime dataHora;
    @Column(columnDefinition = "TEXT")
    private String detalhes;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getEntidade() {
		return entidade;
	}
	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}
	public Long getEntidadeId() {
		return entidadeId;
	}
	public void setEntidadeId(Long entidadeId) {
		this.entidadeId = entidadeId;
	}
	public String getAcao() {
		return acao;
	}
	public void setAcao(String acao) {
		this.acao = acao;
	}
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	public LocalDateTime getDataHora() {
		return dataHora;
	}
	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}
	public String getDetalhes() {
		return detalhes;
	}
	public void setDetalhes(String detalhes) {
		this.detalhes = detalhes;
	}
}
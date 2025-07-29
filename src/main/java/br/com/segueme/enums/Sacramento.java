package br.com.segueme.enums;

public enum Sacramento {
	BATISMO("Batísmo"),
	CRISMA("Crisma"),
	EUCARISTIA("Eucarístia"),
	MATRIMONIO("Matrimônio");

	private final String descricao;

	Sacramento(String descricao) {
		this.descricao = descricao;
	}

	public String getDescricao() {
		return descricao;
	}
}

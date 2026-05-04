package br.com.segueme.entity;

/**
 * Motivos para encerramento antecipado de um mandato de dirigente.
 */
public enum MotivoEncerramentoMandato {

    TERMINO_NATURAL("Término Natural", "Mandato encerrado ao atingir a data de fim"),
    RENUNCIA("Renúncia", "Dirigente renunciou voluntariamente ao mandato"),
    DESTITUICAO("Destituição", "Dirigente foi destituído pela autoridade competente"),
    FALECIMENTO("Falecimento", "Falecimento do dirigente"),
    MUDANCA_COMUNIDADE("Mudança de Comunidade", "Dirigente mudou-se de comunidade"),
    IMPEDIMENTO_SAUDE("Impedimento de Saúde", "Dirigente impedido por motivo de saúde"),
    OUTRO("Outro", "Outro motivo não especificado");

    private final String descricao;
    private final String descricaoDetalhada;

    MotivoEncerramentoMandato(String descricao, String descricaoDetalhada) {
        this.descricao = descricao;
        this.descricaoDetalhada = descricaoDetalhada;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }
}

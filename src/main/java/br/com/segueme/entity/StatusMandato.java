package br.com.segueme.entity;

/**
 * Status do mandato de um dirigente.
 * 
 * Ciclo de vida:
 * ATIVO -> PRORROGADO (se estendido por +1 ano)
 * ATIVO -> ENCERRADO_NATURAL (ao término do mandato)
 * ATIVO -> RENUNCIADO (se o dirigente renuncia)
 * ATIVO -> DESTITUIDO (se removido pela autoridade competente)
 * ATIVO -> ENCERRADO_OUTROS (falecimento, mudança, etc.)
 * PRORROGADO -> ENCERRADO_NATURAL / RENUNCIADO / DESTITUIDO / ENCERRADO_OUTROS
 */
public enum StatusMandato {

    ATIVO("Ativo", "Mandato em vigor"),
    PRORROGADO("Prorrogado", "Mandato prorrogado por mais 1 ano"),
    ENCERRADO_NATURAL("Encerrado", "Mandato encerrado por término natural"),
    RENUNCIADO("Renunciado", "Dirigente renunciou ao mandato"),
    DESTITUIDO("Destituído", "Dirigente foi destituído"),
    ENCERRADO_OUTROS("Enc. Outros Motivos", "Encerrado por outros motivos");

    private final String descricao;
    private final String descricaoDetalhada;

    StatusMandato(String descricao, String descricaoDetalhada) {
        this.descricao = descricao;
        this.descricaoDetalhada = descricaoDetalhada;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getDescricaoDetalhada() {
        return descricaoDetalhada;
    }

    public boolean isEncerrado() {
        return this == ENCERRADO_NATURAL || this == RENUNCIADO 
            || this == DESTITUIDO || this == ENCERRADO_OUTROS;
    }

    public boolean isVigente() {
        return this == ATIVO || this == PRORROGADO;
    }
}

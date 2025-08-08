package br.com.segueme.enums;


/**
 * Enum que representa os possíveis temas das palestras.
 */
public enum TemaPalestra {
    FE("Fé"),
    VOCACAO("Vocação"),
    FAMILIA("Família"),
    SERVICO("Serviço"),
    COMUNIDADE("Comunidade"),
    ORACAO("Oração"),
    SACRAMENTOS("Sacramentos"),
    MARIA("Maria"),
    ESPIRITO_SANTO("Espírito Santo"),
    TESTEMUNHO("Testemunho"),
    NAMORO_CASTIDADE("Namoro e Castidade"),
    OUTRO("Outro");

    private final String nomeExibicao;

    TemaPalestra(String nomeExibicao) {
        this.nomeExibicao = nomeExibicao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}


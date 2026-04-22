package br.com.segueme.enums;

/**
 * Tipos de atividade no cronograma de um encontro.
 */
public enum TipoAtividade {
    PALESTRA("Palestra", "pi pi-book", "#7c3aed"),
    DINAMICA("Dinâmica", "pi pi-bolt", "#0d9668"),
    REFEICAO("Refeição", "pi pi-shopping-cart", "#d97706"),
    ORACAO("Oração", "pi pi-heart", "#db2777"),
    SILENCIO("Silêncio / Reflexão", "pi pi-moon", "#6366f1"),
    INTERVALO("Intervalo / Descanso", "pi pi-clock", "#64748b"),
    LOUVOR("Louvor e Adoração", "pi pi-star", "#f59e0b"),
    TESTEMUNHO("Testemunho", "pi pi-comment", "#0891b2"),
    ENCERRAMENTO("Encerramento", "pi pi-flag", "#dc2626"),
    OUTRO("Outro", "pi pi-ellipsis-h", "#94a3b8");

    private final String descricao;
    private final String icone;
    private final String cor;

    TipoAtividade(String descricao, String icone, String cor) {
        this.descricao = descricao;
        this.icone = icone;
        this.cor = cor;
    }

    public String getDescricao() { return descricao; }
    public String getIcone() { return icone; }
    public String getCor() { return cor; }
}

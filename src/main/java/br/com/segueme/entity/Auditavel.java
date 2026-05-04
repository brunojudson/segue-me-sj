package br.com.segueme.entity;

/**
 * Interface para entidades auditáveis.
 * Implementações devem retornar um resumo estruturado dos campos
 * mais relevantes para fins de rastreabilidade em logs de auditoria.
 * O formato é texto livre, mas segue o padrão: campo=valor | campo=valor ...
 */
public interface Auditavel {

    /**
     * Retorna um resumo legível dos campos-chave da entidade, sem expor
     * dados sensíveis (senhas, tokens, fotos, objetos aninhados completos).
     *
     * @return String com pares campo=valor separados por " | "
     */
    String toAuditString();
}

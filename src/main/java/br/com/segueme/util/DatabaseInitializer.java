package br.com.segueme.util;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.logging.Logger;

/**
 * Classe utilitária para inicialização do banco de dados
 * Cria dados iniciais necessários para o funcionamento do sistema
 */
@Singleton
@Startup
public class DatabaseInitializer {
    
    private static final Logger logger = Logger.getLogger(DatabaseInitializer.class.getName());
    
    @PersistenceContext(unitName = "seguemePU")
    private EntityManager entityManager;
    
    @PostConstruct
    @Transactional
    public void init() {
        logger.info("Inicializando banco de dados...");
        
        try {
            // Verifica se já existem dados no banco
            Long countTipoEquipe = (Long) entityManager.createQuery("SELECT COUNT(t) FROM TipoEquipe t").getSingleResult();
            
            if (countTipoEquipe == 0) {
                logger.info("Criando dados iniciais...");
                //criarDadosIniciais();
                logger.info("Dados iniciais criados com sucesso!");
            } else {
                logger.info("Banco de dados já inicializado. Pulando criação de dados iniciais.");
            }
        } catch (Exception e) {
            logger.severe("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Transactional
    private void criarDadosIniciais() {
        try {
            // Cria tipos de equipe padrão
            entityManager.createNativeQuery(
                    "INSERT INTO tipo_equipe (nome, descricao, eh_dirigente, ativo) VALUES " +
                    "('Dirigente', 'Equipe de direção do encontro', true, true), " +
                    "('Cozinha', 'Equipe responsável pela alimentação', false, true), " +
                    "('Liturgia', 'Equipe responsável pelas celebrações', false, true), " +
                    "('Música', 'Equipe responsável pela animação musical', false, true), " +
                    "('Apoio', 'Equipe de apoio logístico', false, true), " +
                    "('Acolhida', 'Equipe de recepção e acolhimento', false, true)")
                    .executeUpdate();
            
            // Cria pastas padrão para equipe dirigente
            entityManager.createNativeQuery(
                    "INSERT INTO pasta (nome, descricao, data_inicio, data_fim, ativo, equipe_id) " +
                    "SELECT 'Coordenação Geral', 'Coordenação geral do encontro', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 YEAR', true, e.id " +
                    "FROM equipe e JOIN tipo_equipe t ON e.tipo_equipe_id = t.id WHERE t.eh_dirigente = true LIMIT 1")
                    .executeUpdate();
            
            entityManager.createNativeQuery(
                    "INSERT INTO pasta (nome, descricao, data_inicio, data_fim, ativo, equipe_id) " +
                    "SELECT 'Secretaria', 'Responsável pela documentação e comunicação', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 YEAR', true, e.id " +
                    "FROM equipe e JOIN tipo_equipe t ON e.tipo_equipe_id = t.id WHERE t.eh_dirigente = true LIMIT 1")
                    .executeUpdate();
            
            entityManager.createNativeQuery(
                    "INSERT INTO pasta (nome, descricao, data_inicio, data_fim, ativo, equipe_id) " +
                    "SELECT 'Tesouraria', 'Responsável pelas finanças do encontro', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 YEAR', true, e.id " +
                    "FROM equipe e JOIN tipo_equipe t ON e.tipo_equipe_id = t.id WHERE t.eh_dirigente = true LIMIT 1")
                    .executeUpdate();
            
            entityManager.createNativeQuery(
                    "INSERT INTO pasta (nome, descricao, data_inicio, data_fim, ativo, equipe_id) " +
                    "SELECT 'Formação', 'Responsável pela formação dos encontristas', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 YEAR', true, e.id " +
                    "FROM equipe e JOIN tipo_equipe t ON e.tipo_equipe_id = t.id WHERE t.eh_dirigente = true LIMIT 1")
                    .executeUpdate();
            
        } catch (Exception e) {
            logger.severe("Erro ao criar dados iniciais: " + e.getMessage());
            throw e;
        }
    }
}

package br.com.segueme.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;

public class calculaIdade {

    public static void atualizarIdadeCadastro(String url, String user, String password) {
        String selectEncontristas = "SELECT id, pessoa_id, data_inscricao FROM encontrista";
        String selectDataNascimento = "SELECT data_nascimento FROM pessoa WHERE id = ?";
        String updateIdade = "UPDATE encontrista SET idade = ? WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                PreparedStatement psEncontristas = conn.prepareStatement(selectEncontristas);
                PreparedStatement psPessoa = conn.prepareStatement(selectDataNascimento);
                PreparedStatement psUpdate = conn.prepareStatement(updateIdade)) {

            ResultSet rs = psEncontristas.executeQuery();
            int total = 0;
            while (rs.next()) {
                long encontristaId = rs.getLong("id");
                long pessoaId = rs.getLong("pessoa_id");
                Timestamp dataCadastroTs = rs.getTimestamp("data_inscricao");

                if (dataCadastroTs == null)
                    continue;

                // Buscar data de nascimento
                psPessoa.setLong(1, pessoaId);
                ResultSet rsPessoa = psPessoa.executeQuery();
                if (rsPessoa.next()) {
                    Timestamp dataNascimentoTs = rsPessoa.getTimestamp("data_nascimento");
                    if (dataNascimentoTs != null) {
                        // Converter para LocalDate
                        LocalDate nascimento = dataNascimentoTs.toLocalDateTime().toLocalDate();
                        LocalDate cadastro = dataCadastroTs.toLocalDateTime().toLocalDate();
                        int idade = Period.between(nascimento, cadastro).getYears();

                        // Atualizar idade_cadastro
                        psUpdate.setInt(1, idade);
                        psUpdate.setLong(2, encontristaId);
                        psUpdate.executeUpdate();
                        total++;
                        System.out.println("Encontrista ID " + encontristaId + " atualizado para idade " + idade);
                    }
                }
                rsPessoa.close();
            }
            System.out.println("Atualização concluída. Total de registros atualizados: " + total);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao atualizar idades: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // ...restaurarBackup...

        // Atualizar idades
        String url = "jdbc:postgresql://localhost:5432/segueme";
        String user = "postgres";
        String password = "admin";
        atualizarIdadeCadastro(url, user, password);
    }
}
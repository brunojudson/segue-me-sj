package br.com.segueme.util;

import br.com.segueme.entity.Pessoa;

import java.sql.*;
import java.time.LocalDate;

/**
 * Classe utilitária responsável por atualizar todas as pessoas na base de dados,
 * calculando e preenchendo o campo de idade com base na data de nascimento.
 * 
 * Para cada registro da tabela "pessoa", a idade é recalculada e atualizada no banco.
 * 
 * Uso típico: execução pontual para garantir que o campo de idade esteja consistente.
 */
public class AtualizaTodasPessoas {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/segueme";
        String usuario = "postgres";
        String senha = "admin";

        try (Connection conn = DriverManager.getConnection(url, usuario, senha)) {
            String sqlSelect = "SELECT id, nome, data_nascimento FROM pessoa";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSelect)) {

                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String nome = rs.getString("nome");
                    Date dataNascSql = rs.getDate("data_nascimento");
                    LocalDate dataNascimento = dataNascSql != null ? dataNascSql.toLocalDate() : null;

                    Pessoa pessoa = new Pessoa();
                    pessoa.setId(id);
                    pessoa.setNome(nome);
                    pessoa.setDataNascimento(dataNascimento);

                    pessoa.calcularIdade();

                    String sqlUpdate = "UPDATE pessoa SET idade = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                        if (pessoa.getIdade() != null) {
                            ps.setInt(1, pessoa.getIdade());
                        } else {
                            ps.setNull(1, Types.INTEGER);
                        }
                        ps.setLong(2, id);
                        ps.executeUpdate();
                        System.out.println("Pessoa atualizada: " + id + " - " + nome + " | Idade: " + pessoa.getIdade());
                    } catch (SQLException e) {
                        System.err.println("Erro ao atualizar pessoa ID " + id + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("Processo concluído.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
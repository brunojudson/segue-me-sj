package br.com.segueme.util;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BackupPostgres {

    public static void realizarBackupMensal(String host, String port, String database, String user, String password, String backupDir) {
        // Formatar o nome do arquivo de backup com o mês atual
        String mesAtual = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String nomeArquivo = "backup_" + mesAtual + ".sql";
        String caminhoArquivo = backupDir + File.separator + nomeArquivo;

        // Comando para executar o pg_dump e gerar binário -F c (custom) 
        /*String comando = String.format(
            "pg_dump -h %s -p %s -U %s -F c -b -v -f \"%s\" %s",
            host, port, user, caminhoArquivo, database
        );*/

         // Comando para executar o pg_dump e gerar texto -F p (plain)
         String comando = String.format(
            "pg_dump -h %s -p %s -U %s -F c -b -v -f \"%s\" %s",
            host, port, user, caminhoArquivo, database
        );

        // Configurar a variável de ambiente para a senha
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", comando); // Para Windows
        processBuilder.environment().put("PGPASSWORD", password);
        System.out.println("Comando executado: " + comando);
        try {
            // Criar o diretório de backup, se não existir
            File diretorio = new File(backupDir);
            if (!diretorio.exists()) {
                if (diretorio.mkdirs()) {
                    System.out.println("Diretório de backup criado: " + backupDir);
                } else {
                    System.err.println("Erro ao criar o diretório de backup: " + backupDir);
                    return;
                }
            }

            // Executar o comando
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            // Verificar se o comando foi executado com sucesso
            if (exitCode == 0) {
                // Verificar se o arquivo foi realmente gerado
                File arquivoBackup = new File(caminhoArquivo);
                if (arquivoBackup.exists() && arquivoBackup.isFile()) {
                    System.out.println("Backup realizado com sucesso: " + caminhoArquivo);
                } else {
                    System.err.println("Erro: O comando foi executado, mas o arquivo de backup não foi gerado.");
                }
            } else {
                System.err.println("Erro ao realizar o backup. Código de saída: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Erro ao executar o backup: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Configurações do banco de dados e diretório de backup
        String host = "localhost";
        String port = "5432";
        String database = "segueme";
        String user = "postgres";
        String password = "admin";
        String backupDir = "C:\\Backup\\SQL";

        // Realizar o backup
        realizarBackupMensal(host, port, database, user, password, backupDir);
    }
}
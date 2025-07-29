package br.com.segueme.util;

import java.io.*;

public class RestorePostgres {

    public static void restaurarBackup(String host, String port, String database, String user, String password, String backupFile, boolean isCustomFormat) {
        // Comando para restaurar o backup
        String comando;
        if (isCustomFormat) {
            // Restaurar backup no formato custom (-F c)
            comando = String.format(
                "pg_restore -h %s -p %s -U %s -d %s -v \"%s\"",
                host, port, user, database, backupFile
            );
        } else {
            // Restaurar backup no formato plain (-F p)
            comando = String.format(
                "psql -h %s -p %s -U %s -d %s -f \"%s\"",
                host, port, user, database, backupFile
            );
        }

        // Configurar a variável de ambiente para a senha
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", comando); // Para Windows
        processBuilder.environment().put("PGPASSWORD", password);
        System.out.println("Comando executado: " + comando);

        try {
            // Verificar se o arquivo de backup existe
            File arquivoBackup = new File(backupFile);
            if (!arquivoBackup.exists() || !arquivoBackup.isFile()) {
                System.err.println("Erro: O arquivo de backup não foi encontrado: " + backupFile);
                return;
            }

            // Executar o comando
            Process process = processBuilder.start();

            // Consumir as saídas padrão e de erro em threads separadas
            Thread stdoutThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        System.out.println(linha);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Thread stderrThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        System.err.println(linha);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            stdoutThread.start();
            stderrThread.start();

            // Aguardar a finalização do processo
            int exitCode = process.waitFor();
            stdoutThread.join();
            stderrThread.join();

            // Verificar se o comando foi executado com sucesso
            if (exitCode == 0) {
                System.out.println("Backup restaurado com sucesso: " + backupFile);
            } else {
                System.err.println("Erro ao restaurar o backup. Código de saída: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.err.println("Erro ao executar a restauração: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Configurações do banco de dados e arquivo de backup
        String host = "localhost";
        String port = "5432";
        String database = "segueme";
        String user = "postgres";
        String password = "admin";
        String backupFile = "C:\\Backup\\SQL\\backup_2025-05.sql";
        boolean isCustomFormat = true; // Defina como false se o backup for no formato plain (-F p)

        // Restaurar o backup
        restaurarBackup(host, port, database, user, password, backupFile, isCustomFormat);
    }
}
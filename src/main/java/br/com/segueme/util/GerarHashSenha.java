package br.com.segueme.util;

import org.mindrot.jbcrypt.BCrypt;

public class GerarHashSenha {
    public static void main(String[] args) {
        String senha = "123"; // Substitua pela senha real
        String hash = BCrypt.hashpw(senha, BCrypt.gensalt());
        System.out.println("Hash gerado: " + hash);

        // Teste a validação
        boolean senhaValida = BCrypt.checkpw(senha, hash);
        System.out.println("Senha válida: " + senhaValida);
    }
}

package br.com.segueme.controller;


import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

@Named
@ViewScoped
public class EncontristaBean implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
    private String nome;
    private String email;
    private String fotoUrl;
    private String errorMessage;

    @PostConstruct
    public void init() {
        this.fotoUrl = null;
        this.errorMessage = null;
    }

    public void buscarPorId() {
        try {
            String apiUrl = "http://localhost:8080/segue-me-sj/api/encontristas/" + id;
            String username = "admin";
            String password = "123";
            String auth = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            System.out.println(auth);
            // Fetch Encontrista details
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + auth);

            if (connection.getResponseCode() == 200) {
                Scanner scanner = new Scanner(connection.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                // Parse JSON response (use a library like Jackson or Gson for production)
                this.nome = response.contains("\"nome\"") ? response.split("\"nome\":\"")[1].split("\"")[0] : null;
                this.email = response.contains("\"email\"") ? response.split("\"email\":\"")[1].split("\"")[0] : null;

                // Set photo URL
                this.fotoUrl = "http://localhost:8080/segue-me-sj/api/encontristas/" + id + "/foto";
                this.errorMessage = null;
            } else {
                this.errorMessage = "Error: " + connection.getResponseCode();
                this.nome = null;
                this.email = null;
                this.fotoUrl = null;
            }
        } catch (Exception e) {
            this.errorMessage = "Error: " + e.getMessage();
            this.nome = null;
            this.email = null;
            this.fotoUrl = null;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

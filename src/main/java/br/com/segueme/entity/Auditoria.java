package br.com.segueme.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "auditoria", schema = "public")
public class Auditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome da entidade é obrigatório")
    private String entidade;
    @NotNull(message = "ID da entidade é obrigatório")
    private Long entidadeId;
    @NotBlank(message = "Ação é obrigatória")
    private String acao;
    @NotBlank(message = "Usuário é obrigatório")
    private String usuario;
    @NotNull(message = "Data/hora é obrigatória")
    private LocalDateTime dataHora;
    @Column(columnDefinition = "TEXT")
    private String detalhes;
}
package br.com.segueme.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "versiculo", schema = "public")
public class Versiculo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Texto do versículo é obrigatório")
	@Size(min = 5, max = 500, message = "Texto deve ter entre 5 e 500 caracteres")
	@Column(name = "texto", nullable = false, length = 500)
	private String texto;

	@NotBlank(message = "Referência bíblica é obrigatória")
	@Size(min = 3, max = 100, message = "Referência deve ter entre 3 e 100 caracteres")
	@Column(name = "referencia", nullable = false, length = 100)
	private String referencia;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Versiculo other = (Versiculo) obj;
		return Objects.equals(id, other.id);
	}

}

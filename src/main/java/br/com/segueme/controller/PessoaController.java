package br.com.segueme.controller;

import br.com.segueme.entity.Pessoa;
import br.com.segueme.service.PessoaService;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.file.UploadedFile;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Named
@ViewScoped
public class PessoaController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private PessoaService pessoaService;

	private List<Pessoa> pessoas;
	private Pessoa pessoa;
	private Pessoa pessoaSelecionada;
	
	private UploadedFile uploadedFile;
	
	private static final String CAMINHO_FOTOS = "C:\\Desenvovilmento\\fotos\\";

	@PostConstruct
	public void init() {
		carregarPessoas();
		limpar();
	}

	public String getFoto() {
		if (pessoa != null && pessoa.getFoto() != null && !pessoa.getFoto().isEmpty()) {
			// Adiciona um parâmetro único (timestamp) para evitar cache
			return "/fotos/" + pessoa.getFoto() + "?t=" + System.currentTimeMillis();
		}
		// Retorna uma imagem padrão caso o casal não tenha foto
		return "/resources/images/default_avatar.png?t=" + System.currentTimeMillis();
	}

	public void carregarPessoas() {
		pessoas = pessoaService.buscarTodos();
	}

	public void limpar() {
		pessoa = new Pessoa();
	}

	public String salvar() {

		try {
			// Verificar se há uma nova foto para upload
			if (uploadedFile != null) {
				String nomeArquivo = gerarNomeArquivo();
				String caminhoArquivo = CAMINHO_FOTOS+""+nomeArquivo;
				System.out.println(caminhoArquivo);
				// Apagar a foto anterior, se existir
				if (pessoa.getFoto() != null) {
					File fotoAnterior = new File(CAMINHO_FOTOS + "" + pessoa.getFoto());
					if (fotoAnterior.exists()) {
						fotoAnterior.delete();
					}
				}
				// Salvar o novo arquivo no diretório
				Files.copy(uploadedFile.getInputStream(), Paths.get(caminhoArquivo),
						StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Arquivo salvo em: " + caminhoArquivo);

				// Atualizar o nome do arquivo no objeto casal
				pessoa.setFoto(nomeArquivo);
			}
			if (pessoa.getId() == null) {
				pessoaService.salvar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa cadastrada com sucesso!"));
			} else {
				pessoaService.atualizar(pessoa);
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa atualizada com sucesso!"));
			}

			carregarPessoas();
			limpar();
			return "lista?faces-redirect=true";
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
			return null;
		}
	}
	
	private String gerarNomeArquivo() {
		String nome = pessoa.getNome();
		long timestamp = System.currentTimeMillis(); // Gera o timestamp atual
		return nome.replaceAll("\\s+", "_") + "_" + timestamp + ".jpg";
	}

	public String visualizar(Pessoa pessoa) {
		this.pessoaSelecionada = pessoa;
		System.out.println(pessoa.getSexo());
		return "visualizar?faces-redirect=true&id=" + pessoa.getId();
	}

	public String editar(Pessoa pessoa) {
		this.pessoa = pessoa;
		return "cadastro?faces-redirect=true&id=" + pessoa.getId();
	}

	public void prepararExclusao(Pessoa pessoa) {
		this.pessoaSelecionada = pessoa;
	}

	public void excluir() {
		try {
			// Excluir o arquivo associado à pessoa, se existir
	        if (pessoaSelecionada.getFoto() != null) {
	            File arquivoFoto = new File(CAMINHO_FOTOS + "" + pessoaSelecionada.getFoto());
	            if (arquivoFoto.exists()) {
	                boolean deletado = arquivoFoto.delete();
	                if (!deletado) {
	                    FacesContext.getCurrentInstance().addMessage(null,
	                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Não foi possível excluir a foto da pessoa."));
	                }
	            }
	        }
			pessoaService.remover(pessoaSelecionada.getId());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Pessoa excluída com sucesso!"));
			carregarPessoas();
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
		}
	}

	public void carregarPessoa() {
		String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
		if (idParam != null && !idParam.isEmpty()) {
			Long id = Long.valueOf(idParam);
			pessoaService.buscarPorId(id).ifPresent(p -> this.pessoa = p);
		}
	}

	// Getters e Setters

	public List<Pessoa> getPessoas() {
		return pessoas;
	}

	public void setPessoas(List<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}

	public Pessoa getPessoa() {
		return pessoa;
	}

	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}

	public Pessoa getPessoaSelecionada() {
		return pessoaSelecionada;
	}

	public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
		this.pessoaSelecionada = pessoaSelecionada;
	}
	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}
}

package br.com.segueme.service;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import br.com.segueme.entity.Encontrista;
import br.com.segueme.entity.Encontro;
import br.com.segueme.entity.Equipe;
import br.com.segueme.entity.Pessoa;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.repository.EncontristaRepository;
import br.com.segueme.repository.EncontroRepository;
import br.com.segueme.repository.EquipeRepository;
import br.com.segueme.repository.PessoaRepository;
import br.com.segueme.repository.TrabalhadorRepository;

@Dependent
public class TrabalhadorService implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private TrabalhadorRepository trabalhadorRepository;

    @Inject
    private PessoaRepository pessoaRepository;

    @Inject
    private EquipeRepository equipeRepository;

    @Inject
    private EncontroRepository encontroRepository;

    @Inject
    private EncontristaRepository encontristaRepository;

    @Inject
    private AuditoriaService auditoriaService;

    @Inject
    private UsuarioService usuarioService;

    /**
     * Salva um novo trabalhador
     * @param trabalhador Objeto trabalhador a ser salvo
     * @return Trabalhador salvo com ID gerado
     */
    public Trabalhador salvar(Trabalhador trabalhador) {
        // Validações
        if (trabalhador == null) {
            throw new IllegalArgumentException("Trabalhador não pode ser nulo");
        }

        if (trabalhador.getPessoa() == null || trabalhador.getPessoa().getId() == null) {
            throw new IllegalArgumentException("Pessoa do trabalhador é obrigatória");
        }

        if (trabalhador.getEquipe() == null || trabalhador.getEquipe().getId() == null) {
            throw new IllegalArgumentException("Equipe do trabalhador é obrigatória");
        }

        if (trabalhador.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início do trabalhador é obrigatória");
        }

        // Verificar se pessoa existe
        Optional<Pessoa> pessoaExistente = pessoaRepository.findById(trabalhador.getPessoa().getId());
        if (!pessoaExistente.isPresent()) {
            throw new IllegalArgumentException("Pessoa não encontrada com o ID: " + trabalhador.getPessoa().getId());
        }

        // Verificar se equipe existe
        Optional<Equipe> equipeExistente = equipeRepository.findById(trabalhador.getEquipe().getId());
        if (!equipeExistente.isPresent()) {
            throw new IllegalArgumentException("Equipe não encontrada com o ID: " + trabalhador.getEquipe().getId());
        }

        // Verificar se encontro existe, se informado
        if (trabalhador.getEncontro() != null && trabalhador.getEncontro().getId() != null) {
            Optional<Encontro> encontroExistente = encontroRepository.findById(trabalhador.getEncontro().getId());
            if (!encontroExistente.isPresent()) {
                throw new IllegalArgumentException("Encontro não encontrado com o ID: " + trabalhador.getEncontro().getId());
            }
        }

        // Verificar se encontrista existe, se informado
        if (trabalhador.getEncontrista() != null && trabalhador.getEncontrista().getId() != null) {
            Optional<Encontrista> encontristaExistente = encontristaRepository.findById(trabalhador.getEncontrista().getId());
            if (!encontristaExistente.isPresent()) {
                throw new IllegalArgumentException("Encontrista não encontrado com o ID: " + trabalhador.getEncontrista().getId());
            }

            // Atualizar flag de foi encontrista
            trabalhador.setFoiEncontrista(true);
        }

        // Verificar se já existe um trabalhador para esta pessoa e encontro
        if (trabalhador.getEncontro() != null && trabalhador.getEncontro().getId() != null) {
            Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findByPessoaEncontro
                (trabalhador.getPessoa().getId(), trabalhador.getEncontro().getId());

            if (trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Já existe um trabalhador cadastrado para esta pessoa neste encontro");
            }
        }
        trabalhador.calcularIdade();
        // Salvar trabalhador

        Trabalhador trabalhadorSalvo = trabalhadorRepository.save(trabalhador);

        auditoriaService.registrar("Trabalhador", trabalhadorSalvo.getId(), "INCLUÍDO", usuarioService.getUsuarioLogadoNome(),
        		"Dados Salvo: " + trabalhador.toString());

        return trabalhadorSalvo;
    }

    /**
     * Atualiza um trabalhador existente
     * @param trabalhador Objeto trabalhador a ser atualizado
     * @return Trabalhador atualizado
     */
    public Trabalhador atualizar(Trabalhador trabalhador) {
        // Validações
        if (trabalhador == null) {
            throw new IllegalArgumentException("Trabalhador não pode ser nulo");
        }

        if (trabalhador.getId() == null) {
            throw new IllegalArgumentException("ID do trabalhador é obrigatório para atualização");
        }

        // Verificar se trabalhador existe
        Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findById(trabalhador.getId());
        if (!trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com o ID: " + trabalhador.getId());
        }

        // Verificar se o encontro está ativo
        Encontro encontro = trabalhadorExistente.get().getEncontro();
        if (encontro != null && Boolean.FALSE.equals(encontro.getAtivo())) {
        	throw new IllegalArgumentException("Não é possível remover o trabalhador, pois o encontro está finalizado.");
        }
        trabalhador.calcularIdade();

        auditoriaService.registrar("Trabalhador", trabalhador.getId(), "ATUALIZADO", usuarioService.getUsuarioLogadoNome(),
                "Dados atualizados: " + trabalhador.toString());

        // Atualizar trabalhador
        return trabalhadorRepository.update(trabalhador);
    }

    /**
     * Busca um trabalhador pelo ID
     * @param id ID do trabalhador
     * @return Optional contendo o trabalhador, se encontrado
     */
    public Optional<Trabalhador> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }

        return trabalhadorRepository.findById(id);
    }

    /**
     * Busca todos os trabalhadores cadastrados
     * @return Lista de trabalhadores
     */
    public List<Trabalhador> buscarTodos() {
        return trabalhadorRepository.findAll();
    }

    public List<Trabalhador> buscarTodosAtivos() {
        return trabalhadorRepository.findAllAtivos();
    }

    public List<Trabalhador> buscarTodosDistintos() {
        return trabalhadorRepository.findAllDistinct();
    }

    /**
     * Busca trabalhadores por pessoa
     * @param pessoaId ID da pessoa
     * @return Lista de trabalhadores da pessoa
     */
    public List<Trabalhador> buscarPorPessoa(Long pessoaId) {
        if (pessoaId == null) {
            throw new IllegalArgumentException("ID da pessoa não pode ser nulo");
        }

        return trabalhadorRepository.findByPessoa(pessoaId);
    }

    /**
     * Busca trabalhadores por equipe
     * @param equipeId ID da equipe
     * @return Lista de trabalhadores da equipe
     */
    public List<Trabalhador> buscarPorEquipe(Long equipeId) {
        if (equipeId == null) {
            throw new IllegalArgumentException("ID da equipe não pode ser nulo");
        }

        return trabalhadorRepository.findByEquipe(equipeId);
    }

    /**
     * Busca trabalhadores por encontro
     * @param encontroId ID do encontro
     * @return Lista de trabalhadores do encontro
     */
    public List<Trabalhador> buscarPorEncontro(Long encontroId) {
        if (encontroId == null) {
            throw new IllegalArgumentException("ID do encontro não pode ser nulo");
        }

        return trabalhadorRepository.findByEncontro(encontroId);
    }

    /**
     * Busca trabalhadores que são coordenadores
     * @return Lista de trabalhadores coordenadores
     */
    public List<Trabalhador> buscarCoordenadores() {
        return trabalhadorRepository.findCoordenadores();
    }

    /**
     * Busca trabalhadores que foram encontristas
     * @return Lista de trabalhadores que foram encontristas
     */
    public List<Trabalhador> buscarExEncontristas() {
        return trabalhadorRepository.findExEncontristas();
    }

    /**
     * Busca trabalhador por pessoa, equipe e encontro
     * @param pessoaId ID da pessoa
     * @param equipeId ID da equipe
     * @param encontroId ID do encontro
     * @return Optional contendo o trabalhador, se encontrado
     */
    public Optional<Trabalhador> buscarPorPessoaEquipeEncontro(Long pessoaId, Long equipeId, Long encontroId) {
        if (pessoaId == null || equipeId == null || encontroId == null) {
            throw new IllegalArgumentException("IDs da pessoa, equipe e encontro são obrigatórios");
        }

        return trabalhadorRepository.findByPessoaEquipeEncontro(pessoaId, equipeId, encontroId);
    }

    /**
     * Remove um trabalhador do banco de dados
     * @param id ID do trabalhador a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }

        // Verificar se trabalhador existe
        Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findById(id);
        if (!trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com o ID: " + id);
        }

        // Verificar se o encontro está ativo
        Encontro encontro = trabalhadorExistente.get().getEncontro();
        if (encontro != null && Boolean.FALSE.equals(encontro.getAtivo())) {
        	throw new IllegalArgumentException("Não é possível remover o trabalhador, pois o encontro está finalizado.");
        }

        // Verificar se trabalhador possui associações
        if (trabalhadorRepository.hasAssociations(id)) {
            throw new IllegalArgumentException("Não é possível remover o trabalhador pois ele possui associações");
        }

        auditoriaService.registrar("Trabalhador", id , "EXCLUÍDO", usuarioService.getUsuarioLogadoNome(),
        		"Dados Excluído: " + trabalhadorExistente.toString());

        return trabalhadorRepository.delete(id);
    }

    public void desativarPorEncontro(Long encontroId) {
        List<Trabalhador> trabalhadores = trabalhadorRepository.findByEncontro(encontroId);
        for (Trabalhador trabalhador : trabalhadores) {
            trabalhador.setAtivo(false);
            trabalhadorRepository.update(trabalhador);
        }
    }
}

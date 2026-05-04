package br.com.segueme.service;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import br.com.segueme.entity.Dirigente;
import br.com.segueme.entity.MotivoEncerramentoMandato;
import br.com.segueme.entity.Pasta;
import br.com.segueme.entity.StatusMandato;
import br.com.segueme.entity.Trabalhador;
import br.com.segueme.repository.DirigenteRepository;
import br.com.segueme.repository.PastaRepository;
import br.com.segueme.repository.TrabalhadorRepository;

@ApplicationScoped
public class DirigenteService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Inject
    private DirigenteRepository dirigenteRepository;
    
    @Inject
    private TrabalhadorRepository trabalhadorRepository;
    
    @Inject
    private PastaRepository pastaRepository;

    @Inject
    private AuditoriaService auditoriaService;

    @Inject
    private UsuarioService usuarioService;
    
    /**
     * Salva um novo dirigente.
     * Regras:
     * - Mandato inicial é de 1 ano (pode ser menor em caso de mandato remanescente)
     * - Duração máxima sem prorrogação: ~366 dias (1 ano + bissexto)
     * - Não pode haver mandato vigente do mesmo trabalhador na mesma pasta
     * - Status inicial é ATIVO
     */
    public Dirigente salvar(Dirigente dirigente) {
        // Validações básicas
        if (dirigente == null) {
            throw new IllegalArgumentException("Dirigente não pode ser nulo");
        }
        
        if (dirigente.getTrabalhador() == null || dirigente.getTrabalhador().getId() == null) {
            throw new IllegalArgumentException("Trabalhador do dirigente é obrigatório");
        }
        
        if (dirigente.getPasta() == null || dirigente.getPasta().getId() == null) {
            throw new IllegalArgumentException("Pasta do dirigente é obrigatória");
        }
        
        if (dirigente.getDataInicio() == null) {
            throw new IllegalArgumentException("Data de início do dirigente é obrigatória");
        }
        
        if (dirigente.getDataFim() == null) {
            throw new IllegalArgumentException("Data de fim do dirigente é obrigatória");
        }
        
        if (dirigente.getDataFim().isBefore(dirigente.getDataInicio()) 
                || dirigente.getDataFim().isEqual(dirigente.getDataInicio())) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        // Verificar se trabalhador existe
        Optional<Trabalhador> trabalhadorExistente = trabalhadorRepository.findById(dirigente.getTrabalhador().getId());
        if (!trabalhadorExistente.isPresent()) {
            throw new IllegalArgumentException("Trabalhador não encontrado com o ID: " + dirigente.getTrabalhador().getId());
        }
        
        // Verificar se pasta existe
        Optional<Pasta> pastaExistente = pastaRepository.findById(dirigente.getPasta().getId());
        if (!pastaExistente.isPresent()) {
            throw new IllegalArgumentException("Pasta não encontrada com o ID: " + dirigente.getPasta().getId());
        }
        
        // Verificar duração do mandato (máximo 1 ano para mandato novo)
        if (!dirigente.verificarDuracaoMandatoValida()) {
            throw new IllegalArgumentException(
                "Duração do mandato inválida. O mandato inicial deve ser de até 1 ano ("
                + Dirigente.DURACAO_MANDATO_DIAS + " dias)");
        }
        
        // Verificar se já existe um mandato VIGENTE para este trabalhador e pasta
        Optional<Dirigente> mandatoVigente = dirigenteRepository.findMandatoVigente(
                dirigente.getTrabalhador().getId(), dirigente.getPasta().getId());
        if (mandatoVigente.isPresent()) {
            throw new IllegalArgumentException(
                "Já existe um mandato vigente para este trabalhador nesta pasta. "
                + "Encerre o mandato atual antes de criar um novo.");
        }
        
        // Definir status inicial
        dirigente.setStatusMandato(StatusMandato.ATIVO);
        dirigente.setProrrogado(false);
        
        // Salvar dirigente
        Dirigente salvo = dirigenteRepository.save(dirigente);
        auditoriaService.registrar("Dirigente", salvo.getId(), "INCLUÍDO", usuarioService.getUsuarioLogadoNome(), salvo);
        return salvo;
    }
    
    /**
     * Atualiza um dirigente existente.
     * Não permite alterar datas de mandatos já encerrados.
     */
    public Dirigente atualizar(Dirigente dirigente) {
        if (dirigente == null) {
            throw new IllegalArgumentException("Dirigente não pode ser nulo");
        }
        
        if (dirigente.getId() == null) {
            throw new IllegalArgumentException("ID do dirigente é obrigatório para atualização");
        }
        
        Optional<Dirigente> dirigenteExistente = dirigenteRepository.findById(dirigente.getId());
        if (!dirigenteExistente.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + dirigente.getId());
        }
        
        // Não permite editar mandatos encerrados
        StatusMandato statusAtual = dirigenteExistente.get().getStatusMandato();
        if (statusAtual != null && statusAtual.isEncerrado()) {
            throw new IllegalArgumentException(
                "Não é possível editar um mandato encerrado (status: " + statusAtual.getDescricao() + "). "
                + "Mandatos encerrados são imutáveis.");
        }
        
        // Verificar duração do mandato
        if (!dirigente.verificarDuracaoMandatoValida()) {
            int limiteMaximo = dirigente.isProrrogado() ? Dirigente.DURACAO_MAXIMA_DIAS : Dirigente.DURACAO_MANDATO_DIAS + 1;
            throw new IllegalArgumentException(
                "Duração do mandato inválida. Máximo permitido: " + limiteMaximo + " dias"
                + (dirigente.isProrrogado() ? " (mandato prorrogado)" : " (mandato inicial)"));
        }
        
        Dirigente atualizado = dirigenteRepository.update(dirigente);
        auditoriaService.registrar("Dirigente", dirigente.getId(), "ATUALIZADO", usuarioService.getUsuarioLogadoNome(), dirigente);
        return atualizado;
    }
    
    /**
     * Prorroga o mandato de um dirigente por mais 1 ano.
     * Regras:
     * - Só pode ser prorrogado uma vez
     * - Mandato deve estar ativo (não encerrado)
     * - A nova data de fim será dataFim atual + 1 ano
     */
    public Dirigente prorrogarMandato(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        
        Optional<Dirigente> opt = dirigenteRepository.findById(id);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + id);
        }
        
        Dirigente dirigente = opt.get();
        
        if (!dirigente.podeProrrogar()) {
            throw new IllegalArgumentException(
                "Mandato não pode ser prorrogado. " 
                + (dirigente.isProrrogado() ? "Já foi prorrogado anteriormente." 
                   : "O mandato não está ativo."));
        }
        
        dirigente.prorrogar();
        Dirigente prorrogado = dirigenteRepository.update(dirigente);
        auditoriaService.registrar("Dirigente", id, "PRORROGADO",
            usuarioService.getUsuarioLogadoNome(),
            "novaDataFim=" + prorrogado.getDataFim() + " | dataProrrogacao=" + prorrogado.getDataProrrogacao());
        return prorrogado;
    }
    
    /**
     * Encerra o mandato de um dirigente antecipadamente.
     */
    public Dirigente encerrarMandato(Long id, MotivoEncerramentoMandato motivo, String observacao) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        if (motivo == null) {
            throw new IllegalArgumentException("Motivo de encerramento é obrigatório");
        }
        
        Optional<Dirigente> opt = dirigenteRepository.findById(id);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + id);
        }
        
        Dirigente dirigente = opt.get();
        
        if (dirigente.getStatusMandato() != null && dirigente.getStatusMandato().isEncerrado()) {
            throw new IllegalArgumentException("Mandato já está encerrado.");
        }
        
        dirigente.encerrarMandato(motivo, observacao);
        Dirigente encerrado = dirigenteRepository.update(dirigente);
        auditoriaService.registrar("Dirigente", id, "ENCERRADO",
            usuarioService.getUsuarioLogadoNome(),
            "status=" + encerrado.getStatusMandato()
            + " | motivo=" + motivo
            + " | dataEncerramentoEfetivo=" + encerrado.getDataEncerramentoEfetivo()
            + " | observacao=" + observacao);
        return encerrado;
    }
    
    /**
     * Atalho para renúncia de mandato.
     */
    public Dirigente renunciarMandato(Long id, String observacao) {
        return encerrarMandato(id, MotivoEncerramentoMandato.RENUNCIA, observacao);
    }
    
    /**
     * Busca um dirigente pelo ID
     * @param id ID do dirigente
     * @return Optional contendo o dirigente, se encontrado
     */
    public Optional<Dirigente> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        
        return dirigenteRepository.findById(id);
    }
    
    /**
     * Busca todos os dirigentes cadastrados
     * @return Lista de dirigentes
     */
    public List<Dirigente> buscarTodos() {
        return dirigenteRepository.findAll();
    }
    
    /**
     * Busca dirigentes por trabalhador
     * @param trabalhadorId ID do trabalhador
     * @return Lista de dirigentes do trabalhador
     */
    public List<Dirigente> buscarPorTrabalhador(Long trabalhadorId) {
        if (trabalhadorId == null) {
            throw new IllegalArgumentException("ID do trabalhador não pode ser nulo");
        }
        
        return dirigenteRepository.findByTrabalhador(trabalhadorId);
    }
    
    /**
     * Busca dirigentes por pasta
     * @param pastaId ID da pasta
     * @return Lista de dirigentes da pasta
     */
    public List<Dirigente> buscarPorPasta(Long pastaId) {
        if (pastaId == null) {
            throw new IllegalArgumentException("ID da pasta não pode ser nulo");
        }
        
        return dirigenteRepository.findByPasta(pastaId);
    }
    
    /**
     * Busca dirigentes por período de mandato
     * @param dataInicio Data de início
     * @param dataFim Data de fim
     * @return Lista de dirigentes no período
     */
    public List<Dirigente> buscarPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        
        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de fim deve ser posterior à data de início");
        }
        
        return dirigenteRepository.findByPeriodo(dataInicio, dataFim);
    }
    
    /**
     * Busca dirigentes com mandato vigente na data atual
     * @return Lista de dirigentes com mandato vigente
     */
    public List<Dirigente> buscarVigentes() {
        return dirigenteRepository.findVigentes();
    }
    
    /**
     * Busca dirigente por trabalhador e pasta
     * @param trabalhadorId ID do trabalhador
     * @param pastaId ID da pasta
     * @return Optional contendo o dirigente, se encontrado
     */
    public Optional<Dirigente> buscarPorTrabalhadorEPasta(Long trabalhadorId, Long pastaId) {
        if (trabalhadorId == null || pastaId == null) {
            throw new IllegalArgumentException("IDs do trabalhador e da pasta são obrigatórios");
        }
        
        return dirigenteRepository.findByTrabalhadorAndPasta(trabalhadorId, pastaId);
    }
    
    /**
     * Remove um dirigente do banco de dados
     * @param id ID do dirigente a ser removido
     * @return true se removido com sucesso, false caso contrário
     */
    public boolean remover(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do dirigente não pode ser nulo");
        }
        
        // Verificar se dirigente existe
        Optional<Dirigente> dirigenteExistente = dirigenteRepository.findById(id);
        if (!dirigenteExistente.isPresent()) {
            throw new IllegalArgumentException("Dirigente não encontrado com o ID: " + id);
        }
        
        return dirigenteRepository.delete(id);
    }
}

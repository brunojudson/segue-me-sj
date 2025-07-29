package br.com.segueme.converter;

import javax.enterprise.inject.spi.CDI; // Import para CDI programático
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
// import javax.inject.Inject; // Não usaremos @Inject diretamente aqui se estiver falhando

import br.com.segueme.entity.Encontro;
import br.com.segueme.service.EncontroService;

// Mantenha value, mas managed=true pode não ser necessário se a injeção direta não funcionar
// Se managed=true causar problemas de deploy/inicialização, você pode removê-lo.
@FacesConverter(value = "encontroConverter")
public class EncontroConverter implements Converter<Encontro> {

    // Removido @Inject private EncontroService encontroService;
    private EncontroService getEncontroService() {
        // Lookup programático do bean CDI
        // Isso requer que o CDI esteja funcionando corretamente na sua aplicação.
        try {
            return CDI.current().select(EncontroService.class).get();
        } catch (Exception e) {
            // Logar o erro ou lançar uma exceção mais específica
            System.err.println("Falha ao obter EncontroService via CDI em EncontroConverter: " + e.getMessage());
            throw new IllegalStateException("EncontroService não pôde ser obtido via CDI.", e);
        }
    }

    @Override
    public Encontro getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        EncontroService service = getEncontroService(); // Obtém o serviço
        if (service == null) {
            // Isso não deveria acontecer se getEncontroService() lançar exceção em caso de falha.
            System.err.println("EncontroService está nulo em EncontroConverter.getAsObject");
            return null;
        }
        try {
            Long id = Long.valueOf(value);
            return service.buscarPorId(id).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println("Erro de conversão em EncontroConverter - getAsObject: Valor inválido para ID: " + value);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Encontro value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}
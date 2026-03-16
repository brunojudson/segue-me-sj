package br.com.segueme.converter;

import javax.enterprise.inject.spi.CDI;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import br.com.segueme.entity.Equipe;
import br.com.segueme.service.EquipeService;

@FacesConverter(value = "equipeConverter", forClass = Equipe.class)
public class EquipeConverter implements Converter<Equipe> {

    private EquipeService getEquipeService() {
        try {
            return CDI.current().select(EquipeService.class).get();
        } catch (Exception e) {
            System.err.println("Falha ao obter EquipeService via CDI em EquipeConverter: " + e.getMessage());
            throw new IllegalStateException("EquipeService não pôde ser obtido via CDI.", e);
        }
    }

    @Override
    public Equipe getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        EquipeService service = getEquipeService();
        if (service == null) {
            System.err.println("EquipeService está nulo em EquipeConverter.getAsObject");
            return null;
        }
        try {
            Long id = Long.valueOf(value);
            return service.buscarPorId(id).orElse(null);
        } catch (NumberFormatException e) {
            System.err.println("Erro de conversão em EquipeConverter - getAsObject: Valor inválido para ID: " + value);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Equipe value) {
        if (value == null || value.getId() == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}

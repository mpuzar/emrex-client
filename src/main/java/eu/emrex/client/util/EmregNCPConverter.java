package eu.emrex.client.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import eu.emrex.client.controller.EmrexController;
import eu.emrex.client.model.entity.EmregNCP;
import eu.emrex.client.session.EmrexLogger;

@FacesConverter(value = "ncpConverter", forClass = EmregNCP.class)
public class EmregNCPConverter implements Converter {

    private final EmrexLogger log = new EmrexLogger(EmregNCPConverter.class);

    @Inject
    private EmrexController ec;


    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        EmregNCP ncp = ec.getNcpByAcronym(value);
        return ncp;
    }


    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return (value instanceof EmregNCP) ? ((EmregNCP) value).getAcronym() : null;
    }
}

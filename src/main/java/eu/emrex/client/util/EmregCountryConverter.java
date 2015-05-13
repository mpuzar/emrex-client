package eu.emrex.client.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;

import eu.emrex.client.controller.EmrexController;
import eu.emrex.client.model.entity.EmregCountry;
import eu.emrex.client.session.EmrexLogger;

@FacesConverter(value = "countryConverter", forClass = EmregCountry.class)
public class EmregCountryConverter implements Converter {

    private final EmrexLogger log = new EmrexLogger(EmregCountryConverter.class);

    @Inject
    private EmrexController ec;


    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        EmregCountry country = ec.getCountryByCode(value);
        return country;
    }


    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return (value instanceof EmregCountry) ? ((EmregCountry) value).getCountryCode() : null;
    }
}

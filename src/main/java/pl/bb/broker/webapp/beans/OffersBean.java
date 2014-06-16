package pl.bb.broker.webapp.beans;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.StringEscapeUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.xml.XmlCollectionWrapper;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 29.05.14
 * Time: 23:46
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean
@RequestScoped
public class OffersBean implements Serializable {
    public static final String offerREST = "/rs/offer/client/get";
    private List<OffersEntity> offers;

    @ManagedProperty(value = "#{param.city}")
    private String city = "";


    public StreamedContent getImage() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        String index = context.getExternalContext().getRequestParameterMap().get("index");
        if(index!=null && offers!=null && !offers.isEmpty()) {
            int offerIndex = Integer.parseInt(index);
            byte[] image = offers.get(offerIndex).getImage();
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(image)
            );
        }
        if(offers!=null && !offers.isEmpty()) {
            return new DefaultStreamedContent(
                    new ByteArrayInputStream(offers.get(0).getImage())
            );
        }
        return new DefaultStreamedContent();
    }

    @PostConstruct
    public void init() throws Exception {
        String url = null;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getSession(true);
            Client client = Client.create();
            HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            url = req.getRequestURL().toString();
            url = url.substring(0, url.length()-req.getServletPath().length());
            url = url+OffersBean.offerREST;
            if(url.substring(0,5).equals("https")) {
                url = "http"+url.substring(5);
            }

            WebResource webResource = client.resource(url);
            ClientResponse response = null;
            if(city!=null && !city.equals("")) {
                String city2 = city.replace('+', ' ');
                response = webResource.path("city").path(city2).accept(MediaType.APPLICATION_XML_TYPE)
                        .get(ClientResponse.class);
            }
            else {
                response = webResource.accept(MediaType.APPLICATION_XML_TYPE)
                        .get(ClientResponse.class);

            }

            if(response.getStatus()==204) {
                offers = null;
            }
            if(response.getStatus() != 200) {
                throw new Exception("Service error / unavailable: "+response.getStatus());
            }
            XmlCollectionWrapper<OffersEntity> xmlOffers = response.getEntity(XmlCollectionWrapper.class);
            offers = xmlOffers.getItems();
        } catch (Exception e) {
            throw e;
            //show some msg
        }
    }

    public List<OffersEntity> getOffers() {
        return offers;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}

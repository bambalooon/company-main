package pl.bb.broker.webapp.beans;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.xml.XmlCollectionWrapper;
import pl.bb.broker.brokerdb.broker.xml.XmlList;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 15.06.14
 * Time: 21:36
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean
@RequestScoped
public class CitiesBean {
    public static final int MAX_CITIES = 10;
    private List<String> cities;

    public List<String> getCities() {
        if(cities==null || cities.size()<MAX_CITIES) {
            return cities;
        }
        List<String> rnd = new ArrayList<>();
        for(int i=0; i<MAX_CITIES; i++) {
            rnd.add(
                    cities.remove(
                            (int)Math.random()*cities.size()
                    )
            );
        }
        return rnd;
    }

    @PostConstruct
    public void init() throws Exception {
        String url = null;
        try {
            FacesContext context = FacesContext.getCurrentInstance();
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
            ClientResponse response = webResource.path("cities").accept(MediaType.APPLICATION_XML_TYPE)
                    .get(ClientResponse.class);
            if(response.getStatus()==204) {
                cities = null;
                return;
            }
            if(response.getStatus() != 200) {
                throw new Exception("Service error / unavailable: "+response.getStatus());
            }
            XmlList<String> xmlCities = response.getEntity(XmlList.class);
            cities = xmlCities.getList();
        } catch (Exception e) {
            //throw e;
            //show some msg
        }


    }

    public String encodeQuery(String query) throws Exception {
        return java.net.URLEncoder.encode(query, "UTF-8");
    }

}

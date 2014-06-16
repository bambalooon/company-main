package pl.broker.brokerdb.company.ejb;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.xml.XmlCollectionWrapper;
import pl.broker.brokerdb.company.settings.CompanySettings;

import javax.ejb.*;
import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 14.06.14
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class BrokerOfferManager implements OfferManager {
    protected List<OffersEntity> offersList = new ArrayList<>();
    protected String message = "";

    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    public void sendOffers() throws Exception {
        List<OffersEntity> toSend;
        synchronized (offersList) {
            if(offersList.size()==0) {
                return;
            }
            toSend = offersList;
            offersList = new ArrayList<>();
        }
        message = "";

        XmlCollectionWrapper<OffersEntity> xmlOffers = new XmlCollectionWrapper<>();
        xmlOffers.setItems(toSend);

        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(CompanySettings.hostnameVerifier, CompanySettings.context));
        Client client = Client.create(config);
        this.addCookieFilter(client);

        ClientResponse response = null;
        try {
            WebResource webResource = client.resource(CompanySettings.BROKER_OFFERS);
            webResource.type(MediaType.APPLICATION_XML).post(xmlOffers);

            this.authenticate(client);

            response = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, xmlOffers);
            response = webResource.type(MediaType.APPLICATION_XML).post(ClientResponse.class, xmlOffers);

            if(response.getStatus()==201) {
                message += "Wszystkie oferty wysłane!";
            }
            else {
                message += response.toString();
                message += ":"+response.getEntity(String.class);
            }
        } catch (Exception e) {
            message += "Exception: "+e.getMessage();
            message += (":"+response==null ? "" : response.getEntity(String.class));
        }
    }

    public void addOffer(OffersEntity offer) {
        synchronized (offersList) {
            offersList.add(offer);
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    protected void authenticate(Client client) {
        WebResource loginResource = client.resource(CompanySettings.BROKER_LOGIN);
        com.sun.jersey.api.representation.Form form = new Form();
        form.putSingle("j_username", CompanySettings.BROKER_USERNAME);
        form.putSingle("j_password", CompanySettings.BROKER_PASSWORD);
        loginResource.type("application/x-www-form-urlencoded").post(form);
    }

    protected void addCookieFilter(Client client) {
        client.addFilter(new ClientFilter() {
            private ArrayList<Object> cookies;

            @Override
            public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
                if (cookies != null) {
                    request.getHeaders().put("Cookie", cookies);
                }
                ClientResponse response = getNext().handle(request);
                // copy cookies
                if (response.getCookies() != null) {
                    if (cookies == null) {
                        cookies = new ArrayList<Object>();
                    }
                    // A simple addAll just for illustration (should probably check for duplicates and expired cookies)
                    cookies.addAll(response.getCookies());
                }
                return response;
            }
        });
    }
}

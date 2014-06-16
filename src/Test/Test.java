import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.xml.OfferGroup;
import pl.bb.broker.brokerdb.broker.xml.XmlCollectionWrapper;
import pl.broker.brokerdb.company.settings.CompanySettings;
import pl.broker.brokerdb.company.ssl.MyX509KeyManager;
import pl.broker.brokerdb.company.ssl.MyX509TrustManager;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 14.06.14
 * Time: 21:59
 * To change this template use File | Settings | File Templates.
 */
public class Test {

    @org.junit.Test
    public void testMarshal() throws Exception {
        XmlCollectionWrapper<OffersEntity> xmlOffers = new XmlCollectionWrapper<>();
        List<OffersEntity> offers = new ArrayList<>();
        OffersEntity offer = new OffersEntity();
        offer.setId(4);
        offer.setCity("Bangok");
        offers.add(offer);
        xmlOffers.setItems(offers);

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlCollectionWrapper.class);
        Marshaller m = jaxbContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(xmlOffers, System.out);
    }

    @org.junit.Test
    public void test() throws Exception {
//        System.setProperty("javax.net.ssl.keyStorePassword", "company");
//        System.setProperty("javax.net.ssl.keyStore", "c:/company.keystore");
////        System.setProperty("javax.net.ssl.trustStore", CompanySettings.BROKER_KEYSTORE);
////        System.setProperty("javax.net.ssl.trustStorePassword", CompanySettings.BROKER_KEYSTORE_PASSWORD);
//
//        TrustManager[] trustManagers = {new X509TrustManager() {
//            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
//            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
//            public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
//        }};
//
//        SSLContext ctx = SSLContext.getInstance("TLS");
////        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
////        factory.init((KeyStore) null);
////        ctx.init(null, factory.getTrustManagers(), null);
//        HostnameVerifier hv = new HostnameVerifier() {
//            @Override
//            public boolean verify(String s, SSLSession sslSession) {
//                return true;
//            }
//        };
//        ctx.init(null, trustManagers, new SecureRandom());
//        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
//        HttpsURLConnection.setDefaultHostnameVerifier(hv);
////        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(hv, ctx));


//        TrustManager mytm[] = new TrustManager[]{new MyX509TrustManager("c:/jboss.keystore", "b@10n91".toCharArray())};
//        KeyManager mykm[] = new KeyManager[]{new MyX509KeyManager("c:/jboss.keystore", "b@10n91".toCharArray())};
//        SSLContext context = SSLContext.getInstance("TLS");
//        context.init(mykm, mytm, null);
//        HTTPSProperties prop = new HTTPSProperties(getHostnameVerifier(), context);

        KeyStore ks = KeyStore.getInstance("JKS");
        char pass[] = "company".toCharArray();
        FileInputStream fis = new FileInputStream("c:/company.keystore");
        ks.load(fis, pass);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, pass);
        KeyManager[] kms = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers = {new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
            public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
        }};

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kms, trustManagers, null);
        SSLContext.setDefault(context);

        ClientConfig config = new DefaultClientConfig();
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(getHostnameVerifier(), context));
        Client client = Client.create(config);
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

        XmlCollectionWrapper<OffersEntity> xmlOffers = new XmlCollectionWrapper<>();
        List<OffersEntity> offers = new ArrayList<>();
        OffersEntity offer = new OffersEntity();
        offer.setId(4);
        offer.setCity("Bangok");
        offers.add(offer);
        offer = new OffersEntity();
        offer.setId(6);
        offer.setCity("Bangok");
        offers.add(offer);
        xmlOffers.setItems(offers);

        WebResource  webResource = client.resource(CompanySettings.BROKER_OFFERS);
        webResource.path("sec").type(MediaType.APPLICATION_XML).post(xmlOffers);
//        webResource.path("offer").accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);

        // Login:
        WebResource loginResource = client.resource(CompanySettings.BROKER_LOGIN);
        com.sun.jersey.api.representation.Form form = new Form();
        form.putSingle("j_username", CompanySettings.BROKER_USERNAME);
        form.putSingle("j_password", CompanySettings.BROKER_PASSWORD);
        loginResource.type("application/x-www-form-urlencoded").post(form);

//        ClientResponse response = webResource.path("offer").accept(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class);
        ClientResponse response = webResource.path("sec").type(MediaType.APPLICATION_XML).post(ClientResponse.class, xmlOffers);
        response = webResource.path("sec").type(MediaType.APPLICATION_XML).post(ClientResponse.class, xmlOffers);

        System.out.println(response);
        System.out.println(response.getEntity(String.class));

    }

    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
    }

    protected void authenticate(Client client) throws Exception {


        OfferGroup group = new OfferGroup();
        group.setOffers(new ArrayList<OffersEntity>());

//        WebResource securedResource = client.resource(CompanySettings.BROKER_OFFERS);
//        ClientResponse response = securedResource.type(MediaType.APPLICATION_XML_TYPE).post(ClientResponse.class, group);
//        System.out.println(response.toString());
//        System.out.println(response.getCookies());
////        System.out.println(response.getLocation().toString());
//        System.out.println(response.getHeaders().toString());

        WebResource webResource = client.resource(CompanySettings.BROKER_OFFERS+"/test");
        System.out.println(webResource.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class));

        WebResource loginResource = client.resource(CompanySettings.BROKER_LOGIN);

        Form form = new Form();
        form.putSingle("j_username", CompanySettings.BROKER_USERNAME);
        form.putSingle("j_password", CompanySettings.BROKER_PASSWORD);
        ClientResponse response = loginResource.type("application/x-www-form-urlencoded").post(ClientResponse.class, form);
        System.out.println(response.toString());
        System.out.println(response.getCookies());

        webResource = client.resource(CompanySettings.BROKER_OFFERS+"/sec");
        System.out.println(webResource.accept(MediaType.TEXT_PLAIN_TYPE).get(String.class));
    }
}

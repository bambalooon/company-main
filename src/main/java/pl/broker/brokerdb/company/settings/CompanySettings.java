package pl.broker.brokerdb.company.settings;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 14.06.14
 * Time: 21:00
 * To change this template use File | Settings | File Templates.
 */
public class CompanySettings {
    public static final String BROKER_ADDRESS = "https://localhost/broker-main/";
    public static final String BROKER_LOGIN = BROKER_ADDRESS+"j_security_check";
    public static final String BROKER_OFFERS = BROKER_ADDRESS+"rs/offer/management/new/app";
    public static final String BROKER_USERNAME = "zhr";
    public static final String BROKER_PASSWORD = "Harc1@";
    public static final String COMPANY_KEYSTORE = "c:/company.keystore";
    public static final String COMPANY_KEYSTORE_PASSWORD = "company";

    public static KeyManager[] KMS = null;
    public static TrustManager[] TMS = null;
    public static SSLContext context = null;
    public static HostnameVerifier hostnameVerifier = null;

    static {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            char pass[] = COMPANY_KEYSTORE_PASSWORD.toCharArray();
            FileInputStream fis = new FileInputStream(COMPANY_KEYSTORE);
            ks.load(fis, pass);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(ks, pass);
            KMS = keyManagerFactory.getKeyManagers();

            TMS = new TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
                public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
            }};

            context = SSLContext.getInstance("TLS");
            context.init(KMS, TMS, null);
            SSLContext.setDefault(context);
            hostnameVerifier =
            new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };
        } catch (Exception e) {

        }
    }
}

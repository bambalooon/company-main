package pl.broker.brokerdb.company.beans;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.hibernate.HibernateException;
import org.primefaces.event.FileUploadEvent;
import pl.bb.broker.brokerdb.broker.entities.OfferDetailsEntity;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.xml.XmlCollectionWrapper;
import pl.bb.broker.company.companydb.pensjonat.entities.FacilitiesEntity;
import pl.bb.broker.company.companydb.pensjonat.entities.RoomsEntity;
import pl.bb.broker.company.companydb.util.PensjonatDBUserUtil;
import pl.broker.brokerdb.company.ejb.OfferManager;
import pl.broker.brokerdb.company.settings.CompanySettings;
import pl.broker.brokerdb.company.ssl.MyX509KeyManager;
import pl.broker.brokerdb.company.ssl.MyX509TrustManager;

import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 14.06.14
 * Time: 17:12
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean
@SessionScoped
public class NewOfferBean implements Serializable {

    @EJB
    protected OfferManager manager;

    private FacilitiesEntity facility = new FacilitiesEntity();
    private OffersEntity offer = new OffersEntity();
    private List<RoomsEntity> rooms = new ArrayList<>();

    public NewOfferBean() {
        this.addRoom();
    }

    public String getMessage() {
        return manager.getMessage();
    }

    public FacilitiesEntity getFacility() {
        return facility;
    }

    public void setFacility(FacilitiesEntity facility) {
        this.facility = facility;
    }

    public OffersEntity getOffer() {
        return offer;
    }

    public void setOffer(OffersEntity offer) {
        this.offer = offer;
    }

    public List<RoomsEntity> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomsEntity> rooms) {
        this.rooms = rooms;
    }

    public String addRoom() {
        RoomsEntity room = new RoomsEntity();
        rooms.add(room);
        return null;
    }

    public String deleteRoom(RoomsEntity room) {
        if(this.rooms.size()>1) {
            this.rooms.remove(room);
        }
        else {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Oferta musi mieć chociaż jeden pokój.", null));
        }
        return null;
    }

    public void upload(FileUploadEvent event) {
        this.offer.setImage(event.getFile().getContents());
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Upload zakończony powodzeniem.", null));
    }

    public String save() {
        try {
            if(facility.getName()!=null && facility.getAddress()!=null && facility.getAddress().length()>0 && facility.getAddress()!="''") { //save|update facility
                PensjonatDBUserUtil.FACTORY.saveUpdateFacility(facility);
            } else {
                FacilitiesEntity f = PensjonatDBUserUtil.FACTORY.getFacility(facility);
                if(f==null) {
                    FacesContext context = FacesContext.getCurrentInstance();
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Nie ma takiej placówki!", null));
                    return null;
                }
                this.facility = f;
            }
            for(RoomsEntity room : rooms) {
                room.setFacility(facility);
            }
            PensjonatDBUserUtil.FACTORY.saveRooms(rooms);
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd bazy danych! "+e.getMessage(), null));
            return null;
        }

        offer.setFacility(facility.getName());
        offer.setPosted(new java.sql.Date(new Date().getTime()));
        offer.setWithdraw(null);
        offer.setReservations(null);

        List<OfferDetailsEntity> details = new ArrayList<>();
        for(RoomsEntity room : rooms) {
            OfferDetailsEntity detail = new OfferDetailsEntity();
            detail.setOffer(offer);
            detail.setRoomType(room.getRtype());
            detail.setRoom(room.getRname());
            detail.setPrice(room.getPrice());
            details.add(detail);
        }
        offer.setDetails(details);

        manager.addOffer(offer);
        offer = new OffersEntity();

        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Zapis zakończony powodzeniem.", null));
        return null;
    }
}

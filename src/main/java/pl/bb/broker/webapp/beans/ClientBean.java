package pl.bb.broker.webapp.beans;

import pl.bb.broker.brokerdb.broker.entities.FavoritesEntity;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.entities.UsersEntity;
import pl.bb.broker.brokerdb.util.BrokerDBOfferUtil;
import pl.bb.broker.brokerdb.util.BrokerDBReservUtil;

import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 16.06.14
 * Time: 02:11
 * To change this template use File | Settings | File Templates.
 */

@RolesAllowed("CLIENT")
@ManagedBean
@RequestScoped
public class ClientBean {
    private List<OffersEntity> favorites;

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    public List<OffersEntity> getFavorites() {
        if(favorites==null) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
            Principal principal = req.getUserPrincipal();
            if(principal!=null) {
                favorites = BrokerDBOfferUtil.FACTORY.getFavorites(principal.getName());
            }
            else {
                favorites = null;
            }
        }
        return favorites;
    }

    public void setFavorites(List<OffersEntity> favorites) {
        this.favorites = favorites;
    }

    public String addFavorite(OffersEntity offer) {
        if(favorites!=null) {
            this.favorites.add(offer);
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        Principal principal = req.getUserPrincipal();
        if(principal!=null) {
            UsersEntity user = new UsersEntity();
            user.setUsername(principal.getName());
            FavoritesEntity favorite = new FavoritesEntity();
            favorite.setOffer(offer);
            favorite.setUser(user);
            BrokerDBOfferUtil.FACTORY.saveFavorite(favorite);
            userBean.getUser().getFavorites().add(favorite);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Ofertę dodano do ulubionych", null));
        }
        return null;
    }

    public String removeFavorite(OffersEntity offer) {
        if(favorites!=null) {
            this.favorites.remove(offer);
        }
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
        Principal principal = req.getUserPrincipal();
        if(principal!=null) {
            UsersEntity user = new UsersEntity();
            user.setUsername(principal.getName());
            FavoritesEntity favorite = new FavoritesEntity();
            favorite.setOffer(offer);
            favorite.setUser(user);
            BrokerDBOfferUtil.FACTORY.removeFavorite(favorite);
            Collection<FavoritesEntity> favs = userBean.getUser().getFavorites();
            for(FavoritesEntity f : favs) {
                if(f.getOffer().getId()==offer.getId()) {
                    favs.remove(f);
                    break;
                }
            }
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Ofertę usunięto z ulubionych", null));
        }
        return null;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }
}

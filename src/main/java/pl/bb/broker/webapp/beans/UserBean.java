package pl.bb.broker.webapp.beans;

import pl.bb.broker.brokerdb.broker.entities.FavoritesEntity;
import pl.bb.broker.brokerdb.broker.entities.OffersEntity;
import pl.bb.broker.brokerdb.broker.entities.UsersEntity;
import pl.bb.broker.brokerdb.util.BrokerDBOfferUtil;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 16.06.14
 * Time: 06:16
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean
@SessionScoped
public class UserBean {

    private UsersEntity user;

    public UsersEntity getUser() {
        if(user==null) {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();
            Principal principal = req.getUserPrincipal();
            if(principal!=null) {
                user = BrokerDBOfferUtil.FACTORY.getUserWithFavorites(principal.getName());
            }
            else {
                user = null;
            }
        }
        return user;
    }

    public boolean isFavorite(OffersEntity offer) {
        UsersEntity user = getUser();
        if(user==null) {
            return true;
        }
        Collection<FavoritesEntity> favs = user.getFavorites();
        if(favs==null) {
            return false;
        }
        for(FavoritesEntity f : favs) {
            if(f.getOffer().getId()==offer.getId()) {
                return true;
            }
        }
        return false;
    }
}

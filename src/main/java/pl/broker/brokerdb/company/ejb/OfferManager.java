package pl.broker.brokerdb.company.ejb;

import pl.bb.broker.brokerdb.broker.entities.OffersEntity;

import javax.ejb.Remote;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 14.06.14
 * Time: 20:29
 * To change this template use File | Settings | File Templates.
 */

@Remote
public interface OfferManager {
    void addOffer(OffersEntity offer);
    String getMessage();
    void setMessage(String msg);
}

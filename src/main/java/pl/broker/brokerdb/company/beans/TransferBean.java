package pl.broker.brokerdb.company.beans;

import pl.bb.broker.company.invoices.rest.bankclient.BankService;
import pl.bb.broker.company.invoices.rest.bankclient.IBankService;
import pl.bb.broker.company.invoices.rest.bankclient.MoneyTransfer;
import pl.bb.broker.company.invoices.rest.bankclient.ObjectFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: BamBalooon
 * Date: 13.06.14
 * Time: 18:57
 * To change this template use File | Settings | File Templates.
 */

@ManagedBean
@RequestScoped
public class TransferBean {
    public static final long brokerBankAccoundID = 1L;
    private String description;
    private double value;
    private String response;

    public String transferMoney() {
        MoneyTransfer transfer = new MoneyTransfer();
        transfer.setID(TransferBean.brokerBankAccoundID);
        transfer.setDescription(new ObjectFactory().createMoneyTransferDescription(description));
        transfer.setValue(value);

        BankService serviceImpl = new BankService();
        IBankService service = serviceImpl.getBasicHttpBindingIBankService();
        response = service.transferMoney(transfer);
        return null;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getResponse() {
        return response;
    }
}

import Bank.*;
import org.apache.thrift.TException;

public class PremiumAccountService extends BasicAccountManager implements PremiumAccountServie.Iface {

    private ExchangeRates exchangeRates;

    public PremiumAccountService(BankClients clients, ExchangeRates exchangeRates) {
        super(clients);
        this.exchangeRates = exchangeRates;
    }

    public LoanResponse requestLoan(String guid, LoanRequest request) throws NotAuthorizedException, TException {
        Double rrso = 1.2 * exchangeRates.getCurrencyRate(request.getCurrency());
        Double totalCost = rrso * request.getAmount();
        request.


        return new LoanResponse(totalCost.longValue(), totalCost.longValue());
    }
}

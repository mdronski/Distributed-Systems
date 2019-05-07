import Bank.*;
import org.apache.thrift.TException;

public class PremiumAccountManager implements PremiumAccountServie.Iface {

    private ExchangeRates exchangeRates;
    public BankClients clients;


    public PremiumAccountManager(BankClients clients, ExchangeRates exchangeRates) {
        this.clients = clients;
        this.exchangeRates = exchangeRates;
    }

    public long getBalance(String guid) throws NotAuthorizedException, TException {
        try {
            System.out.println(clients.getClient(guid).getBalance());
            return clients.getClient(guid).getBalance();
        }catch (Exception e){
            throw new NotAuthorizedException();
        }
    }

    public LoanResponse requestLoan(String guid, LoanRequest request) throws NotAuthorizedException, TException {
        System.out.println("2");
        try {
            clients.getClient(guid);
        }catch (Exception e){
            return new LoanResponse(0L, 0L);
        }
        System.out.println("3");

        try {
            if (!clients.getClient(guid).isPremium){
                return new LoanResponse(0L, 0L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("4");

        Double rrso = 1.2 * exchangeRates.getCurrencyRate(request.getCurrency());
        Double totalCost = rrso * request.getAmount();
        System.out.println("1");
        return new LoanResponse(totalCost.longValue(), totalCost.longValue());
    }
}

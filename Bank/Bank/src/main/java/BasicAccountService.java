import org.apache.thrift.TException;
import Bank.NotAuthorizedException;

public class BasicAccountService implements Bank.BasicAccountService.Iface {
    private BankClients clients;

    public BasicAccountService(BankClients clients) {
        this.clients = clients;
    }

    @Override
    public long getBalance(String guid) throws NotAuthorizedException, TException {
        try {
            return clients.getClient(guid).getBalance();
        }catch (Exception e){
            throw new NotAuthorizedException();
        }
    }
}

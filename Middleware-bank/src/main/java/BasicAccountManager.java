import Bank.BasicAccountService;
import org.apache.thrift.TException;
import Bank.*;

public class BasicAccountManager implements BasicAccountService.Iface{
    private BankClients clients;

    public BasicAccountManager(BankClients clients) {
        this.clients = clients;
    }

    public long getBalance(String guid) throws NotAuthorizedException, TException {
        try {
            System.out.println(clients.getClient(guid).getBalance());
            return clients.getClient(guid).getBalance();
        }catch (Exception e){
            throw new NotAuthorizedException();
        }
    }
}

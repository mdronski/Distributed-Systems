package main.java;
import Bank.*;
import main.java.BankClients;
import org.apache.thrift.TException;
//import Bank.NotAuthorizedException;
//import Bank.BasicAccountService;

public class BasicAccountManager implements BasicAccountService.Iface {
    private BankClients clients;

    public BasicAccountManager(BankClients clients) {
        this.clients = clients;
    }

    @Override
    public long getBalance(String guid) throws NotAuthorizedException, TException {
        try {
            System.out.println("xd");
            System.out.println(clients.getClient(guid).getBalance());
            return clients.getClient(guid).getBalance();
        }catch (Exception e){
            throw new NotAuthorizedException();
        }
    }
}

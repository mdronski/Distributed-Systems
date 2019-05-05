import Bank.AccountCreationService;
import Bank.AccountData;
import Bank.CreateAccountData;
import org.apache.thrift.TException;

import java.util.UUID;

public class AccountCreator implements AccountCreationService.Iface{

    private BankClients clients;

    public AccountCreator(BankClients clients) {
        this.clients = clients;
    }

    public AccountData createAccount(CreateAccountData data) throws TException {
        boolean isPremium = data.getIncome() > 5000;
        String guid = UUID.randomUUID().toString();
        AccountData accountData = null;
        while (true){
            try {
                accountData = new AccountData(data.getIncome(), guid, isPremium, data);
                clients.addClient(accountData);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return accountData;
    }
}

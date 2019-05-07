import Bank.AccountData;

import java.util.HashMap;

public class BankClients {
    private HashMap<String, AccountData> clients;

    public BankClients() {
        this.clients = new HashMap();
    }

    public void addClient(AccountData data) throws Exception {
        String key = MD5.md5Hash(data.getGuid());
        if (clients.containsKey(data.getGuid()))
            throw new Exception("Client already exists");

        clients.put(data.getGuid(), data);
    }

    public AccountData getClient(String guid) throws Exception {
        if (!clients.containsKey(guid))
            throw new Exception("Client does not exist");

        return clients.get(guid);
    }
}

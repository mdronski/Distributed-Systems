import Bank.AccountCreationService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;
import Bank.*;

public class Bank {
    public static void main(String[] args) {
        BankClients bankClients = new BankClients();
        ExchangeRates exchangeRates = new ExchangeRates();

        Runnable init1 = () -> {
            try {
                initCreation(bankClients);
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        };

        Runnable init2 = () -> {
            try {
                initAccountManagers(bankClients, exchangeRates);
            } catch (TTransportException e) {
                e.printStackTrace();
            }
        };

        Runnable init3 = () -> {
            try {
                CurrencyClient client = new CurrencyClient(exchangeRates);
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        new Thread(init1).start();
        new Thread(init2).start();
        new Thread(init3).start();
    }

    public static void initCreation(BankClients bankClients) throws TTransportException {
        int creationSocket = 9999;

        AccountCreationService.Processor<AccountCreator> creatorProcessor =
                new AccountCreationService.Processor<>(new AccountCreator(bankClients));

        TServerTransport serverTransport = new TServerSocket(creationSocket);
        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

        TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(creatorProcessor));
        System.out.println("Starting the account creation server...");
        server.serve();
    }

    public static void initAccountManagers(BankClients bankClients, ExchangeRates exchangeRates) throws TTransportException {
        int creationSocket = 9998;

        BasicAccountService.Processor<BasicAccountManager> basicAccountManagerProcessor =
                new BasicAccountService.Processor<>(new BasicAccountManager(bankClients));

        PremiumAccountServie.Processor<PremiumAccountManager> premiumAccountManagerProcessor =
                new PremiumAccountServie.Processor<>(new PremiumAccountManager(bankClients, exchangeRates));


        TServerTransport serverTransport = new TServerSocket(creationSocket);
        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

        TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
        multiplex.registerProcessor("B", basicAccountManagerProcessor);
        multiplex.registerProcessor("L", premiumAccountManagerProcessor);

        TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex));
        System.out.println("Starting the account management server...");
        server.serve();
    }
}

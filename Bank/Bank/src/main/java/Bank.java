package main.java;

import java.util.HashMap;
import java.util.Map;
import Bank.CreateAccountData;
import Bank.AccountData;
import Bank.*;
import Bank.AccountCreationService;
import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

public class Bank {
    public static void main(String[] args) throws TTransportException {
        final BankClients bankClients = new BankClients();

        final Runnable init1 = new Runnable() {
            public void run() {
                try {
                    initCreation(bankClients);
                } catch (TTransportException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable init2 = new Runnable() {
            public void run() {
                try {
                    initAccountManagers(bankClients);
                } catch (TTransportException e) {
                    e.printStackTrace();
                }
            }
        };


        new Thread(init1).start();
        new Thread(init2).start();
    }

    public static void initCreation(BankClients bankClients) throws TTransportException {
        int creationSocket = 9999;

        AccountCreationService.Processor<AccountCreator> creatorProcessor =
                new AccountCreationService.Processor<AccountCreator>(new AccountCreator(bankClients));

        TServerTransport serverTransport = new TServerSocket(creationSocket);
        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

        TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(creatorProcessor));
        System.out.println("Starting the account creation server...");
        server.serve();
    }

    public static void initAccountManagers(BankClients bankClients) throws TTransportException {
        int creationSocket = 9998;

        BasicAccountService.Processor<BasicAccountManager> basicAccountManagerProcessor =
                new BasicAccountService.Processor<BasicAccountManager>(new BasicAccountManager(bankClients));


        TServerTransport serverTransport = new TServerSocket(creationSocket);
        TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

        TMultiplexedProcessor multiplex = new TMultiplexedProcessor();
        multiplex.registerProcessor("B", basicAccountManagerProcessor);

        TServer server = new TSimpleServer(new TServer.Args(serverTransport).protocolFactory(protocolFactory).processor(multiplex));
        System.out.println("Starting the account management server...");
        server.serve();
    }
}

package main.java;

import Bank.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

public class CreationClient {
    public static void main(String[] args) throws TException, IOException {
        String host = "localhost";
        int port = 9999;

        TProtocol protocol;
        TTransport transport;
        AccountCreationService.Client creationClient;
        BasicAccountService.Client accountClient;



        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport, true, true);
        accountClient = new BasicAccountService.Client(new TMultiplexedProtocol(protocol, "B"));
        creationClient = new AccountCreationService.Client(new TMultiplexedProtocol(protocol, "C"));
        transport.open();

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        do {

            System.out.print("==> ");
            System.out.flush();
            line = in.readLine();
            String firstname = "Michal";
            String lastname = "Dronski";
            String pesel = "123456789";
            long income = 10000;
            CreateAccountData createAccountData = new CreateAccountData(firstname, lastname, pesel, income);
            AccountData accountData = creationClient.createAccount(createAccountData);
            System.out.println(accountData.balance);
            System.out.println(accountData.guid);
            System.out.println(accountData.isPremium);

            long balance = accountClient.getBalance(accountData.guid);
            System.out.println(balance);
        }
        while (!line.equals("x"));

        transport.close();
    }

    public static AccountCreationService.Client initCreation() throws TTransportException {
        String host = "localhost";
        int port = 9999;

        TProtocol protocol;
        TTransport transport;
        AccountCreationService.Client creationClient;

        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport, true, true);
        creationClient = new AccountCreationService.Client(protocol);
        transport.open();
        return creationClient;
    }

    public static BasicAccountService.Client initBasicManager() throws TTransportException {
        String host = "localhost";
        int port = 9998;

        TProtocol protocol;
        TTransport transport;
        BasicAccountService.Client accountClient;

        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport, true, true);
        accountClient = new BasicAccountService.Client(new TMultiplexedProtocol(protocol, "B"));
        transport.open();
        return accountClient;
    }
}

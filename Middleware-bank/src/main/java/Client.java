import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import Bank.*;

public class Client {
    public static void main(String[] args) throws TException, IOException {
        String host = "localhost";
        int port = 9999;

        AccountCreationService.Client creationClient = initCreation();
        BasicAccountService.Client accountClient = initBasicManager();

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        System.out.println("C - create account\nB - get account balance\nL - loan request");

        do {

            System.out.print("==>");
            System.out.flush();
            line = in.readLine();

            switch (Character.toLowerCase(line.charAt(0))){
                case 'c':
                    createAccount(creationClient);
                    break;
                case 'b':
                    checkBalance(accountClient);
                    break;
                case 'l':
                    break;
                    default:
            }

        }
        while (!line.equals("x"));

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

    public static void createAccount(AccountCreationService.Client client) throws IOException, TException {
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        System.out.println("Enter Name");
        System.out.flush();
        String firstname = in.readLine();

        System.out.println("Enter Last Name");
        System.out.flush();
        String lastname = in.readLine();

        System.out.println("Enter PESEL");
        System.out.flush();
        String pesel = in.readLine();

        System.out.println("Enter income");
        System.out.flush();
        long income = Long.valueOf(in.readLine());

        CreateAccountData createAccountData = new CreateAccountData(firstname, lastname, pesel, income);
        AccountData accountData = client.createAccount(createAccountData);
        System.out.println("Created account:");
        System.out.println("Initial balance = " + accountData.balance);
        System.out.println("GUID = " + accountData.guid);
        System.out.println("Premium status = " + accountData.isPremium);
    }

    public static void checkBalance(BasicAccountService.Client client) throws IOException, TException {
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        System.out.print("Enter GUID: ");
        System.out.flush();
        String guid = in.readLine();

        try {
            long balance = client.getBalance(guid);
            System.out.println("Actual balance = " + balance);
        }catch (NotAuthorizedException e){
            System.out.println("Authorization error!");
        }
    }
}

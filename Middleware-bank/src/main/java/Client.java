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
    public static BasicAccountService.Client accountClient;
    public static PremiumAccountServie.Client loanClient;

    public static void main(String[] args) throws TException, IOException {

        AccountCreationService.Client creationClient = initCreation();
        initManagers();

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        System.out.println("C - create account\nB - get account balance\nL - loan request");

        do {

            System.out.print("==>");
            System.out.flush();
            line = in.readLine();

            switch (Character.toLowerCase(line.charAt(0))) {
                case 'c':
                    createAccount(creationClient);
                    break;
                case 'b':
                    checkBalance(accountClient);
                    break;
                case 'l':
                    requestLoan(loanClient);
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

    public static void initManagers() throws TTransportException {
        String host = "localhost";
        int port = 9998;

        TProtocol protocol;
        TTransport transport;

        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport, true, true);
        accountClient = new BasicAccountService.Client(new TMultiplexedProtocol(protocol, "B"));
        loanClient = new PremiumAccountServie.Client(new TMultiplexedProtocol(protocol, "L"));
        transport.open();
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
        } catch (NotAuthorizedException e) {
            System.out.println("Authorization error!");
        }
    }

    public static void requestLoan(PremiumAccountServie.Client client) throws IOException, TException {
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        try {
            System.out.print("Enter GUID: ");
            System.out.flush();
            String guid = in.readLine();

            System.out.print("Enter currency: ");
            System.out.flush();
            Currency currency;
            switch (in.readLine().toLowerCase()) {
                case "pln":
                    currency = Currency.PLN;
                    break;
                case "eur":
                    currency = Currency.EUR;
                    break;
                case "gbp":
                    currency = Currency.GBP;
                    break;
                case "usd":
                    currency = Currency.USD;
                    break;
                default:
                    return;
            }

            System.out.print("Enter Amount: ");
            System.out.flush();
            long amount = Long.valueOf(in.readLine());

            System.out.print("Enter time: ");
            System.out.flush();
            long time = Long.valueOf(in.readLine());

            try {
                LoanResponse response = client.requestLoan(guid, new LoanRequest(currency, amount, time));
                if (response.homeCurrencyCost == 0){
                    System.out.println("Loan not accepted by bank");
                    return;
                }

                System.out.println("Loan cost = " + response.homeCurrencyCost);
            } catch (NotAuthorizedException e) {
                System.out.println("Authorization error!");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}

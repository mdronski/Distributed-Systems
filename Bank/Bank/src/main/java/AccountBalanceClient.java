package main.java;

import Bank.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;

public class AccountBalanceClient {
    public static void main(String[] args) throws TException, IOException {
        String host = "localhost";
        int port = 9999;

        TProtocol protocol;
        TTransport transport;
        BasicAccountService.Client accountClient;


        transport = new TSocket(host, port);
        protocol = new TBinaryProtocol(transport, true, true);
        accountClient = new BasicAccountService.Client(new TMultiplexedProtocol(protocol, "B"));
        transport.open();

        String line = null;
        java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

        do {

            System.out.print("==> ");
            System.out.flush();
            line = in.readLine();
            long balance = -1;
            System.out.println(line);
            accountClient.getBalance(line);
            System.out.println(balance);

        }
        while (!line.equals("x"));

        transport.close();
    }
}

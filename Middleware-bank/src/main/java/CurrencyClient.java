import io.grpc.*;
import currency.*;
import currency.ExchangeServiceGrpc.ExchangeServiceBlockingStub;
import Bank.*;

import java.util.ArrayList;
import java.util.Arrays;

public class CurrencyClient {
    private String host = "localhost";
    private int port = 9997;
    private ManagedChannel channel;
    private ExchangeServiceBlockingStub blockingStub;
    private ExchangeRates exchangeRates;

    public CurrencyClient(ExchangeRates exchangeRates){
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        blockingStub = ExchangeServiceGrpc.newBlockingStub(channel);
        this.exchangeRates = exchangeRates;
    }

    public void start(){
        synchroniseRates();
        startListening();
    }


    public void synchroniseRates(){
        ArrayList<CurrencyOuterClass.Currency> allCurrencies = new ArrayList<>(Arrays.asList(CurrencyOuterClass.Currency.values()));
        allCurrencies.remove(allCurrencies.size()-1);
        CurrencyOuterClass.Request request = CurrencyOuterClass.Request.newBuilder().addAllCurrencies(allCurrencies).build();
        CurrencyOuterClass.Response response = blockingStub.actualRates(request);
        response.getRatesList().forEach(rate -> exchangeRates.setCurrencyRate(CurrencyConverter.grpcToThrift(rate.getCurrency()), rate.getRate()));
    }

    public void startListening(){
        ArrayList<CurrencyOuterClass.Currency> chosenCurrencies = new ArrayList<>();
        chosenCurrencies.add(CurrencyOuterClass.Currency.EUR);
        chosenCurrencies.add(CurrencyOuterClass.Currency.USD);


        new Thread(() -> {
            CurrencyOuterClass.Request request = CurrencyOuterClass.Request.newBuilder().addAllCurrencies(chosenCurrencies).build();
            blockingStub.ratesStream(request).forEachRemaining(rate -> {
                System.out.println(rate.getCurrency().toString() + ": " + rate.getRate());
                exchangeRates.setCurrencyRate(CurrencyConverter.grpcToThrift(rate.getCurrency()), rate.getRate());
            });
        }).start();

    }
}

import Bank.Currency;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRates {
    private Map<Currency, Double> rates = new HashMap<Currency, Double>();

    public ExchangeRates() {
        EnumSet.allOf(Currency.class).forEach(currency -> rates.put(currency, 1.0D));
    }

    public Double getCurrencyRate(Currency currency){
        return rates.get(currency);
    }
}

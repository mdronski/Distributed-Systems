import currency.CurrencyOuterClass;
import Bank.Currency;

public class CurrencyConverter {

    public static Currency grpcToThrift(CurrencyOuterClass.Currency currency){
        switch (currency){

            case EUR:
                return Currency.EUR;
            case USD:
                return Currency.USD;
            case GBP:
                return Currency.GBP;
            case PLN:
                return Currency.PLN;
            case UNRECOGNIZED:
                break;
        }
        return null;
    }
}

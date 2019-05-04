import Bank.LoanRequest;
import Bank.LoanResponse;
import Bank.NotAuthorizedException;
import Bank.PremiumAccountServie;
import org.apache.thrift.TException;

public class PremiumAccountService extends BasicAccountService implements PremiumAccountServie.Iface {
    @Override
    public LoanResponse requestLoan(long guid, LoanRequest request) throws NotAuthorizedException, TException {
        return null;
    }
}

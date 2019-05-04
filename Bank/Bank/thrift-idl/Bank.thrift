namespace java Bank

enum Currency {
    EUR,
    USD,
    GBP,
    PLN
}

struct CreateAccountData {
    1: string firstname,
    2: string lastname,
    3: string pesel
    4: i64 income
}

struct AccountData {
    1: i64 balance
    2: string guid
    3: bool isPremium
    4: CreateAccountData createData
}

struct LoanRequest {
    1: Currency currency,
    2: i64 amount,
    3: i64 time
}

struct LoanResponse {
    1: i64 homeCurrencyCost,
    2: i64 foreignCurrencyCost
}

exception NotAuthorizedException {
    1: string messgage
}

service AccountCreationService {
    AccountData createAccount(1: CreateAccountData data)
}

service BasicAccountService {
    i64 getBalance(1: string guid) throws(1: NotAuthorizedException e)
}

service PremiumAccountServie extends BasicAccountService{
    LoanResponse requestLoan(1: string guid, 2: LoanRequest request) throws(1: NotAuthorizedException e)
}
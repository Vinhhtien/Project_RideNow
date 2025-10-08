package service;

import dao.AccountDao;
import dao.IAccountDao;
import model.Account;

import java.util.Optional;

public class AccountService implements IAccountService {
    private final IAccountDao dao = new AccountDao();

    @Override
    public Optional<Account> login(String usernameOrEmail, String password) throws Exception {
        return dao.login(usernameOrEmail, password);
    }

    @Override
    public Optional<Account> findByUsername(String username) throws Exception {
        return dao.findByUsername(username);
    }
}

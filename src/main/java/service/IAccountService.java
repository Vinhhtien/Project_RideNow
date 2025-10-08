package service;

import model.Account;
import java.util.Optional;

public interface IAccountService {
    Optional<Account> login(String username, String password) throws Exception;
    Optional<Account> findByUsername(String username) throws Exception;
}

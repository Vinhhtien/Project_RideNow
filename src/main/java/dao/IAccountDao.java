package dao;

import java.util.Optional;

import model.Account;

public interface IAccountDao {
    Optional<Account> login(String username, String password) throws Exception;

    Optional<Account> findByUsername(String username) throws Exception;
}

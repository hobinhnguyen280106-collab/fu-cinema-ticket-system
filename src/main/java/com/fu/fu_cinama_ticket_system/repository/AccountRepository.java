package com.fu.fu_cinama_ticket_system.repository;

import com.fu.fu_cinama_ticket_system.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndAccountIdNot(String username, Long accountId);
}

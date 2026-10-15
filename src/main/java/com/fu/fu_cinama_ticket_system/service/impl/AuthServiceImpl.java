package com.fu.fu_cinama_ticket_system.service.impl;

import com.fu.fu_cinama_ticket_system.dto.RegisterForm;
import com.fu.fu_cinama_ticket_system.dto.SessionUser;
import com.fu.fu_cinama_ticket_system.entity.Account;
import com.fu.fu_cinama_ticket_system.entity.Customer;
import com.fu.fu_cinama_ticket_system.enums.AccountStatus;
import com.fu.fu_cinama_ticket_system.enums.Role;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.repository.AccountRepository;
import com.fu.fu_cinama_ticket_system.repository.CustomerRepository;
import com.fu.fu_cinama_ticket_system.service.AuthService;
import com.fu.fu_cinama_ticket_system.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final int MIN_REGISTER_AGE = 13;

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public SessionUser login(String username, String password) {
        Account account = accountRepository.findByUsername(username.trim().toLowerCase())
                .filter(a -> passwordEncoder.matches(password, a.getPassword()))
                .orElseThrow(() -> new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng"));

        if (account.getStatus() == AccountStatus.LOCKED) {                       // BR-03
            throw new BusinessException("Tài khoản đã bị khoá. Vui lòng liên hệ quản trị viên");
        }

        if (account.getRole() == Role.ADMIN) {
            return new SessionUser(account.getAccountId(), null, "Administrator", Role.ADMIN);
        }
        Customer customer = customerRepository.findByAccount_AccountId(account.getAccountId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ khách hàng"));
        return new SessionUser(account.getAccountId(), customer.getCustomerId(), customer.getFullName(), Role.CUSTOMER);
    }

    @Override
    @Transactional                                                                // Account + Customer cùng 1 transaction
    public SessionUser register(RegisterForm form) {
        String email = form.getEmail().trim().toLowerCase();

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu nhập lại không khớp");
        }
        if (ValidationUtils.age(form.getBirthday()) < MIN_REGISTER_AGE) {            // BR-02
            throw new BusinessException("Bạn phải từ đủ " + MIN_REGISTER_AGE + " tuổi để đăng ký");
        }
        if (accountRepository.existsByUsername(email) || customerRepository.existsByEmail(email)) {   // BR-01
            throw new BusinessException("Email đã được sử dụng");
        }
        if (customerRepository.existsByMobile(form.getMobile())) {
            throw new BusinessException("Số điện thoại đã được sử dụng");
        }
        if (customerRepository.existsByIdentityCard(form.getIdentityCard())) {
            throw new BusinessException("Số CCCD đã được sử dụng");
        }

        Account account = accountRepository.save(new Account(
                email, passwordEncoder.encode(form.getPassword()), Role.CUSTOMER, AccountStatus.ACTIVE));

        Customer customer = new Customer();
        customer.setFullName(form.getFullName().trim());
        customer.setEmail(email);
        customer.setMobile(form.getMobile());
        customer.setBirthday(form.getBirthday());
        customer.setIdentityCard(form.getIdentityCard());
        customer.setAccount(account);
        customerRepository.save(customer);

        return new SessionUser(account.getAccountId(), customer.getCustomerId(), customer.getFullName(), Role.CUSTOMER);
    }
}

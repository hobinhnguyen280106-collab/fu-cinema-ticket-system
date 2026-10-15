package com.fu.fu_cinama_ticket_system.dto;

import com.fu.fu_cinama_ticket_system.enums.Role;
import lombok.Value;

import java.io.Serializable;

@Value
public class SessionUser implements Serializable {

    Long accountId;
    Long customerId;          // null nếu là ADMIN
    String displayName;
    Role role;

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }
}

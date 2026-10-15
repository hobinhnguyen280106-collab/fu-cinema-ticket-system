package com.fu.fu_cinama_ticket_system.util;

import com.fu.fu_cinama_ticket_system.dto.RegisterForm;
import com.fu.fu_cinama_ticket_system.dto.SessionUser;

public interface AuthService {

    SessionUser login(String username, String password);

    SessionUser register(RegisterForm form);
}

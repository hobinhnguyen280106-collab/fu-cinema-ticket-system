package com.fu.fu_cinama_ticket_system.util;

import com.fu.fu_cinama_ticket_system.dto.SessionUser;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    public static final String CURRENT_USER = "currentUser";

    private SessionUtils() {
    }

    public static SessionUser getCurrentUser(HttpSession session) {
        return session == null ? null : (SessionUser) session.getAttribute(CURRENT_USER);
    }

    /** Luôn lấy customerId từ session, KHÔNG lấy từ form/URL. */
    public static Long getCurrentCustomerId(HttpSession session) {
        SessionUser user = getCurrentUser(session);
        return user == null ? null : user.getCustomerId();
    }
}

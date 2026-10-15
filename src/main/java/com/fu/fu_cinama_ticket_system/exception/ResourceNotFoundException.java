package com.fu.fu_cinama_ticket_system.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " không tồn tại (id = " + id + ")");
    }
}

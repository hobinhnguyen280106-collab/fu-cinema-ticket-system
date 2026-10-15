package com.fu.fu_cinama_ticket_system.service;

import com.fu.fu_cinama_ticket_system.dto.RoomForm;
import com.fu.fu_cinama_ticket_system.entity.Room;

import java.util.List;

public interface RoomService {

    List<Room> findAll();

    List<Room> findActive();

    RoomForm getForm(Long roomId);

    void create(RoomForm form);

    void update(Long roomId, RoomForm form);

    /** @return true nếu đã xoá hẳn, false nếu chỉ chuyển MAINTENANCE */
    boolean delete(Long roomId);
}
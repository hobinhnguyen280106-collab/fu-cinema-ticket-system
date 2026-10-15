package com.fu.fu_cinama_ticket_system.service.impl;

import com.fu.fu_cinama_ticket_system.dto.RoomForm;
import com.fu.fu_cinama_ticket_system.entity.Room;
import com.fu.fu_cinama_ticket_system.enums.RoomStatus;
import com.fu.fu_cinama_ticket_system.exception.BusinessException;
import com.fu.fu_cinama_ticket_system.exception.ResourceNotFoundException;
import com.fu.fu_cinama_ticket_system.repository.RoomRepository;
import com.fu.fu_cinama_ticket_system.repository.ShowtimeRepository;
import com.fu.fu_cinama_ticket_system.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll(Sort.by("roomName"));
    }

    @Override
    public List<Room> findActive() {
        return roomRepository.findByStatusOrderByRoomNameAsc(RoomStatus.ACTIVE);
    }

    @Override
    public RoomForm getForm(Long roomId) {
        Room r = find(roomId);
        RoomForm f = new RoomForm();
        f.setRoomId(r.getRoomId());
        f.setRoomName(r.getRoomName());
        f.setRoomType(r.getRoomType());
        f.setTotalRows(r.getTotalRows());
        f.setSeatsPerRow(r.getSeatsPerRow());
        f.setStatus(r.getStatus());
        return f;
    }

    @Override
    @Transactional
    public void create(RoomForm form) {
        if (roomRepository.existsByRoomNameIgnoreCase(form.getRoomName().trim())) {
            throw new BusinessException("Tên phòng đã tồn tại");
        }
        Room room = new Room();
        room.setRoomName(form.getRoomName().trim());
        room.setRoomType(form.getRoomType());
        room.setTotalRows(form.getTotalRows());
        room.setSeatsPerRow(form.getSeatsPerRow());
        room.setStatus(form.getStatus());
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void update(Long roomId, RoomForm form) {
        Room room = find(roomId);
        if (roomRepository.existsByRoomNameIgnoreCaseAndRoomIdNot(form.getRoomName().trim(), roomId)) {
            throw new BusinessException("Tên phòng đã tồn tại");
        }
        boolean layoutChanged = !room.getTotalRows().equals(form.getTotalRows())
                || !room.getSeatsPerRow().equals(form.getSeatsPerRow());
        if (layoutChanged && showtimeRepository.existsByRoom_RoomId(roomId)) {
            throw new BusinessException("Không thể đổi sơ đồ ghế của phòng đã có suất chiếu");
        }
        room.setRoomName(form.getRoomName().trim());
        room.setRoomType(form.getRoomType());
        room.setTotalRows(form.getTotalRows());
        room.setSeatsPerRow(form.getSeatsPerRow());
        room.setStatus(form.getStatus());
    }

    @Override
    @Transactional
    public boolean delete(Long roomId) {
        Room room = find(roomId);
        if (showtimeRepository.existsByRoom_RoomId(roomId)) {
            room.setStatus(RoomStatus.MAINTENANCE);
            return false;
        }
        roomRepository.delete(room);
        return true;
    }

    private Room find(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng chiếu", id));
    }
}

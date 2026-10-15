package com.fu.fu_cinama_ticket_system.dto;

import com.fu.fu_cinama_ticket_system.enums.RoomStatus;
import com.fu.fu_cinama_ticket_system.enums.RoomType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomForm {

    private Long roomId;

    @NotBlank(message = "Tên phòng không được để trống")
    @Size(max = 50, message = "Tên phòng tối đa 50 ký tự")
    private String roomName;

    @NotNull(message = "Vui lòng chọn loại phòng")
    private RoomType roomType;

    @NotNull(message = "Số hàng không được để trống")
    @Min(value = 1, message = "Số hàng từ 1 đến 26")
    @Max(value = 26, message = "Số hàng từ 1 đến 26")
    private Integer totalRows;

    @NotNull(message = "Số ghế mỗi hàng không được để trống")
    @Min(value = 1, message = "Số ghế mỗi hàng từ 1 đến 30")
    @Max(value = 30, message = "Số ghế mỗi hàng từ 1 đến 30")
    private Integer seatsPerRow;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private RoomStatus status;
}

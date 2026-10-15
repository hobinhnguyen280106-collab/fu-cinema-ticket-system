@'
## 📌 Tóm tắt thay đổi
Mô tả ngắn gọn những gì đã được chỉnh sửa hoặc bổ sung trong PR này.

## 🔗 Liên kết Issue
- Closes #
- Refs #1

## 🛠 Những thay đổi chính
- [ ] Thêm mới / cập nhật tính năng
- [ ] Sửa lỗi (Bug fix)
- [ ] Cấu hình / Tài liệu (Chore/Docs)

## ✅ Checklist kiểm tra
- [ ] Mã nguồn đã được build thành công ở local (`./mvnw clean compile`)
- [ ] Đã chạy test và pass
- [ ] Không chứa thông tin nhạy cảm (mật khẩu, database credentials)
  '@ | Set-Content -Path ".github\pull_request_template.md" -Encoding UTF8
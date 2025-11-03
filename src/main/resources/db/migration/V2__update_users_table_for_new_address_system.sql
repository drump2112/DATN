-- Migration script để cập nhật bảng Users sử dụng địa chỉ mới
-- File: V2__update_users_table_for_new_address_system.sql

-- Thêm cột AddressID vào bảng Users
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Users' AND COLUMN_NAME = 'AddressID')
BEGIN
    ALTER TABLE Users ADD AddressID INT NULL;
END

-- Tạo index cho AddressID
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Users_AddressID')
    CREATE INDEX IX_Users_AddressID ON Users(AddressID);

-- Nếu muốn xóa cột Address cũ (chạy sau khi đã migrate dữ liệu)
-- ALTER TABLE Users DROP COLUMN Address;
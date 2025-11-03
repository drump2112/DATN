-- Reset và tạo lại toàn bộ hệ thống địa chỉ
-- File: V6__reset_and_recreate_address_system.sql

-- 1️⃣ Gỡ constraint nếu tồn tại
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
           WHERE CONSTRAINT_NAME = 'FK_Users_Address' AND TABLE_NAME = 'Users')
BEGIN
    ALTER TABLE Users DROP CONSTRAINT FK_Users_Address;
END;

-- 2️⃣ Xóa index nếu đã tồn tại (tránh lỗi khi tạo lại)
IF EXISTS (SELECT name FROM sys.indexes WHERE name = 'IX_Users_AddressID' AND object_id = OBJECT_ID('dbo.Users'))
BEGIN
    DROP INDEX IX_Users_AddressID ON dbo.Users;
END;

-- 3️⃣ Xóa các bảng cũ (nếu có)
IF OBJECT_ID('dbo.Addresses', 'U') IS NOT NULL DROP TABLE dbo.Addresses;
IF OBJECT_ID('dbo.Communes', 'U') IS NOT NULL DROP TABLE dbo.Communes;
IF OBJECT_ID('dbo.Provinces', 'U') IS NOT NULL DROP TABLE dbo.Provinces;
IF OBJECT_ID('dbo.AddressBackup', 'U') IS NOT NULL DROP TABLE dbo.AddressBackup;

-- 4️⃣ Tạo lại bảng Provinces
CREATE TABLE Provinces (
    ProvinceCode VARCHAR(2) PRIMARY KEY,
    ProvinceName NVARCHAR(100) NOT NULL,
    ProvinceNameEn VARCHAR(100),
    ProvinceFullName NVARCHAR(150),
    ProvinceFullNameEn VARCHAR(150),
    CodeName VARCHAR(50)
);

-- 5️⃣ Tạo lại bảng Communes
CREATE TABLE Communes (
    CommuneCode VARCHAR(5) PRIMARY KEY,
    CommuneName NVARCHAR(100) NOT NULL,
    CommuneNameEn VARCHAR(100),
    CommuneFullName NVARCHAR(150),
    CommuneFullNameEn VARCHAR(150),
    CodeName VARCHAR(50),
    ProvinceCode VARCHAR(2) NOT NULL,
    CONSTRAINT FK_Communes_Province
        FOREIGN KEY (ProvinceCode)
        REFERENCES Provinces(ProvinceCode)
);

-- 6️⃣ Tạo lại bảng Addresses
CREATE TABLE Addresses (
    id INT IDENTITY(1,1) PRIMARY KEY,
    SpecificAddress NVARCHAR(500),
    CommuneCode VARCHAR(5),
    ProvinceCode VARCHAR(2),
    FullAddress NVARCHAR(1000),
    IsDefault BIT DEFAULT 0,
    IsActive BIT DEFAULT 1,
    CONSTRAINT FK_Addresses_Commune
        FOREIGN KEY (CommuneCode)
        REFERENCES Communes(CommuneCode),
    CONSTRAINT FK_Addresses_Province
        FOREIGN KEY (ProvinceCode)
        REFERENCES Provinces(ProvinceCode)
);

-- 7️⃣ Thêm cột AddressID vào Users nếu chưa có
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Users' AND COLUMN_NAME = 'AddressID')
BEGIN
    ALTER TABLE Users ADD AddressID INT NULL;
END;

-- 8️⃣ Thêm foreign key cho Users
ALTER TABLE Users
ADD CONSTRAINT FK_Users_Address
    FOREIGN KEY (AddressID)
    REFERENCES Addresses(id);

-- 9️⃣ Tạo các index
CREATE INDEX IX_Communes_ProvinceCode ON Communes(ProvinceCode);
CREATE INDEX IX_Addresses_CommuneCode ON Addresses(CommuneCode);
CREATE INDEX IX_Addresses_ProvinceCode ON Addresses(ProvinceCode);
CREATE INDEX IX_Addresses_IsActive ON Addresses(IsActive);
CREATE INDEX IX_Users_AddressID ON Users(AddressID);

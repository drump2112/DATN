-- Script để tạo hệ thống địa chỉ 2 cấp
-- Chạy thủ công trong SQL Server Management Studio

-- Xóa bảng cũ nếu có
DROP TABLE IF EXISTS Addresses;
DROP TABLE IF EXISTS Communes;
DROP TABLE IF EXISTS Provinces;

-- Tạo bảng Provinces
CREATE TABLE Provinces (
    ProvinceCode VARCHAR(2) PRIMARY KEY,
    ProvinceName NVARCHAR(100) NOT NULL,
    ProvinceNameEn VARCHAR(100),
    ProvinceFullName NVARCHAR(150),
    ProvinceFullNameEn VARCHAR(150),
    CodeName VARCHAR(50)
);

-- Tạo bảng Communes
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

-- Tạo bảng Addresses
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

-- Thêm cột AddressID vào Users
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Users' AND COLUMN_NAME = 'AddressID')
BEGIN
    ALTER TABLE Users ADD AddressID INT NULL;
END

-- Tạo foreign key constraint cho Users
ALTER TABLE Users
ADD CONSTRAINT FK_Users_Address
    FOREIGN KEY (AddressID)
    REFERENCES Addresses(id);

-- Tạo các index
CREATE INDEX IX_Communes_ProvinceCode ON Communes(ProvinceCode);
CREATE INDEX IX_Addresses_CommuneCode ON Addresses(CommuneCode);
CREATE INDEX IX_Addresses_ProvinceCode ON Addresses(ProvinceCode);
CREATE INDEX IX_Addresses_IsActive ON Addresses(IsActive);
CREATE INDEX IX_Users_AddressID ON Users(AddressID);
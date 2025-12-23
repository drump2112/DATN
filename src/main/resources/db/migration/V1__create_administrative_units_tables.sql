-- Migration script để tạo bảng đơn vị hành chính 2 cấp mới
-- File: V1__create_administrative_units_tables.sql

-- Tạo bảng Provinces (Tỉnh/Thành phố)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Provinces')
BEGIN
    CREATE TABLE Provinces (
        ProvinceCode VARCHAR(2) PRIMARY KEY,
        ProvinceName NVARCHAR(100) NOT NULL,
        ProvinceNameEn VARCHAR(100),
        ProvinceFullName NVARCHAR(150),
        ProvinceFullNameEn VARCHAR(150),
        CodeName VARCHAR(50)
    );
END

-- Tạo bảng Communes (Phường/Xã)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Communes')
BEGIN
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
END

-- Tạo bảng Addresses mới
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Addresses')
BEGIN
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
END

-- Tạo index để tăng hiệu suất truy vấn
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Communes_ProvinceCode')
    CREATE INDEX IX_Communes_ProvinceCode ON Communes(ProvinceCode);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Addresses_CommuneCode')
    CREATE INDEX IX_Addresses_CommuneCode ON Addresses(CommuneCode);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Addresses_ProvinceCode')
    CREATE INDEX IX_Addresses_ProvinceCode ON Addresses(ProvinceCode);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Addresses_IsActive')
    CREATE INDEX IX_Addresses_IsActive ON Addresses(IsActive);

-- Backup dữ liệu địa chỉ cũ (nếu có)
-- Tạo bảng tạm để lưu trữ địa chỉ cũ
IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
           WHERE TABLE_NAME = 'Users' AND COLUMN_NAME = 'Address')
BEGIN
    IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AddressBackup')
    BEGIN
        CREATE TABLE AddressBackup (
            UserId INT,
            OldAddress NVARCHAR(MAX)
        );
    END

    INSERT INTO AddressBackup (UserId, OldAddress)
    SELECT id, Address FROM Users WHERE Address IS NOT NULL;
END
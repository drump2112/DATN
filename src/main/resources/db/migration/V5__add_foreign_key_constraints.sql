-- Migration script để thêm foreign key constraints
-- File: V5__add_foreign_key_constraints.sql

-- Thêm foreign key constraint cho Users.AddressID -> Addresses.id
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS
               WHERE CONSTRAINT_NAME = 'FK_Users_Address' AND TABLE_NAME = 'Users')
BEGIN
    ALTER TABLE Users
    ADD CONSTRAINT FK_Users_Address
        FOREIGN KEY (AddressID)
        REFERENCES Addresses(id);
END
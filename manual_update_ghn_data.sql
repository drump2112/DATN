-- Script update thủ công GHN Ward Code và District ID cho các địa chỉ quan trọng
-- Chạy script này nếu không muốn dùng tool sync tự động

-- ================================
-- HÀ NỘI (Province ID: 201)
-- ================================

-- Quận Cầu Giấy, Hà Nội (District ID: 1454)
UPDATE Communes
SET GHNWardCode = '21012', GHNDistrictId = 1454
WHERE CommuneName LIKE N'%Dịch Vọng%' AND ProvinceCode = '01';

UPDATE Communes
SET GHNWardCode = '21010', GHNDistrictId = 1454
WHERE CommuneName LIKE N'%Mai Dịch%' AND ProvinceCode = '01';

-- Quận Ba Đình, Hà Nội (District ID: 1452)
UPDATE Communes
SET GHNWardCode = '11005', GHNDistrictId = 1452
WHERE CommuneName LIKE N'%Ngọc Hà%' AND ProvinceCode = '01';

UPDATE Communes
SET GHNWardCode = '11001', GHNDistrictId = 1452
WHERE CommuneName LIKE N'%Cống Vị%' AND ProvinceCode = '01';

-- ================================
-- HỒ CHÍ MINH (Province ID: 202)
-- ================================

-- Quận 1, TP.HCM (District ID: 1442)
UPDATE Communes
SET GHNWardCode = '20801', GHNDistrictId = 1442
WHERE CommuneName LIKE N'%Bến Nghé%' AND ProvinceCode = '79';

UPDATE Communes
SET GHNWardCode = '20809', GHNDistrictId = 1442
WHERE CommuneName LIKE N'%Bến Thành%' AND ProvinceCode = '79';

-- Quận 3, TP.HCM (District ID: 1443)
UPDATE Communes
SET GHNWardCode = '20301', GHNDistrictId = 1443
WHERE CommuneName LIKE N'%Võ Thị Sáu%' AND ProvinceCode = '79';

-- ================================
-- AN GIANG (Province ID: 217)
-- ================================

-- Thành phố Long Xuyên (District ID: 1701)
UPDATE Communes
SET GHNWardCode = '440101', GHNDistrictId = 1701
WHERE CommuneName LIKE N'%Mỹ Bình%' AND ProvinceCode = '91';

UPDATE Communes
SET GHNWardCode = '440102', GHNDistrictId = 1701
WHERE CommuneName LIKE N'%Mỹ Long%' AND ProvinceCode = '91';

UPDATE Communes
SET GHNWardCode = '440103', GHNDistrictId = 1701
WHERE CommuneName LIKE N'%Đông Xuyên%' AND ProvinceCode = '91';

UPDATE Communes
SET GHNWardCode = '440104', GHNDistrictId = 1701
WHERE CommuneName LIKE N'%Mỹ Phước%' AND ProvinceCode = '91';

-- Thành phố Châu Đốc (District ID: 1702)
UPDATE Communes
SET GHNWardCode = '440201', GHNDistrictId = 1702
WHERE CommuneName LIKE N'%Châu Phú A%' AND ProvinceCode = '91';

UPDATE Communes
SET GHNWardCode = '440202', GHNDistrictId = 1702
WHERE CommuneName LIKE N'%Châu Phú B%' AND ProvinceCode = '91';

-- ================================
-- ĐÀ NẴNG (Province ID: 203)
-- ================================

-- Quận Hải Châu (District ID: 1490)
UPDATE Communes
SET GHNWardCode = '10109', GHNDistrictId = 1490
WHERE CommuneName LIKE N'%Thanh Bình%' AND ProvinceCode = '48';

UPDATE Communes
SET GHNWardCode = '10101', GHNDistrictId = 1490
WHERE CommuneName LIKE N'%Thạch Thang%' AND ProvinceCode = '48';

-- ================================
-- CẦN THƠ (Province ID: 227)
-- ================================

-- Quận Ninh Kiều (District ID: 1580)
UPDATE Communes
SET GHNWardCode = '420101', GHNDistrictId = 1580
WHERE CommuneName LIKE N'%Cái Khế%' AND ProvinceCode = '92';

UPDATE Communes
SET GHNWardCode = '420102', GHNDistrictId = 1580
WHERE CommuneName LIKE N'%An Hòa%' AND ProvinceCode = '92';

-- Kiểm tra kết quả
SELECT
    CommuneCode,
    CommuneName,
    GHNWardCode,
    GHNDistrictId,
    ProvinceCode
FROM Communes
WHERE GHNWardCode IS NOT NULL
ORDER BY ProvinceCode, CommuneName;

-- Đếm số lượng đã update
SELECT
    COUNT(*) as TotalUpdated,
    COUNT(CASE WHEN GHNWardCode IS NOT NULL THEN 1 END) as HasGHNData,
    COUNT(CASE WHEN GHNWardCode IS NULL THEN 1 END) as MissingGHNData
FROM Communes;

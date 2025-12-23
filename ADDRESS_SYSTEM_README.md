# Hệ thống Địa chỉ 2 cấp mới - Đơn vị hành chính Việt Nam

## Tổng quan
Hệ thống địa chỉ mới được thiết kế theo đơn vị hành chính 2 cấp của Việt Nam:
1. **Tỉnh/Thành phố trực thuộc trung ương** (Province)
2. **Phường/Xã/Thị trấn** (Commune)

## Cấu trúc Database

### 1. Bảng Provinces (Tỉnh/Thành phố)
- `ProvinceCode`: Mã tỉnh (VARCHAR(2)) - Primary Key
- `ProvinceName`: Tên tỉnh (NVARCHAR(100))
- `ProvinceNameEn`: Tên tiếng Anh
- `ProvinceFullName`: Tên đầy đủ (VD: "Thành phố Hà Nội")
- `ProvinceFullNameEn`: Tên đầy đủ tiếng Anh
- `CodeName`: Tên code (VD: "ha_noi")

### 2. Bảng Communes (Phường/Xã)
- `CommuneCode`: Mã phường/xã (VARCHAR(5)) - Primary Key
- `CommuneName`: Tên phường/xã (NVARCHAR(100))
- `CommuneNameEn`: Tên tiếng Anh
- `CommuneFullName`: Tên đầy đủ (VD: "Phường Trúc Bạch")
- `CommuneFullNameEn`: Tên đầy đủ tiếng Anh
- `CodeName`: Tên code
- `ProvinceCode`: Mã tỉnh (Foreign Key)

### 3. Bảng Addresses (Địa chỉ chi tiết)
- `id`: ID tự tăng - Primary Key
- `SpecificAddress`: Địa chỉ cụ thể (số nhà, tên đường)
- `CommuneCode`: Mã phường/xã (Foreign Key)
- `ProvinceCode`: Mã tỉnh (Foreign Key)
- `FullAddress`: Địa chỉ đầy đủ (tự động tạo)
- `IsDefault`: Địa chỉ mặc định
- `IsActive`: Trạng thái hoạt động

## API Endpoints

### Provinces
- `GET /api/provinces` - Lấy danh sách tất cả tỉnh/thành phố
- `GET /api/provinces/{provinceCode}` - Lấy thông tin tỉnh theo mã
- `GET /api/provinces/search?name={name}` - Tìm kiếm tỉnh theo tên

### Communes
- `GET /api/communes?provinceCode={provinceCode}` - Lấy danh sách phường/xã theo tỉnh
- `GET /api/communes/{communeCode}` - Lấy thông tin phường/xã theo mã
- `GET /api/communes/search?name={name}` - Tìm kiếm phường/xã theo tên

### Addresses
- `POST /api/addresses` - Tạo địa chỉ mới
- `PUT /api/addresses/{addressId}` - Cập nhật địa chỉ
- `GET /api/addresses/{addressId}` - Lấy thông tin địa chỉ
- `GET /api/addresses` - Lấy danh sách địa chỉ
- `DELETE /api/addresses/{addressId}` - Xóa địa chỉ (soft delete)
- `GET /api/addresses/search?keyword={keyword}` - Tìm kiếm địa chỉ

## Cách sử dụng

### 1. Tạo địa chỉ mới
```http
POST /api/addresses
Content-Type: application/x-www-form-urlencoded

specificAddress=123 Đường ABC&communeCode=00001&provinceCode=01
```

### 2. Lấy danh sách phường/xã theo tỉnh
```http
GET /api/communes?provinceCode=01
```

### 3. Tìm kiếm tỉnh
```http
GET /api/provinces/search?name=Hà Nội
```

## Migration Database

### 1. Chạy migration tạo bảng mới
```sql
-- Chạy file V1__create_administrative_units_tables.sql
```

### 2. Import dữ liệu tỉnh/thành phố
```sql
-- Chạy file provinces_data.sql
```

### 3. Import dữ liệu phường/xã (mẫu)
```sql
-- Chạy file communes_sample_data.sql
```

### 4. Cập nhật bảng Users
```sql
-- Chạy file V2__update_users_table_for_new_address_system.sql
```

## Lưu ý quan trọng

1. **Backup dữ liệu**: Trước khi migration, hãy backup dữ liệu địa chỉ cũ
2. **Dữ liệu phường/xã**: File mẫu chỉ có một số phường/xã, cần import đầy đủ từ nguồn chính thức
3. **Soft Delete**: Địa chỉ bị xóa chỉ set `IsActive = false`, không xóa vật lý
4. **Full Address**: Được tự động tạo khi lưu/cập nhật địa chỉ

## Nguồn dữ liệu
- Dữ liệu được chuẩn hóa theo Nghị định 63/2020/NĐ-CP về đơn vị hành chính
- Có thể tải dữ liệu đầy đủ từ: https://danhmuchanhchinh.gso.gov.vn/

## Model Classes
- `Province.java` - Model cho tỉnh/thành phố
- `Commune.java` - Model cho phường/xã
- `Address.java` - Model cho địa chỉ chi tiết
- `User.java` - Đã cập nhật sử dụng `Address` entity

## Service Classes
- `AddressService.java` - Service xử lý logic địa chỉ

## Repository Classes
- `ProvinceRepository.java` - Repository cho tỉnh/thành phố
- `CommuneRepository.java` - Repository cho phường/xã
- `AddressRepository.java` - Repository cho địa chỉ

## DTO Classes
- `AddressDTO.java` - DTO cho địa chỉ
- `CreateAddressRequestDTO.java` - DTO cho request tạo địa chỉ mới
- `SimpleDTO.java` - DTO đơn giản cho dropdown/select
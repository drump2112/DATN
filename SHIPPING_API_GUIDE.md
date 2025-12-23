# Hướng dẫn sử dụng API giao hàng

## Tổng quan
Sau khi cập nhật hệ thống địa chỉ 2 cấp, bạn có thể lấy mã tỉnh và mã phường/xã từ bảng User thông qua các API endpoint mới.

## API Endpoints cho thông tin giao hàng

### 1. Lấy thông tin giao hàng đầy đủ
```http
GET /api/shipping/user/{userId}
```

**Response:**
```json
{
  "userId": 1,
  "userFullName": "Nguyễn Văn A",
  "userPhone": "0123456789",
  "addressId": 10,
  "specificAddress": "123 Đường Nguyễn Du",
  "fullAddress": "123 Đường Nguyễn Du, Phường Phúc Xá, Thành phố Hà Nội",
  "provinceCode": "01",
  "provinceName": "Hà Nội",
  "communeCode": "00001",
  "communeName": "Phúc Xá",
  "provinceFullName": "Thành phố Hà Nội",
  "communeFullName": "Phường Phúc Xá"
}
```

### 2. Lấy chỉ mã tỉnh (để gọi API giao hàng)
```http
GET /api/shipping/user/{userId}/province-code
```

**Response:**
```
01
```

### 3. Lấy chỉ mã phường/xã (để gọi API giao hàng)
```http
GET /api/shipping/user/{userId}/commune-code
```

**Response:**
```
00001
```

### 4. Kiểm tra địa chỉ đầy đủ
```http
GET /api/shipping/user/{userId}/complete-address-check
```

**Response:**
```json
true
```

### 5. Cập nhật địa chỉ cho người dùng
```http
POST /api/shipping/user/{userId}/update-address?addressId={addressId}
```

## Ví dụ sử dụng với API giao hàng

### Với GHN (Giao Hàng Nhanh)
```java
// Lấy thông tin từ User
ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);

// Tạo request cho GHN API
GHNCreateOrderRequest ghnRequest = GHNCreateOrderRequest.builder()
    .to_name(shippingInfo.getUserFullName())
    .to_phone(shippingInfo.getUserPhone())
    .to_address(shippingInfo.getSpecificAddress())
    .to_province_id(convertProvinceCodeToGHNId(shippingInfo.getProvinceCode()))
    .to_district_id(convertCommuneCodeToGHNId(shippingInfo.getCommuneCode()))
    .build();
```

### Với VTP (Viettel Post)
```java
// Lấy thông tin từ User
ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);

// Tạo request cho VTP API
VTPCreateOrderRequest vtpRequest = VTPCreateOrderRequest.builder()
    .RECEIVER_FULLNAME(shippingInfo.getUserFullName())
    .RECEIVER_PHONE(shippingInfo.getUserPhone())
    .RECEIVER_ADDRESS(shippingInfo.getSpecificAddress())
    .RECEIVER_PROVINCE(shippingInfo.getProvinceCode())
    .RECEIVER_DISTRICT(shippingInfo.getCommuneCode())
    .build();
```

### Với J&T Express
```java
// Lấy thông tin từ User
ShippingInfoDTO shippingInfo = getUserShippingInfo(userId);

// Tạo request cho J&T API
JTCreateOrderRequest jtRequest = JTCreateOrderRequest.builder()
    .receiver_name(shippingInfo.getUserFullName())
    .receiver_phone(shippingInfo.getUserPhone())
    .receiver_address(shippingInfo.getSpecificAddress())
    .receiver_city(shippingInfo.getProvinceCode())
    .receiver_area(shippingInfo.getCommuneCode())
    .build();
```

## Service Methods

### UserShippingService Methods:

1. **getUserShippingInfo(Integer userId)** - Lấy thông tin đầy đủ
2. **getUserProvinceCode(Integer userId)** - Chỉ lấy mã tỉnh
3. **getUserCommuneCode(Integer userId)** - Chỉ lấy mã phường/xã
4. **hasCompleteAddress(Integer userId)** - Kiểm tra địa chỉ đầy đủ
5. **updateUserAddress(Integer userId, Integer addressId)** - Cập nhật địa chỉ

## Mapping Code với API giao hàng

Bạn cần tạo các hàm mapping để chuyển đổi mã tỉnh/phường/xã từ hệ thống sang mã của từng nhà vận chuyển:

```java
@Service
public class ShippingCodeMappingService {

    // Mapping mã tỉnh sang GHN
    public Integer convertProvinceCodeToGHNId(String provinceCode) {
        Map<String, Integer> mapping = Map.of(
            "01", 269, // Hà Nội
            "79", 202, // TP.HCM
            "48", 291  // Đà Nẵng
            // ... thêm các tỉnh khác
        );
        return mapping.get(provinceCode);
    }

    // Mapping mã phường/xã sang GHN
    public Integer convertCommuneCodeToGHNId(String communeCode) {
        // Logic mapping phường/xã
        // ...
    }

    // Tương tự cho VTP, J&T...
}
```

## Lưu ý quan trọng

1. **Mã tỉnh**: Sử dụng mã 2 số theo quy chuẩn (01, 79, 48...)
2. **Mã phường/xã**: Sử dụng mã 5 số theo quy chuẩn (00001, 26734...)
3. **Validation**: Luôn kiểm tra `hasCompleteAddress()` trước khi tạo đơn hàng
4. **Mapping**: Cần tạo bảng mapping giữa mã hệ thống và mã của từng nhà vận chuyển
5. **Cache**: Có thể cache thông tin địa chỉ để tăng performance

## Frontend Usage

### JavaScript/AJAX
```javascript
// Lấy thông tin giao hàng
fetch(`/api/shipping/user/${userId}`)
  .then(response => response.json())
  .then(data => {
    console.log('Province Code:', data.provinceCode);
    console.log('Commune Code:', data.communeCode);
    console.log('Full Address:', data.fullAddress);
  });

// Kiểm tra địa chỉ đầy đủ
fetch(`/api/shipping/user/${userId}/complete-address-check`)
  .then(response => response.json())
  .then(hasComplete => {
    if (hasComplete) {
      // Cho phép tạo đơn hàng
    } else {
      // Yêu cầu cập nhật địa chỉ
    }
  });
```

### React/TypeScript
```typescript
interface ShippingInfo {
  userId: number;
  userFullName: string;
  userPhone: string;
  provinceCode: string;
  communeCode: string;
  fullAddress: string;
  // ... other fields
}

const getShippingInfo = async (userId: number): Promise<ShippingInfo> => {
  const response = await fetch(`/api/shipping/user/${userId}`);
  return await response.json();
};

// Sử dụng
const shippingInfo = await getShippingInfo(123);
console.log('Mã tỉnh:', shippingInfo.provinceCode);
console.log('Mã phường/xã:', shippingInfo.communeCode);
```
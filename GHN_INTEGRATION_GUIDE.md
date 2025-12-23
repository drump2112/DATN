# Tích hợp API Giao Hàng Nhanh (GHN)

## 🎯 Tổng quan
Hệ thống đã được tích hợp với API Giao Hàng Nhanh để tính phí ship chính xác theo địa chỉ giao hàng thực tế của khách hàng.

## 🔧 Cấu hình

### Application Properties
```properties
# GHN API Configuration
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api/v2
ghn.api.token=YOUR_GHN_TOKEN
ghn.shop.id=YOUR_SHOP_ID
ghn.from.district.id=1454  # Quận Cầu Giấy, Hà Nội
ghn.from.ward.code=21012   # Phường Dịch Vọng
```

### Lấy thông tin GHN:

#### 1. Đăng ký và cấu hình cơ bản:
1. Đăng ký tài khoản tại: https://5sao.ghn.dev/
2. Tạo shop và lấy Shop ID
3. Tạo API Token từ dashboard

#### 2. Cách lấy District ID và Ward Code:

**Phương pháp 1: Qua GHN Dashboard**
- Đăng nhập vào GHN Dashboard
- Tạo Shop → Nhập địa chỉ kho hàng
- Trong **Shop Settings** sẽ hiển thị District ID và Ward Code

**Phương pháp 2: Qua API (Khuyến nghị)**
```bash
# Bước 1: Lấy danh sách districts của Hà Nội (Province ID = 269)
GET /api/ghn-districts/269

# Bước 2: Tìm district "Cầu Giấy" → lấy DistrictID = 1454
# Bước 3: Lấy danh sách wards của district 1454
GET /api/ghn-wards/1454

# Bước 4: Tìm ward "Dịch Vọng" → lấy WardCode = "21012"
```

**Ví dụ cho một số địa chỉ phổ biến:**
| Địa chỉ | Province ID | District ID | Ward Code |
|---------|-------------|-------------|-----------|
| Phường Dịch Vọng, Cầu Giấy, Hà Nội | 269 | 1454 | 21012 |
| Phường Bến Nghé, Q1, TP.HCM | 202 | 1463 | 21211 |
| Phường Hải Tân, Hải Dương | 203 | 1482 | 21308 |

## 📚 API Endpoints

### 1. Tính phí giao hàng
```
GET /api/shipping-fee?provinceCode={code}&communeCode={code}&weight={grams}&totalValue={vnd}
```

**Parameters:**
- `provinceCode` (required): Mã tỉnh đích
- `communeCode` (optional): Mã phường/xã đích
- `weight` (optional): Trọng lượng gói hàng (gram), mặc định 500g
- `totalValue` (optional): Giá trị đơn hàng (VNĐ) để tính bảo hiểm

**Response:**
```json
{
  "fee": 35000,
  "message": "Tính phí thành công"
}
```

### 2. Test kết nối GHN
```
GET /api/test-ghn
```

**Response:**
```json
{
  "success": true,
  "message": "GHN API hoạt động bình thường",
  "testFee": 42000,
  "testRoute": "Hà Nội -> TP.HCM, 500g, 100,000 VNĐ"
}
```

### 3. Lấy danh sách Districts
```
GET /api/ghn-districts/{provinceId}
```

**Ví dụ:** `/api/ghn-districts/269` (Hà Nội)

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "DistrictID": 1454,
      "ProvinceID": 269,
      "DistrictName": "Cầu Giấy",
      "Code": "0100",
      "Type": 1,
      "SupportType": 3
    }
  ]
}
```

### 4. Lấy danh sách Wards
```
GET /api/ghn-wards/{districtId}
```

**Ví dụ:** `/api/ghn-wards/1454` (Cầu Giấy)

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "WardCode": "21012",
      "DistrictID": 1454,
      "WardName": "Dịch Vọng",
      "NameExtension": ["Phường Dịch Vọng"]
    }
  ]
}
```

## 🗺️ Mapping Tỉnh/Thành phố

Hệ thống sử dụng `GHNMappingHelper` để chuyển đổi mã tỉnh nội bộ sang GHN Province ID:

| Tỉnh/Thành phố | Mã hệ thống | GHN Province ID |
|----------------|-------------|-----------------|
| Hà Nội | 01 | 269 |
| TP.HCM | 79 | 202 |
| Hải Phòng | 31 | 203 |
| Đà Nẵng | 48 | 204 |
| Cần Thơ | 92 | 205 |

*Xem đầy đủ trong `GHNMappingHelper.java`*

## 🔄 Quy trình tính phí

1. **Input**: Mã tỉnh/phường + trọng lượng + giá trị đơn hàng
2. **Mapping**: Chuyển đổi mã tỉnh/phường sang GHN format
3. **API Call**: Gọi GHN API để lấy phí chính xác
4. **Fallback**: Nếu GHN API fail, sử dụng logic cứng theo vùng miền
5. **Output**: Phí giao hàng (VNĐ)

## 🛠️ Cách sử dụng trong Frontend

### JavaScript - Checkout Page
```javascript
// Tính phí ship realtime khi chọn địa chỉ
function calculateShippingFee(provinceCode, communeCode) {
    const totalValue = parseFloat($('#totalAmount').text().replace(/[^\d]/g, ''));
    const weight = calculateEstimatedWeight(); // Ước tính từ giỏ hàng

    const params = new URLSearchParams({
        provinceCode: provinceCode,
        communeCode: communeCode,
        weight: weight,
        totalValue: totalValue
    });

    $.get(`/api/shipping-fee?${params}`)
        .done(function(response) {
            updateShippingFee(response.fee);
        })
        .fail(function() {
            updateShippingFee(30000); // Fallback
        });
}
```

## 🎯 Lợi ích

### ✅ **Tính chính xác**
- Phí ship thực tế từ GHN API thay vì ước tính
- Tính theo khoảng cách và logistics thực tế

### ✅ **Tự động hóa**
- Không cần cập nhật bảng giá thủ công
- Đồng bộ với giá GHN realtime

### ✅ **Độ tin cậy cao**
- Fallback logic khi API fail
- Mapping đầy đủ 63 tỉnh/thành phố

### ✅ **Trải nghiệm tốt**
- Tính phí ngay khi chọn địa chỉ
- Hiển thị transparent cho khách hàng

## ⚠️ Lưu ý

1. **API Key bảo mật**: Không expose GHN token ra frontend
2. **Rate Limiting**: GHN có giới hạn số request/phút
3. **Error Handling**: Luôn có fallback khi API fail
4. **Testing**: Test định kỳ với endpoint `/api/test-ghn`
5. **Monitoring**: Log và monitor API calls để debug

## 🔧 Troubleshooting

### Lỗi thường gặp:

**"GHN API không phản hồi"**
- Kiểm tra token và shop ID
- Verify network connection
- Check GHN service status

**"Phí ship = 0 VNĐ"**
- Province/Ward mapping không đúng
- Kiểm tra GHNMappingHelper
- Verify input parameters

**"Fallback luôn được sử dụng"**
- Debug GHNService.calculateShippingFee()
- Check logs cho exception details
- Test với `/api/test-ghn`

## 📞 Hỗ trợ

- GHN Developer Portal: https://5sao.ghn.dev/
- GHN API Documentation: https://api.ghn.vn/home/docs/detail?id=78
- Technical Support: Liên hệ team phát triển
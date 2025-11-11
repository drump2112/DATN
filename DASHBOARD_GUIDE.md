# Dashboard Thống Kê - Hướng Dẫn Sử Dụng

## 📊 Tổng Quan

Dashboard thống kê được thiết kế với 3 cấp độ API để đảm bảo hoạt động ổn định:

### 🎯 Cấp độ API

1. **Simple API** (`/admin/api/simple/stats`)
   - Chỉ sử dụng các query cơ bản: `count()`
   - Hoạt động ngay cả khi database có ít dữ liệu
   - Hiển thị thống kê số lượng cơ bản

2. **Complex API** (`/admin/api/dashboard/stats`)
   - Sử dụng query phức tạp với JOIN và GROUP BY
   - Hiển thị thống kê chi tiết, biểu đồ
   - Yêu cầu dữ liệu đầy đủ trong database

3. **Test API** (`/admin/api/test/dashboard/stats`)
   - Dữ liệu mẫu để demo
   - Hoạt động độc lập không cần database
   - Hiển thị đầy đủ tính năng dashboard

### 🔄 Logic Hoạt Động

```
1. Thử Simple API
   ├─ Thành công → Hiển thị stats cơ bản + thử Complex API
   └─ Thất bại → Chuyển sang Test API

2. Complex API (nếu Simple API thành công)
   ├─ Thành công → Hiển thị biểu đồ chi tiết
   └─ Thất bại → Sử dụng Test data cho biểu đồ

3. Test API (fallback cuối cùng)
   ├─ Hiển thị toàn bộ dữ liệu mẫu
   └─ Thông báo "Dữ liệu mẫu"
```

## 🛠 Cài Đặt & Khắc Phục

### Lỗi Repository Query

**Lỗi**: `Could not resolve attribute 'name' of 'com.example.DATN.models.Role'`

**Nguyên nhân**: Entity `Role` có thuộc tính `nameRole` chứ không phải `name`

**Đã sửa**:
```java
// Trước
@Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")

// Sau
@Query("SELECT COUNT(u) FROM User u WHERE u.role.nameRole = :roleName")
```

### Lỗi Import trong OrderItem

**Lỗi**: Import sai package cho `@Builder`

**Đã sửa**:
```java
// Xóa import sai
import groovy.transform.builder.Builder;
import org.hibernate.annotations.DialectOverride.Formula;

// Giữ import đúng
import lombok.Builder;
```

### SQL Server Compatibility

**Query được tối ưu cho SQL Server**:
- Sử dụng `TOP 10` thay vì `LIMIT 10`
- Sử dụng `INNER JOIN` thay vì `JOIN`
- Sử dụng `CASE WHEN` cho quarter calculation

## 📊 Thành Phần Dashboard

### 1. Thống Kê Tổng Quan (4 Cards)
- **Tổng đơn hàng**: `orderRepository.count()`
- **Tổng doanh thu**: `SUM(finalAmount)` từ đơn COMPLETED
- **Tổng sản phẩm**: `productRepository.count()`
- **Tổng khách hàng**: `userRepository.countByRoleId(3)`

### 2. Biểu Đồ Doanh Thu Theo Tháng
- **Công nghệ**: Flot Charts
- **Dữ liệu**: Revenue + Order count theo tháng
- **Tính năng**: Tooltip, zoom, responsive

### 3. Tỷ Lệ Hoàn Thành & Hủy
- **Tính toán**: `(completed/total) * 100`
- **Hiển thị**: Progress bars + phần trăm
- **Màu sắc**: Xanh (hoàn thành), Đỏ (hủy)

### 4. Top Sản Phẩm Bán Chạy
- **Query**: JOIN OrderItem → ProductVariant → Product
- **Hiển thị**: Top 5 với huy chương 🥇🥈🥉
- **Thông tin**: Tên, mã, số lượng bán

### 5. Biểu Đồ Trạng Thái Đơn Hàng
- **Công nghệ**: Chart.js Doughnut
- **Dữ liệu**: Phân bố theo status
- **Màu sắc**: Khác nhau cho mỗi trạng thái

### 6. Sản Phẩm Bán Chạy Theo Quý
- **Hiển thị**: Bảng responsive
- **Logic**: CASE WHEN để tính quý
- **Dữ liệu**: Top sản phẩm mỗi quý

## 🎨 UI/UX Features

### Responsive Design
- **Mobile**: Cards stack vertically
- **Tablet**: 2 columns layout
- **Desktop**: Full 4 columns

### Loading States
- **Spinner**: Hiển thị khi đang tải
- **Error**: Thông báo khi lỗi
- **Empty**: Thông báo khi không có dữ liệu

### Animations
- **Hover**: Cards lift up
- **Progress**: Animated bars
- **Charts**: Smooth transitions

## 🔧 Tùy Chỉnh

### Thêm Thống Kê Mới

1. **Thêm vào DTO**:
```java
// DashboardStatsDto.java
private Long newStatistic;
```

2. **Thêm query vào Repository**:
```java
// OrderRepository.java
@Query("SELECT COUNT(*) FROM ...")
Long getNewStatistic();
```

3. **Cập nhật Service**:
```java
// DashboardService.java
.newStatistic(getNewStatistic())
```

4. **Cập nhật Frontend**:
```javascript
// dashboard.html
$('#newStat').text(formatNumber(data.newStatistic));
```

### Thêm Biểu Đồ Mới

1. **Thêm container HTML**
2. **Thêm render function JavaScript**
3. **Gọi function trong `loadDashboardData()`

## 🚀 Endpoints

### Dashboard Chính
- **URL**: `/admin/home`
- **Template**: `admin/dashboard.html`

### APIs
- **Simple**: `/admin/api/simple/stats`
- **Complex**: `/admin/api/dashboard/stats`
- **Test**: `/admin/api/test/dashboard/stats`

### Test Dashboard
- **URL**: `/admin/test/dashboard`
- **Template**: `admin/dashboard-test.html`

## 📝 Troubleshooting

### Dashboard Trống
1. Kiểm tra console browser
2. Thử endpoint simple trước
3. Kiểm tra kết nối database
4. Sử dụng test dashboard

### Query Lỗi
1. Kiểm tra entity relationships
2. Kiểm tra column names
3. Test query đơn giản trước

### Performance Chậm
1. Thêm index cho các columns thường query
2. Optimize query với EXPLAIN
3. Sử dụng pagination cho data lớn

---

**Tác giả**: GitHub Copilot
**Ngày**: November 2025
**Version**: 1.0
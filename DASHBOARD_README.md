# Dashboard Quản Trị - Hướng Dẫn Sử Dụng

## Tổng quan
Dashboard quản trị cung cấp cái nhìn tổng quan về hoạt động kinh doanh của cửa hàng với các thống kê chi tiết và trực quan.

## Các Thành Phần Chính

### 1. Thống kê Tổng quan (Cards)
- **Tổng đơn hàng**: Hiển thị tổng số đơn hàng đã được tạo
- **Tổng doanh thu**: Doanh thu từ các đơn hàng đã hoàn thành
- **Tổng sản phẩm**: Số lượng sản phẩm có trong hệ thống
- **Tổng khách hàng**: Số lượng khách hàng đã đăng ký

### 2. Biểu đồ Doanh thu theo Tháng
- Hiển thị doanh thu và số lượng đơn hàng theo từng tháng trong năm
- Sử dụng thư viện Flot Charts để vẽ biểu đồ cột và đường
- Có tooltip hiển thị chi tiết khi hover

### 3. Tỷ lệ Hoàn thành và Hủy đơn
- **Tỷ lệ hoàn thành**: Phần trăm đơn hàng có trạng thái "COMPLETED"
- **Tỷ lệ hủy đơn**: Phần trăm đơn hàng có trạng thái "CANCELLED"
- Hiển thị dưới dạng progress bar

### 4. Sản phẩm Bán chạy
- Top 5 sản phẩm có lượng bán cao nhất
- Hiển thị với huy chương (🥇🥈🥉) cho top 3
- Thông tin bao gồm: tên sản phẩm, mã sản phẩm, số lượng đã bán

### 5. Thống kê Trạng thái Đơn hàng
- Biểu đồ tròn (doughnut chart) hiển thị phân bố trạng thái đơn hàng
- Các trạng thái: Chờ xử lý, Đã xác nhận, Đang giao, Hoàn thành, Đã hủy
- Sử dụng Chart.js

### 6. Sản phẩm Bán chạy theo Quý
- Bảng hiển thị sản phẩm bán chạy nhất mỗi quý
- Thông tin: quý/năm, tên sản phẩm, số lượng bán

## Cấu trúc Code

### Backend
- **DashboardController**: Controller chính xử lý request
- **DashboardService**: Service xử lý logic thống kê
- **DashboardStatsDto**: DTO chứa dữ liệu response
- **OrderRepository**: Repository với các query thống kê

### Frontend
- **dashboard.html**: Template Thymeleaf chính
- **dashboard-test.html**: Template test với dữ liệu mẫu
- **main.css**: CSS styles cho dashboard
- **JavaScript**: Xử lý AJAX, vẽ charts, format dữ liệu

### API Endpoints
- `GET /admin/api/dashboard/stats`: API chính lấy dữ liệu thống kê
- `GET /admin/api/test/dashboard/stats`: API test với dữ liệu mẫu
- `GET /admin/home`: Trang dashboard chính
- `GET /admin/test/dashboard`: Trang dashboard test

## Cách sử dụng

### 1. Truy cập Dashboard
```
http://localhost:8080/admin/home
```

### 2. Dashboard Test (với dữ liệu mẫu)
```
http://localhost:8080/admin/test/dashboard
```

### 3. API Test
```
http://localhost:8080/admin/api/test/dashboard/stats
```

## Tính năng Đặc biệt

### Auto Fallback
Dashboard sẽ tự động fallback sang dữ liệu test nếu:
- Database chưa có dữ liệu
- Có lỗi kết nối database
- API gặp lỗi

### Loading States
- Hiển thị spinner khi đang tải dữ liệu
- Error state khi không thể tải dữ liệu
- Empty state khi không có dữ liệu

### Responsive Design
- Tương thích với mobile và tablet
- Charts tự động resize
- Table responsive

## Thư viện Sử dụng

### JavaScript Libraries
- **jQuery**: DOM manipulation và AJAX
- **Flot Charts**: Biểu đồ doanh thu
- **Chart.js**: Biểu đồ trạng thái đơn hàng
- **Peity**: Mini charts
- **Bootstrap**: UI framework

### CSS Frameworks
- **Font Awesome**: Icons
- **Bootstrap**: Grid system và components
- **Inspinia Theme**: Admin template

## Customization

### Thêm Thống kê mới
1. Thêm field vào `DashboardStatsDto`
2. Thêm method vào `DashboardService`
3. Thêm query vào `Repository`
4. Cập nhật frontend JavaScript

### Thay đổi Colors
Sửa màu sắc trong file `main.css`:
```css
.dashboard .ibox-title .label {
  background-color: #your-color;
}
```

### Thêm Charts mới
Sử dụng các thư viện có sẵn:
- Flot Charts cho line/bar charts
- Chart.js cho pie/doughnut charts
- Peity cho mini charts

## Troubleshooting

### Dashboard không hiển thị dữ liệu
1. Kiểm tra console browser có lỗi không
2. Kiểm tra API endpoint hoạt động
3. Kiểm tra kết nối database
4. Sử dụng test endpoint để debug

### Charts không hiển thị
1. Kiểm tra JavaScript libraries đã load
2. Kiểm tra container có đúng ID không
3. Kiểm tra dữ liệu trả về có đúng format không

### Performance Issues
1. Thêm caching cho API
2. Phân trang cho dữ liệu lớn
3. Lazy loading cho charts

## Future Enhancements

1. **Real-time Updates**: WebSocket để cập nhật real-time
2. **Export Features**: Xuất báo cáo PDF/Excel
3. **Date Range Filters**: Lọc theo khoảng thời gian
4. **Drill-down Analytics**: Chi tiết hóa thống kê
5. **Comparison Charts**: So sánh theo thời gian
6. **Mobile App**: Ứng dụng mobile cho admin
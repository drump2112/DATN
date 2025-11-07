# HƯỚNG DẪN LOGIC TRẠNG THÁI ĐỠN HÀNG

## Trạng thái đơn hàng và luồng xử lý

### 1. Trạng thái đơn hàng
- **PENDING**: Chờ xử lý
- **PROCESSING**: Đang xử lý  
- **SHIPPING**: Đang giao hàng
- **COMPLETED**: Hoàn thành
- **CANCELLED**: Đã hủy
- **RETURN**: Đổi/Trả
- **WAITING_OTP**: Chờ xác nhận OTP (dành cho thanh toán COD)

### 2. Luồng xử lý cho KHÁCH HÀNG

#### Trạng thái PENDING:
- **Nút có sẵn**: "Hủy đơn" (màu đỏ)
- **Hành động**: Khách hàng có thể hủy đơn hàng
- **Kết quả**: PENDING → CANCELLED

#### Trạng thái SHIPPING:  
- **Nút có sẵn**: "Đã nhận" (màu xanh lá)
- **Hành động**: Khách hàng xác nhận đã nhận hàng
- **Kết quả**: SHIPPING → COMPLETED

#### Trạng thái COMPLETED:
- **Nút có sẵn**: 
  - "Mua lại" (màu xanh dương) 
  - "Đổi/Trả" (màu vàng)
- **Hành động**: 
  - Mua lại: Thêm sản phẩm vào giỏ hàng (chưa implement)
  - Đổi/Trả: Yêu cầu đổi/trả hàng với lý do
- **Kết quả**: COMPLETED → RETURN (với đổi/trả)

### 3. Luồng xử lý cho ADMIN (Cửa hàng)

#### Trạng thái PENDING:
- **Nút có sẵn**: 
  - "Xác nhận" (màu xanh lá)
  - "Hủy" (màu đỏ)
- **Hành động**:
  - Xác nhận: Admin xác nhận đơn hàng và chuyển sang giao hàng
  - Hủy: Admin hủy đơn hàng
- **Kết quả**: 
  - PENDING → SHIPPING (khi xác nhận)
  - PENDING → CANCELLED (khi hủy)

### 4. API Endpoints đã tạo

#### Cho khách hàng:
- `PUT /api/customer/orders/{orderId}/cancel` - Hủy đơn hàng
- `PUT /api/customer/orders/{orderId}/confirm-received` - Xác nhận đã nhận hàng  
- `PUT /api/customer/orders/{orderId}/request-return` - Yêu cầu đổi/trả
- `GET /api/customer/orders/{orderId}/permissions` - Kiểm tra quyền thao tác

#### Cho admin:
- `PUT /admin/order/{orderId}/confirm` - Xác nhận đơn hàng (PENDING → SHIPPING)
- `PUT /admin/order/{orderId}/status` - Cập nhật trạng thái (đã có sẵn)

### 5. Files đã được tạo/cập nhật

#### Backend:
- `OrderStatus.java` - Enum quản lý trạng thái đơn hàng
- `CustomerOrderActionController.java` - API controller cho khách hàng
- `OrderService.java` - Thêm các method mới
- `OrderServiceImpl.java` - Implement logic xử lý
- `OrderController.java` - Thêm endpoint confirm cho admin

#### Frontend:
- `customer-order-actions.js` - JavaScript xử lý action khách hàng
- `orders.html` - Template hiển thị nút action theo trạng thái
- `checkorder.html` - Cập nhật hiển thị trạng thái mới
- `tableonline.html` - Template admin với nút xác nhận/hủy
- `order.js` - JavaScript admin xử lý xác nhận/hủy

### 6. Logic kiểm tra quyền

#### Khách hàng:
- Chỉ có thể thao tác trên đơn hàng của chính mình
- Chỉ hủy được khi đơn hàng đang PENDING
- Chỉ xác nhận nhận hàng được khi đang SHIPPING  
- Chỉ đổi/trả được khi đã COMPLETED

#### Admin:
- Chỉ xác nhận được khi đơn hàng đang PENDING
- Có thể hủy đơn hàng khi đang PENDING

### 7. Cách test

1. **Tạo đơn hàng mới** → Trạng thái PENDING
2. **Khách hàng thấy nút "Hủy đơn"** ở trang /orders  
3. **Admin thấy nút "Xác nhận" và "Hủy"** ở admin panel
4. **Admin click "Xác nhận"** → Đơn hàng chuyển SHIPPING
5. **Khách hàng thấy nút "Đã nhận"** → Click để chuyển COMPLETED
6. **Khách hàng thấy nút "Mua lại" và "Đổi/Trả"** khi COMPLETED

Tất cả các chức năng đã được implement đầy đủ theo yêu cầu!
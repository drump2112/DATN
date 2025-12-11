# SweetAlert Configuration Usage Guide

## Cách sử dụng SweetAlert Config trong dự án

### 1. Include file config trong template

Thêm vào `base.html` hoặc các template layout:

```html
<!-- SweetAlert2 -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<!-- SweetAlert Config (phải load sau SweetAlert2) -->
<script th:src="@{/js/sweetalert-config.js}"></script>
```

### 2. Sử dụng SweetAlert Utils

#### Success Messages
```javascript
// Thay thế: Swal.fire("Thành công!", "Đã lưu thành công", "success");
SwalUtils.success("Thành công!", "Đã lưu thành công");

// Với options tùy chỉnh
SwalUtils.success("Đăng ký thành công!", "Vui lòng kiểm tra email", { timer: 3000 });
```

#### Error Messages
```javascript
// Thay thế: Swal.fire("Lỗi!", "Có lỗi xảy ra", "error");
SwalUtils.error("Lỗi!", "Có lỗi xảy ra");
```

#### Confirmation Dialogs
```javascript
// Thay thế:
// Swal.fire({
//   title: "Xác nhận?",
//   icon: "question",
//   showCancelButton: true,
//   confirmButtonText: "OK",
//   cancelButtonText: "Hủy"
// })
SwalUtils.confirm("Xác nhận?", "Bạn có chắc chắn?", "OK", "Hủy")
  .then((result) => {
    if (result.isConfirmed) {
      // Xử lý khi confirm
    }
  });
```

#### Delete Confirmation
```javascript
// Thay thế các confirmation xóa
SwalUtils.confirmDelete("Xác nhận xóa", "Bạn có chắc muốn xóa?")
  .then((result) => {
    if (result.isConfirmed) {
      // Xử lý xóa
    }
  });
```

#### Toast Notifications
```javascript
// Toast nhỏ ở góc màn hình
SwalUtils.toast('success', 'Đã thêm vào giỏ hàng');
SwalUtils.toast('error', 'Có lỗi xảy ra');
SwalUtils.toast('info', 'Thông tin đã được cập nhật');
```

#### Info Messages
```javascript
SwalUtils.info("Thông tin", "Nhấp vào nút Chỉnh sửa để thay đổi", {
  showCancelButton: true,
  confirmButtonText: "Chỉnh sửa ngay",
  cancelButtonText: "Đóng"
});
```

#### Loading States
```javascript
// Hiển thị loading
SwalUtils.loading("Đang xử lý...", "Vui lòng chờ");

// Đóng loading
SwalUtils.close();
```

### 3. Consistent Styling

Tất cả SweetAlert sẽ tự động có:
- **Primary color**: `#1ab394` (brand color)
- **Cancel color**: `#6c757d` (gray)
- **Icons**: Font Awesome trong buttons
- **Animations**: Fade in/out effects
- **Timer**: Progress bar cho success messages
- **Responsive**: Tự động responsive

### 4. Migration từ code cũ

#### Before:
```javascript
Swal.fire({
  title: "Xác nhận thêm nhân viên?",
  icon: "question",
  showCancelButton: true,
  confirmButtonText: "Thêm",
  cancelButtonText: "Hủy"
}).then((result) => {
  if (result.isConfirmed) {
    // xử lý
  }
});
```

#### After:
```javascript
SwalUtils.confirm(
  "Xác nhận thêm nhân viên?",
  "Bạn có chắc chắn muốn thêm nhân viên với thông tin này?",
  "Thêm",
  "Hủy"
).then((result) => {
  if (result.isConfirmed) {
    // xử lý
  }
});
```

### 5. Files đã được cập nhật

✅ `/js/shop/register.js`
✅ `/js/shop/cart-page.js`
✅ `/js/admin/employee.js`
✅ `/js/admin/customer.js`

### 6. Files cần cập nhật tiếp

- `/js/shop/checkout.js`
- `/js/shop/product-detail.js`
- `/js/admin/product.js`
- `/js/admin/order.js`
- `/js/admin/brand.js`
- `/js/admin/categories.js`
- `/js/admin/size.js`
- `/js/admin/color.js`
- `/js/admin/voucher.js`
- Các template HTML inline scripts

### 7. Include trong templates

Thêm vào `fragments/base.html`:

```html
<!-- Trong phần <head> hoặc trước </body> -->
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script th:src="@{/js/sweetalert-config.js}"></script>
```
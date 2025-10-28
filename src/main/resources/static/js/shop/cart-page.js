$(document).ready(function () {
  // Chỉ chạy khi đang ở trang giỏ hàng
  if ($(".shoping-cart-table").length === 0) {
    return;
  }

  function renderCart(cart) {
    const $tbody = $(".shoping-cart-table tbody");
    $tbody.empty();

    if (!cart || cart.length === 0) {
      $tbody.append(`
      <tr>
        <td colspan="5" class="text-center">Giỏ hàng trống</td>
      </tr>
    `);
      $(".cart-summary-total").text("0 VNĐ");
      $(".ibox-title .pull-right strong").text("0");
      return;
    }

    cart.forEach((item, index) => {
      $tbody.append(`
      <tr data-variant-id="${item.variantId}">
        <td width="90">
          <img src="${item.image}" alt="${item.name}" style="width:80px;height:auto;"/>
        </td>
        <td class="desc">
          <h3>${item.name}</h3>
          <p><small>Màu: ${item.colorName} | Size: ${item.sizeName}</small></p>
        </td>
        <td>${item.price.toLocaleString()} VNĐ</td>
        <td>
          <input type="number" min="1" class="form-control quantity-input" value="${item.quantity}" style="width:80px"/>
        </td>
        <td>
          <strong>${(item.price * item.quantity).toLocaleString()} VNĐ</strong>
        </td>
        <td>
          <button class="btn btn-danger btn-sm remove-item">
            <i class="fa fa-trash"></i>
          </button>
        </td>
      </tr>
    `);
    });

    const total = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    $(".cart-summary-total").text(total.toLocaleString() + " VNĐ");

    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    $(".ibox-title .pull-right strong").text(totalItems);
  }

  function loadCart() {
    $.get("/cart/items")
      .done(function(cart) {
        renderCart(cart);
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      })
      .fail(function(xhr) {
        console.error("Error loading cart:", xhr);
      });
  }

  loadCart();

  $(document).on("click", ".remove-item", function () {
    const $row = $(this).closest("tr");
    const variantId = $row.data("variant-id");

    console.log("Remove button clicked, variantId:", variantId);

    if (!variantId) {
      console.error("Variant ID not found");
      Swal.fire({
        icon: 'error',
        title: 'Lỗi',
        text: 'Không thể xóa sản phẩm. Vui lòng thử lại!',
        confirmButtonText: 'OK'
      });
      return;
    }

    Swal.fire({
      title: 'Xác nhận xóa',
      text: 'Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d33',
      cancelButtonColor: '#3085d6',
      confirmButtonText: 'Xóa',
      cancelButtonText: 'Hủy'
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/cart/remove/${variantId}`,
          method: "POST",
          success: function(cart) {
            console.log("Item removed successfully", cart);
            renderCart(cart);
            const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
            $("#cart-count").text(totalItems);

            Swal.fire({
              icon: 'success',
              title: 'Thành công',
              text: 'Đã xóa sản phẩm khỏi giỏ hàng',
              timer: 1500,
              showConfirmButton: false
            });
          },
          error: function(xhr, status, error) {
            console.error("Error removing item:", xhr, status, error);
            console.error("Response:", xhr.responseText);
            Swal.fire({
              icon: 'error',
              title: 'Lỗi',
              text: 'Có lỗi xảy ra khi xóa sản phẩm. Vui lòng thử lại!',
              confirmButtonText: 'OK'
            });
          }
        });
      }
    });
  });

  $(document).on("change", ".quantity-input", function () {
    const variantId = $(this).closest("tr").data("variant-id");
    const newQuantity = parseInt($(this).val());
    if (newQuantity > 0) {
      $.ajax({
        url: `/cart/update/${variantId}`,
        method: "POST",
        data: { quantity: newQuantity },
        success: function(cart) {
          renderCart(cart);
          const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
          $("#cart-count").text(totalItems);
        },
        error: function(xhr) {
          console.error("Error updating quantity:", xhr);
        }
      });
    }
  });
});

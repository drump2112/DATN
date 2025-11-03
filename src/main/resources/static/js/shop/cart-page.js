$(document).ready(function () {
  // Chỉ chạy khi đang ở trang giỏ hàng
  if ($(".shoping-cart-table").length === 0) {
    return;
  }

  function renderCart(cart) {
    const $tbody = $(".shoping-cart-table tbody");
    const $tableContainer = $("#cartTableContainer");
    const $emptyState = $("#emptyCartState");
    const $productCount = $(".product-count strong");

    $tbody.empty();

    // Update product count
    $productCount.text(cart ? cart.length : 0);

    if (!cart || cart.length === 0) {
      // Show empty state
      $tableContainer.hide();
      $emptyState.show();
      $(".cart-summary-total").text("0 VNĐ");
      $("#subtotalAmount").text("0 VNĐ");
      return;
    }

    // Hide empty state and show table
    $emptyState.hide();
    $tableContainer.show();

    cart.forEach((item, index) => {
      $tbody.append(`
      <tr data-variant-id="${item.variantId}">
        <td>
          <img src="${item.image || '/static/assets/img/no-image.png'}" alt="${item.name}" class="product-image"/>
        </td>
        <td class="product-info">
          <div class="product-name">${item.name}-${item.variantCode}</div>
          <div class="product-variant">Màu: ${item.colorName} | Size: ${item.sizeName}</div>
        </td>
        <td>
          <span class="price-display">${item.price.toLocaleString()} VNĐ</span>
        </td>
        <td>
          <div class="quantity-controls">
            <input type="number" min="1" class="quantity-input" value="${item.quantity}"/>
          </div>
        </td>
        <td>
          <span class="total-price">${(item.price * item.quantity).toLocaleString()} VNĐ</span>
        </td>
        <td>
          <div class="action-buttons">
            <button class="btn-remove remove-item">
              <i class="fa fa-trash"></i> Xóa
            </button>
          </div>
        </td>
      </tr>
    `);
    });

    const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
    $(".cart-summary-total").text(subtotal.toLocaleString() + " VNĐ");
    $("#subtotalAmount").text(subtotal.toLocaleString() + " VNĐ");

    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    $productCount.text(cart.length);
  }

  function loadCart() {
    $.get("/cart/items")
      .done(function (cart) {
        renderCart(cart);
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      })
      .fail(function (xhr) {
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
      SwalUtils.error('Lỗi', 'Không thể xóa sản phẩm. Vui lòng thử lại!');
      return;
    }

    SwalUtils.confirmDelete(
      'Xác nhận xóa',
      'Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?'
    ).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/cart/remove/${variantId}`,
          method: "POST",
          success: function (cart) {
            console.log("Item removed successfully", cart);
            renderCart(cart);
            const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
            $("#cart-count").text(totalItems);

            SwalUtils.toast('success', 'Đã xóa sản phẩm khỏi giỏ hàng', { timer: 1500 });
          },
          error: function (xhr, status, error) {
            console.error("Error removing item:", xhr, status, error);
            console.error("Response:", xhr.responseText);
            SwalUtils.error('Lỗi', 'Có lỗi xảy ra khi xóa sản phẩm. Vui lòng thử lại!');
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
        success: function (cart) {
          renderCart(cart);
          const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
          $("#cart-count").text(totalItems);
        },
        error: function (xhr) {
          console.error("Error updating quantity:", xhr);
        }
      });
    }
  });
});

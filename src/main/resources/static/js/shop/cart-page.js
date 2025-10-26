$(document).ready(function () {
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

  // Load cart from server
  function loadCart() {
    $.get("/cart/items")
      .done(function(cart) {
        renderCart(cart);
        // Cập nhật số lượng trên cart icon
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      })
      .fail(function(xhr) {
        console.error("Error loading cart:", xhr);
      });
  }

  loadCart();

  $(document).on("click", ".remove-item", function () {
    const variantId = $(this).closest("tr").data("variant-id");
    $.ajax({
      url: `/cart/remove/${variantId}`,
      method: "DELETE",
      success: function(cart) {
        renderCart(cart);
        // Cập nhật số lượng trên cart icon
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      },
      error: function(xhr) {
        console.error("Error removing item:", xhr);
      }
    });
  });

  // Cập nhật số lượng
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
          // Cập nhật số lượng trên cart icon
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

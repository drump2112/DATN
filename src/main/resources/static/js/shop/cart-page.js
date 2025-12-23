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
      const maxQty = item.maxQuantity || 999; // Fallback nếu không có maxQuantity
      $tbody.append(`
      <tr data-variant-id="${item.variantId}" data-max-quantity="${maxQty}">
        <td>
          <img src="${item.image || '/static/assets/img/no-image.png'}" alt="${item.name}" class="product-image"/>
        </td>
        <td class="product-info">
          <div class="product-name">${item.name}-${item.variantCode}</div>
          <div class="product-variant">Màu: ${item.colorName} | Size: ${item.sizeName}</div>
          <div class="stock-info"><small class="text-muted">Còn lại: ${maxQty} sản phẩm</small></div>
        </td>
        <td>
          <span class="price-display">${item.price.toLocaleString()} VNĐ</span>
        </td>
        <td>
          <div class="quantity-controls">
            <input type="number" min="1" max="${maxQty}" class="quantity-input" value="${item.quantity}"
                   title="Số lượng tối đa: ${maxQty}" oninput="this.value = this.value > ${maxQty} ? ${maxQty} : this.value"/>
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
        const totalItems = cart ? cart.reduce((sum, item) => sum + item.quantity, 0) : 0;
        $("#cart-count").text(totalItems);

        // Luôn đồng bộ localStorage với server
        if (!cart || cart.length === 0) {
          localStorage.removeItem("cart");
        } else {
          localStorage.setItem("cart", JSON.stringify(cart));
        }

        console.log("Cart loaded from server:", cart);
        console.log("Total items:", totalItems);
      })
      .fail(function (xhr) {
        console.error("Error loading cart:", xhr);
        // Fallback: thử load từ localStorage
        const localCart = JSON.parse(localStorage.getItem("cart")) || [];
        renderCart(localCart);
        const totalItems = localCart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      });
  }  loadCart();

  $(document).on("click", ".remove-item", function () {
    const $row = $(this).closest("tr");
    const variantId = $row.data("variant-id");

    console.log("Remove button clicked, variantId:", variantId);

    if (!variantId) {
      console.error("Variant ID not found");
      toastr.error('Không thể xóa sản phẩm. Vui lòng thử lại!', 'Lỗi');
      return;
    }

    SwalUtils.confirmDelete(
      'Xác nhận xóa sản phẩm',
      'Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?'
    ).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/cart/remove/${variantId}`,
          method: "POST",
          success: function (cart) {
            console.log("Item removed successfully", cart);
            renderCart(cart);
            const totalItems = cart ? cart.reduce((sum, item) => sum + item.quantity, 0) : 0;
            $("#cart-count").text(totalItems);

            // Đồng bộ localStorage
            if (!cart || cart.length === 0) {
              localStorage.removeItem("cart");
            } else {
              localStorage.setItem("cart", JSON.stringify(cart));
            }

            toastr.success('Đã xóa sản phẩm khỏi giỏ hàng', 'Thành công');
          },
          error: function (xhr, status, error) {
            console.error("Error removing item:", xhr, status, error);
            console.error("Response:", xhr.responseText);
            toastr.error('Có lỗi xảy ra khi xóa sản phẩm. Vui lòng thử lại!', 'Lỗi');
          }
        });
      }
    });
  });

  // Real-time validation cho quantity input
  $(document).on("keyup blur paste input", ".quantity-input", function () {
    const $input = $(this);
    const $row = $input.closest("tr");
    const maxQuantity = parseInt($row.data("max-quantity")) || 999;
    let value = parseInt($input.val());

    // Kiểm tra và điều chỉnh giá trị
    if (isNaN(value) || value < 1) {
      $input.addClass('invalid-quantity');
      if ($input.is(':focus')) {
        // Chỉ hiện warning khi đang focus
        toastr.warning('Số lượng phải >= 1', 'Giá trị không hợp lệ');
      }
    } else if (value > maxQuantity) {
      $input.addClass('invalid-quantity');
      toastr.warning(`Chỉ còn ${maxQuantity} sản phẩm trong kho!`);
    } else {
      $input.removeClass('invalid-quantity');
    }
  });

  $(document).on("change input", ".quantity-input", function () {
    const $input = $(this);
    const $row = $input.closest("tr");
    const variantId = $row.data("variant-id");
    const maxQuantity = parseInt($row.data("max-quantity")) || 999;
    let newQuantity = parseInt($input.val());

    console.log("Quantity change detected:", newQuantity, "Max:", maxQuantity);

    // Validation phía client
    if (isNaN(newQuantity) || newQuantity < 1) {
      newQuantity = 1;
      $input.val(1);
      toastr.warning('Số lượng không hợp lệ, đã đặt về 1', 'Cảnh báo');
      return;
    }

    if (newQuantity > maxQuantity) {
      $input.val(maxQuantity); // Reset về max
      toastr.error(`Số lượng tối đa có thể mua là ${maxQuantity} sản phẩm!`, 'Vượt quá số lượng tồn kho');
      console.log("Quantity validation failed, reset to max:", maxQuantity);
      return;
    }

    if (newQuantity > 0) {
      $.ajax({
        url: `/cart/update/${variantId}`,
        method: "POST",
        data: { quantity: newQuantity },
        success: function (cart) {
          renderCart(cart);
          const totalItems = cart ? cart.reduce((sum, item) => sum + item.quantity, 0) : 0;
          $("#cart-count").text(totalItems);

          // Đồng bộ localStorage
          if (!cart || cart.length === 0) {
            localStorage.removeItem("cart");
          } else {
            localStorage.setItem("cart", JSON.stringify(cart));
          }

          toastr.success('Đã cập nhật số lượng', 'Thành công');
        },
        error: function (xhr) {
          console.error("Error updating quantity:", xhr);

          // Xử lý lỗi từ server
          if (xhr.status === 400) {
            try {
              const errorResponse = JSON.parse(xhr.responseText);
              if (errorResponse.error === 'QUANTITY_EXCEEDED') {
                $(this).val(errorResponse.maxQuantity); // Reset về max
                toastr.error(errorResponse.message, 'Vượt quá số lượng tồn kho');
                return;
              }
            } catch (e) {
              console.error("Could not parse error response:", e);
            }
          }

          toastr.error('Có lỗi xảy ra khi cập nhật số lượng', 'Lỗi');
          loadCart(); // Reload để lấy dữ liệu đúng
        }
      });
    }
  });

  // Real-time validation khi user đang gõ
  $(document).on("keyup", ".quantity-input", function () {
    const $input = $(this);
    const $row = $input.closest("tr");
    const maxQuantity = parseInt($row.data("max-quantity")) || 999;
    let currentValue = parseInt($input.val());

    // Kiểm tra real-time
    if (currentValue > maxQuantity) {
      $input.css("border-color", "#ed5565"); // Highlight red
      $input.attr("title", `Số lượng tối đa: ${maxQuantity}`);
    } else {
      $input.css("border-color", ""); // Reset
      $input.attr("title", "");
    }
  });

  // Validation khi blur (rời khỏi input)
  $(document).on("blur", ".quantity-input", function () {
    const $input = $(this);
    const $row = $input.closest("tr");
    const maxQuantity = parseInt($row.data("max-quantity")) || 999;
    let newQuantity = parseInt($input.val());

    if (newQuantity > maxQuantity) {
      $input.val(maxQuantity);
      toastr.error(`Đã điều chỉnh về số lượng tối đa: ${maxQuantity}`, 'Vượt quá số lượng tồn kho');
      $input.css("border-color", "");
    }
  });

  // Debug button handler
  $(document).on("click", "#debug-cart", function() {
    $.get("/cart/debug")
      .done(function(debugInfo) {
        console.log("=== CART DEBUG INFO ===");
        console.log("Session ID:", debugInfo.sessionId);
        console.log("Cart Size:", debugInfo.cartSize);
        console.log("Cart Items:", debugInfo.cartItems);
        console.log("Session Attributes:", debugInfo.sessionAttributes);
        console.log("LocalStorage Cart:", JSON.parse(localStorage.getItem("cart") || "[]"));

        alert(`Session ID: ${debugInfo.sessionId}\nCart Size: ${debugInfo.cartSize}\nLocalStorage Size: ${JSON.parse(localStorage.getItem("cart") || "[]").length}\nCheck console for details`);
      })
      .fail(function(xhr) {
        console.error("Debug failed:", xhr);
      });
  });

  // Xử lý nút Đặt Hàng - kiểm tra số lượng tồn kho trước khi chuyển trang checkout
  $(document).on("click", "#btnProceedCheckout", function(e) {
    e.preventDefault();

    const $btn = $(this);
    const checkoutUrl = $btn.data("checkout-url");

    // Kiểm tra giỏ hàng từ server để lấy số lượng tồn kho mới nhất
    $.get("/cart/validate-stock")
      .done(function(response) {
        if (response.valid) {
          // Tất cả sản phẩm đều còn hàng, chuyển đến trang checkout
          window.location.href = checkoutUrl;
        } else {
          // Có sản phẩm hết hàng hoặc không đủ số lượng
          let itemsHtml = '<div class="stock-error-list" style="text-align: left; margin-top: 15px;">';
          response.outOfStockItems.forEach(function(item) {
            if (item.availableQuantity <= 0) {
              itemsHtml += `
                <div class="stock-error-item" style="padding: 10px; margin-bottom: 8px; background: #fff5f5; border-left: 3px solid #dc3545; border-radius: 4px;">
                  <div style="font-weight: 600; color: #333;">${item.productName}</div>
                  <div style="font-size: 13px; color: #666;">Mã: ${item.variantCode}</div>
                  <div style="font-size: 13px; color: #dc3545; margin-top: 4px;">
                    <i class="fa fa-times-circle"></i> Sản phẩm tạm hết hàng
                  </div>
                </div>`;
            } else {
              itemsHtml += `
                <div class="stock-error-item" style="padding: 10px; margin-bottom: 8px; background: #fff8e6; border-left: 3px solid #ffc107; border-radius: 4px;">
                  <div style="font-weight: 600; color: #333;">${item.productName}</div>
                  <div style="font-size: 13px; color: #666;">Mã: ${item.variantCode}</div>
                  <div style="font-size: 13px; color: #856404; margin-top: 4px;">
                    <i class="fa fa-exclamation-triangle"></i> Chỉ còn <strong>${item.availableQuantity}</strong> sản phẩm (Số lượng yêu cầu: ${item.requestedQuantity})
                  </div>
                </div>`;
            }
          });
          itemsHtml += '</div>';
          itemsHtml += '<div style="margin-top: 15px; padding: 10px; background: #f8f9fa; border-radius: 4px; font-size: 13px; color: #666;"><i class="fa fa-info-circle"></i> Vui lòng điều chỉnh số lượng hoặc xóa sản phẩm không còn hàng để tiếp tục đặt hàng.</div>';

          Swal.fire({
            title: '<i class="fa fa-shopping-cart" style="color: #dc3545;"></i> Không thể tiến hành đặt hàng',
            html: itemsHtml,
            icon: null,
            confirmButtonText: 'Cập nhật giỏ hàng',
            confirmButtonColor: '#1ab394',
            showClass: {
              popup: 'animate__animated animate__fadeInDown'
            },
            hideClass: {
              popup: 'animate__animated animate__fadeOutUp'
            }
          });

          // Reload lại giỏ hàng để cập nhật số lượng mới nhất
          loadCart();
        }
      })
      .fail(function(xhr) {
        console.error("Error validating cart stock:", xhr);
        toastr.error("Có lỗi xảy ra khi kiểm tra tồn kho. Vui lòng thử lại!", "Lỗi");
      });
  });
});

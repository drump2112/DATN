$(document).ready(function() {
  // Cập nhật số lượng sản phẩm trong giỏ hàng khi load trang
  function updateCartCount() {
    $.get("/cart/items")
      .done(function(cart) {
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      })
      .fail(function(xhr) {
        console.error("Error loading cart count:", xhr);
      });
  }

  window.updateCartCount = updateCartCount;

  updateCartCount();

  $("#cart-icon a").click(function(e) {
    e.preventDefault();

    $.get("/cart/items")
      .done(function(cart) {
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);

        if (totalItems === 0) {
          toastr.warning("Giỏ hàng trống", "Thông báo");
        } else {
          window.location.href = "/cart";
        }
      })
      .fail(function(xhr) {
        console.error("Error checking cart:", xhr);
        toastr.error("Không thể kiểm tra giỏ hàng", "Lỗi");
      });
  });
});
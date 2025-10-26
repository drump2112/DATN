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

  updateCartCount();
});
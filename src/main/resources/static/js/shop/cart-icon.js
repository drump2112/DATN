$(document).ready(function() {
  // Cập nhật số lượng sản phẩm trong giỏ hàng khi load trang
  function updateCartCount() {
    $.get("/cart/items")
      .done(function(cart) {
        const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);

        // Đồng bộ localStorage
        if (!cart || cart.length === 0) {
          localStorage.removeItem("cart");
        } else {
          localStorage.setItem("cart", JSON.stringify(cart));
        }

        console.log("Cart count updated:", totalItems);
      })
      .fail(function(xhr) {
        console.error("Error loading cart count:", xhr);
        // Fallback: load từ localStorage
        const localCart = JSON.parse(localStorage.getItem("cart")) || [];
        const totalItems = localCart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      });
  }

  // Function để sync localStorage lên server (nếu cần khôi phục)
  function syncLocalStorageToServer() {
    const localCart = JSON.parse(localStorage.getItem("cart")) || [];

    if (localCart.length > 0) {
      console.log("Syncing localStorage to server:", localCart);

      // Gửi từng item lên server
      const promises = localCart.map(item => {
        return $.ajax({
          url: "/cart/add",
          method: "POST",
          contentType: "application/json",
          data: JSON.stringify(item)
        });
      });

      Promise.all(promises).then(() => {
        console.log("LocalStorage synced to server successfully");
        updateCartCount();
      }).catch(error => {
        console.error("Error syncing localStorage to server:", error);
      });
    }
  }

  window.updateCartCount = updateCartCount;
  window.syncLocalStorageToServer = syncLocalStorageToServer;

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
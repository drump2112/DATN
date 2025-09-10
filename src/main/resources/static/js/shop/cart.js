// cart.js
// Bắt sự kiện change cho voucherType (dù modal có render sau)

$(document).ready(function () {
  function loadCart() {
    const cart = JSON.parse(localStorage.getItem("cart")) || [];
    const totalQuantity = cart.reduce((sum, item) => sum + item.quantity, 0);
    $("#cart-count").text(totalQuantity);
    return cart;
  }

  function saveCart(cart) {
    localStorage.setItem("cart", JSON.stringify(cart));
    const totalQuantity = cart.reduce((sum, item) => sum + item.quantity, 0);
    $("#cart-count").text(totalQuantity);
  }

  function clearCart() {
    localStorage.removeItem("cart");
    $("#cart-count").text(0);
  }

  function getTotal() {
    const cart = loadCart();
    return cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
  }

  window.Cart = { loadCart, saveCart, clearCart, getTotal };
  window.cartData = loadCart(); // biến global
});

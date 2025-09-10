// cart-page.js
$(document).ready(function () {
  function renderCart() {
    const cart = Cart.loadCart();
    const $tbody = $(".shoping-cart-table tbody");
    $tbody.empty();

    if (cart.length === 0) {
      $tbody.append(`
        <tr>
          <td colspan="5" class="text-center">Giỏ hàng trống</td>
        </tr>
      `);
      $(".cart-summary-total").text("0 đ");
      return;
    }

    cart.forEach((item, index) => {
      console.log(item.name); // Giày Nike Air
      console.log(item.colorName); // Đỏ
      console.log(item.sizeName);
      $tbody.append(`
        <tr data-index="${index}">
          <td width="90">
            <img src="${item.image}" alt="${item.name}" style="width:80px;height:auto;"/>
          </td>
          <td class="desc">
            <h3>${item.name}</h3>
            <p><small>Màu: ${item.colorName} | Size: ${item.sizeName}</small></p> 
		  </td>
          <td>${item.price.toLocaleString()} đ</td>
          <td>
            <input type="number" min="1" class="form-control quantity-input" value="${item.quantity}" style="width:80px"/>
          </td>
          <td>
            <strong>${(item.price * item.quantity).toLocaleString()} đ</strong>
          </td>
          <td>
			<button class="btn btn-danger btn-sm remove-item" data-index="${index}">
              <i class="fa fa-trash"></i>
            </button>
          </td>
        </tr>
      `);
    });

    // cập nhật tổng
    $(".cart-summary-total").text(Cart.getTotal().toLocaleString() + " đ");
  }

  // Render lần đầu
  renderCart();

  // Xóa sản phẩm
  $(document).on("click", ".remove-item", function () {
    const index = $(this).closest("tr").data("index");
    let cart = Cart.loadCart();
    cart.splice(index, 1); // xóa 1 phần tử
    Cart.saveCart(cart);
    renderCart();
  });

  // Cập nhật số lượng
  $(document).on("change", ".quantity-input", function () {
    const index = $(this).closest("tr").data("index");
    const newQuantity = parseInt($(this).val());
    let cart = Cart.loadCart();
    if (newQuantity > 0) {
      cart[index].quantity = newQuantity;
      Cart.saveCart(cart);
      renderCart();
    }
  });
});

// add-to-cart.js
$(document).ready(function () {
  $(document).on("click", "#add-to-cart", function () {
    const variants = window.productVariants;
    let cart = window.cartData;

    const selectedColorId = parseInt($('input[name="color"]:checked').val());
    const selectedSizeId = parseInt($('input[name="size"]:checked').val());

    const selectedVariant = variants.find(
      (v) => v.colorId === selectedColorId && v.sizeId === selectedSizeId,
    );
    if (!selectedVariant) return;

    const cartItem = {
      variantId: selectedVariant.id,
      colorId: selectedColorId,
      colorName: selectedVariant.colorName,
      sizeId: selectedSizeId,
      sizeName: selectedVariant.sizeName,
      name: selectedVariant.productName,
      price: selectedVariant.price,
      image: selectedVariant.imageUrls?.[0] || "",
      quantity: 1
    };

    $.ajax({
      url: "/cart/add",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify(cartItem),
      success: function(updatedCart) {
        window.cartData = updatedCart;
        // Cập nhật số lượng trên cart icon
        const totalItems = updatedCart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalItems);
      },
      error: function(xhr) {
        console.error("Error adding to cart:", xhr);
      }
    });

    // Animation bay ảnh
    const productImg = $(".product-images img").first();
    if (productImg.length) {
      const cartIcon = $("#cart-icon");
      const imgClone = productImg
        .clone()
        .css({
          position: "absolute",
          top: productImg.offset().top,
          left: productImg.offset().left,
          width: productImg.width(),
          height: productImg.height(),
          opacity: 0.8,
          zIndex: 1000,
        })
        .appendTo("body");

      imgClone.animate(
        {
          top: cartIcon.offset().top,
          left: cartIcon.offset().left,
          width: 30,
          height: 30,
          opacity: 0.2,
        },
        800,
        "swing",
        function () {
          imgClone.remove();
        },
      );
    }
  });
});

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

    const existingItem = cart.find(
      (item) =>
        item.variantId === selectedVariant.id &&
        item.colorId === selectedColorId &&
        item.sizeId === selectedSizeId,
    );

    if (existingItem) {
      existingItem.quantity += 1;
    } else {
      cart.push({
        variantId: selectedVariant.id,
        colorId: selectedColorId,
        colorName: selectedVariant.colorName, // thêm
        sizeId: selectedSizeId,
        sizeName: selectedVariant.sizeName, // thêm
        name: selectedVariant.productName,
        price: selectedVariant.price,
        image: selectedVariant.imageUrls?.[0] || "",
        quantity: 1,
      });
    }

    window.Cart.saveCart(cart);
    window.cartData = cart;

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

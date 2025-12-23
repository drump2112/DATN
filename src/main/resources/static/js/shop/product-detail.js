$(document).ready(function () {
  const rawData = $("#variant-data").data("variants");
  const variants = typeof rawData === "string" ? JSON.parse(rawData) : rawData;

  const colorContainer = $("#color .color");
  const sizeContainer = $("#size .size");

  const uniqueColors = [
    ...new Map((variants || []).map((v) => [v.colorId, v.colorName])).entries(),
  ];
  colorContainer.empty();

  uniqueColors.forEach(([colorId, colorName], index) => {
    const checked = index === 0 ? "checked" : "";
    colorContainer.append(`
      <input type="radio" name="color" id="color-${colorId}" value="${colorId}" ${checked}>
      <label for="color-${colorId}">${colorName}</label>
    `);
  });

  function formatPrice(price) {
    return parseFloat(price).toLocaleString('vi-VN', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    });
  }

  function updatePriceAndQuantity(variant) {
    $(".product-main-price").html(
      `${formatPrice(variant.price)} <small class="text-muted">VNĐ</small>`,
    );
    $("#variant-quantity").text(`Còn lại: ${variant.quantity} sản phẩm`);
    $("#current-variant-code").text(`- Mã: ${variant.variantCode || 'N/A'}`);

    // Cập nhật max quantity cho input
    const quantityInput = $("#quantity-input");
    quantityInput.attr("max", variant.quantity);

    // Reset quantity về 1 và kiểm tra availability
    quantityInput.val(1);
    updateQuantityControls(variant.quantity);
  }

  function updateQuantityControls(maxQuantity) {
    const quantityInput = $("#quantity-input");
    const decreaseBtn = $("#decrease-qty");
    const increaseBtn = $("#increase-qty");
    const currentQty = parseInt(quantityInput.val()) || 1;

    // Disable/Enable buttons dựa trên số lượng hiện tại
    decreaseBtn.prop("disabled", currentQty <= 1);
    increaseBtn.prop("disabled", currentQty >= maxQuantity);

    // Disable add to cart nếu hết hàng
    const addToCartBtn = $("#add-to-cart");
    if (maxQuantity <= 0) {
      addToCartBtn.prop("disabled", true).text("Hết Hàng");
      quantityInput.prop("disabled", true);
      decreaseBtn.prop("disabled", true);
      increaseBtn.prop("disabled", true);
    } else {
      addToCartBtn.prop("disabled", false).html('<i class="fa fa-cart-plus"></i> Thêm Vào Giỏ Hàng');
      quantityInput.prop("disabled", false);
    }
  }

  function updateImages(imageUrls) {
    const slickContainer = $(".product-images");
    slickContainer.css("visibility", "hidden");

    if (slickContainer.hasClass("slick-initialized")) {
      slickContainer.slick("unslick");
    }
    slickContainer.empty();

    if (imageUrls && imageUrls.length > 0) {
      imageUrls.forEach((url) => {
        slickContainer.append(`
          <div style="width:100%;display:flex;justify-content:center;align-items:center;">
            <img src="${url}" alt="Ảnh sản phẩm"
                 style="width:100%;height:450px;object-fit:contain;display:block;"/>
          </div>
        `);
      });

      slickContainer.slick({
        dots: true,
        arrows: true,
        infinite: true,
        slidesToShow: 1,
        slidesToScroll: 1,
      });
    } else {
      // Hiển thị ảnh placeholder nếu không có ảnh
      slickContainer.append(`
        <div style="width:100%;display:flex;justify-content:center;align-items:center;">
          <div style="width:100%;height:450px;background:#f8f9fa;display:flex;justify-content:center;align-items:center;border:2px dashed #dee2e6;">
            <div style="text-align:center;color:#6c757d;">
              <i class="fa fa-image" style="font-size:48px;margin-bottom:10px;"></i>
              <p style="margin:0;">Không có ảnh</p>
            </div>
          </div>
        </div>
      `);
    }

    setTimeout(() => slickContainer.css("visibility", "visible"), 50);
  }

  function renderSizesAndInfo(colorId) {
    const filteredVariants = variants.filter((v) => v.colorId === colorId);
    let uniqueSizes = [
      ...new Map(filteredVariants.map((v) => [v.sizeId, v.sizeName])).entries(),
    ];
    uniqueSizes.sort((a, b) => Number(a[1]) - Number(b[1]));
    sizeContainer.empty();

    uniqueSizes.forEach(([sizeId, sizeName], index) => {
      const checked = index === 0 ? "checked" : "";
      sizeContainer.append(`
        <input type="radio" name="size" id="size-${sizeId}" value="${sizeId}" ${checked}>
        <label for="size-${sizeId}">${sizeName}</label>
      `);
    });

    const defaultSizeId = uniqueSizes[0]?.[0];
    const selectedVariant = filteredVariants.find(
      (v) => v.sizeId === defaultSizeId,
    );
    if (selectedVariant) {
      updatePriceAndQuantity(selectedVariant);
      updateImages(selectedVariant.imageUrls || []);
    }
  }

  $(document).on("change", 'input[name="color"]', function () {
    renderSizesAndInfo(parseInt($(this).val()));
  });

  $(document).on("change", 'input[name="size"]', function () {
    const selectedColorId = parseInt($('input[name="color"]:checked').val());
    const selectedSizeId = parseInt($(this).val());
    const selectedVariant = variants.find(
      (v) => v.colorId === selectedColorId && v.sizeId === selectedSizeId,
    );
    if (selectedVariant) updatePriceAndQuantity(selectedVariant);
  });

  if (uniqueColors.length > 0) {
    renderSizesAndInfo(uniqueColors[0][0]);
  }

  // Quantity control handlers
  $(document).on("click", "#decrease-qty", function() {
    const quantityInput = $("#quantity-input");
    const currentQty = parseInt(quantityInput.val()) || 1;
    if (currentQty > 1) {
      quantityInput.val(currentQty - 1);
      const maxQty = parseInt(quantityInput.attr("max")) || 0;
      updateQuantityControls(maxQty);
    }
  });

  $(document).on("click", "#increase-qty", function() {
    const quantityInput = $("#quantity-input");
    const currentQty = parseInt(quantityInput.val()) || 1;
    const maxQty = parseInt(quantityInput.attr("max")) || 0;
    if (currentQty < maxQty) {
      quantityInput.val(currentQty + 1);
      updateQuantityControls(maxQty);
    }
  });

  $(document).on("input change", "#quantity-input", function() {
    const quantityInput = $(this);
    const maxQty = parseInt(quantityInput.attr("max")) || 0;
    let currentQty = parseInt(quantityInput.val());

    // Validate input - xử lý cả trường hợp NaN và empty
    if (isNaN(currentQty) || currentQty < 1 || quantityInput.val() === '') {
      currentQty = 1;
      quantityInput.val(1);
    } else if (currentQty > maxQty) {
      currentQty = maxQty;
      quantityInput.val(maxQty);
      toastr.warning(`Số lượng tối đa có thể mua là ${maxQty} sản phẩm!`, 'Vượt quá số lượng');
    }

    updateQuantityControls(maxQty);
  });

  // Prevent invalid characters in quantity input
  $(document).on("keydown", "#quantity-input", function(e) {
    // Allow: backspace, delete, tab, escape, enter
    if ([46, 8, 9, 27, 13].indexOf(e.keyCode) !== -1 ||
        // Allow: Ctrl+A, Ctrl+C, Ctrl+V, Ctrl+X
        (e.keyCode === 65 && e.ctrlKey === true) ||
        (e.keyCode === 67 && e.ctrlKey === true) ||
        (e.keyCode === 86 && e.ctrlKey === true) ||
        (e.keyCode === 88 && e.ctrlKey === true)) {
      return;
    }
    // Ensure that it is a number and stop the keypress
    if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
      e.preventDefault();
    }
  });

  // Add to cart validation
  $(document).on("click", "#add-to-cart", function() {
    const selectedColorId = parseInt($('input[name="color"]:checked').val());
    const selectedSizeId = parseInt($('input[name="size"]:checked').val());
    const selectedQuantity = parseInt($("#quantity-input").val()) || 1;

    const selectedVariant = variants.find(
      (v) => v.colorId === selectedColorId && v.sizeId === selectedSizeId,
    );

    if (!selectedVariant) {
      toastr.warning('Vui lòng chọn màu sắc và kích cỡ!', 'Chưa chọn sản phẩm');
      return;
    }

    if (selectedQuantity > selectedVariant.quantity) {
      toastr.error(`Số lượng yêu cầu (${selectedQuantity}) vượt quá số lượng còn lại (${selectedVariant.quantity})!`, 'Vượt quá số lượng tồn kho');
      return;
    }

    if (selectedVariant.quantity <= 0) {
      toastr.error('Sản phẩm đã hết hàng!', 'Hết hàng');
      return;
    }    // Proceed with adding to cart - chỉ chạy hiệu ứng nếu thành công (async)
    addToCart(selectedVariant, selectedQuantity).then(function(success) {
      if (success) {
        showAddToCartAnimation();
      }
    });
  });

  // Add to cart function - sử dụng API server thay vì localStorage
  function addToCart(variant, quantity) {
    // Kiểm tra trước với session hiện tại
    return $.get("/cart/items")
      .then(function(cart) {
        // Kiểm tra sản phẩm đã có trong giỏ hàng server chưa
        const existingItem = cart.find(item => item.variantId === variant.id);

        if (existingItem) {
          // Kiểm tra tổng số lượng sau khi cộng thêm
          const newTotalQuantity = existingItem.quantity + quantity;
          if (newTotalQuantity > variant.quantity) {
            toastr.error(`Không thể thêm ${quantity} sản phẩm. Bạn đã có ${existingItem.quantity} trong giỏ hàng. Tối đa có thể mua ${variant.quantity} sản phẩm.`, 'Vượt quá số lượng cho phép');
            return Promise.reject('quantity_exceeded');
          }
        }

        // Tạo cart item để gửi lên server
        const cartItem = {
          variantId: variant.id,
          variantCode: variant.variantCode,
          colorId: variant.colorId,
          colorName: variant.colorName,
          sizeId: variant.sizeId,
          sizeName: variant.sizeName,
          name: variant.productName,
          price: parseFloat(variant.price),
          image: variant.imageUrls && variant.imageUrls.length > 0 ? variant.imageUrls[0] : '',
          quantity: quantity,
          maxQuantity: variant.quantity // Thêm thông tin số lượng tồn kho
        };

        // Gửi lên server
        return $.ajax({
          url: "/cart/add",
          method: "POST",
          contentType: "application/json",
          data: JSON.stringify(cartItem)
        });
      })
      .then(function(updatedCart) {
        // Cập nhật số lượng giỏ hàng trên header từ server response
        const totalQuantity = updatedCart.reduce((sum, item) => sum + item.quantity, 0);
        $("#cart-count").text(totalQuantity);

        // Đồng bộ với localStorage để backup
        localStorage.setItem("cart", JSON.stringify(updatedCart));

        // Hiển thị thông báo thành công
        showSuccessMessage(`Đã thêm ${quantity} sản phẩm vào giỏ hàng!`);

        return true; // Trả về true khi thành công
      })
      .catch(function(error) {
        console.error("Error adding to cart:", error);
        if (error !== 'quantity_exceeded') {
          toastr.error('Không thể thêm vào giỏ hàng. Vui lòng thử lại!', 'Có lỗi xảy ra');
        }
        return false; // Trả về false khi có lỗi
      });
  }  function showSuccessMessage(message) {
    // Sử dụng toastr cho thông báo thành công
    toastr.success(message, 'Thành công');
  }

  function showAddToCartAnimation() {
    // Hiệu ứng bay ảnh vào giỏ hàng
    const productImg = $(".product-images img").first();
    const cartIcon = $("#cart-icon");

    if (productImg.length && cartIcon.length) {
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
          top: cartIcon.offset().top + 10,
          left: cartIcon.offset().left + 10,
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
  }

  window.productVariants = variants;
});

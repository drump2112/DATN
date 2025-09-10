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

  function updatePriceAndQuantity(variant) {
    $(".product-main-price").html(
      `${variant.price} <small class="text-muted">VNĐ</small>`,
    );
    $("dl dd").text(`Còn lại: ${variant.quantity} sản phẩm`);
  }

  function updateImages(imageUrls) {
    const slickContainer = $(".product-images");
    slickContainer.css("visibility", "hidden");

    if (slickContainer.hasClass("slick-initialized")) {
      slickContainer.slick("unslick");
    }
    slickContainer.empty();

    imageUrls.forEach((url) => {
      slickContainer.append(`
        <div style="width:310px;display:flex;justify-content:center;align-items:center;">
          <img src="${url}" alt="Ảnh sản phẩm"
               style="width:100%;height:310px;object-fit:contain;display:block;"/>
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

  if (uniqueColors.length > 0) renderSizesAndInfo(uniqueColors[0][0]);

  // expose biến thể cho file khác (giỏ hàng)
  window.productVariants = variants;
});

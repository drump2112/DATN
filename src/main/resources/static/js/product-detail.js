$(document).ready(function () {
  // 🚀 Lấy dữ liệu biến thể từ thẻ HTML chứa JSON (đặt ở cuối trang)
  const rawData = $("#variant-data").data("variants");
  const variants = typeof rawData === "string" ? JSON.parse(rawData) : rawData;

  console.log(rawData);

  const colorContainer = $("#color .color"); // ✅ Thẻ chứa input radio màu
  const sizeContainer = $("#size .size"); // ✅ Thẻ chứa input radio size

  // ✅ Lấy danh sách màu duy nhất (theo colorId) từ variants
  const uniqueColors = [
    ...new Map(variants.map((v) => [v.colorId, v.colorName])).entries(),
  ];
  colorContainer.empty(); // Xóa nội dung cũ (nếu có) trong phần màu

  // ✅ Render radio cho từng màu
  uniqueColors.forEach(([colorId, colorName], index) => {
    const checked = index === 0 ? "checked" : ""; // Mặc định chọn màu đầu tiên
    const radio = `
      <input type="radio" name="color" id="color-${colorId}" value="${colorId}" ${checked}>
      <label for="color-${colorId}">${colorName}</label>
    `;
    colorContainer.append(radio); // ✅ CHỖ NÀY render vào: <div class="color">
  });

  // ✅ Hàm xử lý: render các size theo màu
  function renderSizesAndInfo(colorId) {
    // 🔍 Lọc ra các biến thể có colorId được chọn
    const filteredVariants = variants.filter((v) => v.colorId === colorId);

    // ✅ Lấy danh sách size duy nhất trong màu được chọn
    let uniqueSizes = [
      ...new Map(filteredVariants.map((v) => [v.sizeId, v.sizeName])).entries(),
    ];
    uniqueSizes.sort((a, b) => Number(a[1]) - Number(b[1]));
    sizeContainer.empty(); // Xóa size cũ

    // ✅ Render radio size (có chọn sẵn size đầu tiên)
    uniqueSizes.forEach(([sizeId, sizeName], index) => {
      const checked = index === 0 ? "checked" : "";
      const radio = `
        <input type="radio" name="size" id="size-${sizeId}" value="${sizeId}" ${checked}>
        <label for="size-${sizeId}">${sizeName}</label>
      `;
      sizeContainer.append(radio); // ✅ CHỖ NÀY render vào: <div class="size">
    });

    // ✅ Lấy biến thể đầu tiên theo màu và size đầu tiên -> để hiển thị giá và số lượng
    const defaultSizeId = uniqueSizes[0]?.[0];
    const selectedVariant = filteredVariants.find(
      (v) => v.sizeId === defaultSizeId,
    );

    if (selectedVariant) {
      updatePriceAndQuantity(selectedVariant); // Gọi hàm cập nhật giá + số lượng
      updateImages(selectedVariant.imageUrls || []);
    }
  }

  // ✅ Cập nhật giá + số lượng khi chọn biến thể mới
  function updatePriceAndQuantity(variant) {
    $(".product-main-price").html(
      `${variant.price} <small class="text-muted">VNĐ</small>`,
    );
    $("dl dd").text(`Còn lại: ${variant.quantity} sản phẩm`);
  }

  // (tuỳ chọn) Cập nhật hình ảnh nếu cần

  function updateImages(imageUrls) {
    const slickContainer = $(".product-images");

    slickContainer.css("visibility", "hidden");

    // Nếu slick đã khởi tạo → hủy
    if (slickContainer.hasClass("slick-initialized")) {
      slickContainer.slick("unslick");
    }

    // Xóa ảnh cũ và thêm ảnh mới
    slickContainer.empty();
    imageUrls.forEach((url) => {
      slickContainer.append(
        `<div style="width:310px;display:flex;justify-content:center;align-items:center;">
         <img src="${url}" alt="Ảnh sản phẩm"
              style="width:100%;height:310px;object-fit:contain;display:block;"/>
       </div>`,
      );
    });
    // Khởi tạo lại slick
    slickContainer.slick({
      dots: true,
      arrows: true,
      infinite: true,
      slidesToShow: 1,
      slidesToScroll: 1,
    });
    setTimeout(() => slickContainer.css("visibility", "visible"), 50);
  }

  // ✅ SỰ KIỆN: Khi chọn màu mới (bạn hỏi phần này)
  $(document).on("change", 'input[name="color"]', function () {
    const selectedColorId = parseInt($(this).val());
    renderSizesAndInfo(selectedColorId); // ⬅️ Gọi lại để render size mới theo màu vừa chọn
  });

  // ✅ SỰ KIỆN: Khi chọn size mới
  $(document).on("change", 'input[name="size"]', function () {
    const selectedColorId = parseInt($('input[name="color"]:checked').val());
    const selectedSizeId = parseInt($(this).val());

    // Lấy đúng biến thể theo cả màu và size
    const selectedVariant = variants.find(
      (v) => v.colorId === selectedColorId && v.sizeId === selectedSizeId,
    );

    if (selectedVariant) {
      updatePriceAndQuantity(selectedVariant); // Cập nhật lại giá + số lượng
    }
  });

  // ✅ GỌI LẦN ĐẦU: render mặc định cho màu đầu tiên khi load trang
  if (uniqueColors.length > 0) {
    const defaultColorId = uniqueColors[0][0];
    renderSizesAndInfo(defaultColorId);
  }
});

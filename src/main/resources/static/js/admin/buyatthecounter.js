$(document).ready(function () {
  console.log("buyatthecounter.js loaded");
  updateClock();
  setInterval(updateClock, 1000);

  // Khởi tạo Select2 cho khách hàng
  $("#customerFilter").select2({
    placeholder: "Tìm Kiếm Khách Hàng",
    allowClear: true,
    ajax: {
      url: "/seller/buyatthecounter/select2",
      dataType: "json",
      delay: 50,
      data: (params) => ({ q: params.term }),
      processResults: (data) => ({ results: data }),
      cache: true,
    },
  });

  let tabCount = 1;
  let orders = {}; // lưu dữ liệu của từng tab theo id

  // ====== Hàm tính số lượng còn lại so với tồn kho và các tab khác ======
  function getRemainingQuantity(variantCode, currentTabId) {
    let maxQuantity = 0;

    // Lấy maxQuantity từ một tab có sản phẩm này hoặc mặc định
    for (let tab in orders) {
      const item = orders[tab].find((i) => i.code === variantCode);
      if (item) {
        maxQuantity = item.maxQuantity;
        break;
      }
    }

    let totalOtherTabs = 0;
    for (let tab in orders) {
      if (tab !== currentTabId) {
        const item = orders[tab].find((i) => i.code === variantCode);
        if (item) totalOtherTabs += item.quantity;
      }
    }

    return maxQuantity - totalOtherTabs;
  }

  // ========== Thêm tab mới ==========
  $(document).on("click", "#addTab", function (e) {
    e.preventDefault();
    tabCount++;
    const newTabId = "tab-" + tabCount;

    const newTab = `
      <li>
        <a data-toggle="tab" href="#${newTabId}">
          <i class="fa fa-bell"></i> Đơn Hàng ${tabCount}
          <button type="button" class="close close-tab" data-tab="#${newTabId}" style="font-size:12px;margin-left:6px;">×</button>
        </a>
      </li>`;
    $("#addTab").closest("li").before(newTab);

    const newTabContent = `
      <div id="${newTabId}" class="tab-pane">
        <div class="table-responsive">
          <table class="table table-striped table-hover" id="table-${newTabId}">
            <thead>
              <tr>
                <th>Ảnh</th>
                <th>Mã SPCT</th>
                <th>Tên sản phẩm</th>
                <th>Màu</th>
                <th>Size</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody></tbody>
          </table>
        </div>
      </div>`;
    $("#orderTabContent").append(newTabContent);

    orders[newTabId] = [];
    $(".nav-tabs li").removeClass("active");
    $(`.nav-tabs a[href="#${newTabId}"]`).parent().addClass("active");
    $(".tab-pane").removeClass("active");
    $(`#${newTabId}`).addClass("active");
    updateClientDetail(newTabId);
  });

  // ========== Xóa tab ==========
  $(document).on("click", ".close-tab", function (e) {
    e.stopPropagation();
    const tabId = $(this).data("tab");
    $(`a[href='${tabId}']`).parent().remove();
    $(tabId).remove();
    delete orders[tabId.replace("#", "")];

    const firstTab = $("#orderTabs li:first-child a").attr("href");
    $(".nav-tabs li:first-child").addClass("active");
    $(".tab-pane:first-child").addClass("active");
    updateClientDetail(firstTab.replace("#", ""));
  });

  // ========== Khi chuyển tab ==========
  $(document).on("shown.bs.tab", 'a[data-toggle="tab"]', function (e) {
    const activeTabId = $(e.target).attr("href").replace("#", "");
    updateClientDetail(activeTabId);
  });

  // ========== Tìm kiếm sản phẩm ==========
  $("#searchInput").on("input", function () {
    const keyword = $(this).val().trim();
    const resultsBox = $("#searchResults");
    if (keyword.length < 1) {
      resultsBox.hide();
      return;
    }

    $.get(`/api/product-variants/search?q=${keyword}`, function (data) {
      resultsBox.empty();
      if (data.length === 0) {
        resultsBox.append(
          `<div class="list-group-item">Không tìm thấy sản phẩm</div>`,
        );
      } else {
        data.forEach((p) => {
          const img =
            p.imageUrls && p.imageUrls.length > 0
              ? p.imageUrls[0]
              : "/img/no-image.png";
          const quantity = p.quantity ?? 0;
          const outOfStock = quantity === 0;

          const item = `
            <a href="#" class="list-group-item list-group-item-action ${outOfStock ? "text-muted" : ""}"
              data-id="${p.id}"
              data-name="${p.productName}"
              data-color="${p.colorName}"
              data-size="${p.sizeName}"
              data-price="${p.price}"
              data-quantity="${quantity}"
              data-image="${img}"
              data-code="${p.variantCode}">
              <div class="d-flex align-items-center row">
                <div class="col-sm-1">
                  <img src="${img}" width="40" height="40" class="me-2 rounded" style="${outOfStock ? "opacity:0.5" : ""}">
                </div>
                <div class="col-sm-10">
                  <div><b>${p.productName}</b> (${p.colorName} / ${p.sizeName})</div>
                  <small>Mã: ${p.variantCode} | ${p.price.toLocaleString()}đ</small>
                </div>
                ${outOfStock ? `<div class="text-danger"><span class="badge badge-danger">Hết hàng</span></div>` : `<span class="badge badge-info">Số lượng: ${quantity}</span>`}
              </div>
            </a>`;
          resultsBox.append(item);
        });
      }
      resultsBox.show();
    });
  });

  // ========== Chọn sản phẩm từ kết quả tìm kiếm ==========
  $(document).on("click", "#searchResults a", function (e) {
    e.preventDefault();

    const quantityAvailable = parseInt($(this).data("quantity")); // tồn kho thực tế
    if (quantityAvailable === 0) {
      toastr.warning("<b>Sản phẩm này đã hết hàng!</b>");
      return;
    }

    const activeTabId = $(".nav-tabs li.active a")
      .attr("href")
      .replace("#", "");
    const tbody = $(`#table-${activeTabId} tbody`);
    const price = parseFloat($(this).data("price"));
    const variantCode = $(this).data("code");

    // Tính tổng số lượng đã thêm ở tất cả các tab
    let totalAdded = 0;
    for (let tab in orders) {
      const item = orders[tab].find((i) => i.code === variantCode);
      if (item) totalAdded += item.quantity;
    }

    const remainingQuantity = quantityAvailable - totalAdded;
    if (remainingQuantity <= 0) {
      toastr.warning("Sản phẩm này đã hết hàng theo tồn kho thực tế!");
      return;
    }

    const existingItem = orders[activeTabId].find(
      (i) => i.code === variantCode,
    );
    if (existingItem) {
      if (existingItem.quantity < remainingQuantity) {
        existingItem.quantity++;
        existingItem.total = existingItem.price * existingItem.quantity;

        const row = tbody.find(`tr[data-code="${variantCode}"]`);
        row.find(".quantity-input").val(existingItem.quantity);
        row.find(".total").text(existingItem.total.toLocaleString() + "đ");
      } else {
        toastr.warning("Đã đạt tối đa số lượng tồn kho còn lại!");
      }
    } else {
      // thêm sản phẩm mới, số lượng khởi tạo = 1
      const product = {
        id: $(this).data("id"),
        image: $(this).data("image"),
        code: variantCode,
        name: $(this).data("name"),
        color: $(this).data("color"),
        size: $(this).data("size"),
        price,
        quantity: 1,
        total: price,
        quantityAvailable, // lưu tồn kho thực tế
      };
      orders[activeTabId].push(product);

      const row = `
        <tr data-code="${product.code}">
            <td><img src="${product.image}" width="50"></td>
            <td>${product.code}</td>
            <td>${product.name}</td>
            <td>${product.color}</td>
            <td>${product.size}</td>
            <td>
                <input type="number" class="form-control form-control-sm quantity-input"
                       min="1" max="${remainingQuantity}" value="1" style="width:70px;">
            </td>
            <td>${product.price.toLocaleString()}đ</td>
            <td class="total">${product.total.toLocaleString()}đ</td>
            <td><button class="btn btn-danger btn-sm delete-row"><i class="fa fa-trash"></i></button></td>
        </tr>`;
      tbody.append(row);
    }

    $("#searchResults").hide();
    $("#searchInput").val("");
    updateClientDetail(activeTabId);
  });

  // ========== Khi thay đổi số lượng ==========
  $(document).on("input", ".quantity-input", function () {
    const row = $(this).closest("tr");
    const activeTabId = $(".nav-tabs li.active a")
      .attr("href")
      .replace("#", "");
    const index = row.index();
    const product = orders[activeTabId][index];
    if (!product) return;

    let newQuantity = parseInt($(this).val());

    // Tính tổng số lượng của sản phẩm này ở các tab khác
    let totalOtherTabs = 0;
    for (let tab in orders) {
      if (tab !== activeTabId) {
        const item = orders[tab].find((i) => i.code === product.code);
        if (item) totalOtherTabs += item.quantity;
      }
    }

    const maxAvailable = product.quantityAvailable - totalOtherTabs;

    if (newQuantity > maxAvailable) {
      // Hiển thị cảnh báo trước khi reset
      toastr.warning("Số lượng vượt quá tồn kho thực tế!");
      newQuantity = maxAvailable;
    } else if (newQuantity < 1 || isNaN(newQuantity)) {
      newQuantity = 1;
    }

    product.quantity = newQuantity;
    product.total = product.price * product.quantity;

    // Cập nhật giao diện
    row.find(".quantity-input").val(product.quantity);
    row.find(".total").text(product.total.toLocaleString() + "đ");

    updateClientDetail(activeTabId);
  });

  // ========== Xóa dòng sản phẩm ==========
  $(document).on("click", ".delete-row", function () {
    const activeTabId = $(".nav-tabs li.active a")
      .attr("href")
      .replace("#", "");
    const index = $(this).closest("tr").index();
    orders[activeTabId].splice(index, 1);
    $(this).closest("tr").remove();
    updateClientDetail(activeTabId);
  });

  // ========== Cập nhật tổng tiền ==========
  function updateClientDetail(tabId) {
    const list = orders[tabId] || [];
    const total = list.reduce((sum, item) => sum + item.total, 0);
    $("#clientDetail")
      .find('.list-group-item:contains("Tổng Tiền Hàng") span.pull-right')
      .text(total.toLocaleString() + " VNĐ");
  }

  // ========== Hiển thị đồng hồ thực tế ==========
  function updateClock() {
    const now = new Date();
    const hours = String(now.getHours()).padStart(2, "0");
    const minutes = String(now.getMinutes()).padStart(2, "0");
    const seconds = String(now.getSeconds()).padStart(2, "0");
    const day = String(now.getDate()).padStart(2, "0");
    const month = String(now.getMonth() + 1).padStart(2, "0");
    const year = now.getFullYear();
    const timeString = `${hours}:${minutes}:${seconds} - ${day}.${month}.${year}`;
    $("#realtime-clock").text(timeString);
  }

  // Khởi tạo tab đầu tiên
  orders["tab-1"] = [];
  updateClientDetail("tab-1");

  // ========== Xử lý thanh toán ==========

  $(document).on("click", "#checkoutCounter", function () {
    const activeTabId = $(".nav-tabs li.active a")
      .attr("href")
      .replace("#", "");
    const userId = $("#customerFilter").val(); // hoặc id khách hiện chọn

    Swal.fire({
      title: "Xác nhận thanh toán?",
      text: "Bạn có chắc muốn tạo và hoàn tất đơn hàng này?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Xác nhận",
      cancelButtonText: "Hủy",
      confirmButtonColor: "#3085d6",
      cancelButtonColor: "#d33",
    }).then((result) => {
      if (result.isConfirmed) {
        checkoutCounterOrder(activeTabId, userId);
      }
    });
  });

  function checkoutCounterOrder(tabId, userId) {
    const orderItems = orders[tabId].map((i) => ({
      productVariantId: i.id, // code lưu ProductVariantId hoặc variantCode
      quantity: i.quantity,
      unitPrice: i.price,
    }));

    if (orderItems.length === 0) {
      toastr.warning("Chưa có sản phẩm nào trong đơn!");
      return;
    }

    const paymentMethod = $('input[name="paymentMethod"]:checked').val();

    $.ajax({
      url: "/api/orders/counter",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify({
        userId: userId,
        paymentMethod: paymentMethod,
        items: orderItems,
      }),
      success: function (res) {
        if (res.success) {
          toastr.success(`Tạo đơn hàng thành công! Mã đơn: ${res.orderCode}`);

          $.get(`/api/orders/${res.orderCode}/details`, function (invoice) {
            showInvoiceModal(invoice);
          }).fail(function () {
            toastr.error("Không thể tải chi tiết hóa đơn!");
          });

          // Xóa tab hiện tại hoặc reset giỏ
          orders[tabId] = [];
          $(`#table-${tabId} tbody`).empty();
          updateClientDetail(tabId);
        } else {
          toastr.error(res.message || "Tạo đơn hàng thất bại!");
        }
      },
      error: function (err) {
        toastr.error(err.responseJSON?.message || "Lỗi khi tạo đơn hàng!");
      },
    });
  }

  let currentInvoice = null;

  function showInvoiceModal(invoice) {
    currentInvoice = invoice; // lưu lại để in sau
    const html = renderInvoiceHTML(invoice);
    $("#invoiceContent").html(html);
    $("#invoiceModal").modal("show");
  }

  $(document).on("click", "#fastAddCustomer", function () {
    $("#fastAddCustomerModal").modal("show");
  });

  $(document).on("click", "#printInvoice", function () {
    if (!currentInvoice) {
      toastr.warning("Không có hóa đơn để in!");
      return;
    }

    const html = renderInvoiceHTML(currentInvoice);
    const win = window.open("", "", "height=700,width=900");
    win.document.write("<html><head><title>In hóa đơn</title>");
    win.document.write(`
    <link rel="stylesheet" href="/css/bootstrap.min.css">
    <style>
      body { font-family: Arial, sans-serif; padding: 20px; }
      .ibox-content { border: none !important; }
      .text-right { text-align: right; }
      .text-navy { color: #1ab394; font-weight: bold; }
      .well { background: #f5f5f5; padding: 10px; border-radius: 5px; }
      table { width: 100%; }
    </style>
  `);
    win.document.write("</head><body>");
    win.document.write(html);
    win.document.write("</body></html>");
    win.document.close();
    win.print();
  });

  $(document).on("click", "#canclePrint", function () {
    // Đóng modal hóa đơn, không in, không hủy đơn
    $("#invoiceModal").modal("hide");

    // Có thể reset lại form hoặc tab hiện tại nếu cần
    toastr.info("Thanh toán hoàn tất, không in hóa đơn.");
  });


  // Ẩn kết quả khi nhấn ESC trong input hoặc ngoài vùng tìm kiếm
  $("#searchInput").on("keydown", function (e) {
    if (e.key === "Escape") {
      e.preventDefault();
      $("#searchResults").hide();
      $(this).blur();
    }
  });

  $(document).on("keydown", function (e) {
    if (e.key === "Escape") {
      $("#searchResults").hide();
    }
  });

  // Ẩn khi click ra ngoài
  $(document).on("click", function (e) {
    if (!$(e.target).closest("#searchResults, #searchInput").length) {
      $("#searchResults").hide();
    }
  });

  // ===== Hàm render HTML hóa đơn =====
  function renderInvoiceHTML(invoice) {
    return `
    <div class="ibox-content p-xl">
      <div class="row">
        <div class="col-sm-6">
          <address>
            <strong> DTD- Sneaker</strong><br>
            123 Trịnh Văn Bô, Hà Nội<br>
            <abbr title="Phone">SĐT:</abbr> 0975-478-916
          </address>
        </div>

        <div class="col-sm-6 text-right">
          <h4>Mã Đơn Hàng.</h4>
          <h4 class="text-navy">${invoice.orderCode}</h4>
          <address>
            <strong>${invoice.customerName || "Khách lẻ"}</strong><br>
            ${invoice.shippingAddress || "Tại quầy"}<br>
            <abbr title="Phone">SĐT:</abbr> ${invoice.customerPhone || "-"}
          </address>
          <p>
            <span><strong>Ngày lập hóa đơn:</strong> ${new Date(invoice.orderDate).toLocaleString()}</span><br>
            <span><strong>Trạng Thái:</strong> ${invoice.status || "Đã thanh toán"}</span>
          </p>
        </div>
      </div>

      <div class="table-responsive m-t">
        <table class="invoice-table" style="width:100%; border-collapse: collapse; margin-top:20px;">
          <thead>
            <tr style="background:#f3f3f3; border-bottom:2px solid #ddd;">
              <th style="text-align:left; padding:8px; width:30%;">Tên sản phẩm</th>
              <th style="text-align:center; padding:8px; width:10%;">Màu</th>
              <th style="text-align:center; padding:8px; width:10%;">Size</th>
              <th style="text-align:center; padding:8px; width:10%;">SL</th>
              <th style="text-align:right; padding:8px; width:20%;">Đơn giá</th>
              <th style="text-align:right; padding:8px; width:20%;">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${invoice.items
        .map(
          (i) => `
              <tr style="border-bottom:1px solid #eee;">
                <td style="padding:6px;">${i.productName}</td>
                <td style="text-align:center;">${i.color || "-"}</td>
                <td style="text-align:center;">${i.size || "-"}</td>
                <td style="text-align:center;">${i.quantity}</td>
                <td style="text-align:right;">${i.unitPrice.toLocaleString()}đ</td>
                <td style="text-align:right;">${i.totalPrice.toLocaleString()}đ</td>
              </tr>
            `,
        )
        .join("")}
          </tbody>
        </table>
      </div>

      <table class="invoice-total" style="width:100%; margin-top:20px; border-collapse: collapse;">
        <tbody>
          <tr>
            <td style="text-align:right; padding:6px;"><strong>Tổng tiền hàng:</strong></td>
            <td style="text-align:right; padding:6px; width:150px;">${invoice.totalAmount.toLocaleString()}đ</td>
          </tr>
          <tr>
            <td style="text-align:right; padding:6px;"><strong>Giảm giá:</strong></td>
            <td style="text-align:right; padding:6px;">${(invoice.discountAmount || 0).toLocaleString()}đ</td>
          </tr>
          <tr>
            <td style="text-align:right; padding:6px;"><strong>Tổng thanh toán:</strong></td>
            <td style="text-align:right; padding:6px;"><span class="text-navy" style="color:#1ab394; font-weight:bold;">${invoice.finalAmount.toLocaleString()}đ</span></td>
          </tr>
        </tbody>
      </table>

      <div class="well m-t" style="margin-top:20px; background:#f9f9f9; padding:10px; border-radius:5px;">
        <strong>Ghi chú:</strong> Cảm ơn quý khách đã mua hàng tại DTD-Sneaker!
      </div>

   ${invoice.paymentMethod === "BANK_TRANSFER"
        ? `
      <div style="text-align:center; margin-top:20px;">
        <h4>Mã QR Thanh Toán</h4>
        <img src="https://img.vietqr.io/image/MB-9704223451-compact.png?amount=${invoice.finalAmount}&addInfo=ThanhToan-${invoice.orderCode}"
             alt="QR Thanh Toán" width="200" height="200">
        <p><i>Quét mã để thanh toán chuyển khoản</i></p>
      </div>
    `
        : ""
      }

</div>
`;
  }

  // +++++++++++++ VOUCHER ++++++++++++++//

  $(document).on("click", "#showListVoucher", function () {
    $.ajax({
      type: 'GET',
      url: `/seller/buyatthecounter/discounts`,
      dataType: 'json',
      success: function (data) {
        const voucherList = data;
        loadHtmlVoucherSelectList(voucherList);

        $("#listVoucher").modal("show");
      },
      error: function () {
        Swal.fire({
          title: "Lỗi",
          text: "Có lỗi xảy ra khi tải danh sách voucher",
          icon: "error"
        });
      }
    });
  });

  function loadHtmlVoucherSelectList(voucherList) {
    let html = '';

    if (voucherList.length > 0) {
      voucherList.forEach(item => {
        const amount = parseInt(item.discountValue);
        const endDate = new Date(item.endDate);
        const formattedDate = endDate.toLocaleDateString('vi-VN');
        const discountType = item.discountType;
        if (discountType === 'FIXED') {
          html += `
          <div class="col-12 col-sm-6 col-md-4 mb-3">
            <div data-voucher="${item.id}"
                class="voucher-item card shadow-sm h-100 cursor-pointer"
                style="border: 1px solid #9caec2; border-radius: 10px; padding: 6px; margin-bottom: 10px">
              <div class="card-body d-flex flex-column justify-content-between"
                  style="padding: 16px;">
                <div>
                  <h5 class="text-primary mb-2" style="font-weight: 600;">
                    Giảm <strong>${amount.toLocaleString()} VNĐ</strong>
                  </h5>
                  <p class="text-muted mb-5" style="font-size: 14px;">HSD: ${formattedDate}</p>
                </div>
                <button class="btn btn-outline-primary btn-sm w-100 select-voucher">
                  Chọn
                </button>
              </div>
            </div>
          </div>`;
        } else {
          html += `
          <div class="col-12 col-sm-6 col-md-4 mb-3">
            <div data-voucher="${item.id}"
                class="voucher-item card shadow-sm h-100 cursor-pointer"
                style="border: 1px solid #9caec2; border-radius: 10px; padding: 6px; margin-bottom: 5px">
              <div class="card-body d-flex flex-column justify-content-between"
                  style="padding: 16px;">
                <div>
                  <h5 class="text-primary mb-2" style="font-weight: 600;">
                    Giảm <strong>${amount.toLocaleString()} %</strong>
                  </h5>
                  <p class="text-muted mb-5" style="font-size: 14px;">HSD: ${formattedDate}</p>
                </div>
                <button class="btn btn-outline-primary btn-sm w-100 select-voucher">
                  Chọn
                </button>
              </div>
            </div>
          </div>`;
        }

      });
    } else {
      html = '<div class="text-center text-muted py-4">Không có mã giảm giá nào</div>';
    }

    $("#listVoucher .modal-body").html(`
    <div class="row vourcher-list" style="max-height: 400px; overflow-y: auto;">
      ${html}
    </div>
  `);
    handleClickVoucher(voucherList);
  }

  function handleClickVoucher(voucherList) {
    $(".voucher-item").on("click", function () {
      const voucherId = $(this).data("voucher");
      const voucher = voucherList.find(v => v.id == voucherId);

      if (!voucher) return;

      // Giả sử bạn có biến lưu tổng tiền đơn hàng:
      const totalOrderAmount = parseFloat($("#totalOrderAmount").text().replace(/[^\d]/g, "")) || 0;

      // Kiểm tra điều kiện áp dụng
      const now = new Date();
      const start = new Date(voucher.startDate);
      const end = new Date(voucher.endDate);

      if (totalOrderAmount < voucher.minOrderAmount) {
        Swal.fire("Không đủ điều kiện!", "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher này.", "warning");
        return;
      }
      if (now < start || now > end) {
        Swal.fire("Voucher hết hạn hoặc chưa đến ngày áp dụng!", "", "error");
        return;
      }

      // Tính giá trị giảm
      let discountAmount = 0;
      if (voucher.discountType === "PERCENT") {
        discountAmount = (totalOrderAmount * voucher.discountValue) / 100;
        if (voucher.maxDiscountValue && discountAmount > voucher.maxDiscountValue) {
          discountAmount = voucher.maxDiscountValue;
        }
      } else {
        discountAmount = voucher.discountValue;
      }

      // Cập nhật giao diện
      $("#listVoucher").modal("hide");
      $("#showListVoucher").hide();

      $("#voucherDescription").html(
        `Đang áp dụng: <strong>${voucher.code}</strong>
      (${voucher.discountType === "PERCENT" ? voucher.discountValue + "%" : voucher.discountValue.toLocaleString() + "đ"})`
      );
      $("#selectedVoucherInfo").show();

      // Cập nhật tiền giảm & tổng thanh toán
      $("#discountAmount").text(discountAmount.toLocaleString() + " đ");
      const newTotal = totalOrderAmount - discountAmount;
      $("#finalTotal").text(newTotal.toLocaleString() + " đ");

      // Lưu voucher đã chọn nếu cần
      window.selectedVoucher = voucher;

      Swal.fire("Áp dụng thành công!", "Voucher đã được áp dụng vào đơn hàng.", "success");
    });
  }

  $(document).on("click", "#cancelVoucher", function () {
    $("#selectedVoucherInfo").hide();
    $("#showListVoucher").show();

    // Reset tiền giảm
    $("#discountAmount").text("0 đ");

    const totalOrderAmount = parseFloat($("#totalOrderAmount").text().replace(/[^\d]/g, "")) || 0;
    $("#finalTotal").text(totalOrderAmount.toLocaleString() + " đ");

    window.selectedVoucher = null;

    Swal.fire("Đã hủy voucher", "Bạn có thể chọn lại voucher khác.", "info");
  });


});

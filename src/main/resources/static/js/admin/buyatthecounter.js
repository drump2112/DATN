$(document).ready(function () {
  console.log("buyatthecounter.js loaded");

  // Cấu hình toastr để hiển thị ở góc trên phải
  toastr.options = {
    "closeButton": true,
    "debug": false,
    "newestOnTop": true,
    "progressBar": true,
    "positionClass": "toast-top-right",
    "preventDuplicates": false,
    "onclick": null,
    "showDuration": "300",
    "hideDuration": "1000",
    "timeOut": "3000",
    "extendedTimeOut": "1000",
    "showEasing": "swing",
    "hideEasing": "linear",
    "showMethod": "fadeIn",
    "hideMethod": "fadeOut"
  };

  updateClock();
  setInterval(updateClock, 1000);

  loadQuickProductList();


  // Khởi tạo Select2 cho khách hàng
  $("#customerFilter").select2({
    placeholder: "Tìm Kiếm Khách Hàng (Tên hoặc SĐT)",
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

  // Khi chọn khách hàng, lưu vào state của tab hiện tại
  $("#customerFilter").on("select2:select select2:clear", function (e) {
    const activeTabId = $(".nav-tabs li.active a").attr("href").replace("#", "");
    if (!orders[activeTabId]) return;
    const val = $(this).val();
    orders[activeTabId].customerId = val ? val : null;
  });


  // orders will hold per-tab state: { items: [...], customerId: null, voucher: null, discountAmount: 0 }
  let orders = {};
  let tabIndex = 1; // chỉ đếm thứ tự thực tế
  const MAX_TABS = 6;



  // ====== Hàm tính số lượng còn lại so với tồn kho và các tab khác ======
  function getRemainingQuantity(variantCode, currentTabId) {
    let maxQuantity = 0;


    // Lấy maxQuantity từ một tab có sản phẩm này hoặc mặc định
    for (let tab in orders) {
      const item = orders[tab].items ? orders[tab].items.find((i) => i.code === variantCode) : null;
      if (item) {
        maxQuantity = item.maxQuantity;
        break;
      }
    }


    let totalOtherTabs = 0;
    for (let tab in orders) {
      if (tab !== currentTabId) {
        const item = orders[tab].items ? orders[tab].items.find((i) => i.code === variantCode) : null;
        if (item) totalOtherTabs += item.quantity;
      }
    }


    return maxQuantity - totalOtherTabs;
  }


  // ========== Thêm tab mới ==========
  $(document).on("click", "#addTab", function (e) {
    e.preventDefault();

    const currentTabCount = Object.keys(orders).length;
    if (currentTabCount >= MAX_TABS) {
      toastr.warning(`Hoàn thành các đơn `);
      return;
    }

    const newTabId = `tab-${Date.now()}`; // id duy nhất
    const orderNumber = currentTabCount + 1;

    const newTab = `
      <li class="nav-item">
        <a class="nav-link" data-toggle="tab" href="#${newTabId}">
          <i class="fa fa-shopping-basket"></i> Đơn Hàng ${orderNumber}
          <button type="button" class="close close-tab" data-tab="#${newTabId}" style="font-size:12px;margin-left:6px;">×</button>
        </a>
      </li>`;
    $("#addTab").closest("li").before(newTab);

    const newTabContent = `
      <div id="${newTabId}" class="tab-pane">
        <div class="order-table-body">
          <table class="table table-striped table-hover" id="table-${newTabId}">
            <tbody></tbody>
          </table>
        </div>
      </div>`;

    $("#orderTabContent").append(newTabContent);

    // thêm dữ liệu vào orders (per-tab state)
    orders[newTabId] = { items: [], customerId: null, voucher: null, discountAmount: 0 };

    // active tab mới bằng Bootstrap API
    $(`.nav-tabs a[href="#${newTabId}"]`).tab('show');

    updateTabLabels();
    updateClientDetail(newTabId);

    // Debug: log để kiểm tra
    console.log(`Created new tab: ${newTabId}`, orders[newTabId]);
  });


  // ========== Xóa tab ==========
  $(document).on("click", ".close-tab", function (e) {
    e.stopPropagation();
    const tabId = $(this).data("tab");
    $(`a[href='${tabId}']`).closest("li").remove();
    $(tabId).remove();
    delete orders[tabId.replace("#", "")];

    // Nếu không còn tab nào thì tạo lại tab đầu tiên
    if (Object.keys(orders).length === 0) {
      $("#addTab").trigger("click");
      return;
    }

    // Kích hoạt tab đầu tiên bằng Bootstrap API
    const firstTab = $(".nav-tabs li:not(:last) a").first();
    firstTab.tab('show');

    updateTabLabels();
    updateClientDetail(firstTab.attr("href").replace("#", ""));
  });

  function updateTabLabels() {
    $(".nav-tabs li:not(:last) a").each(function (index) {
      const orderNumber = index + 1;
      $(this).html(`
        <i class="fa fa-shopping-basket"></i> Đơn Hàng ${orderNumber}
        <button type="button" class="close close-tab" data-tab="${$(this).attr("href")}" style="font-size:12px;margin-left:6px;">×</button>
      `);
    });
  }


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

    const quantityAvailable = parseInt($(this).data("quantity"));
    if (quantityAvailable === 0) {
      toastr.warning("<b>Sản phẩm này đã hết hàng!</b>");
      return;
    }

    const activeTabId = $(".nav-tabs li.active a")
      .attr("href")
      .replace("#", "");
    console.log(`Adding product to active tab: ${activeTabId}`);
    const tbody = $(`#table-${activeTabId} tbody`);
    const price = parseFloat($(this).data("price"));
    const variantCode = $(this).data("code");

    // Tính tổng số lượng đã thêm ở tất cả các tab
    let totalAdded = 0;
    for (let tab in orders) {
      const item = orders[tab].items ? orders[tab].items.find((i) => i.code === variantCode) : null;
      if (item) totalAdded += item.quantity;
    }

    const remainingQuantity = quantityAvailable - totalAdded;
    if (remainingQuantity <= 0) {
      toastr.warning("Sản phẩm này đã hết hàng theo tồn kho thực tế!");
      return;
    }

    // find existing in this tab
    const existingItem = orders[activeTabId].items.find((i) => i.code === variantCode);
    if (existingItem) {
      // Remove empty cart message when updating existing product
      tbody.find('.empty-cart-row').remove();

      if (existingItem.quantity < remainingQuantity) {
        existingItem.quantity++;
        existingItem.total = existingItem.price * existingItem.quantity;

        const row = tbody.find(`tr[data-code="${variantCode}"]`);
        row.find(".quantity-input").val(existingItem.quantity);
        row.find(".total").text(existingItem.total.toLocaleString() + "đ");

        // Show update notification
        toastr.info(`<b>Đã cập nhật số lượng!</b><br>${$(this).data("name")} - SL: ${existingItem.quantity}`);
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
      // Remove empty cart message before adding product
      tbody.find('.empty-cart-row').remove();

      orders[activeTabId].items.push(product);

      // Show success notification
      toastr.success(`<b>Đã thêm sản phẩm vào đơn hàng!</b><br>${product.name} - ${product.color}/${product.size}`);      const row = `
        <tr data-code="${product.code}">
            <td><img src="${product.image}"></td>
            <td title="${product.code}">${product.code.length > 8 ? product.code.substring(0, 8) + '...' : product.code}</td>
            <td title="${product.name}">${product.name.length > 15 ? product.name.substring(0, 15) + '...' : product.name}</td>
            <td>${product.color}</td>
            <td>${product.size}</td>
            <td>
                <input type="number" class="form-control quantity-input" min="1" max="${remainingQuantity}" value="1">
            </td>
            <td>${product.price.toLocaleString()}đ</td>
            <td class="total" style="font-weight: bold; color: #1ab394;">${product.total.toLocaleString()}đ</td>
            <td><button class="btn btn-danger btn-xs delete-row" title="Xóa"><i class="fa fa-trash"></i></button></td>
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
    const product = orders[activeTabId].items[index];
    if (!product) return;

    let newQuantity = parseInt($(this).val());

    // Tính tổng số lượng của sản phẩm này ở các tab khác
    let totalOtherTabs = 0;
    for (let tab in orders) {
      if (tab !== activeTabId) {
        const item = orders[tab].items.find((i) => i.code === product.code);
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
    orders[activeTabId].items.splice(index, 1);
    $(this).closest("tr").remove();
    updateClientDetail(activeTabId);
  });


  // ========== Cập nhật tổng tiền ==========
  function updateClientDetail(tabId) {
    console.log(`Updating client detail for tab: ${tabId}`, orders[tabId]);
    const state = orders[tabId] || { items: [], customerId: null, voucher: null, discountAmount: 0 };
    const list = state.items;
    const total = list.reduce((sum, item) => sum + item.total, 0);

    // Handle empty cart state
    const tbody = $(`#table-${tabId} tbody`);
    if (list.length === 0) {
      // Show empty state message only if tbody is completely empty or has no product rows
      if (tbody.find('tr[data-code]').length === 0) {
        tbody.html(`
          <tr class="empty-cart-row">
            <td colspan="9" class="order-table-empty">
              <i class="fa fa-shopping-cart fa-2x" style="color: #d1d1d1; margin-bottom: 10px;"></i>
              <div>Giỏ hàng trống</div>
              <small>Thêm sản phẩm để bắt đầu tạo đơn hàng</small>
            </td>
          </tr>
        `);
      }

      state.voucher = null;
      state.discountValue = 0;
      state.discountAmount = 0
      $("#voucherDescription").html('');
      $("#discountValue").text("0 VNĐ");
      $("#finalTotal").text("0 VNĐ");
      $("#selectedVoucherInfo").hide();
      // $("#showListVoucher").show();
    } else {
      // Remove empty state message if there are products
      tbody.find('.empty-cart-row').remove();
    }
    if (total > 0 && !state.voucher) {
      $.get(`/api/vouchers/suggest?orderTotal=${total}`)
        .done(function (suggestion) {
          if (suggestion) {
            // Hiển thị voucher gợi ý
            $("#voucherSuggest").show();
            $("#suggestVoucherDesc").html(
              `<strong>${suggestion.code}</strong> - ${suggestion.name}<br>`
            );
            $("#suggestVoucherSaving").text(
              `Tiết kiệm: ${suggestion.discountAmount.toLocaleString()} VNĐ`
            );
            $("#applySuggestedVoucher").data('suggestion', suggestion);
          } else {
            $("#voucherSuggest").hide();
          }
        })
        .fail(function () {
          $("#voucherSuggest").hide();
        });
    } else {
      $("#voucherSuggest").hide();
    }

    // Cập nhật tổng tiền hàng
    $("#clientDetail")
      .find('.list-group-item:contains("Tổng Tiền Hàng") span.pull-right')
      .text(total.toLocaleString() + " VNĐ");

    // Cập nhật tổng thanh toán (finalTotal) dựa trên tổng tiền hàng và giảm giá
    const discount = parseFloat(state.discountAmount) || 0;
    const finalTotal = total - discount;
    console.log("Final Total:", finalTotal);
    $("#finalTotal").text(finalTotal.toLocaleString() + " VNĐ");

    // Update totalOrderAmount UI
    $("#totalOrderAmount").text(total.toLocaleString() + " VNĐ");

    // Update customer select UI to reflect per-tab customer
    if (state.customerId) {
      // set select2 value (assumes option may not exist locally)
      const currentVal = $("#customerFilter").val();
      if (currentVal !== String(state.customerId)) {
        // Try setting value; if option doesn't exist, create a temporary option
        if ($("#customerFilter option[value='" + state.customerId + "']").length === 0) {
          const opt = new Option("Khách #" + state.customerId, state.customerId, true, true);
          $("#customerFilter").append(opt).trigger('change');
        } else {
          $("#customerFilter").val(state.customerId).trigger('change');
        }
      }
    } else {
      // clear select2
      $("#customerFilter").val(null).trigger('change');
    }
    // Update voucher UI per tab
    if (state.voucher) {
      $("#voucherDescription").html(
        `Đang áp dụng: <strong>${state.voucher.code}</strong> (${state.voucher.discountType === 'PERCENT' ? state.voucher.discountValue + '%' : state.voucher.discountValue.toLocaleString() + ' VNĐ'})`
      );
      $("#discountValue").text((state.discountAmount || 0).toLocaleString() + " VNĐ");
      $("#discountAmount").text((state.discountAmount || 0).toLocaleString() + " đ");
      $("#selectedVoucherInfo").show();
      $("#showListVoucher").hide();
    } else {
      $("#voucherDescription").html('');
      $("#discountValue").text("0 VNĐ");
      $("#discountAmount").text("0 đ");
      $("#selectedVoucherInfo").hide();
      $("#showListVoucher").show();
    }
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

  // Khởi tạo tab đầu tiên (per-tab state)
  orders["tab-1"] = { items: [], customerId: null, voucher: null, discountAmount: 0 };
  updateClientDetail("tab-1");



  // cache products for quick list so addToCurrentOrder can access full object
  let productsCache = [];
  function loadQuickProductList() {
    $.ajax({
      url: '/api/product-variants/variants',
      method: 'GET',
      success: function (products) {
        productsCache = products || [];
        renderProductList(productsCache);
      },
      error: function () {
        $('#quickProductList').html('<p class="text-danger">Không thể tải danh sách sản phẩm.</p>');
      }
    });
  }

  // Render danh sách sản phẩm ra HTML
  function renderProductList(products) {
    const container = $('#quickProductList');
    container.empty();
    if (!products || products.length === 0) {
      container.html('<p class="text-muted">Không có sản phẩm nào.</p>');
      return;
    }

    products.forEach((p, idx) => {
      const isOutOfStock = p.quantity <= 0;
      const stockClass = isOutOfStock ? 'text-danger' : (p.quantity <= 5 ? 'text-warning' : 'text-muted');
      const stockText = isOutOfStock ? 'HẾT HÀNG' : `SL: ${p.quantity}`;

      const html = `
            <div class="col-md-6 mb-3">
              <div class="ibox ${isOutOfStock ? 'out-of-stock' : ''}" style="margin-bottom: 10px;">
                <div class="ibox-content product-box" style="padding: 10px;">
                  <div class="row">
                    <div class="col-sm-4">
                      <div class="product-imitation ${isOutOfStock ? 'opacity-50' : ''}" style="position: relative; height: 60px;">
                        <img src="${p.imageUrls && p.imageUrls.length > 0 ? p.imageUrls[0] : '/img/no-image.png'}"
                          alt="${p.productName}" style="width: 100%; height: 60px; object-fit: cover; border-radius: 4px;">
                        ${isOutOfStock ? '<div class="out-of-stock-overlay"><span style="font-size: 10px;">HẾT HÀNG</span></div>' : ''}
                      </div>
                    </div>
                    <div class="col-sm-8">
                      <div class="product-desc">
                        <div style="font-size: 12px; font-weight: bold; color: #1ab394;">${p.price.toLocaleString()} VNĐ</div>
                        <div style="font-size: 11px; margin: 2px 0;">${p.productName}</div>
                        <div style="font-size: 10px;" class="${stockClass}">${p.colorName} / ${p.sizeName}</div>
                        <div style="font-size: 10px; margin: 2px 0;" class="${stockClass}"><strong>${stockText}</strong></div>
                        <button class="btn btn-xs btn-outline btn-primary addToOrderBtn ${isOutOfStock ? 'disabled' : ''}"
                          data-idx="${idx}" data-id="${p.id}" data-name="${p.name}" data-price="${p.price}"
                          ${isOutOfStock ? 'disabled' : ''} style="font-size: 10px; padding: 2px 6px; margin-top: 2px;">
                          <i class="fa fa-cart-plus"></i> ${isOutOfStock ? 'Hết hàng' : 'Thêm'}
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>`;
      container.append(html);
    });

    // Gắn sự kiện thêm sản phẩm vào đơn: truyền toàn bộ object sản phẩm
    $('.addToOrderBtn').click(function () {
      const idx = $(this).data('idx');
      const product = productsCache[idx];
      if (!product) {
        toastr.error('Sản phẩm không hợp lệ');
        return;
      }
      addToCurrentOrder(product);
    });
  }

  // Hàm thêm sản phẩm vào đơn hàng giống hành vi khi chọn từ search
  function addToCurrentOrder(product) {
    // product cần có các trường: id, variantCode (hoặc variant code), productName, imageUrls, colorName, sizeName, price, quantity (tồn kho)
    const activeTabId = $('.nav-tabs li.active a').attr('href').replace('#', '');
    if (!orders[activeTabId]) orders[activeTabId] = { items: [], customerId: null, voucher: null, discountAmount: 0 };

    const tbody = $(`#table-${activeTabId} tbody`);

    const variantCode = product.variantCode || product.id || product.variant_code || product.code;
    const quantityAvailable = parseInt(product.quantity ?? product.qty ?? 0);
    const price = parseFloat(product.price ?? 0);

    if (quantityAvailable === 0) {
      toastr.warning('<b>Sản phẩm này đã hết hàng!</b>');
      return;
    }

    // Tính tổng đã thêm ở các tab
    let totalAdded = 0;
    for (let tab in orders) {
      const item = orders[tab].items ? orders[tab].items.find((i) => i.code === variantCode) : null;
      if (item) totalAdded += item.quantity;
    }

    const remainingQuantity = quantityAvailable - totalAdded;
    if (remainingQuantity <= 0) {
      toastr.warning('Sản phẩm này đã hết hàng theo tồn kho thực tế!');
      return;
    }

    // Nếu đã tồn tại trong tab hiện tại, tăng số lượng
    const existingItem = orders[activeTabId].items.find((i) => i.code === variantCode);
    if (existingItem) {
      // Remove empty cart message when updating existing product
      tbody.find('.empty-cart-row').remove();

      if (existingItem.quantity < remainingQuantity) {
        existingItem.quantity++;
        existingItem.total = existingItem.price * existingItem.quantity;

        const row = tbody.find(`tr[data-code="${variantCode}"]`);
        row.find('.quantity-input').val(existingItem.quantity);
        row.find('.total').text(existingItem.total.toLocaleString() + 'đ');

        // Show update notification
        toastr.info(`<b>Đã cập nhật số lượng!</b><br>${product.productName || product.name} - SL: ${existingItem.quantity}`);
      } else {
        toastr.warning('Đã đạt tối đa số lượng tồn kho còn lại!');
      }
    } else {
      // Thêm mới
      const prodObj = {
        id: product.id,
        image: (product.imageUrls && product.imageUrls.length > 0) ? product.imageUrls[0] : (product.image || '/img/no-image.png'),
        code: variantCode,
        name: product.productName || product.name || product.title || 'Sản phẩm',
        color: product.colorName || product.color || '-',
        size: product.sizeName || product.size || '-',
        price: price,
        quantity: 1,
        total: price,
        quantityAvailable: quantityAvailable,
      };

      // Remove empty cart message before adding product
      tbody.find('.empty-cart-row').remove();

      orders[activeTabId].items.push(prodObj);

      // Show success notification
      toastr.success(`<b>Đã thêm sản phẩm vào đơn hàng!</b><br>${prodObj.name} - ${prodObj.color}/${prodObj.size}`);

      const row = `
        <tr data-code="${prodObj.code}">
            <td><img src="${prodObj.image}"></td>
            <td title="${prodObj.code}">${prodObj.code.length > 8 ? prodObj.code.substring(0, 8) + '...' : prodObj.code}</td>
            <td title="${prodObj.name}">${prodObj.name.length > 15 ? prodObj.name.substring(0, 15) + '...' : prodObj.name}</td>
            <td>${prodObj.color}</td>
            <td>${prodObj.size}</td>
            <td>
                <input type="number" class="form-control quantity-input" min="1" max="${remainingQuantity}" value="1">
            </td>
            <td>${prodObj.price.toLocaleString()}đ</td>
            <td class="total" style="font-weight: bold; color: #1ab394;">${prodObj.total.toLocaleString()}đ</td>
            <td><button class="btn btn-danger btn-xs delete-row" title="Xóa"><i class="fa fa-trash"></i></button></td>
        </tr>`;
      tbody.append(row);

      // Cập nhật lại giá và gợi ý voucher sau khi thêm sản phẩm
      updateClientDetail(activeTabId);
    }

    updateClientDetail(activeTabId);
  }

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
        checkOutCounterOrder(activeTabId, userId);
      }
    });
  });

  function showQrModal(qrUrl, orderCode, tabId, userId, paymentMethod, orderItems, voucherId, discountAmount) {
    $("#qrImage").attr("src", qrUrl);
    $("#qrOrderCode").text(orderCode);
    $("#qrPaymentModal").modal("show");

    // Khi bấm “Đã Thanh Toán”
    $("#confirmQrPaid").off("click").on("click", function () {
      $("#qrPaymentModal").modal("hide");

      $("#qrPaymentModal").one("hidden.bs.modal", function () {
        toastr.info("Đang tạo đơn hàng...");

        createCounterOrder(userId, paymentMethod, orderItems, tabId, voucherId, discountAmount);
      });
    });

  }

  // Unified checkout that uses per-tab state
  function checkOutCounterOrder(tabId) {
    const state = orders[tabId];
    if (!state) return;
    const orderItems = state.items.map((i) => ({
      productVariantId: i.id,
      quantity: i.quantity,
      unitPrice: i.price,
    }));

    if (orderItems.length === 0) {
      toastr.warning("Chưa có sản phẩm nào trong đơn!");
      return;
    }

    const paymentMethod = $('input[name="paymentMethod"]:checked').val();
    const voucherId = state.voucher ? state.voucher.id : null;
    const discountAmount = state.discountAmount || 0;
    const userId = state.customerId || null;

    if (paymentMethod === "TRANSFER") {
      const totalAmount = parseFloat($("#finalTotal").text().replace(/[^\d]/g, "")) || 0;
      const orderCode = "HD" + Date.now();
      const qrUrl = `https://img.vietqr.io/image/970436-0691000350665-compact.png?amount=${totalAmount}&addInfo=${orderCode}`;

      showQrModal(qrUrl, orderCode, tabId, userId, paymentMethod, orderItems, voucherId, discountAmount);

    } else {
      createCounterOrder(userId, paymentMethod, orderItems, tabId, voucherId, discountAmount);
    }
  }

  function createCounterOrder(userId, paymentMethod, orderItems, tabId, voucherId, discountAmount) {
    $.ajax({
      url: "/api/orders/counter",
      method: "POST",
      contentType: "application/json",
      data: JSON.stringify({
        userId: userId,
        paymentMethod: paymentMethod,
        items: orderItems,
        voucherId: voucherId || null,
        discountAmount: discountAmount || 0,
      }),
      success: function (res) {
        if (res.success) {
          toastr.success(`Tạo đơn hàng thành công! Mã đơn: ${res.orderCode}`);

          $.get(`/api/orders/${res.orderCode}/details`, function (invoice) {
            showInvoiceModal(invoice);
          }).fail(function () {
            toastr.error("Không thể tải chi tiết hóa đơn!");
          });

          $("#showListVoucher").show();
          $("#selectedVoucherInfo").hide();
          $("#discountValue").text("0 VNĐ");
          $("#finalTotal").text("0 VNĐ");

          // Cập nhật số lượng sản phẩm trong cache
          updateProductQuantitiesAfterOrder(orderItems);

          // Reset tab to empty per-tab state
          orders[tabId] = { items: [], customerId: null, voucher: null, discountAmount: 0 };
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

  // Function để cập nhật số lượng sản phẩm sau khi thanh toán thành công
  function updateProductQuantitiesAfterOrder(orderItems) {
    console.log("🔄 Updating product quantities after successful order...");

    // Cập nhật productsCache
    orderItems.forEach(orderItem => {
      const productInCache = productsCache.find(p => p.id === orderItem.productVariantId);
      if (productInCache) {
        const newQuantity = Math.max(0, productInCache.quantity - orderItem.quantity);
        console.log(`📦 Product ${productInCache.variantCode}: ${productInCache.quantity} → ${newQuantity}`);
        productInCache.quantity = newQuantity;
      }
    });

    // Re-render lại danh sách sản phẩm với số lượng mới
    renderProductList(productsCache);

    // Thông báo
    toastr.info("Đã cập nhật số lượng sản phẩm!");
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

  // Thêm khách hàng nhanh
  $(document).on("click", "#addCustomer", function () {
    const fullName = $("#fastAddCustomerModal input[name='fullName']").val().trim();
    const phone = $("#fastAddCustomerModal input[name='phone']").val().trim();

    if (!fullName) {
      toastr.error("Vui lòng nhập tên khách hàng!");
      return;
    }

    if (!phone) {
      toastr.error("Vui lòng nhập số điện thoại!");
      return;
    }

    // Validate phone number (Vietnam format)
    const phoneRegex = /^0[3|5|7|8|9][0-9]{8}$|^0[2][0-9]{9}$/;
    if (!phoneRegex.test(phone)) {
      toastr.error("Số điện thoại không đúng định dạng!");
      return;
    }

    $.ajax({
      url: "/admin/customers/quick-add",
      method: "POST",
      data: { fullName: fullName, phone: phone },
      success: function (response) {
        toastr.success(response.message);
        $("#fastAddCustomerModal").modal("hide");
        $("#fastAddCustomerModal input").val(""); // Clear form

        // Refresh customer list in select2
        $("#customerFilter").val(null).trigger("change");
      },
      error: function (xhr) {
        const errorMessage = xhr.responseJSON?.message || "Thêm khách hàng thất bại!";
        toastr.error(errorMessage);
      }
    });
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
                <td style="padding:6px;">${i.productName}- ${i.variantCode} </td>
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
            <td style="text-align:right; padding:6px; width:150px;">${invoice.totalAmount.toLocaleString()} VNĐ</td>
          </tr>
          <tr>
            <td style="text-align:right; padding:6px;"><strong>Giảm giá:</strong></td>
            <td style="text-align:right; padding:6px;">${(invoice.discountAmount || 0).toLocaleString()} VNĐ</td>
          </tr>
          <tr>
            <td style="text-align:right; padding:6px;"><strong>Tổng thanh toán:</strong></td>
            <td style="text-align:right; padding:6px;"><span class="text-navy" style="color:#1ab394; font-weight:bold;">${invoice.finalAmount.toLocaleString()} VNĐ</span></td>
          </tr>
           <tr>
            <td style="text-align:right; padding:6px;"><strong>Hình Thức Thanh Toán:</strong></td>
            <td style="text-align:right; padding:6px;"><span class="text-navy" style="color:#1ab394; font-weight:bold;">${invoice.paymentMethod === 'CASH' ? 'Tiền Mặt' : 'Chuyển Khoản'}</span></td>
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
    const total = parseFloat($("#totalOrderAmount").text().replace(/[^\d]/g, "")) || 0;

    $.ajax({
      type: "GET",
      url: `/api/vouchers/available`,
      data: { orderTotal: total },
      dataType: "json",
      success: function (data) {
        const voucherList = data;
        // loadHtmlVoucherSelectList(voucherList);
        renderVouchers(voucherList, total)
        $("#voucherModal").modal("show");
      },
      error: function (xhr) {
        console.error("Lỗi khi tải danh sách voucher:", xhr);
        Swal.fire({
          title: "Lỗi",
          text: "Không thể tải danh sách voucher khả dụng. Vui lòng thử lại.",
          icon: "error"
        });
      }
    });
  });


  function renderVouchers(vouchers, subtotal) {
    const $grid = $("#voucherGrid");
    if ($grid.length === 0) return;
    $grid.empty();

    let best = null;
    vouchers.forEach((v) => {
      if (!best || v.discountAmount > best.discountAmount) best = v;
    });

    vouchers.forEach((v) => {
      const disabled = v.discountAmount <= 0;
      const isBest = best && v.code === best.code;

      const $card = $(`
      <div class="voucher-card ${disabled ? "disabled" : ""}" data-code="${v.code}">
        ${isBest ? '<span class="best-badge">Gợi ý tốt nhất</span>' : ""}
        <div class="voucher-header mb-2">
          <b>${v.code}</b>
          <div class="small text-muted">${v.name || ""}</div>
          ${v.minOrderAmount
          ? `<div class='small text-muted'>Yêu cầu tối thiểu: ${v.minOrderAmount.toLocaleString()} VNĐ</div>`
          : ""
        }
        </div>
        <div class="voucher-footer d-flex justify-content-between align-items-center">
          <span class="badge">${v.discountAmount.toLocaleString()} VNĐ</span>
          <button class="btn btn-sm btn-success apply-voucher" ${disabled ? "disabled" : ""}>Áp dụng</button>
        </div>
      </div>
    `);

      $card.data('voucher', v);

      $grid.append($card);
    });
  }

  // Sự kiện áp dụng
  $(document)
    .off("click", ".apply-voucher")
    .on("click", ".apply-voucher", function () {
      const $card = $(this).closest(".voucher-card");

      const voucher = $card.data('voucher');
      if (!voucher) return;

      const totalOrderAmount =
        parseFloat($("#totalOrderAmount").text().replace(/[^\d]/g, "")) || 0;

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

      let computedDiscount = 0;
      if (voucher.discountType === "PERCENT") {
        computedDiscount = (totalOrderAmount * voucher.discountValue) / 100;
        if (voucher.maxDiscountValue && computedDiscount > voucher.maxDiscountValue) {
          computedDiscount = voucher.maxDiscountValue;
        }
      } else {
        computedDiscount = voucher.discountValue;
      }

      $("#voucherModal").modal("hide");
      $("#showListVoucher").hide();

      $("#voucherDescription").html(
        `Đang áp dụng: <strong>${voucher.code}</strong> (${voucher.discountType === "PERCENT"
          ? voucher.discountValue + "%"
          : voucher.discountValue.toLocaleString() + " VNĐ"
        })`
      );

      $("#discountValue").text(computedDiscount.toLocaleString() + " VNĐ");
      $("#selectedVoucherInfo").show();

      const newTotal = totalOrderAmount - computedDiscount;
      $("#finalTotal").text(newTotal.toLocaleString() + " VNĐ");

      const activeTabId = $(".nav-tabs li.active a").attr("href").replace("#", "");
      if (orders[activeTabId]) {
        orders[activeTabId].voucher = voucher;
        orders[activeTabId].discountAmount = computedDiscount;
      }

      Swal.fire("Áp dụng thành công!", "Voucher đã được áp dụng vào đơn hàng.", "success");
    });

  $(document).on("click", "#cancelVoucher", function () {
    $("#selectedVoucherInfo").hide();
    $("#showListVoucher").show();

    const activeTabId = $(".nav-tabs li.active a").attr("href").replace("#", "");
    if (orders[activeTabId]) {
      orders[activeTabId].voucher = null;
      orders[activeTabId].discountAmount = 0;
    }

    $("#discountValue").text("0 VNĐ");

    const totalOrderAmount = parseFloat($("#totalOrderAmount").text().replace(/[^\d]/g, "")) || 0;
    $("#finalTotal").text(totalOrderAmount.toLocaleString() + " VNĐ");

    Swal.fire("Đã hủy voucher", "Bạn có thể chọn lại voucher khác.", "info");

    updateClientDetail(activeTabId);
  });

  $(document).on("click", "#applySuggestedVoucher", function () {
    const suggestion = $(this).data('suggestion');
    if (!suggestion) return;

    const activeTabId = $(".nav-tabs li.active a").attr("href").replace("#", "");
    if (!orders[activeTabId]) return;

    $("#voucherSuggest").hide();
    $("#showListVoucher").hide();
    $("#voucherDescription").html(
      `Đang áp dụng: <strong>${suggestion.code}</strong> (${suggestion.discountType === 'PERCENT' ? suggestion.discountValue + '%' : suggestion.discountValue.toLocaleString() + ' VNĐ'})`
    );
    $("#selectedVoucherInfo").show();

    $("#discountValue").text(suggestion.discountAmount.toLocaleString() + " VNĐ");
    $("#finalTotal").text(suggestion.totalAfter.toLocaleString() + " VNĐ");

    orders[activeTabId].voucher = suggestion;
    orders[activeTabId].discountAmount = suggestion.discountAmount;

    toastr.success("Đã áp dụng voucher gợi ý thành công!");
  });

  $(document).on("input", "#customerMoney", function () {
    const customerMoney = parseFloat($(this).val()) || 0;

    let finalTotalText = $("#finalTotal").text().replace(/[^\d]/g, '');
    const finalTotal = parseFloat(finalTotalText) || 0;

    const change = customerMoney - finalTotal;

    $("#changeMoney").text(`${change > 0 ? change.toLocaleString() : 0} VNĐ`);
  });

  // ========== PRICE MONITORING  ==========
  let priceCheckInterval = null;
  let isPriceMonitoringActive = false;

  function startPriceMonitoring() {
    if (isPriceMonitoringActive) return;

    console.log("Starting price monitoring...");
    isPriceMonitoringActive = true;

    priceCheckInterval = setInterval(() => {
      checkPriceChanges();
    }, 20000); // 18 giây check một lần
  }

  function stopPriceMonitoring() {
    if (!isPriceMonitoringActive) return;

    console.log("Stopping price monitoring...");
    isPriceMonitoringActive = false;

    if (priceCheckInterval) {
      clearInterval(priceCheckInterval);
      priceCheckInterval = null;
    }
  }

  function checkPriceChanges() {
    if (!navigator.onLine) return;

    const codesInCart = getVariantCodesInCart();
    if (codesInCart.length === 0) return;

    console.log("🔍 Checking prices for:", codesInCart.length, "variants");

    $.ajax({
      url: '/api/product-variants/check-prices',
      method: 'POST',
      contentType: 'application/json',
      data: JSON.stringify({ variantCodes: codesInCart }),
      timeout: 5000,
      success: function(currentPrices) {
        detectAndHandlePriceChanges(currentPrices);
      },
      error: function(xhr, status, error) {
        console.log("Price check failed:", status, error);
      }
    });
  }

  function getVariantCodesInCart() {
    const codes = new Set();

    for (let tabId in orders) {
      if (orders[tabId].items) {
        orders[tabId].items.forEach(item => {
          codes.add(item.code);
        });
      }
    }

    return Array.from(codes);
  }

  function detectAndHandlePriceChanges(currentPrices) {
    const changedItems = [];

    for (let tabId in orders) {
      if (!orders[tabId].items) continue;

      orders[tabId].items.forEach((item, index) => {
        const currentPrice = currentPrices[item.code];

        if (currentPrice && parseFloat(currentPrice) !== parseFloat(item.price)) {
          changedItems.push({
            tabId: tabId,
            itemIndex: index,
            item: item,
            oldPrice: parseFloat(item.price),
            newPrice: parseFloat(currentPrice)
          });
        }
      });
    }

    if (changedItems.length > 0) {
      showPriceChangeAlert(changedItems);
    }
  }

  function showPriceChangeAlert(changedItems) {
    // Dừng monitoring khi hiện modal để tránh conflict
    stopPriceMonitoring();

    // Tạo HTML content cho modal
    let htmlContent = `
        <div class="price-change-alert">
            <div class="alert-description">
                <i class="fa fa-exclamation-triangle"></i>
                <strong>Giá sản phẩm trong giỏ hàng đã thay đổi!</strong>
                <br>
                <small>Vui lòng kiểm tra và cập nhật giá mới để đảm bảo tính chính xác.</small>
            </div>
            <div class="price-change-list">
    `;

    changedItems.forEach(change => {
        const priceDiff = change.newPrice - change.oldPrice;
        const diffClass = priceDiff > 0 ? 'price-increase' : 'price-decrease';
        const diffText = priceDiff > 0 ? `+${priceDiff.toLocaleString()}đ` : `${priceDiff.toLocaleString()}đ`;

        htmlContent += `
            <div class="price-change-item">
                <div class="product-name">
                    <strong>${change.item.name}</strong> (${change.item.color}/${change.item.size})
                </div>
                <div class="price-comparison">
                    <div class="old-price">
                        <div class="label">Giá cũ:</div>
                        <div class="value">${change.oldPrice.toLocaleString()}đ</div>
                    </div>
                    <div class="new-price">
                        <div class="label">Giá mới:</div>
                        <div class="value">${change.newPrice.toLocaleString()}đ</div>
                    </div>
                    <div class="price-difference ${diffClass}">
                        <div class="label">Chênh lệch:</div>
                        <div class="value">${diffText}</div>
                    </div>
                </div>
            </div>
        `;
    });

    htmlContent += `
            </div>
            <div class="alert-question">
                Bạn có muốn cập nhật giá mới cho tất cả sản phẩm trong giỏ hàng không?
            </div>
        </div>
    `;

    // Thêm CSS cho modal
    const customCSS = `
        <style>
            .price-change-alert {
                text-align: left;
                max-height: 400px;
                overflow-y: auto;
            }
            .alert-description {
                color: #856404;
                background-color: #fff3cd;
                border: 1px solid #ffeaa7;
                padding: 12px 16px;
                border-radius: 6px;
                margin-bottom: 20px;
                font-size: 14px;
            }
            .alert-description i {
                margin-right: 8px;
            }
            .price-change-list {
                margin-bottom: 20px;
            }
            .price-change-item {
                border: 1px solid #e9ecef;
                border-radius: 8px;
                padding: 15px;
                margin-bottom: 12px;
                background-color: #f8f9fa;
            }
            .product-name {
                margin-bottom: 12px;
                color: #495057;
                font-size: 16px;
            }
            .price-comparison {
                display: flex;
                flex-direction: column;
                gap: 8px;
            }
            .old-price, .new-price {
                display: flex;
                justify-content: space-between;
                align-items: center;
                padding: 8px 12px;
                border-radius: 4px;
                font-size: 14px;
            }
            .old-price {
                background-color: #f1aeb5;
                color: #721c24;
            }
            .new-price {
                background-color: #d4edda;
                color: #155724;
            }
            .price-difference {
                text-align: center;
                padding: 8px;
                border-radius: 4px;
                font-weight: bold;
                font-size: 14px;
            }
            .price-increase {
                background-color: #f8d7da;
                color: #721c24;
            }
            .price-decrease {
                background-color: #d1ecf1;
                color: #0c5460;
            }
            .label {
                font-weight: bold;
                min-width: 60px;
            }
            .value {
                flex: 1;
                text-align: center;
            }
            .total {
                font-weight: bold;
                min-width: 100px;
                text-align: right;
            }
            .alert-question {
                text-align: center;
                color: #495057;
                font-size: 16px;
                margin-top: 20px;
                padding: 15px;
                background-color: #e9ecef;
                border-radius: 6px;
                border: 1px solid #ced4da;
            }
            .price-change-footer {
                display: flex;
                justify-content: space-between;
                gap: 15px;
                padding: 20px 0 0 0;
                border-top: 1px solid #e9ecef;
                margin-top: 20px;
            }
            @media (max-width: 768px) {
                .price-change-footer {
                    flex-direction: column;
                }
                .price-change-footer .btn {
                    width: 100%;
                    margin: 5px 0;
                }
            }
        </style>
    `;

    // Tạo custom footer với buttons đồng bộ style
    const footerHtml = `
        <div class="price-change-footer">
            <button type="button" class="btn btn-secondary" id="btnCancelOrder">
                <i class="fa fa-times"></i>
                <span>Hủy cập nhật</span>
            </button>
            <button type="button" class="btn btn-primary" id="btnContinueOrder">
                <i class="fa fa-check"></i>
                <span>Cập nhật giá mới</span>
            </button>
        </div>
    `;

    Swal.fire({
        title: '<i class="fa fa-exclamation-triangle text-warning"></i> Giá sản phẩm đã thay đổi',
        html: customCSS + htmlContent,
        showConfirmButton: false,
        showCancelButton: false,
        width: '600px',
        customClass: {
            popup: 'price-change-modal',
            title: 'price-change-title',
            content: 'price-change-content'
        },
        showClass: {
            popup: 'animate__animated animate__fadeInDown'
        },
        hideClass: {
            popup: 'animate__animated animate__fadeOutUp'
        },
        footer: footerHtml,
        didOpen: () => {
            // Bind events cho custom buttons
            document.getElementById('btnCancelOrder').addEventListener('click', () => {
                // Đóng modal và tiếp tục monitoring
                Swal.close();
                toastr.info("Đã giữ nguyên giá cũ trong giỏ hàng.");
                startPriceMonitoring();
            });

            document.getElementById('btnContinueOrder').addEventListener('click', () => {
                // Cập nhật giá mới và tiếp tục monitoring
                updateAllPricesToNew(changedItems);
                Swal.close();
                toastr.success("Đã cập nhật giá mới cho tất cả sản phẩm!");
                startPriceMonitoring();
            });
        }
    });
  }

  function updateAllPricesToNew(changedItems) {
    changedItems.forEach(change => {
      const tabState = orders[change.tabId];
      if (tabState && tabState.items && tabState.items[change.itemIndex]) {
        const item = tabState.items[change.itemIndex];
        item.price = change.newPrice;
        item.total = item.price * item.quantity;

        const row = $(`#table-${change.tabId} tr[data-code="${item.code}"]`);
        if (row.length > 0) {
          row.find('td').eq(6).text(item.price.toLocaleString() + 'đ'); // Price column
          row.find('.total').text(item.total.toLocaleString() + 'đ'); // Total column
        }
      }
    });

    // Update totals for affected tabs
    const affectedTabs = [...new Set(changedItems.map(c => c.tabId))];
    affectedTabs.forEach(tabId => {
      updateClientDetail(tabId);
    });

    // Reload product list để đảm bảo tất cả tab đều có giá mới nhất
    // Tránh conflict với các tab sau này
    console.log("🔄 Reloading product list after price update...");
    loadQuickProductList();
  }

  function getTabDisplayName(tabId) {
    const tabIndex = Object.keys(orders).indexOf(tabId) + 1;
    return `Đơn ${tabIndex}`;
  }

  $(document).ready(function() {
    // ... existing code ...

    // Start price monitoring
    startPriceMonitoring();

    $(window).on('beforeunload', function() {
      stopPriceMonitoring();
    });
  });

});



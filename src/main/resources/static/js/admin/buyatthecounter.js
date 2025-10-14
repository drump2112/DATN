$(document).ready(function () {
  let orderCount = 2; // ban đầu có 2 tab

  // --- Tìm kiếm realtime ---
  $("#searchInput").on("input", function () {
    const keyword = $(this).val().trim();
    if (keyword.length < 2) {
      $("#searchResults").hide();
      return;
    }

    $.ajax({
      url: "/api/products/search",
      data: { q: keyword },
      success: function (products) {
        const list = $("#searchResults");
        list.empty();
        if (products.length === 0) {
          list.hide();
          return;
        }

        products.forEach((p) => {
          list.append(`
          <a href="#" class="list-group-item list-group-item-action search-item"
             data-id="${p.id}"
             data-name="${p.name}"
             data-image="${p.imageUrl}"
             data-color="${p.color}"
             data-size="${p.size}"
             data-price="${p.price}">
            <div class="d-flex align-items-center">
              <img src="${p.imageUrl}" width="40" height="40" class="me-2 rounded">
              <div>
                <strong>${p.name}</strong><br>
                <small>${p.color} / ${p.size}</small> - 
                <span class="text-danger">${p.price.toLocaleString()}đ</span>
              </div>
            </div>
          </a>
        `);
        });
        list.show();
      },
    });
  });

  // --- Khi chọn sản phẩm từ kết quả ---
  $(document).on("click", ".search-item", function (e) {
    e.preventDefault();
    const p = $(this).data();

    const activeTabId = $(".nav-tabs li.active a").attr("href").substring(1); // ví dụ tab-1
    const table = $(`#table-${activeTabId} tbody`);

    table.append(`
    <tr>
      <td><img src="${p.image}" width="40"></td>
      <td>${p.name}</td>
      <td>${p.color}</td>
      <td>${p.size}</td>
      <td>${p.price.toLocaleString()}đ</td>
      <td><input type="number" value="1" min="1" class="form-control input-sm quantity"></td>
      <td class="line-total">${p.price.toLocaleString()}đ</td>
      <td><button class="btn btn-danger btn-sm remove-row"><i class="fa fa-trash"></i></button></td>
    </tr>
  `);

    $("#searchResults").hide();
    $("#searchInput").val("");
  });

  // --- Xóa sản phẩm ---
  $(document).on("click", ".remove-row", function () {
    $(this).closest("tr").remove();
  });

  // --- Tính lại tổng khi thay đổi số lượng ---
  $(document).on("input", ".quantity", function () {
    const row = $(this).closest("tr");
    const price = parseInt(
      row.find("td:nth-child(5)").text().replace(/[^\d]/g, ""),
    );
    const qty = parseInt($(this).val());
    const total = price * qty;
    row.find(".line-total").text(total.toLocaleString() + "đ");
  });

  // --- Thêm tab đơn hàng mới ---
  function addOrderTab() {
    orderCount++;
    const newId = `tab-${orderCount}`;

    // Thêm tab header
    $(".nav-tabs").append(`
    <li><a data-toggle="tab" href="#${newId}"><i class="fa fa-user"></i> Đơn ${orderCount}</a></li>
  `);

    // Thêm nội dung tab mới
    $(".tab-content").append(`
    <div id="${newId}" class="tab-pane">
      <div class="table-responsive">
        <table id="table-${newId}" class="table table-striped table-hover">
          <tbody></tbody>
        </table>
      </div>
    </div>
  `);
  }
});

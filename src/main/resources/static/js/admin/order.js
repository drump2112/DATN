$(document).ready(function () {

  // Cấu hình toastr
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

  // Removed select2 dropdown functionality since we're using static badges now

  // Tìm kiếm đơn hàng với phân trang
  function searchOrder(page) {
    if (page === undefined || page === null) {
      page = 0;
    }

    console.log("🔍 Searching orders - page:", page);

    var keyword = $("#orderInput").val() ? $("#orderInput").val().trim() : "";
    var paymentMethod = $("#orderPaymentFilter").val() || "";
    var dateStart = $("#dateStartFilter").val() || "";
    var dateEnd = $("#dateEndFilter").val() || "";

    // Validate ngày tháng
    if (dateStart && dateEnd) {
      var startDate = new Date(dateStart);
      var endDate = new Date(dateEnd);

      if (startDate > endDate) {
        toastr.error("Ngày bắt đầu không thể lớn hơn ngày kết thúc!");
        return;
      }

      // Kiểm tra khoảng cách không quá 1 năm
      var daysDiff = (endDate - startDate) / (1000 * 60 * 60 * 24);
      if (daysDiff > 365) {
        toastr.warning("Khoảng thời gian tìm kiếm không nên quá 1 năm!");
      }
    }

    // Hiển thị loading
    SwalUtils.loading("Đang tìm kiếm...", "Vui lòng chờ trong giây lát");

    // Xác định orderType dựa trên URL hiện tại và dropdown selection
    var currentPath = window.location.pathname;
    var orderType = "";

    if (currentPath.includes('/completed/')) {
      // Đối với trang completed, sử dụng giá trị từ dropdown
      var selectedFilter = $("#orderTypeTilter").val();
      if (selectedFilter && selectedFilter !== "") {
        orderType = selectedFilter; // "Online" hoặc "Offline"
      }
      // Nếu không chọn gì (selectedFilter = "" hoặc null), để orderType = "" để controller hiểu là tìm completed orders
    } else if (currentPath.includes('/offline/')) {
      orderType = "Offline";
    } else if (currentPath.includes('/Online/')) {
      orderType = "Online";
    }

    console.log("Searching orders:", {
      page: page,
      keyword: keyword,
      orderType: orderType,
      paymentMethod: paymentMethod,
      dateStart: dateStart,
      dateEnd: dateEnd,
      currentPath: currentPath,
      selectedFilter: $("#orderTypeTilter").val()
    });

    $.ajax({
      url: "/admin/order/search",
      type: "GET",
      data: {
        page: page || 0,
        keyword: keyword,
        orderType: orderType,
        paymentMethod: paymentMethod,
        dateStart: dateStart,
        dateEnd: dateEnd
      },
      success: function (response) {
        console.log("Search successful, response length:", response.length);
        console.log("Response preview:", response.substring(0, 200) + "...");

        // Đóng loading
        SwalUtils.close();

        $("#productTableContainer").html(response);

        // Kiểm tra xem có phân trang được render không
        var paginationCount = $(".pagination li").length;
        console.log("Pagination items found:", paginationCount);

        // Thông báo thành công
        toastr.success('Tìm kiếm thành công!');
      },
      error: function (xhr, status, error) {
        console.error("Search failed:", {
          status: status,
          error: error,
          responseText: xhr.responseText
        });

        // Đóng loading
        SwalUtils.close();

        // Hiển thị lỗi
        toastr.error("Đã xảy ra lỗi khi tìm kiếm đơn hàng. Vui lòng thử lại!");
      }
    });
  }

  window.searchOrder = searchOrder;

  // Backup function để đảm bảo phân trang hoạt động
  window.goToPage = function(page) {
    console.log("goToPage called with:", page);
    searchOrder(page);
  };

  function toggleOrderItems(button) {
    var $btn = $(button);
    var orderId = $btn.data('id');
    if (!orderId) return;

    var $row = $('#order-items-' + orderId);
    var $container = $row.find('#order-items-content');

    // Toggle icon
    var $icon = $btn.find('i');

    if ($row.is(':visible')) {
      $row.slideUp(300, function() {
        $btn.removeClass('active');
        $icon.removeClass('fa-eye-slash').addClass('fa-eye');
      });
      return;
    }

    // Close all other open rows
    $('tr[id^="order-items-"]').slideUp(300);
    $('.btn-toggle-detail').removeClass('active').find('i').removeClass('fa-eye-slash').addClass('fa-eye');

    if ($container.data('loaded') !== true) {
      $container.html('<div class="py-4 text-center"><i class="fa fa-spinner fa-spin fa-2x" style="color: #1ab394;"></i><p class="mt-2" style="color: #676a6c;">Đang tải chi tiết đơn hàng...</p></div>');

      $.ajax({
        url: '/admin/order/' + orderId + '/items',
        type: 'GET',
        dataType: 'html',
        success: function (html) {
          $container.html(html);
          $container.data('loaded', true);
          $row.slideDown(400, function() {
            $btn.addClass('active');
            $icon.removeClass('fa-eye').addClass('fa-eye-slash');
          });
        },
        error: function (xhr, status, err) {
          console.error('Lỗi tải order items:', err);
          $container.html('<div class="text-center py-4"><div class="text-danger"><i class="fa fa-exclamation-triangle fa-2x"></i><p class="mt-2">Lỗi khi tải chi tiết đơn hàng.</p></div></div>');
          $row.slideDown(300);
        }
      });
    } else {
      $row.slideDown(400, function() {
        $btn.addClass('active');
        $icon.removeClass('fa-eye').addClass('fa-eye-slash');
      });
    }
  }

  window.toggleOrderItems = toggleOrderItems;

  // updateOrderStatus function removed - now using action buttons in detail area instead

  // Validate ngày khi thay đổi
  function validateDateRange() {
    var dateStart = $("#dateStartFilter").val();
    var dateEnd = $("#dateEndFilter").val();

    if (dateStart && dateEnd) {
      var startDate = new Date(dateStart);
      var endDate = new Date(dateEnd);

      if (startDate > endDate) {
        $("#dateEndFilter").addClass("error-input");
        toastr.error("Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu!");
        return false;
      } else {
        $("#dateStartFilter, #dateEndFilter").removeClass("error-input");
      }
    }
    return true;
  }

  // Event listeners
  $("#orderTypeTilter, #orderTypeFilter, #orderPaymentFilter").on('change', function() {
    searchOrder(0);
  });

  $("#dateStartFilter, #dateEndFilter").on('change', function() {
    if (validateDateRange()) {
      searchOrder(0);
    }
  });

  $("#orderInput").on('keyup', function(e) {
    if (e.keyCode === 13) { // Enter key
      searchOrder(0);
    }
  });

  // Clear date filters
  function clearDateFilters() {
    $("#dateStartFilter").val('').removeClass('error-input');
    $("#dateEndFilter").val('').removeClass('error-input');
    searchOrder(0);
    toastr.info("Đã xóa bộ lọc ngày tháng");
  }

  // Handle admin order actions (Xác nhận/Hủy cho PENDING orders, Đã lấy hàng cho WAITING_PICKUP orders)
  function handleAdminOrderAction(button) {
    if (button.disabled) return;

    const orderId = button.getAttribute('data-order-id');
    const action = button.getAttribute('data-action');

    let confirmConfig = {};

    switch(action) {
      case 'confirm':
        confirmConfig = {
          title: 'Xác nhận đơn hàng',
          text: 'Bạn có chắc muốn xác nhận đơn hàng này?',
          icon: 'question',
          confirmButtonText: 'Xác nhận'
        };
        break;
      case 'pickup':
        confirmConfig = {
          title: 'Xác nhận đã lấy hàng',
          text: 'Bạn có chắc muốn xác nhận đã lấy hàng cho đơn này?',
          icon: 'question',
          confirmButtonText: 'Đã lấy hàng'
        };
        break;
      case 'cancel':
        confirmConfig = {
          title: 'Hủy đơn hàng',
          text: 'Bạn có chắc muốn hủy đơn hàng này? Hành động này không thể hoàn tác.',
          icon: 'warning',
          confirmButtonText: 'Hủy đơn hàng'
        };
        break;
    }

    // Show confirmation dialog
    Swal.fire({
      title: confirmConfig.title,
      text: confirmConfig.text,
      icon: confirmConfig.icon,
      showCancelButton: true,
      confirmButtonColor: (action === 'confirm' || action === 'pickup') ? '#1ab394' : '#ed5565',
      cancelButtonColor: '#6c757d',
      confirmButtonText: confirmConfig.confirmButtonText,
      cancelButtonText: 'Hủy'
    }).then((result) => {
      if (result.isConfirmed) {
        performAdminOrderAction(orderId, action, button);
      }
    });
  }

  function performAdminOrderAction(orderId, action, button) {
    // Disable button during request
    button.disabled = true;
    const originalHTML = button.innerHTML;
    button.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Đang xử lý...';

    let endpoint = `/admin/order/${orderId}/${action}`;

    fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    .then(response => response.json())
    .then(data => {
      if (data.success) {
        // Show success message
        Swal.fire({
          title: 'Thành công!',
          text: data.message || 'Đã cập nhật trạng thái đơn hàng',
          icon: 'success',
          timer: 2000,
          showConfirmButton: false
        });

        // Cập nhật chi tiết đơn hàng mà không đóng nó lại
        refreshOrderDetailInPlace(orderId, action);
      } else {
        throw new Error(data.message || 'Có lỗi xảy ra');
      }
    })
    .catch(error => {
      console.error('Error:', error);
      Swal.fire({
        title: 'Lỗi!',
        text: error.message || 'Có lỗi xảy ra khi xử lý đơn hàng',
        icon: 'error'
      });
    })
    .finally(() => {
      // Restore button
      button.disabled = false;
      button.innerHTML = originalHTML;
    });
  }

  // Function để cập nhật chi tiết đơn hàng mà không đóng nó lại
  function refreshOrderDetailInPlace(orderId, action) {
    var $detailRow = $('#order-items-' + orderId);
    var $mainRow = $detailRow.prev('.order-row');

    // Lưu trạng thái hiển thị hiện tại
    var isVisible = $detailRow.is(':visible');

    // Xác định trạng thái mới dựa trên action
    var newStatus = '';
    var newStatusText = '';
    var newStatusBadgeClass = '';

    switch(action) {
      case 'confirm':
        newStatus = 'WAITING_PICKUP';
        newStatusText = 'Chờ lấy hàng';
        newStatusBadgeClass = 'badge badge-info';
        break;
      case 'pickup':
        newStatus = 'SHIPPING';
        newStatusText = 'Đang giao';
        newStatusBadgeClass = 'badge badge-primary';
        break;
      case 'cancel':
        newStatus = 'CANCELLED';
        newStatusText = 'Đã hủy';
        newStatusBadgeClass = 'badge badge-danger';
        break;
    }

    // Cập nhật badge trạng thái trong hàng chính của bảng
    if (newStatus) {
      var $statusCell = $mainRow.find('td').eq(9); // Cột thứ 10 (index 9) là cột trạng thái
      if ($statusCell.length) {
        $statusCell.html('<span class="' + newStatusBadgeClass + '">' + newStatusText + '</span>');
      }
    }

    // Cập nhật header bar trong chi tiết đơn hàng
    $.ajax({
      url: '/admin/order/' + orderId + '/detail-fragment',
      type: 'GET',
      dataType: 'html',
      success: function(html) {
        // Cập nhật toàn bộ nội dung chi tiết (header bar với trạng thái mới + nút action mới)
        var $detailContainer = $detailRow.find('td').first();
        $detailContainer.html(html);

        // Load lại order items vào container mới
        var $newContainer = $detailContainer.find('#order-items-content');

        $.ajax({
          url: '/admin/order/' + orderId + '/items',
          type: 'GET',
          dataType: 'html',
          success: function(itemsHtml) {
            $newContainer.html(itemsHtml);
            $newContainer.data('loaded', true);
          },
          error: function() {
            console.error('Lỗi load order items');
            $newContainer.html('<div class="text-center py-4 text-danger"><i class="fa fa-exclamation-triangle"></i> Lỗi tải chi tiết sản phẩm</div>');
          }
        });

        // Giữ trạng thái hiển thị
        if (isVisible) {
          $detailRow.show();
        }
      },
      error: function(xhr, status, err) {
        console.error('Lỗi cập nhật chi tiết đơn hàng:', err);
        // Fallback: reload toàn bộ trang nhưng giữ thông báo
        toastr.info('Đang làm mới danh sách...');
        setTimeout(function() {
          searchOrder(0);
        }, 500);
      }
    });
  }

  window.refreshOrderDetailInPlace = refreshOrderDetailInPlace;
  window.clearDateFilters = clearDateFilters;
  window.handleAdminOrderAction = handleAdminOrderAction;
})

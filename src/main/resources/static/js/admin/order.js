$(document).ready(function () {

  function formatState(state) {
    if (!state.id) return state.text; // placeholder
    let colorClass = '';
    switch (state.id) {
      case 'PENDING': colorClass = 'badge badge-primary'; break;
      case 'PROCESSING': colorClass = 'badge badge-warning'; break;
      case 'COMPLETED': colorClass = 'badge badge-success'; break;
      case 'CANCELLED': colorClass = 'badge badge-danger'; break;
    }
    return $('<span class="' + colorClass + '">' + state.text + '</span>');
  }

  // Initialize select2 on page load
  initializeSelect2();

  function initializeSelect2() {
    $('.status-dropdown').select2({
      templateResult: formatState,
      templateSelection: formatState,
      width: '100%',
      minimumResultsForSearch: -1
    });
  }

  window.initializeSelect2 = initializeSelect2;

  function searchOrder(page) {
    var keyword = $("#orderInput").val().trim();
    var orderType = $("#orderTypeTilter").val();
    var paymentMethod = $("#orderPaymentFilter").val();
    var dateStart = $("#dateStartFilter").val();
    var dateEnd = $("#dateEndFilter").val();

    $.ajax({
      url: "/admin/order/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        orderType: orderType,
        paymentMethod: paymentMethod,
        dateStart: dateStart,
        dateEnd: dateEnd
      },
      success: function (response) {
        $("#productTableContainer").html(response);
        // Khởi tạo lại select2 sau khi cập nhật
        initializeSelect2();
      },
      error: function () {
        alert("Đã xảy ra lỗi khi tìm kiếm!");
      },
    });
  }

  window.searchOrder = searchOrder;

  function toggleOrderItems(button) {
    var $btn = $(button);
    var orderId = $btn.data('id');
    if (!orderId) return;

    var $row = $('#order-items-' + orderId);
    var $container = $row.find('.order-items-container');

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

  function updateOrderStatus(selectElement) {
    var $select = $(selectElement);
    var orderId = $select.data('id');
    var newStatus = $select.val();

    if (!orderId || !newStatus) return;

    Swal.fire({
      title: 'Xác nhận thay đổi trạng thái?',
      text: 'Bạn có chắc muốn thay đổi trạng thái đơn hàng này?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Xác nhận',
      cancelButtonText: 'Hủy',
      customClass: {
        popup: 'swal-pop-zindex',
      },
      backdrop: `rgba(0,0,0,0.4)`,
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: '/admin/order/' + orderId + '/status',
          type: 'PUT',
          data: {
            status: newStatus
          },
          success: function(response) {
            Swal.fire({
              icon: 'success',
              title: 'Thành công',
              text: response.message,
              timer: 1500,
              showConfirmButton: false
            });
          },
          error: function(xhr) {
            Swal.fire({
              icon: 'error',
              title: 'Lỗi',
              text: xhr.responseJSON?.message || 'Có lỗi xảy ra khi cập nhật trạng thái'
            });
            // Reset lại giá trị cũ nếu lỗi
            location.reload();
          }
        });
      } else {
        // Reset lại giá trị cũ nếu hủy
        location.reload();
      }
    });
  }

  window.updateOrderStatus = updateOrderStatus
})

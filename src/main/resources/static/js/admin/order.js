$(document).ready(function () {
  $('.status-dropdown').select2({
    templateResult: formatState,
    templateSelection: formatState,
    width: '100%',
    minimumResultsForSearch: -1
  });

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


  function toggleOrderItems(button) {
    var $btn = $(button);
    var orderId = $btn.data('id');
    if (!orderId) return;

    var $row = $('#order-items-' + orderId);
    var $container = $row.find('.order-items-container');

    if ($row.is(':visible')) {
      $row.hide();
      return;
    }

    if ($container.data('loaded') !== true) {
      $container.html('<div class="py-3"><i class="fa fa-spinner fa-spin"></i> Đang tải...</div>');

      $.ajax({
        url: '/admin/order/' + orderId + '/items',
        type: 'GET',
        dataType: 'html',
        success: function (html) {
          $container.html(html);
          $container.data('loaded', true);
          $row.show();
        },
        error: function (xhr, status, err) {
          console.error('Lỗi tải order items:', err);
          $container.html('<div class="text-danger py-2">Lỗi khi tải chi tiết đơn hàng.</div>');
          $row.show();
        }
      });
    } else {
      $row.show();
    }
  }

  window.toggleOrderItems = toggleOrderItems
})
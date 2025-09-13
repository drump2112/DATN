$(document).ready(function () {
  initFilter();

  // Tìm kiếm sản phẩm
  function searchProductVariants(page) {
    const keyword = $("#searchInput").val().trim();
    const colorId = $("#colorFilter").val();
    const sizeId = $("#sizeFilter").val();
    const cateId = $("#cateFilter").val();

    $.ajax({
      url: "/admin/inventory/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        colorId: colorId,
        sizeId: sizeId,
        cateId: cateId,
      },
      success: function (response) {
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải danh sách sản phẩm!");
      },
    });
  }

  function initFilter() {
    $("#colorFilter").select2({
      placeholder: "Chọn Màu Sắc",
      allowClear: true,
      ajax: {
        url: "/admin/color/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#sizeFilter").select2({
      placeholder: "Chọn Kích Thước",
      allowClear: true,
      ajax: {
        url: "/admin/size/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#cateFilter").select2({
      placeholder: "Chọn danh mục",
      allowClear: true,
      ajax: {
        url: "/admin/category/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });
  }

  // window.toggleStatus = function (productId, isActive) {
  //   const title = isActive
  //     ? "Bạn có chắc muốn khóa sản phẩm này?"
  //     : "Bạn có chắc muốn kích hoạt sản phẩm này?";
  //
  //   Swal.fire({
  //     title: title,
  //     icon: "warning",
  //     showCancelButton: true,
  //     confirmButtonText: "Xác nhận",
  //     cancelButtonText: "Hủy",
  //     customClass: {
  //       popup: "swal-pop-zindex",
  //     },
  //     backdrop: `rgba(0,0,0,0.4)`,
  //   }).then((result) => {
  //     if (result.isConfirmed) {
  //       $.ajax({
  //         url: `/admin/product/${productId}/toggle-status`,
  //         type: "PUT",
  //         success: function (data) {
  //           Swal.fire("Thành công", data.message, "success");
  //           const currentPage =
  //             parseInt(
  //               $("#paginationContainer .paginate_button.active a").text(),
  //             ) - 1 || 0;
  //           searchProduct(currentPage);
  //         },
  //         error: function (xhr) {
  //           Swal.fire(
  //             "Lỗi",
  //             xhr.responseJSON?.message || "Có lỗi xảy ra",
  //             "error",
  //           );
  //         },
  //       });
  //     }
  //   });
  // };
  //
  // // Gán hàm cho window
  // window.openAddModal = openAddModal;
  window.searchProductVariants = searchProductVariants;
});

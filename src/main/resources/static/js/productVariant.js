Dropzone.autoDiscover = false;
var avatarDropzone = null;

$(document).ready(function () {
  function initSelect2s() {
    $("#sanPham").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn Sản Phẩm",
      allowClear: true,
      ajax: {
        url: "/admin/product/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#mauSac").select2({
      dropdownParent: $("#myModal"),
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

    $("#kichCo").select2({
      dropdownParent: $("#myModal"),
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

    $("#sanPham").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn Sản Phẩm",
      allowClear: true,
      ajax: {
        url: "/admin/product/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

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

    $("#statusFilter")
      .select2({
        placeholder: "Chọn Trạng Thái",
        allowClear: true,
      })
      .val(null)
      .trigger("change");
    $("#status").select2();
  }

  $("#resetFilterBtn").on("click", function () {
    $("#colorFilter").val(null).trigger("change");
    $("#sizeFilter").val(null).trigger("change");
    $("#cateFilter").val(null).trigger("change");
    $("#statusFilter").val(null).trigger("change");

    $.ajax({
      url: "/admin/productVariant/table",
      type: "GET",
      data: {
        page: 0,
        size: 5,
      },
      success: function (response) {
        // Render lại fragment bảng
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải lại bảng");
      },
    });
    // Nếu có thêm input khác
    $("#searchInput").val(""); // ví dụ ô tìm kiếm text
  });

  initSelect2s();

  avatarDropzone = new Dropzone("#avatarDropzone", {
    url: "/dummy-upload",
    autoProcessQueue: false,
    clickable: true,
    maxFiles: 3,
    acceptedFiles: "image/*",
    addRemoveLinks: true,
    dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
    dictRemoveFile: "Xóa ảnh",
    dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
    previewsContainer: "#avatarDropzone",
  });

  $(".touchspin1").TouchSpin({
    min: 0,
    max: 999999,
    buttondown_class: "btn btn-white",
    buttonup_class: "btn btn-white",
  });

  $(".touchspin2").TouchSpin({
    min: 0,
    max: 999999999,
    step: 1000,
    buttondown_class: "btn btn-white",
    buttonup_class: "btn btn-white",
  });

  $("#btnOpenAddModal").on("click", () => {
    $("#modalTitle").text("Thêm Sản Phẩm Chi Tiết");
    $("#btnAddProduct").show();
    $("#btnUpdateProduct").hide();
    avatarDropzone.removeAllFiles(true);
    $("#productForm")[0].reset();
    $("#myModal").modal("show");
  });

  $("#btnAddProduct").click(function () {
    Swal.fire({
      title: "Xác nhận thêm mới?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Thêm",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        let formData = collectFormData(); // 👈 gom logic lấy dữ liệu
        submitProductVariant(formData); // 👈 gom logic gửi ajax
      }
    });
  });

  function searchProductVariants(page) {
    const keyword = $("#searchInput").val().trim();
    const colorId = $("#colorFilter").val();
    const sizeId = $("#sizeFilter").val();
    const cateId = $("#cateFilter").val();
    const status = $("#statusFilter").val();

    $.ajax({
      url: "/admin/productVariant/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        colorId: colorId,
        sizeId: sizeId,
        cateId: cateId,
        status: status,
      },
      success: function (response) {
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải danh sách sản phẩm!");
      },
    });
  }

  function submitProductVariant(formData) {
    $.ajax({
      url: "/admin/productVariant/add",
      method: "POST",
      processData: false,
      contentType: false,
      data: formData,
      success: function (res) {
        Swal.fire("Thành công", res.message, "success");
        $("#myModal").modal("hide");
        resetForm();

        $.get("/admin/productVariant/count").done(function (totalItems) {
          const pageSize = 5;
          const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
          searchProductVariants(lastPage);
        });
      },
      error: function (xhr) {
        Swal.fire("Lỗi", xhr.responseJSON?.message || "Thêm thất bại", "error");
      },
    });
  }

  function collectFormData() {
    const formData = new FormData();
    formData.append("sku", $("#sku").val());
    formData.append("price", $(".touchspin2").val());
    formData.append("quantity", $(".touchspin1").val());
    formData.append("status", $("#status").val());
    formData.append("productId", $("#sanPham").val());
    formData.append("colorId", $("#mauSac").val());
    formData.append("sizeId", $("#kichCo").val());

    avatarDropzone.getAcceptedFiles().forEach((file) => {
      formData.append("images", file);
    });

    return formData;
  }

  window.toggleStatus = function (productVariantId, isActive) {
    const title = isActive
      ? "Ngừng kinh doanh sản phẩm này?"
      : "Kích hoạt sản phẩm này?";

    Swal.fire({
      title: title,
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Xác nhận",
      cancelButtonText: "Hủy",
      customClass: {
        popup: "swal-pop-zindex",
      },
      backdrop: `rgba(0,0,0,0.4)`,
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/admin/productVariant/${productVariantId}/toggle-status`,
          type: "PUT",
          success: function (data) {
            Swal.fire("Thành công", data.message, "success");
            const currentPage =
              parseInt(
                $("#paginationContainer .paginate_button.active a").text(),
              ) - 1 || 0;
            searchProductVariants(currentPage);
          },
          error: function (xhr) {
            Swal.fire(
              "Lỗi",
              xhr.responseJSON?.message || "Có lỗi xảy ra",
              "error",
            );
          },
        });
      }
    });
  };
  function resetForm() {
    $("#productForm")[0].reset();
    $(".select2").val(null).trigger("change");
  }

  window.searchProductVariants = searchProductVariants;
});

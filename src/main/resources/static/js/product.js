Dropzone.autoDiscover = false;
var avatarDropzone = null;

$(document).ready(function () {
  // Khởi tạo Select2
  function initSelect2s() {
    $("#danhMuc").select2({
      dropdownParent: $("#myModal"),
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

    $("#thuongHieu").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn thương hiệu",
      allowClear: true,
      ajax: {
        url: "/admin/brand/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });
  }

  initSelect2s();

  // Khởi tạo Dropzone một lần duy nhất
  avatarDropzone = new Dropzone("#avatarDropzone", {
    url: "/dummy-upload",
    autoProcessQueue: false,
    clickable: true,
    maxFiles: 1,
    acceptedFiles: "image/*",
    addRemoveLinks: true,
    dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
    dictRemoveFile: "Xóa ảnh",
    dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
    previewsContainer: "#avatarDropzone",
  });

  // Đồng bộ file Dropzone với input file
  avatarDropzone.on("addedfile", function (file) {
    if (this.files.length > 1) {
      this.removeFile(this.files[0]); // Xóa file cũ nếu có
    }
    if (file instanceof File) {
      const dt = new DataTransfer();
      dt.items.add(file);
      document.getElementById("avatarInput").files = dt.files;
    }
  });

  // Làm mới Dropzone và input file khi modal đóng
  $("#myModal").on("hidden.bs.modal", function () {
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true); // Xóa tất cả tệp
    }
    $("#avatarInput").val(""); // Đặt lại input file
    $("#danhMuc").val(null).trigger("change"); // Đặt lại Select2 danh mục
    $("#thuongHieu").val(null).trigger("change"); // Đặt lại Select2 thương hiệu
  });

  // Mở modal để thêm sản phẩm
  function openAddModal() {
    $("#modalTitle").text("Thêm Sản Phẩm");
    $("#productForm").trigger("reset");
    $("#danhMuc").val(null).trigger("change");
    $("#thuongHieu").val(null).trigger("change");
    $("#btnAddProduct").show();
    $("#btnUpdateProduct").hide();
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true); // Đảm bảo Dropzone trống
    }
    $("#myModal").modal("show");
  }

  // Mở modal để cập nhật/chi tiết sản phẩm
  function openEditModal(product) {
    console.log("Dữ liệu sản phẩm:", product); // Debug dữ liệu sản phẩm
    $("#modalTitle").text("Chi Tiết Và Cập Nhật Sản Phẩm");
    $("#productForm").trigger("reset");

    $("#productId").val(product.id);
    $("#maSp").val(product.productCode);
    $("#tenSp").val(product.name);
    $("#description").val(product.description);

    // Set Select2
    if (product.categoryId && product.categoryName) {
      const option = new Option(
        product.categoryName,
        product.categoryId,
        true,
        true,
      );
      $("#danhMuc").append(option).trigger("change");
    }

    if (product.brandId && product.brandName) {
      const option = new Option(product.brandName, product.brandId, true, true);
      $("#thuongHieu").append(option).trigger("change");
    }

    // Reset ảnh
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }

    // Tải ảnh thumbnail nếu có
    if (avatarDropzone && product.thumbnail) {
      const thumbnail = product.thumbnail.startsWith("/")
        ? `${window.location.origin}${product.thumbnail}`
        : product.thumbnail;
      console.log("Đang tải thumbnail:", thumbnail); // Debug URL ảnh
      const mockFile = {
        name: thumbnail.split("/").pop() || "thumbnail.jpg",
        size: 12345,
        type: "image/jpeg",
        accepted: true,
      };
      avatarDropzone.emit("addedfile", mockFile);
      avatarDropzone.emit("complete", mockFile);
      avatarDropzone.createThumbnailFromUrl(
        mockFile,
        thumbnail,
        function () {
          console.log("Thumbnail loaded successfully");
        },
        function () {
          console.error("Failed to load thumbnail:", thumbnail);
          toastr.error("Không thể tải ảnh thumbnail!");
        },
      );
      avatarDropzone.files.push(mockFile);
    } else {
      console.warn(
        "Không có thumbnail hoặc thumbnail không hợp lệ:",
        product.thumbnail,
      );
    }

    $("#btnAddProduct").hide();
    $("#btnUpdateProduct").show();
    $("#myModal").modal("show");
  }

  // Xử lý click nút chi tiết
  window.handleDetailClick = function (button) {
    const id = $(button).data("id");
    const currentPage =
      parseInt($("#paginationContainer .paginate_button.active a").text()) -
        1 || 0;
    $("#productForm").data("current-page", currentPage);

    $.ajax({
      url: `/admin/product/${id}`,
      method: "GET",
      success: function (product) {
        openEditModal(product);
      },
      error: function () {
        toastr.error("Không thể lấy thông tin sản phẩm!");
      },
    });
  };

  // Thêm sản phẩm
  $("#btnAddProduct").click(function () {
    Swal.fire({
      title: "Xác Nhận Thêm Sản Phẩm",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Thêm",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        const dz = Dropzone.forElement("#avatarDropzone");
        const files = dz.getAcceptedFiles();
        const avatarFile = files.length > 0 ? files[0] : null;

        const formData = new FormData();
        formData.append("name", $("#tenSp").val());
        formData.append("categoryId", $("#danhMuc").val());
        formData.append("brandId", $("#thuongHieu").val());
        formData.append("description", $("#description").val());

        if (avatarFile) {
          formData.append("thumbnail", avatarFile, avatarFile.name);
        }

        $.ajax({
          url: "/admin/product/add",
          method: "POST",
          processData: false,
          contentType: false,
          data: formData,
          success: function (response) {
            Swal.fire("Thành công!", response.message, "success");
            $("#myModal").modal("hide");
            $.get("/admin/product/count").done(function (totalItems) {
              const pageSize = 5;
              const lastPage = Math.max(
                0,
                Math.ceil(totalItems / pageSize) - 1,
              );
              searchProduct(lastPage);
            });
          },
          error: function (xhr) {
            Swal.fire(
              "Lỗi!",
              xhr.responseJSON?.message || "Thêm thất bại",
              "error",
            );
          },
        });
      }
    });
  });

  // Cập nhật sản phẩm
  $("#btnUpdateProduct").on("click", function () {
    Swal.fire({
      title: "Xác Nhận Cập Nhật Sản Phẩm",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Cập Nhật",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        const formData = new FormData();
        formData.append("name", $("#tenSp").val());
        formData.append("categoryId", $("#danhMuc").val());
        formData.append("brandId", $("#thuongHieu").val());
        formData.append("description", $("#description").val());

        const files = avatarDropzone.getAcceptedFiles();
        if (files.length > 0) {
          formData.append("thumbnail", files[0]);
        }

        const productId = $("#productId").val();

        $.ajax({
          url: `/admin/product/${productId}`,
          type: "PUT",
          data: formData,
          processData: false,
          contentType: false,
          success: function (response) {
            Swal.fire("Cập nhật thành công!", response.message, "success");
            $("#myModal").modal("hide");
            const currentPage = $("#productForm").data("current-page") || 0;
            searchProduct(currentPage);
          },
          error: function (xhr) {
            toastr.error("Cập nhật thất bại: " + xhr.responseText);
          },
        });
      }
    });
  });

  // Tìm kiếm sản phẩm
  function searchProduct(page) {
    const keyword = $("#searchInput").val().trim();
    const isActive = $("#statusFilter").val();

    $.ajax({
      url: "/admin/product/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        isActive: isActive,
      },
      success: function (response) {
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải danh sách sản phẩm!");
      },
    });
  }

  // Gán hàm cho window
  window.openAddModal = openAddModal;
  window.searchProduct = searchProduct;
});

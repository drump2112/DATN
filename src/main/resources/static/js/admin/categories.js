// xem chi tiết
window.handleDetailClick = function (button) {
  const category = {
    id: $(button).data("id"),
    code: $(button).data("code"),
    name: $(button).data("name"),
    isActive: $(button).data("isActive"),
  };
  const currentPage =
    parseInt($("#paginationContainer .paginate_button.active a").text()) - 1 ||
    0;
  $("#categoryForm").data("current-page", currentPage);
  openEditModal(category, true);
};

// mở modal và hiển thị chi tiêt category
function openEditModal(data, isEditable) {
  clearErrors();
  $("#id").val(data.id);
  $("#code").val(data.code).prop("readonly", true);
  $("#name").val(data.name).prop("readonly", !isEditable);

  $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Danh mục");
  $("#btnAddCategory").hide();
  $("#btnUpdate").toggle(isEditable);
  $("#myModal").modal("show");
}

// Hàm làm mới input
function clearErrors() {
  $("#code").val("");
  $("#name").val("");
}

// mở modal nhập thêm
function openAddModal() {
  console.log(" oke đã  tới đây");
  clearErrors();
  $("#modalTitle").text("Thêm Danh mục");
  $("#categoryForm input, #categoryForm select")
    .prop("readonly", false)
    .prop("disabled", false);
  $("#categoryForm #maCategory").prop("readonly", true).prop("disabled", true);
  $("#btnAddCategory").show();
  $("#btnUpdate").hide();
  $("#myModal").modal("show");
}

//  Hàm validation tên Danh mục
function validateCategoryName() {
  const name = $("#name").val().trim();
  let errorMessage = "";

  // Không để trống
  if (!name) {
    errorMessage = "Danh mục không được để trống!";
  }
  // Chỉ cho phép chữ cái + khoảng trắng (Unicode)
  else if (!/^[\p{L}\s]+$/u.test(name)) {
    errorMessage = "Danh mục chỉ được nhập chữ!";
  }
  // Giới hạn độ dài
  else if (name.length < 2 || name.length > 50) {
    errorMessage = "Tên danh mục phải từ 2 đến 50 ký tự!";
  }

  if (errorMessage) {
    Swal.fire("Lỗi!", errorMessage, "error");
    return false;
  }
  return true;
}

// thêm Danh mục
$("#btnAddCategory").click(function () {
  if (!validateCategoryName()) return; //   check trước khi gửi

  Swal.fire({
    title: "Xác nhận thêm Danh mục?",
    icon: "question",
    showCancelButton: true,
    confirmButtonText: "Thêm",
    cancelButtonText: "Hủy",
  }).then((result) => {
    if (result.isConfirmed) {
      const formData = new FormData();
      formData.append("id", $("#id").val().trim());
      formData.append("categoryCode", $("#code").val() || null);
      formData.append("name", $("#name").val().trim());
      formData.append("isActive", true);

      $.ajax({
        url: "/admin/categories/add",
        method: "POST",
        processData: false,
        contentType: false,
        data: formData,
        success: function (response) {
          Swal.fire("Thành công!", response.message, "success");
          $("#myModal").modal("hide");
          const currentPage = getCurrentPage();
          searchCategory(currentPage);
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

// get trang hiện tại
function getCurrentPage() {
  return parseInt($("#paginationContainer").attr("data-current-page")) || 0;
}

// Tìm kiếm category
function searchCategory(page) {
  var keyword = $("#searchInput").val().trim();
  var isActive = $("#statusFilter").val() || null;

  $.ajax({
    url: "/admin/categories/search",
    type: "GET",
    data: {
      page: page,
      keyword: keyword,
      isActive: isActive,
    },
    success: function (response) {
      $("#categoryTableContainer").html(response);
    },
    error: function () {
      searchCategory(0);
    },
  });
}

// Chuyển đổi trạng thái
window.toggleStatus = function (userId, isActive) {
  const title = isActive
    ? "Bạn có chắc muốn vô hiệu hóa Danh mục này?"
    : "Bạn có chắc muốn kích hoạt Danh mục này?";

  Swal.fire({
    title: title,
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Xác nhận",
    cancelButtonText: "Hủy",
    customClass: {
      popup: "swal-pop-zindex",
    },
    backdrop: `rgba(0, 0, 0, 0.4)`,
  }).then((result) => {
    if (result.isConfirmed) {
      $.ajax({
        url: `/admin/categories/${userId}/toggle-status`,
        type: "PUT",
        success: function (data) {
          Swal.fire("Thành công", data.message, "success");
          const currentPage = getCurrentPage();
          searchCategory(currentPage);
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

//cập nhật

$("#btnUpdate").on("click", function () {
  if (!validateCategoryName(false)) return;

  Swal.fire({
    title: "Xác nhận cập nhật danh mục?",
    icon: "question",
    showCancelButton: true,
    confirmButtonText: "Cập nhật",
    cancelButtonText: "Hủy",
  }).then((result) => {
    if (result.isConfirmed) {
      const formData = new FormData();
      formData.append("categoryCode", $("#code").val().trim());
      formData.append("name", $("#name").val().trim());
      const categoryId = $("#id").val();

      $.ajax({
        url: `/admin/categories/${categoryId}`,
        type: "PUT",
        data: formData,
        processData: false,
        contentType: false,
        success: function (response) {
          Swal.fire("Cập nhật thành công!", response.message, "success");
          $("#myModal").modal("hide");
          const currentPage = $("#categoryForm").data("current-page") || 0;
          searchSize(currentPage);
        },
        error: function (xhr) {
          toastr.error("Cập nhật thất bại: " + xhr.responseText);
        },
      });
    }
  });
});

Dropzone.autoDiscover = false;
var avatarDropzone = null;

$(document).ready(function () {
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
  });

  // Hàm xóa thông báo lỗi
  function clearErrors() {
    $(".text-danger").text("");
  }

  // Mở modal để thêm nhân viên
  function openAddModal() {
    clearErrors();
    $("#modalTitle").text("Thêm Nhân Viên");
    $("#employeeForm")[0].reset();
    $("#employeeForm input, #employeeForm select")
      .prop("readonly", false)
      .prop("disabled", false);
    $("#employeeForm #maNv").prop("readonly", true).prop("disabled", true);
    $("#btnAdd").show();
    $("#btnUpdate").hide();
    $("#usernameGroup").show();
    $("#passwordGroup").show();
    $("#ngaySinhGroup").appendTo(".col-sm-6.b-r");
    $('input[name="gender"]').iCheck("uncheck");
    $("#genderNam").iCheck("check");
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true); // Đảm bảo Dropzone trống
    }
    $("#myModal").modal("show");
  }

  // Mở modal để cập nhật/chi tiết nhân viên
  function openEditModal(data, isEditable) {
    clearErrors();
    $("#userId").val(data.id);
    $("#maNv").val(data.userCode).prop("readonly", true);
    $("#hoTen").val(data.fullName).prop("readonly", !isEditable);
    $("#email").val(data.email).prop("readonly", !isEditable);
    $("#sdt").val(data.phone).prop("readonly", !isEditable);
    $("#tenDangNhap").val(data.userName).prop("readonly", !isEditable);
    $("#matKhau").val("").prop("readonly", !isEditable);
    $("#vaiTro").val(data.role).prop("disabled", !isEditable);
    $("#diaChi").val(data.address).prop("readonly", !isEditable);
    $("#dob").val(data.dateOfBirth).prop("readonly", !isEditable);
    $("#usernameGroup").hide();
    $("#passwordGroup").hide();
    $("#ngaySinhGroup").appendTo(".col-sm-6:last");

    if (data.gender === true || data.gender === "true") {
      $("#genderNam").iCheck("check");
    } else {
      $("#genderNu").iCheck("check");
    }

    if (!isEditable) {
      $('input[name="gender"]').iCheck("disable");
    } else {
      $('input[name="gender"]').iCheck("enable");
    }

    $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Nhân Viên");
    $("#btnAdd").hide();
    $("#btnUpdate").toggle(isEditable);

    // Xóa ảnh cũ trong Dropzone trước khi tải ảnh mới
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }

    // Tải ảnh avatar nếu có
    if (avatarDropzone && data.avatar) {
      const avatarUrl = data.avatar.startsWith("/")
        ? `${window.location.origin}${data.avatar}`
        : data.avatar;
      const mockFile = {
        name: avatarUrl.split("/").pop(),
        size: 12345,
        type: "image/jpeg",
      };
      avatarDropzone.emit("addedfile", mockFile);
      avatarDropzone.emit("complete", mockFile);
      avatarDropzone.createThumbnailFromUrl(mockFile, avatarUrl);
      avatarDropzone.files.push(mockFile);
    }

    $("#myModal").modal("show");
  }

  // Xử lý sự kiện click chi tiết
  window.handleDetailClick = function (button) {
    const user = {
      id: $(button).data("id"),
      userCode: $(button).data("usercode"),
      fullName: $(button).data("fullname"),
      email: $(button).data("email"),
      userName: $(button).data("username"),
      phone: $(button).data("phone"),
      address: $(button).data("address"),
      role: $(button).data("role"),
      avatar: $(button).data("avatar"),
      gender: $(button).data("gender"),
      dateOfBirth: $(button).data("dob"),
    };
    const currentPage =
      parseInt($("#paginationContainer .paginate_button.active a").text()) -
        1 || 0;
    $("#employeeForm").data("current-page", currentPage);
    openEditModal(user, true);
  };

  // Validate dữ liệu
  function validateForm(isAddMode) {
    clearErrors();
    let isValid = true;

    const fullName = $("#hoTen").val().trim();
    const email = $("#email").val().trim();
    const phone = $("#sdt").val().trim();
    const dateOfBirth = $("#dob").val().trim();
    const userName = $("#tenDangNhap").val().trim();
    const password = $("#matKhau").val().trim();
    const role = $("#vaiTro").val();
    const diachi = $("#diaChi").val().trim();

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^(03|05|07|08|09)\d{8}$/;

    if (!fullName) {
      $("#error-hoTen").text("Họ tên không được để trống");
      isValid = false;
    } else if (fullName.length > 30) {
      $("#error-hoTen").text("Họ tên không quá 30 ký tự");
      isValid = false;
    }

    if (!email) {
      $("#error-email").text("Email không được để trống");
      isValid = false;
    } else if (!emailRegex.test(email)) {
      $("#error-email").text("Email không hợp lệ");
      isValid = false;
    } else if (email.length > 50) {
      $("#error-email").text("Email không quá 50 ký tự");
      isValid = false;
    }

    if (!phone) {
      $("#error-sdt").text("Số điện thoại không được để trống");
      isValid = false;
    } else if (!phoneRegex.test(phone)) {
      $("#error-sdt").text("Số điện thoại không hợp lệ");
      isValid = false;
    } else if (phone.length > 10) {
      $("#error-sdt").text("Số điện thoại không quá 10 chữ số");
      isValid = false;
    }

    if (!dateOfBirth) {
      $("#error-dob").text("Chọn ngày sinh");
      isValid = false;
    }

    if (isAddMode) {
      if (!userName) {
        $("#error-tenDangNhap").text("Tên đăng nhập không được để trống");
        isValid = false;
      } else if (userName.length > 20) {
        $("#error-tenDangNhap").text("Tên đăng nhập không quá 20 ký tự");
        isValid = false;
      }

      if (!password) {
        $("#error-matKhau").text("Mật khẩu không được để trống");
        isValid = false;
      } else if (password.length < 8) {
        $("#error-matKhau").text("Mật khẩu không ít hơn 8 ký tự");
        isValid = false;
      }
    }

    if (!role) {
      $("#error-vaiTro").text("Vai trò không được để trống");
      isValid = false;
    }

    if (!diachi) {
      $("#error-diaChi").text("Địa chỉ không được để trống");
      isValid = false;
    }

    return isValid;
  }

  // Thêm nhân viên
  $("#btnAdd").click(function () {
    if (!validateForm(true)) return;

    Swal.fire({
      title: "Xác nhận thêm nhân viên?",
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
        formData.append("fullName", $("#hoTen").val().trim());
        formData.append("email", $("#email").val().trim());
        formData.append("phone", $("#sdt").val().trim());
        formData.append("userName", $("#tenDangNhap").val().trim());
        formData.append("password", $("#matKhau").val().trim());
        formData.append("address", $("#diaChi").val().trim());
        formData.append("roleId", $("#vaiTro").val());
        formData.append("gender", $("input[name='gender']:checked").val());
        formData.append("dateOfBirth", $("#dob").val());

        if (avatarFile) {
          formData.append("avatar", avatarFile, avatarFile.name);
        }

        $.ajax({
          url: "/admin/employee/add",
          method: "POST",
          processData: false,
          contentType: false,
          data: formData,
          success: function (response) {
            Swal.fire("Thành công!", response.message, "success");
            $("#myModal").modal("hide");
            $.get("/admin/employee/count").done(function (totalItems) {
              const pageSize = 5;
              const lastPage = Math.max(
                0,
                Math.ceil(totalItems / pageSize) - 1,
              );
              searchUser(lastPage);
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

  // Cập nhật nhân viên
  $("#btnUpdate").on("click", function () {
    if (!validateForm(false)) return;

    Swal.fire({
      title: "Xác nhận cập nhật nhân viên?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Cập nhật",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        const formData = new FormData();
        formData.append("fullName", $("#hoTen").val().trim());
        formData.append("email", $("#email").val().trim());
        formData.append("phone", $("#sdt").val().trim());
        formData.append("roleId", $("#vaiTro").val());
        formData.append("address", $("#diaChi").val().trim());
        formData.append("dateOfBirth", $("#dob").val());
        formData.append("gender", $("input[name='gender']:checked").val());

        const files = avatarDropzone.getAcceptedFiles();
        if (files.length > 0) {
          formData.append("avatar", files[0]);
        }

        const employeeId = $("#userId").val();

        $.ajax({
          url: `/admin/employee/${employeeId}`,
          type: "PUT",
          data: formData,
          processData: false,
          contentType: false,
          success: function (response) {
            Swal.fire("Cập nhật thành công!", response.message, "success");
            $("#myModal").modal("hide");
            const currentPage = $("#employeeForm").data("current-page") || 0;
            searchUser(currentPage);
          },
          error: function (xhr) {
            toastr.error("Cập nhật thất bại: " + xhr.responseText);
          },
        });
      }
    });
  });

  // Tìm kiếm nhân viên
  function searchUser(page) {
    var keyword = $("#searchInput").val().trim();
    var isActive = $("#statusFilter").val() || null;

    $.ajax({
      url: "/admin/employee/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        isActive: isActive,
      },
      success: function (response) {
        $("#employeeTableContainer").html(response);
      },
      error: function () {
        searchUser(0);
      },
    });
  }

  // Chuyển đổi trạng thái
  window.toggleStatus = function (userId, isActive) {
    const title = isActive
      ? "Bạn có chắc muốn vô hiệu hóa tài khoản này?"
      : "Bạn có chắc muốn kích hoạt tài khoản này?";

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
          url: `/admin/employee/${userId}/toggle-status`,
          type: "PUT",
          success: function (data) {
            Swal.fire("Thành công", data.message, "success");
            const currentPage =
              parseInt(
                $("#paginationContainer .paginate_button.active a").text(),
              ) - 1 || 0;
            searchUser(currentPage);
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

  // Gán hàm cho window
  window.openAddModal = openAddModal;
  window.searchUser = searchUser;
});

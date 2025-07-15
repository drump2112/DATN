Dropzone.autoDiscover = false;
var avatarDropzone = null;

$(document).ready(function () {
  if (!avatarDropzone) {
    avatarDropzone = new Dropzone("#avatarDropzone", {
      url: "/dummy-upload", // dummy, không upload
      autoProcessQueue: false,
      clickable: true,
      maxFiles: 1,
      acceptedFiles: "image/*",
      addRemoveLinks: true,
      dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
      previewsContainer: "#avatarDropzone",
    });

    avatarDropzone.on("addedfile", function (file) {
      if (this.files.length > 1) {
        // Giữ lại file vừa thêm (file) và remove file cũ
        this.removeFile(this.files[0]);
      }
      // Đồng bộ input file (để gửi backend nếu muốn)
      if (file instanceof File) {
        const dt = new DataTransfer();
        dt.items.add(file);
        document.getElementById("avatarInput").files = dt.files;
      }
    });
  }

  $("#myModal").on("hidden.bs.modal", function () {
    if (avatarDropzone) {
      avatarDropzone.destroy();
      avatarDropzone = null;
    }
    $("#avatarInput").val("");

    // Init lại Dropzone
    avatarDropzone = new Dropzone("#avatarDropzone", {
      url: "/dummy-upload",
      autoProcessQueue: false,
      clickable: true,
      maxFiles: 1,
      acceptedFiles: "image/*",
      addRemoveLinks: true,
      dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
      previewsContainer: "#avatarDropzone",
    });

    avatarDropzone.on("addedfile", function (file) {
      if (this.files.length > 1) {
        this.removeFile(this.files[0]);
      }

      if (file instanceof File) {
        const dt = new DataTransfer();
        dt.items.add(file);
        document.getElementById("avatarInput").files = dt.files;
      }
    });
  });

  // Mở modal thêm
  function openAddModal() {
    $("#modalTitle").text("Thêm Nhân Viên");
    $("#employeeForm")[0].reset();
    $("#employeeForm input, #employeeForm select")
      .prop("readonly", false)
      .prop("disabled", false);
    $("#employeeForm #maNv").prop("readonly", true).prop("disabled", true);
    $("#btnAdd").show();
    $("#btnUpdate").hide();

    $("#myModal").modal("show");
  }
  // Mở modal chi tiết / cập nhật
  function openEditModal(data, isEditable) {
    $("#maNv").val(data.userCode).prop("readonly", true);
    $("#hoTen").val(data.fullName).prop("readonly", !isEditable);
    $("#email").val(data.email).prop("readonly", !isEditable);
    $("#sdt").val(data.phone).prop("readonly", !isEditable);
    $("#tenDangNhap").val(data.userName).prop("readonly", !isEditable);
    $("#matKhau").val("").prop("readonly", !isEditable);
    $("#vaiTro").val(data.role).prop("disabled", !isEditable);
    $("#diaChi").val(data.address).prop("readonly", !isEditable);

    $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Nhân Viên");
    $("#btnAdd").hide();
    $("#btnUpdate").toggle(isEditable);

    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }

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

  // Click chi tiết
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
    };
    openEditModal(user, true);
  };

  // Validate dữ liệu
  function validateForm() {
    let isValid = true;

    // Xóa thông báo lỗi cũ
    $(".text-danger").text("");

    const fullName = $("#hoTen").val().trim();
    const email = $("#email").val().trim();
    const phone = $("#sdt").val().trim();
    const userName = $("#tenDangNhap").val().trim();
    const password = $("#matKhau").val().trim();
    const role = $("#vaiTro").val();
    const diachi = $("#diaChi").val().trim();

    // Regex kiểm tra
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const phoneRegex = /^(03|05|07|08|09)\d{8}$/;

    if (!fullName) {
      $("#error-hoTen").text("Họ tên không được để trống");
      isValid = false;
    }

    if (!email || !emailRegex.test(email)) {
      $("#error-email").text("Email không hợp lệ");
      isValid = false;
    }

    if (!phone || !phoneRegex.test(phone)) {
      $("#error-sdt").text("Số điện thoại không hợp lệ");
      isValid = false;
    }

    if (!userName) {
      $("#error-tenDangNhap").text("Tên đăng nhập không được để trống");
      isValid = false;
    }

    if (!password) {
      $("#error-matKhau").text("Mật khẩu không được để trống");
      isValid = false;
    }

    if (!role) {
      $("#error-vaiTro").text("Vai trò không được để trống");
      isValid = false;
    }

    if (!diachi) {
      $("#error-diaChi").text("Địa Chỉ không được để trống");
      isValid = false;
    }

    return isValid;
  }

  // Thêm nhân viên
  $("#btnAdd").click(function () {
    if (!validateForm()) return;

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
        console.log(files[0]);
        const avatarFile = files.length > 0 ? files[0] : null;

        const formData = new FormData();

        formData.append("fullName", $("#hoTen").val());
        formData.append("email", $("#email").val());
        formData.append("phone", $("#sdt").val());
        formData.append("userName", $("#tenDangNhap").val());
        formData.append("password", $("#matKhau").val());
        formData.append("address", $("#diaChi").val());
        formData.append("roleId", $("#vaiTro").val());
        formData.append("gender", $("input[name='gender']:checked").val());
        formData.append("dateOfBirth", $("#dob").val());

        if (avatarFile) {
          formData.append("avatar", avatarFile, avatarFile.name);
          console.log("Name File:", avatarFile.name);
        } else {
          console.log("No file select");
        }

        for (let [key, value] of formData.entries()) {
          console.log(key, value);
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
            // Load lại bảng
            $("#employeeTableContainer").load("/admin/employee/table #table");
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

  // function searchUser(page) {
  //   var keyword = $("#searchInput").val().trim();
  //   var isActive = $("#statusFilter").val();
  //
  //   $.ajax({
  //     url: "/admin/employee/search",
  //     type: "GET",
  //     data: {
  //       page: page,
  //       keyword: keyword,
  //       isActive: isActive,
  //     },
  //     success: function (response) {
  //       $("#employeeTableContainer").html(response); // Cập nhật nội dung fragment
  //     },
  //     error: function () {
  //       searchUser(0);
  //     },
  //   });
  // }

  function searchUser(page) {
    var keyword = $("#searchInput").val().trim();
    var isActive = $("#statusFilter").val();

    $.ajax({
      url: "/admin/employee/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        isActive: isActive,
      },
      success: function (response) {
        $("#employeeTableContainer").html(response); // Cập nhật nội dung fragment
      },
      error: function () {
        searchUser(0);
      },
    });
  }
  // Toggle trạng thái
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
            $("#employeeTableContainer").load("/admin/employee/table #table");
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

  window.openAddModal = openAddModal;
  window.searchUser = searchUser;
});

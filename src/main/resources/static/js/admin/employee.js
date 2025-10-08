Dropzone.autoDiscover = false;

var avatarDropzone = null;

$(document).ready(function () {
  // === Khởi tạo Dropzone ===
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

  // Đồng bộ file Dropzone với input hidden
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

  // Reset Dropzone khi đóng modal
  $("#myModal").on("hidden.bs.modal", function () {
    if (avatarDropzone) avatarDropzone.removeAllFiles(true);
    $("#avatarInput").val("");
    $("#employeeForm")[0].reset();
    validator.resetForm();
    $("#employeeForm .form-control").removeClass("error");
    $("#employeeForm label.error").remove(); // <-- Xóa label error
  });

  // === Khởi tạo validate ===

  $.validator.addMethod(
    "pattern",
    function (value, element, param) {
      if (this.optional(element)) {
        return true;
      }
      if (typeof param === "string") {
        param = new RegExp(param);
      }
      return param.test(value);
    },
    "Giá trị không đúng định dạng",
  );

  var validator = $("#employeeForm").validate({
    ignore: ":hidden:not(.select2-hidden-accessible)",
    rules: {
      fullName: { required: true, maxlength: 30 },
      email: { required: true, email: true, maxlength: 50 },
      phone: {
        required: true,
        maxlength: 10,
        pattern: /^(03|05|07|08|09)\d{8}$/,
      },
      dateOfBirth: { required: true },
      userName: { required: true, maxlength: 20 },
      password: { required: true, minlength: 8 },
      address: {
        normalizer: function (value) {
          return $.trim(value);
        },
        required: true,
      },
    },
    messages: {
      fullName: {
        required: "Họ tên không được để trống",
        maxlength: "Họ tên không quá 30 ký tự",
      },
      email: {
        required: "Email không được để trống",
        email: "Email không hợp lệ",
        maxlength: "Email không quá 50 ký tự",
      },
      phone: {
        required: "Số điện thoại không được để trống",
        maxlength: "Số điện thoại không quá 10 kí tự",
        pattern: "Số điện thoại không hợp lệ",
      },
      dateOfBirth: { required: "Chọn ngày sinh" },
      userName: {
        required: "Tên đăng nhập không được để trống",
        maxlength: "Tên đăng nhập không quá 20 ký tự",
      },
      password: {
        required: "Mật khẩu không được để trống",
        minlength: "Mật khẩu không ít hơn 8 ký tự",
      },
      address: { required: "Địa chỉ không được để trống" },
    },
    errorPlacement: function (error, element) {
      element.before(error);
    },
  });

  // === Modal Add ===
  function openAddModal() {
    // $("#userId").val("");
    validator.resetForm();
    $("#employeeForm .error").removeClass("error");

    $("#modalTitle").text("Thêm Nhân Viên");
    $("#employeeForm")[0].reset();
    $("#employeeForm input, #employeeForm select")
      .prop("readonly", false)
      .prop("disabled", false);
    $("#employeeForm #maNv").prop("readonly", true).prop("disabled", true);

    // Chỉ required khi thêm
    $("#tenDangNhap").rules("add", { required: true });
    $("#matKhau").rules("add", { required: true });

    $("#btnAdd").show();
    $("#btnUpdate").hide();
    $("#usernameGroup").show();
    $("#passwordGroup").show();
    $("#ngaySinhGroup").appendTo(".col-sm-6.b-r");
    $('input[name="gender"]').iCheck("uncheck");
    $("#genderNam").iCheck("check");

    if (avatarDropzone) avatarDropzone.removeAllFiles(true);

    $("#myModal").modal("show");
  }

  // === Modal Edit ===
  function openEditModal(data, isEditable) {
    validator.resetForm();
    $("#employeeForm .error").removeClass("error");

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

    $('input[name="gender"]').iCheck(isEditable ? "enable" : "disable");

    $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Nhân Viên");
    $("#btnAdd").hide();
    $("#btnUpdate").toggle(isEditable);

    // Loại bỏ required khi edit
    $("#tenDangNhap").rules("remove", "required");
    $("#matKhau").rules("remove", "required");

    if (avatarDropzone) avatarDropzone.removeAllFiles(true);

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

  // === Click chi tiết để mở modal edit ===
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

  // === Add ===
  $("#btnAdd").click(function (e) {
    e.preventDefault();
    if (!$("#employeeForm").valid()) return;

    Swal.fire({
      title: "Xác nhận thêm nhân viên?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Thêm",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {

        const formData = new FormData();

        const dz = Dropzone.forElement("#avatarDropzone");
        const files = dz.getAcceptedFiles();
        const avatarFile = files.length > 0 ? files[0] : null;

        // Chỉ thêm các trường cần thiết vào FormData
        $("#employeeForm")
          .serializeArray()
          .forEach((field) => {
            formData.append(field.name, $.trim(field.value));
          });
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

            $("#statusFilter").val("");

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

  // === Update ===
  $("#btnUpdate").on("click", function (e) {
    e.preventDefault();
    if (!$("#employeeForm").valid()) return;

    Swal.fire({
      title: "Xác nhận cập nhật nhân viên?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Cập nhật",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        const formData = new FormData();
        const files = avatarDropzone.getAcceptedFiles();
        if (files.length > 0) {
          formData.append("avatar", files[0]);
        }

        // Chỉ thêm các trường cần thiết, loại bỏ tenDangNhap và matKhau
        $("#employeeForm")
          .serializeArray()
          .forEach((field) => {
            if (field.name !== "tenDangNhap" && field.name !== "matKhau") {
              formData.append(field.name, field.value);
            }
          });

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
            Swal.fire(
              "Lỗi",
              xhr.responseJSON?.message || "Cập nhật thất bại",
              "error",
            );
          },
        });
      }
    });
  });

  // === Search ===
  function searchUser(page) {
    var keyword = $("#searchInput").val().trim();
    var isActive = $("#statusFilter").val() || null;

    $.ajax({
      url: "/admin/employee/search",
      type: "GET",
      data: { page: page, keyword: keyword, isActive: isActive },
      success: function (response) {
        $("#employeeTableContainer").html(response);
      },
      error: function () {
        searchUser(0);
      },
    });
  }

  // === Toggle Status ===
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
      customClass: { popup: "swal-pop-zindex" },
      backdrop: `rgba(0, 0, 0, 0.4)`,
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

  window.openAddModal = openAddModal;
  window.searchUser = searchUser;
});

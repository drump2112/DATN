Dropzone.autoDiscover = false;

var avatarDropzone = null;

$(document).ready(function () {
  // === Load dữ liệu tỉnh/thành phố ===
  loadProvinces();

  // === Khởi tạo Select2 cho các select địa chỉ ===
  initSelect2();

  // === Xử lý thay đổi tỉnh/thành phố ===
  $(document).on('change', '#tinhThanh', function() {
    const provinceCode = $(this).val();
    console.log("Province code selected:", provinceCode);
    $("#phuongXa").empty().append('<option value="">-- Chọn Phường/Xã --</option>');

    if (provinceCode) {
      console.log("Loading communes for province:", provinceCode);
      loadCommunes(provinceCode);
      $("#phuongXa").prop("disabled", false);
    } else {
      $("#phuongXa").prop("disabled", true);
    }
  });

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

    // Reset Select2
    $("#tinhThanh").val('').trigger('change.select2');
    $("#phuongXa").val('').trigger('change.select2');
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
      specificAddress: {
        normalizer: function (value) {
          return $.trim(value);
        },
        required: true,
      },
      provinceCode: { required: true },
      communeCode: { required: true },
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
      specificAddress: { required: "Địa chỉ cụ thể không được để trống" },
      provinceCode: { required: "Chọn tỉnh/thành phố" },
      communeCode: { required: "Chọn phường/xã" },
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

    // Load provinces when opening modal (after form reset)
    loadProvinces();

    // Reinitialize Select2 in modal
    setTimeout(() => {
      initSelect2();
    }, 100);

    $("#myModal").modal("show");
  }

  // === Modal Edit ===
  function openEditModal(data, isEditable) {
    validator.resetForm();
    $("#employeeForm .error").removeClass("error");

    // Load provinces first, then set values
    loadProvinces();

    $("#userId").val(data.id);
    $("#maNv").val(data.userCode).prop("readonly", true);
    $("#hoTen").val(data.fullName).prop("readonly", !isEditable);
    $("#email").val(data.email).prop("readonly", !isEditable);
    $("#sdt").val(data.phone).prop("readonly", !isEditable);
    $("#tenDangNhap").val(data.userName).prop("readonly", !isEditable);
    $("#matKhau").val("").prop("readonly", !isEditable);
    $("#vaiTro").val(data.role).prop("disabled", !isEditable);
    $("#diaChiCuThe").val(data.specificAddress).prop("readonly", !isEditable);

    // Reinitialize Select2 and set values
    setTimeout(() => {
      initSelect2();
      $("#tinhThanh").val(data.provinceCode).prop("disabled", !isEditable);
      if (data.provinceCode) {
        loadCommunes(data.provinceCode);
        setTimeout(() => {
          $("#phuongXa").val(data.communeCode).prop("disabled", !isEditable);
        }, 500);
      }
    }, 200);
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
      specificAddress: $(button).data("specific-address"),
      provinceCode: $(button).data("province-code"),
      communeCode: $(button).data("commune-code"),
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
    if (!$("#employeeForm").valid()) return;em

    SwalUtils.confirm(
      "Xác nhận thêm nhân viên?",
      "Bạn có chắc chắn muốn thêm nhân viên với thông tin này?",
      "Thêm",
      "Hủy"
    ).then((result) => {
      if (result.isConfirmed) {

        const formData = new FormData();

        const dz = Dropzone.forElement("#avatarDropzone");
        const files = dz.getAcceptedFiles();
        const avatarFile = files.length > 0 ? files[0] : null;

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
            SwalUtils.success("Thành công!", response.message);

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
            SwalUtils.error(
              "Lỗi!",
              xhr.responseJSON?.message || "Thêm thất bại"
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

    SwalUtils.confirm(
      "Xác nhận cập nhật nhân viên?",
      "Bạn có chắc chắn muốn cập nhật thông tin nhân viên này?",
      "Cập nhật",
      "Hủy"
    ).then((result) => {
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
            SwalUtils.success("Cập nhật thành công!", response.message);
            $("#myModal").modal("hide");
            const currentPage = $("#employeeForm").data("current-page") || 0;
            searchUser(currentPage);
          },
          error: function (xhr) {
            SwalUtils.error(
              "Lỗi",
              xhr.responseJSON?.message || "Cập nhật thất bại"
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

    SwalUtils.confirm(
      title,
      "",
      "Xác nhận",
      "Hủy",
      {
        icon: "warning",
        customClass: { popup: "swal-pop-zindex" },
        backdrop: `rgba(0, 0, 0, 0.4)`
      }
    ).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/admin/employee/${userId}/toggle-status`,
          type: "PUT",
          success: function (data) {
            SwalUtils.success("Thành công", data.message);
            const currentPage =
              parseInt(
                $("#paginationContainer .paginate_button.active a").text(),
              ) - 1 || 0;
            searchUser(currentPage);
          },
          error: function (xhr) {
            SwalUtils.error(
              "Lỗi",
              xhr.responseJSON?.message || "Có lỗi xảy ra"
            );
          },
        });
      }
    });
  };

  // === Khởi tạo Select2 ===
  function initSelect2() {
    $("#tinhThanh").select2({
      placeholder: "-- Chọn Tỉnh/Thành phố --",
      allowClear: true,
      width: '100%'
    });

    $("#phuongXa").select2({
      placeholder: "-- Chọn Phường/Xã --",
      allowClear: true,
      width: '100%'
    });
  }

  // === Load provinces ===
  function loadProvinces() {
    console.log("Loading provinces...");
    $.get("/api/provinces").done(function(data) {
      console.log("Provinces loaded:", data);
      $("#tinhThanh").empty().append('<option value="">-- Chọn Tỉnh/Thành phố --</option>');
      data.forEach(function(province) {
        $("#tinhThanh").append(`<option value="${province.code}">${province.name}</option>`);
      });
      // Trigger select2 after loading data
      $("#tinhThanh").trigger('change.select2');
    }).fail(function(xhr, status, error) {
      console.error("Không thể tải danh sách tỉnh/thành phố:", xhr.responseText);
    });
  }

  // === Load communes by province ===
  function loadCommunes(provinceCode) {
    console.log("Loading communes for province:", provinceCode);
    console.log("API URL:", `/api/communes?provinceCode=${provinceCode}`);

    $.get(`/api/communes?provinceCode=${provinceCode}`).done(function(data) {
      console.log("Communes loaded - Response:", data);
      console.log("Communes count:", data.length);

      $("#phuongXa").empty().append('<option value="">-- Chọn Phường/Xã --</option>');

      if (data.length === 0) {
        console.log("No communes found for province:", provinceCode);
        $("#phuongXa").append('<option value="">-- Không có dữ liệu --</option>');
      } else {
        data.forEach(function(commune) {
          $("#phuongXa").append(`<option value="${commune.code}">${commune.name}</option>`);
        });
      }
      // Trigger select2 after loading data
      $("#phuongXa").trigger('change.select2');
      console.log("Communes added to select");
    }).fail(function(xhr, status, error) {
      console.error("Không thể tải danh sách phường/xã:", status, error);
      console.error("Response text:", xhr.responseText);
      $("#phuongXa").empty().append('<option value="">-- Lỗi tải dữ liệu --</option>');
      $("#phuongXa").trigger('change.select2');
    });
  }  window.openAddModal = openAddModal;
  window.searchUser = searchUser;
  window.loadProvinces = loadProvinces;
  window.loadCommunes = loadCommunes;
});

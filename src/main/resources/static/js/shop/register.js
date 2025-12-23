Dropzone.autoDiscover = false;

var avatarDropzone = null;

$(document).ready(function () {
  console.log("load register.js");

  // Khởi tạo dropzone cho avatar
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
    thumbnailWidth: 140,
    thumbnailHeight: 140,
  });

  // === Load dữ liệu tỉnh/thành phố ===
  loadProvinces();

  // === Khởi tạo Select2 cho các select địa chỉ ===
  initSelect2();

  // Khởi tạo iCheck cho radio buttons
  $(".i-checks input").iCheck({
    checkboxClass: "icheckbox_square-green",
    radioClass: "iradio_square-green",
  });

  // Xử lý khi chọn tỉnh/thành phố
  $("#provinceSelect").change(function() {
    const provinceCode = $(this).val();
    console.log("Province code selected:", provinceCode);
    $("#communeSelect").empty().append('<option value="">Chọn Phường/Xã</option>');

    if (provinceCode) {
      console.log("Loading communes for province:", provinceCode);
      loadCommunes(provinceCode);
      $("#communeSelect").prop("disabled", false);
    } else {
      $("#communeSelect").prop("disabled", true);
    }
  });

  // Xử lý submit form đăng ký
  $("#btnRegister").click(function (e) {
    e.preventDefault();

    // Validate form
    if (!validateRegisterForm()) return;

    SwalUtils.confirm(
      "Xác nhận đăng ký tài khoản?",
      "Bạn có chắc chắn muốn tạo tài khoản với thông tin này?",
      "Đăng ký",
      "Hủy"
    ).then((result) => {
      if (result.isConfirmed) {
        submitRegisterForm();
      }
    });
  });
});

// Function khởi tạo Select2
function initSelect2() {
  $("#provinceSelect").select2({
    placeholder: "Chọn Tỉnh/Thành phố",
    allowClear: true,
    width: '100%'
  });

  $("#communeSelect").select2({
    placeholder: "Chọn Phường/Xã",
    allowClear: true,
    width: '100%'
  });
}

function loadProvinces() {
  console.log("Loading provinces...");
  $.get("/api/provinces").done(function(data) {
    console.log("Provinces loaded:", data);
    console.log("Type of data:", typeof data);
    console.log("Is data an array:", Array.isArray(data));
    console.log("Data keys:", Object.keys(data || {}));

    $("#provinceSelect").empty().append('<option value="">Chọn Tỉnh/Thành phố</option>');

    // Check if data is array
    if (Array.isArray(data)) {
      data.forEach(function(province) {
        $("#provinceSelect").append(`<option value="${province.code}">${province.name}</option>`);
      });
    } else {
      console.error("Data is not an array:", data);
      $("#provinceSelect").append('<option value="">Lỗi: Dữ liệu không đúng định dạng</option>');
    }

    // Trigger select2 after loading data
    $("#provinceSelect").trigger('change.select2');
  }).fail(function(xhr, status, error) {
    console.error("Không thể tải danh sách tỉnh/thành phố:", xhr.responseText);
  });
}

// Function để load danh sách phường/xã theo tỉnh
function loadCommunes(provinceCode) {
  console.log("Loading communes for province:", provinceCode);
  console.log("API URL:", `/api/communes?provinceCode=${provinceCode}`);

  $.get(`/api/communes?provinceCode=${provinceCode}`).done(function(data) {
    console.log("Communes loaded - Response:", data);
    console.log("Type of communes data:", typeof data);
    console.log("Is communes data an array:", Array.isArray(data));

    $("#communeSelect").empty().append('<option value="">Chọn Phường/Xã</option>');

    if (Array.isArray(data)) {
      console.log("Communes count:", data.length);
      if (data.length === 0) {
        console.log("No communes found for province:", provinceCode);
        $("#communeSelect").append('<option value="">-- Không có dữ liệu --</option>');
      } else {
        data.forEach(function(commune) {
          $("#communeSelect").append(`<option value="${commune.code}">${commune.name}</option>`);
        });
      }
    } else {
      console.error("Communes data is not an array:", data);
      $("#communeSelect").append('<option value="">Lỗi: Dữ liệu không đúng định dạng</option>');
    }

    $("#communeSelect").prop('disabled', false);
    // Trigger select2 after loading data
    $("#communeSelect").trigger('change.select2');
    console.log("Communes added to select");
  }).fail(function(xhr, status, error) {
    console.error("Không thể tải danh sách phường/xã:", status, error);
    console.error("Response text:", xhr.responseText);
    $("#communeSelect").empty().append('<option value="">-- Lỗi tải dữ liệu --</option>');
    $("#communeSelect").trigger('change.select2');
  });
}

// Function validate form đăng ký
function validateRegisterForm() {
  let isValid = true;

  // Clear previous errors
  $(".text-danger").text("");

  // Validate required fields
  const email = $("#email").val().trim();
  const userName = $("#userName").val().trim();
  const password = $("#password").val().trim();
  const fullName = $("#hoTen").val().trim();
  const phone = $("#sdt").val().trim();
  const dateOfBirth = $("#dob").val();
  const provinceCode = $("#provinceSelect").val();
  const communeCode = $("#communeSelect").val();

  if (!email) {
    $("#error-email").text("Email không được để trống");
    isValid = false;
  } else if (!validateEmail(email)) {
    $("#error-email").text("Email không đúng định dạng");
    isValid = false;
  }

  if (!userName) {
    $("#error-userName").text("Tên đăng nhập không được để trống");
    isValid = false;
  } else if (userName.length < 3) {
    $("#error-userName").text("Tên đăng nhập phải có ít nhất 3 ký tự");
    isValid = false;
  } else if (!/^[a-zA-Z0-9_]+$/.test(userName)) {
    $("#error-userName").text("Tên đăng nhập chỉ chứa chữ cái, số và dấu gạch dưới");
    isValid = false;
  }

  if (!password) {
    $("#error-password").text("Mật khẩu không được để trống");
    isValid = false;
  } else if (password.length < 6) {
    $("#error-password").text("Mật khẩu phải có ít nhất 6 ký tự");
    isValid = false;
  }

  if (!fullName) {
    $("#error-hoTen").text("Họ tên không được để trống");
    isValid = false;
  }

  if (!phone) {
    $("#error-phone").text("Số điện thoại không được để trống");
    isValid = false;
  } else if (!validatePhone(phone)) {
    $("#error-phone").text("Số điện thoại không đúng định dạng");
    isValid = false;
  }

  if (!dateOfBirth) {
    $("#error-dob").text("Ngày sinh không được để trống");
    isValid = false;
  } else {
    const birthDate = new Date(dateOfBirth);
    const today = new Date();
    const age = today.getFullYear() - birthDate.getFullYear();

    if (age < 13) {
      $("#error-dob").text("Bạn phải từ 13 tuổi trở lên");
      isValid = false;
    } else if (age > 100) {
      $("#error-dob").text("Ngày sinh không hợp lệ");
      isValid = false;
    }
  }

  if (!provinceCode) {
    $("#error-province").text("Vui lòng chọn tỉnh/thành phố");
    isValid = false;
  }

  if (!communeCode) {
    $("#error-commune").text("Vui lòng chọn phường/xã");
    isValid = false;
  }

  return isValid;
}



function validateEmail(email) {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return emailRegex.test(email) && email.length <= 100;
}

function validatePhone(phone) {
  // Vietnam phone number: starts with 0 and has 10-11 digits
  const phoneRegex = /^0[3|5|7|8|9][0-9]{8}$|^0[2][0-9]{9}$/;
  return phoneRegex.test(phone);
}

// Function submit form đăng ký
function submitRegisterForm() {
  const formData = new FormData();

  // Get form data
  const email = $("#email").val().trim();
  const userName = $("#userName").val().trim();
  const password = $("#password").val().trim();
  const fullName = $("#hoTen").val().trim();
  const phone = $("#sdt").val().trim();
  const dateOfBirth = $("#dob").val();
  const gender = $("input[name='gender']:checked").val();
  const provinceCode = $("#provinceSelect").val();
  const communeCode = $("#communeSelect").val();
  const specificAddress = $("#specificAddress").val().trim();

  // Append form data
  formData.append("email", email);
  formData.append("userName", userName);
  formData.append("password", password);
  formData.append("fullName", fullName);
  formData.append("phone", phone);
  formData.append("dateOfBirth", dateOfBirth);
  formData.append("gender", gender || "true");
  formData.append("provinceCode", provinceCode);
  formData.append("communeCode", communeCode);
  formData.append("specificAddress", specificAddress);

  // Add avatar if selected
  const dz = Dropzone.forElement("#avatarDropzone");
  const files = dz.getAcceptedFiles();
  if (files.length > 0) {
    formData.append("avatar", files[0]);
  }

  // Show loading state
  $("#btnRegister").prop('disabled', true).addClass('loading');

  // Submit form
  $.ajax({
    url: "/customer/auth/register",
    method: "POST",
    processData: false,
    contentType: false,
    data: formData,
    success: function(response) {
      $("#btnRegister").prop('disabled', false).removeClass('loading');
      SwalUtils.success(
        "Đăng ký thành công!",
        response.message || "Vui lòng kiểm tra email để xác thực tài khoản",
        { timer: 3000 }
      ).then(() => {
        window.location.href = "/customer/auth/";
      });
    },
    error: function(xhr) {
      $("#btnRegister").prop('disabled', false).removeClass('loading');
      const errorMessage = xhr.responseJSON?.message || "Đăng ký thất bại";
      SwalUtils.error("Lỗi!", errorMessage);
    }
  });
}


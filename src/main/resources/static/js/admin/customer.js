Dropzone.autoDiscover = false;
var avatarDropzone = null;
$(document).ready(function () {
   // === Load dữ liệu tỉnh/thành phố ===
   loadProvinces();

   // === Xử lý thay đổi tỉnh/thành phố ===
   $(document).on('change', '#tinhThanh', function() {
       const provinceCode = $(this).val();
       console.log("Province code selected:", provinceCode);
       $("#phuongXa").empty().append('<option value="">-- Chọn Phường/Xã --</option>');

       if (provinceCode) {
           console.log("Loading communes for province:", provinceCode);
           loadCommunes(provinceCode).then(() => {
               $("#phuongXa").prop("disabled", false);
           }).catch(error => {
               $("#phuongXa").prop("disabled", true);
           });
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
       $("#customerForm .form-control").removeClass("error");
       $("#customerForm label.error").remove(); // <-- Xóa label error
   });

   // Reset form khi đóng modal update mật khẩu
   $("#updateMatKhau").on("hidden.bs.modal", function () {
       $("#updateMatKhauForm")[0].reset();
       validatorPassword.resetForm();
       $("#updateMatKhauForm .form-control").removeClass("error");
       $("#updateMatKhauForm label.error").remove();
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


   var validator = $("#customerForm").validate({
       ignore: ":hidden:not(.select2-hidden-accessible)",
       rules: {
           fullName: {required: true, maxlength: 30},
           email: {required: true, email: true, maxlength: 50},
           phone: {
               required: true,
               maxlength: 10,
               pattern: /^(03|05|07|08|09)\d{8}$/,
           },
           dateOfBirth: {required: true},
           tinhThanh: {required: true},
           phuongXa: {required: true},
           specificAddress: {
               normalizer: function (value) {
                   return $.trim(value);
               },
               required: true,
               maxlength: 100,
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
           dateOfBirth: {required: "Chọn ngày sinh"},
           tinhThanh: {required: "Vui lòng chọn Tỉnh/Thành phố"},
           phuongXa: {required: "Vui lòng chọn Phường/Xã"},
           specificAddress: {
               required: "Địa chỉ cụ thể không được để trống",
               maxlength: "Địa chỉ cụ thể không quá 100 ký tự",
           },
       },
       errorPlacement: function (error, element) {
           element.before(error);
       },
   });


   // === Modal Add ===
   function openAddModal() {
       // $("#userId").val("");
       validator.resetForm();
       $("#customerForm .error").removeClass("error");


       $("#modalTitle").text("Thêm Khách hàng");
       $("#customerForm")[0].reset();
       $("#customerForm input, #customerForm select")
           .prop("readonly", false)
           .prop("disabled", false);
       $("#customerForm #maNv").prop("readonly", true).prop("disabled", true);


       // Chỉ required khi thêm
       $("#tenDangNhap").rules("add", {required: true});
       $("#password").rules("add", {required: true});


       $("#btnAdd").show();
       $("#btnUpdate").hide();
       $("#usernameGroup").show();
       $("#passwordGroup").show();
       $("#ngaySinhGroup").appendTo(".col-sm-6.b-r");
       $('input[name="gender"]').iCheck("uncheck");
       $("#genderNam").iCheck("check");

       // Reset địa chỉ
       $("#tinhThanh").val("").prop("disabled", false);
       $("#phuongXa").val("").prop("disabled", true);
       $("#diaChiCuThe").val("").prop("readonly", false);


       if (avatarDropzone) avatarDropzone.removeAllFiles(true);


       $("#myModal").modal("show");
   }


   // === Modal Edit ===
   function openEditModal(data, isEditable) {
       validator.resetForm();
       $("#customerForm .error").removeClass("error");


       $("#userId").val(data.id);
       $("#maNv").val(data.userCode).prop("readonly", true);
       $("#hoTen").val(data.fullName).prop("readonly", !isEditable);
       $("#emailrs").val(data.email).prop("readonly", !isEditable);
       $("#sdtrs").val(data.phone).prop("readonly", !isEditable);
       $("#vaiTro").val(data.role).prop("disabled", !isEditable);
       $("#dob").val(data.dateOfBirth).prop("readonly", !isEditable);

       // Xử lý địa chỉ có cấu trúc
       console.log("Processing address data:", {
           provinceCode: data.provinceCode,
           communeCode: data.communeCode,
           specificAddress: data.specificAddress,
           address: data.address
       });

       if (data.provinceCode && data.communeCode) {
           // Nếu có dữ liệu địa chỉ cấu trúc mới
           console.log("Setting structured address data");
           $("#tinhThanh").val(data.provinceCode).prop("disabled", !isEditable);
           $("#diaChiCuThe").val(data.specificAddress || "").prop("readonly", !isEditable);

           // Load communes cho province được chọn và set giá trị
           if (data.provinceCode) {
               loadCommunes(data.provinceCode).then(() => {
                   $("#phuongXa").val(data.communeCode).prop("disabled", !isEditable);
               });
           }
       } else {
           // Trường hợp không có dữ liệu địa chỉ cấu trúc
           console.log("Setting fallback address data");
           $("#tinhThanh").val("").prop("disabled", !isEditable);
           $("#phuongXa").val("").prop("disabled", true);

           // Ưu tiên hiển thị specificAddress, nếu không có thì hiển thị address
           const addressToShow = data.specificAddress || data.address || "";
           console.log("Address to show:", addressToShow);
           $("#diaChiCuThe").val(addressToShow).prop("readonly", !isEditable);
       }


       $("#ngaySinhGroup").appendTo(".col-sm-6:last");


       $("#avatarDropzone").addClass("dz-disabled").off("click").css("pointer-events", "none");


       if (data.gender === true || data.gender === "true") {
           $("#genderNam").iCheck("check");
       } else {
           $("#genderNu").iCheck("check");
       }


       $('input[name="gender"]').iCheck(isEditable ? "enable" : "disable");


       $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Khách hàng");
       $("#btnAdd").hide();
       $("#btnUpdate").toggle(isEditable);




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


   var validatorPassword = $("#updateMatKhauForm").validate({
       rules: {
           password: {required: true, maxlength: 16, minlength: 8},
       },
       messages: {
           password: {
               required: "Mật Khẩu không được để trống",
               maxlength: "Mật Khẩu không quá 16 kí tự",
               minlength: "Mật Khẩu không ít hơn 8 kí tự",
           },
       },
       errorPlacement: function (error, element) {
           element.before(error);
       },
   });


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
           provinceCode: $(button).data("provincecode"),
           provinceName: $(button).data("provincename"),
           communeCode: $(button).data("communecode"),
           communeName: $(button).data("communename"),
           specificAddress: $(button).data("specificaddress"),
           role: $(button).data("role"),
           avatar: $(button).data("avatar"),
           gender: $(button).data("gender"),
           dateOfBirth: $(button).data("dob"),
       };
       console.log("User data for edit:", user);
       const currentPage =
           parseInt($("#paginationContainer .paginate_button.active a").text()) -
           1 || 0;
       $("#customerForm").data("current-page", currentPage);
       openEditModal(user, true);
   };




   // === Search ===
   function searchUser(page) {
       var keyword = $("#searchInput").val().trim();
       var isActive = $("#statusFilter").val() || null;


       $.ajax({
           url: "/admin/employee/search",
           type: "GET",
           data: {page: page, keyword: keyword, isActive: isActive},
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
               customClass: {popup: "swal-pop-zindex"},
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




   // === Add ===
   $("#btnAdd").click(function (e) {
       e.preventDefault();
       if (!$("#customerForm").valid()) return;


       SwalUtils.confirm(
           "Xác nhận thêm Khách hàng?",
           "Bạn có chắc chắn muốn thêm khách hàng với thông tin này?",
           "Thêm",
           "Hủy"
       ).then((result) => {
           if (result.isConfirmed) {


               const formData = new FormData();


               const dz = Dropzone.forElement("#avatarDropzone");
               const files = dz.getAcceptedFiles();
               const avatarFile = files.length > 0 ? files[0] : null;


               // Chỉ thêm các trường cần thiết vào FormData
               $("#customerForm")
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
       if (!$("#customerForm").valid()) return;


       SwalUtils.confirm(
           "Xác nhận cập nhật Khách hàng?",
           "Bạn có chắc chắn muốn cập nhật thông tin khách hàng này?",
           "Cập nhật",
           "Hủy"
       ).then((result) => {
           if (result.isConfirmed) {
               const formData = new FormData();
               const files = avatarDropzone.getAcceptedFiles();
               if (files.length > 0) {
                   formData.append("avatar", files[0]);
               }

               $("#customerForm")
                   .serializeArray()
                   .forEach((field) => {
                       if (field.name !== "tenDangNhap" && field.name !== "confirmPassword") {
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
                       const currentPage = $("#customerForm").data("current-page") || 0;
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

   //  reset password
   window.handleResetpMatKhau = function (button) {
       const user = {
           id: $(button).data("id"),
           userCode: $(button).data("usercode"),
           fullName: $(button).data("fullname"),
           email: $(button).data("email"),
           userName: $(button).data("username"),
           phone: $(button).data("phone"),
           password: $(button).data("password"),
       };
       console.log(user + "oke trước hien thi");
       openEditModalMatKhau(user, true);
   }


   function openEditModalMatKhau(data, isEditable) {
       validatorPassword.resetForm();
       $("#updateMatKhauForm .error").removeClass("error");

       console.log("oke truoc hien thi + " + data)
       $("#id").val(data.id);
       $("#makh").val(data.userCode).prop("readonly", true);
       $("#email").val(data.email).prop("readonly", true);
       $("#sdt").val(data.phone).prop("readonly", true);
       $("#username").val(data.userName).prop("readonly", true);
       $("#fullName").val(data.fullName).prop("readonly", true);
       $("#password").val("").prop("readonly", false);

       $("#modalTitleUpdateMatKhau").text("Cập nhật mật khẩu Khách hàng");
       $("#resetmatkhau").show();
       $("#btnUpdatematKhau").show();
       $("#updateMatKhau").modal("show");
   }


   $("#btnUpdatematKhau").click(function (e) {
       e.preventDefault();
       if (!$("#updateMatKhauForm").valid()) return;
       SwalUtils.confirm(
           "Xác nhận cập nhật mật khẩu Khách hàng?",
           "",
           "Cập nhật",
           "Hủy"
       ).then((result) => {
           if (result.isConfirmed) {
               const passwordValue = document.getElementById("password").value;
               const formData = new FormData();
               formData.append("password", passwordValue);
               const employeeId = $("#id").val();
               $.ajax({
                   url: `/admin/customers/password/${employeeId}`,
                   type: "PUT",
                   data: formData,
                   processData: false,
                   contentType: false,
                   success: function (response) {
                       SwalUtils.success("Cập nhật mật khẩu thành công!", response.message);
                       $("#updateMatKhau").modal("hide");
                       searchUser($("#paginationContainer").data("current-page") || 0);
                   },
                   error: function (xhr) {
                       SwalUtils.error(
                           "Lỗi",
                           xhr.responseJSON?.message || "Cập nhật mật khẩu thất bại"
                       );
                   },
               });
           }
       });
   });



   $("#resetmatkhau").click(function (e) {
       Resetpassword();
   });


   function Resetpassword() {
       // lấy ra id từ form
       const userId = $("#id").val()
       console.log(userId + "id để reset")
       console.log(userId + "oge");
       SwalUtils.confirm(
           'Bạn có chắc muốn cấp lại mật khẩu cho tài khoản này?',
           '',
           'Xác nhận',
           'Hủy',
           { icon: 'warning' }
       ).then((result) => {
           if (result.isConfirmed) {
               fetch(`/admin/customers/${userId}/resetpassword`, {
                   method: 'PUT'
               })
                   .then(res => {
                       if (!res.ok) throw new Error('Khôi phục mật khẩu thất bại');
                       return res.json();
                   })
                   .then(data => {
                       SwalUtils.success(
                           data.message,
                           '',
                           {
                               timer: 1500,
                               showConfirmButton: false
                           }
                       );
                       setTimeout(() => location.reload(), 1000);
                   })
                   .catch(err => {
                       SwalUtils.error("Lỗi", err.message);
                   });
           }
       });
   }




   window.openAddModal = openAddModal;
   window.searchUser = searchUser;
});

// === Load danh sách Tỉnh/Thành phố ===
function loadProvinces() {
    fetch('/api/provinces')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(provinces => {
            console.log("Loaded provinces:", provinces);
            const select = $("#tinhThanh");
            select.empty();
            select.append('<option value="">-- Chọn Tỉnh/Thành phố --</option>');

            provinces.forEach(province => {
                select.append(`<option value="${province.code}">${province.name}</option>`);
            });
        })
        .catch(error => {
            console.error('Error loading provinces:', error);
            SwalUtils.error('Lỗi!', 'Không thể tải danh sách tỉnh/thành phố.');
        });
}

// === Load danh sách Phường/Xã theo Tỉnh ===
function loadCommunes(provinceCode) {
    return fetch(`/api/communes?provinceCode=${provinceCode}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(communes => {
            console.log(`Loaded communes for province ${provinceCode}:`, communes);
            const select = $("#phuongXa");
            select.empty();
            select.append('<option value="">-- Chọn Phường/Xã --</option>');

            if (communes && communes.length > 0) {
                communes.forEach(commune => {
                    select.append(`<option value="${commune.code}">${commune.name}</option>`);
                });
            } else {
                console.warn(`No communes found for province code: ${provinceCode}`);
                select.append('<option value="">Không có dữ liệu phường/xã</option>');
            }
            return communes;
        })
        .catch(error => {
            console.error('Error loading communes:', error);
            const select = $("#phuongXa");
            select.empty();
            select.append('<option value="">Lỗi tải dữ liệu</option>');
            throw error;
        });
}


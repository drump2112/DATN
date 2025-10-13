Dropzone.autoDiscover = false;

var avatarDropzone = null;

$(document).ready(function () {
  console.log("load register.js");

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

  $(".i-checks input").iCheck({
    checkboxClass: "icheckbox_square-green",
    radioClass: "iradio_square-green",
  });

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
});


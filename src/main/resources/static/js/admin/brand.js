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
        this.removeFile(this.files[0]);
      }

      if (file instanceof File) {
        const dt = new DataTransfer();
        dt.items.add(file);
        document.getElementById("avatarInput").files = dt.files;
      }
    });
  }

  function openAddModal() {
    $("#modalTitle").text("Thêm Thương Hiệu");

    $("#myModal").modal("show");
  }

  $("#btnAddBrand").click(function () {
    Swal.fire({
      title: "Xác nhận thêm thương hiệu ?",
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

        formData.append("name", $("#nameBrand").val());

        if (avatarFile) {
          formData.append("logoUrl", avatarFile, avatarFile.name);
          console.log("Name File:", avatarFile.name);
        } else {
          console.log("No file select");
        }

        $.ajax({
          url: "/admin/brand/add",
          method: "POST",
          processData: false,
          contentType: false,
          data: formData,
          success: function (response) {
            Swal.fire("Thành công!", response.message, "success");
            $("#myModal").modal("hide");
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

  window.openAddModal = openAddModal;
});

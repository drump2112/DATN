Dropzone.autoDiscover = false;
var avatarDropzone = null;

$(document).ready(function () {
  $("#brandForm").validate({
    ignore: [],
    rules: {
      nameBrand: { required: true, maxlength: 20 },
      avatar: { required: true },
    },
    messages: {
      nameBrand: {
        required: "Vui lòng nhập tên thương hiệu",
        maxlength: "Tên thương hiệu không quá 20 ký tự",
      },
      avatar: { required: "Vui lòng chọn logo" },
    },
    errorPlacement: function (error, element) {
      error.insertAfter(element); // lỗi hiển thị dưới input
    },
  });

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
    $("#brandForm")[0].reset();
    $("#brandForm").validate().resetForm();
    $("#brandId").val("");

    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }

    $("#btnAddBrand").show();
    $("#btnUpdateBrand").hide();
    $("#myModal").modal("show");
  }

  window.handleDetailClick = function (button) {
    const id = $(button).data("id");

    $.ajax({
      url: `/admin/brand/${id}`,
      type: "GET",
      success: function (res) {
        $("#brandId").val(res.id);
        $("#brandCode").val(res.brandCode);
        $("#nameBrand").val(res.name);

        if (avatarDropzone) {
          avatarDropzone.removeAllFiles(true);

          if (res.logoUrl && res.logoUrl.length > 0) {
            // Lấy ảnh đầu tiên thôi
            let url = res.logoUrl;
            let mockFile = { name: url.split("/").pop(), size: 12345 };

            avatarDropzone.emit("addedfile", mockFile);
            avatarDropzone.emit("complete", mockFile);

            // Gán ảnh thật vào thumbnail
            $(mockFile.previewElement)
              .find("img[data-dz-thumbnail]")
              .attr("src", url)
              .css({
                width: "120px",
                height: "120px",
                objectFit: "cover",
                objectPosition: "center",
              });

            avatarDropzone.files.push(mockFile);
          }
        }
        openEditModal(true);
      },
      error: function (err) {
        Swal.fire("Lỗi", "Không thể tải dữ liệu sản phẩm", "error");
      },
    });
  };

  function openEditModal(isEditable) {
    $("#modalTitle").text("Chi Tiết Và Cập Nhật");
    $("#btnAddBrand").hide();
    $("#btnUpdate").show();
    $("#myModal").modal("show");
  }

  $("#btnAddBrand").click(function () {
    if (!$("#brandForm").valid()) {
      return;
    }

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

  window.toggleStatus = function (id, isActive) {
    const title = isActive
      ? "Bạn có chắc muốn khóa thuong hiệu  này?"
      : "Bạn có chắc muốn mở khóa thương hiệu này?";

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
          url: `/admin/brand/${id}/toggle-status`,
          type: "PUT",
          success: function (data) {
            Swal.fire("Thành công", data.message, "success");
            const currentPage =
              parseInt(
                $("#paginationContainer .paginate_button.active a").text(),
              ) - 1 || 0;
            searchBrand(currentPage);
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

  function searchBrand(page) {
    let data = {
      page: page,
      size: 5,
    };

    let keyword = $("#searchInput").val().trim();
    let isActive = $("#statusFilter").val();

    if (keyword && keyword.length > 0) {
      data.keyword = keyword;
    }
    if (isActive !== null && isActive !== undefined && isActive !== "") {
      data.isActive = isActive;
    }

    $.ajax({
      url: "/admin/brand/search",
      type: "GET",
      data: data,
      success: function (response) {
        $("#brandTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải dữ liệu thương hiệu");
      },
    });
  }

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
      $("#brandForm")[0].reset();

      $("#brandId").val("");

      if (avatarDropzone) {
        avatarDropzone.removeAllFiles(true);
      }

      $("#btnAddBrand").show();
      $("#btnUpdateBrand").hide();
      $("#myModal").modal("show");
    }

    window.handleDetailClick = function (button) {
      const id = $(button).data("id");

      $.ajax({
        url: `/admin/brand/${id}`,
        type: "GET",
        success: function (res) {
          $("#brandId").val(res.id);
          $("#brandCode").val(res.brandCode);
          $("#nameBrand").val(res.name);

          if (avatarDropzone) {
            avatarDropzone.removeAllFiles(true);

            if (res.logoUrl && res.logoUrl.length > 0) {
              // Lấy ảnh đầu tiên thôi
              let url = res.logoUrl;
              let mockFile = { name: url.split("/").pop(), size: 12345 };

              avatarDropzone.emit("addedfile", mockFile);
              avatarDropzone.emit("complete", mockFile);

              // Gán ảnh thật vào thumbnail
              $(mockFile.previewElement)
                .find("img[data-dz-thumbnail]")
                .attr("src", url)
                .css({
                  width: "120px",
                  height: "120px",
                  objectFit: "cover",
                  objectPosition: "center",
                });

              avatarDropzone.files.push(mockFile);
            }
          }
          openEditModal(true);
        },
        error: function (err) {
          Swal.fire("Lỗi", "Không thể tải dữ liệu sản phẩm", "error");
        },
      });
    };

    function openEditModal(isEditable) {
      $("#modalTitle").text("Chi Tiết Và Cập Nhật");
      $("#btnAddBrand").hide();
      $("#btnUpdate").show();
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

    window.toggleStatus = function (id, isActive) {
      const title = isActive
        ? "Bạn có chắc muốn khóa thuong hiệu  này?"
        : "Bạn có chắc muốn mở khóa thương hiệu này?";

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
            url: `/admin/brand/${id}/toggle-status`,
            type: "PUT",
            success: function (data) {
              Swal.fire("Thành công", data.message, "success");
              const currentPage =
                parseInt(
                  $("#paginationContainer .paginate_button.active a").text(),
                ) - 1 || 0;
              searchBrand(currentPage);
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

    function searchBrand(page) {
      let data = {
        page: page,
        size: 5,
      };

      let keyword = $("#searchInput").val().trim();
      let isActive = $("#statusFilter").val();

      if (keyword && keyword.length > 0) {
        data.keyword = keyword;
      }
      if (isActive !== null && isActive !== undefined && isActive !== "") {
        data.isActive = isActive;
      }

      $.ajax({
        url: "/admin/brand/search",
        type: "GET",
        data: data,
        success: function (response) {
          $("#brandTableContainer").html(response);
        },
        error: function () {
          toastr.error("Không thể tải dữ liệu thương hiệu");
        },
      });
    }

    $("#resetFilterBtn").on("click", function () {
      $("#statusFilter").val("");
      $("#searchInput").val("");
      searchBrand(0);
    });

    window.searchBrand = searchBrand;
    window.openAddModal = openAddModal;
  });

  window.searchBrand = searchBrand;
  window.openAddModal = openAddModal;
});

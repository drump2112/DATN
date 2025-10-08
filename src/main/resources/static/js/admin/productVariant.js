$(document).ready(function () {
  // Khởi tạo jQuery Steps
  $('[data-toggle="popover"]').popover();

  initFilter();

  $("#productForm")
    .steps({
      bodyTag: "fieldset",
      onStepChanging: function (event, currentIndex, newIndex) {
        var form = $(this);

        // Cho phép quay lại mà không cần validate
        if (currentIndex > newIndex) {
          return true;
        }

        // Lấy fieldset hiện tại
        var currentFieldset = form.find("fieldset").eq(currentIndex);

        // Cấu hình validate để bỏ qua các trường ẩn, trừ Select2
        form.validate().settings.ignore =
          ":hidden:not(.select2-hidden-accessible)";

        // Chỉ validate các trường trong fieldset hiện tại
        var isValid = true;
        currentFieldset.find(":input").each(function () {
          if (!$(this).valid()) {
            isValid = false;
          }
        });

        // Debug
        console.log("Current Index:", currentIndex, "New Index:", newIndex);
        console.log("Fieldset valid:", isValid);
        if (!isValid) {
          console.log("Validation errors:", form.validate().errorList);
          currentFieldset.find(":input").each(function () {
            console.log($(this).attr("name"), "is valid:", $(this).valid());
          });
        }

        // Xóa class error nếu hợp lệ
        if (isValid) {
          $(form)
            .parent()
            .find(".steps ul li")
            .eq(currentIndex)
            .removeClass("error");
        }

        return isValid;
      },
      onStepChanged: function (event, currentIndex, priorIndex) {
        var form = $(this);

        // Render ô input cho kích cỡ ở bước 2
        if (priorIndex === 0 && currentIndex === 1) {
          // Lấy danh sách kích cỡ đã chọn
          var selectedSizes = $("#kichCo").select2("data");

          // Lấy fieldset của bước 2
          var step2Fieldset = form.find("fieldset").eq(1).find(".row");

          // Xóa nội dung cũ trong bước 2
          step2Fieldset.empty();

          // Tạo bảng HTML
          var tableHtml = `
                        <div class="table-responsive">
                            <table class="table table-bordered table-striped">
                                <thead>
                                    <tr>
                                        <th>Size</th>
                                        <th>Số Lượng</th>
                                    </tr>
                                </thead>
                                <tbody>
                    `;

          // Tạo hàng cho mỗi kích cỡ
          selectedSizes.forEach(function (size, index) {
            tableHtml += `
                            <tr>
                                <td>${size.text}</td>
                                <td>
                                    <input type="text" name="quantity_${size.id}" class="form-control touchspin-size required" value="0" />
                                </td>
                            </tr>
                        `;
          });

          tableHtml += `
                                </tbody>
                            </table>
                        </div>
                    `;

          // Thêm bảng vào bước 2
          step2Fieldset.append(tableHtml);

          // Khởi tạo TouchSpin cho các ô Số Lượng
          $(".touchspin-size").TouchSpin({
            min: 0,
            max: 999999,
            buttondown_class: "btn btn-white",
            buttonup_class: "btn btn-white",
          });

          // Thêm quy tắc validate động cho các ô Số Lượng và SKU
          selectedSizes.forEach(function (size) {
            $(`input[name="quantity_${size.id}"]`).rules("add", {
              required: true,
              min: 1,
              messages: {
                required: "Vui lòng nhập số lượng",
                min: "Số lượng phải lớn hơn 0",
              },
            });
            // $(`input[name="sku_${size.id}"]`).rules("add", {
            //   required: true,
            //   messages: {
            //     required: "Vui lòng nhập SKU",
            //   },
            // });
          });

          console.log("Rendered sizes in step 2:", selectedSizes);
        }
      },
      onFinishing: function (event, currentIndex) {
        var form = $(this);
        form.validate().settings.ignore =
          ":hidden:not(.select2-hidden-accessible)";
        return form.valid(); // Validate toàn bộ form khi hoàn tất
      },
      onFinished: async function (event, currentIndex) {
        var form = $(this);
        // Hiển thị SweetAlert xác nhận
        const result = await Swal.fire({
          title: "Xác nhận thêm sản phẩm",
          text: "Bạn có chắc chắn muốn thêm sản phẩm này không?",
          icon: "question",
          showCancelButton: true,
          confirmButtonText: "Xác nhận",
          cancelButtonText: "Hủy",
          reverseButtons: true,
        });

        // Chỉ tiến hành submit nếu người dùng nhấn "Xác nhận"
        if (result.isConfirmed) {
          var formData = collectFormData();
          try {
            await submitProductVariant(formData); // Chờ submit hoàn tất
            // Các hành động tiếp theo chỉ thực hiện nếu submit thành công
            $("#myModal").modal("hide");
            $("#productForm")[0].reset();
            $("#sanPham, #mauSac, #kichCo, #status")
              .val(null)
              .trigger("change.select2");
            $("#productForm fieldset").eq(1).find(".row").empty();
            $.get("/admin/productVariant/count").done(function (totalItems) {
              const pageSize = 5;
              const lastPage = Math.max(
                0,
                Math.ceil(totalItems / pageSize) - 1,
              );
              searchProductVariants(lastPage);
            });
          } catch (error) {
            console.error("Submit failed:", error);
          }
        }
      },
    })
    .validate({
      ignore: ":hidden:not(.select2-hidden-accessible)",
      // Vô hiệu hóa validate tự động
      onfocusout: false,
      onkeyup: false,
      onclick: false,
      rules: {
        sanPham: { required: true },
        mauSac: { required: true },
        "kichCo[]": { required: true },
        price: {
          required: true,
          min: 1000,
        },
        status: { required: true },
      },
      messages: {
        sanPham: { required: "Vui lòng chọn sản phẩm" },
        mauSac: { required: "Vui lòng chọn màu sắc" },
        "kichCo[]": { required: "Vui lòng chọn ít nhất một kích cỡ" },
        price: {
          required: "Vui lòng nhập giá",
          min: "Giá phải lớn hơn hoặc bằng 1000",
        },
        status: { required: "Vui lòng chọn trạng thái" },
      },
      errorPlacement: function (error, element) {
        var formGroup = element.closest(".form-group");
        var label = formGroup.find("label").first();

        if (label.length) {
          error.insertAfter(label);
        } else {
          error.insertAfter(element);
        }
      },
    });

  // Log giá trị khi nhấn Next
  $("#productForm").on("click", ".actions a[href='#next']", function () {
    console.log("sanPham:", $("#sanPham").val());
    console.log("mauSac:", $("#mauSac").val());
    console.log("kichCo:", $("#kichCo").val());
    console.log("price:", $("#price").val());
    console.log("status:", $("#status").val());
    // Log giá trị Số Lượng và SKU ở bước 2
    $("#productForm fieldset")
      .eq(1)
      .find(":input")
      .each(function () {
        console.log($(this).attr("name"), ":", $(this).val());
      });
  });

  // ==============================
  // Dropzone cho Add
  // ==============================
  let avatarDropzone = null;
  $("#myModal").on("shown.bs.modal", function () {
    console.log("Modal shown, initializing Select2 and Dropzone");
    initSelect2s();

    // Khởi tạo Dropzone
    try {
      if (!avatarDropzone) {
        Dropzone.autoDiscover = false;
        avatarDropzone = new Dropzone("#avatarDropzone", {
          url: "/dummy-upload",
          autoProcessQueue: false,
          clickable: true,
          maxFiles: 3,
          acceptedFiles: "image/*",
          addRemoveLinks: true,
          dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
          dictRemoveFile: "Xóa ảnh",
          dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
          previewsContainer: "#avatarDropzone",
        });
        console.log("Dropzone initialized successfully");
      }
    } catch (error) {
      console.error("Error initializing Dropzone:", error);
    }
  });

  // ==============================
  // Dropzone cho Update
  // ==============================
  // Chặn Dropzone tự động khởi tạo

  // Khởi tạo Dropzone cho Update

  Dropzone.autoDiscover = false;
  let avatarDropzoneUpdate = new Dropzone("#avatarDropzoneUpdate", {
    url: "/dummy-upload",
    autoProcessQueue: false,
    clickable: true,
    maxFiles: 3,
    acceptedFiles: "image/*",
    addRemoveLinks: true,
    dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
    dictRemoveFile: "Xóa ảnh",
    previewsContainer: "#avatarDropzoneUpdate",
  });

  // Reset form và Dropzone khi mở modal
  $("#btnOpenAddModal").on("click", () => {
    console.log("Opening modal");
    $("#modalTitle").text("Thêm Sản Phẩm Chi Tiết");
    $("#btnAddProduct").show();
    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }
    $("#productForm")[0].reset();
    $("#sanPham, #mauSac, #kichCo").val(null).trigger("change.select2");
    $("#productForm fieldset").eq(1).find(".row").empty();
    $("#myModal").modal("show");
  });

  // ==============================
  // Click nút sửa -> load dữ liệu vào form update
  // ==============================

  function initSelect2sUpdateModal() {
    $("#color")
      .select2({
        dropdownParent: $("#updateModal"),
        placeholder: "Chọn Màu Sắc",
        allowClear: true,
        ajax: {
          url: "/admin/color/select2",
          dataType: "json",
          delay: 50,
          data: (params) => ({ q: params.term }),
          processResults: (data) => ({ results: data }),
          cache: true,
        },
      })
      .val(null)
      .trigger("change.select2");

    $("#size")
      .select2({
        dropdownParent: "#updateModal",
        placeholder: "Chọn Kích Thước",
        allowClear: true,
        ajax: {
          url: "/admin/size/select2",
          dataType: "json",
          delay: 50,
          data: (params) => ({ q: params.term }),
          processResults: (data) => ({ results: data }),
          cache: true,
        },
      })
      .val(null)
      .trigger("change.select2");

    $("#status")
      .select2({
        dropdownParent: "#myModal",
        placeholder: "Chọn Trạng Thái",
        allowClear: true,
      })
      .val(null)
      .trigger("change.select2");
  }

  window.handleDetailClick = function (button) {
    initSelect2sUpdateModal();

    console.log("function called");
    const id = $(button).data("id");

    $.ajax({
      url: `/admin/productVariant/${id}`,
      type: "GET",
      success: function (res) {
        console.log("👉 API trả về:", res);
        console.log("id = " + res.id);
        // fill input
        $("#updateProductForm #productId").val(res.id);
        $("#updateProductForm #maSp").val(res.variantCode || res.productCode);
        $("#updateProductForm #tenSp").val(res.productName);
        $("#updateProductForm #description").val(res.productDescription);

        // Giá & số lượng (cần đặt đúng id trong form)
        $("#updateProductForm #price").val(res.price);
        $("#updateProductForm #quantity").val(res.quantity);

        // select2: danh mục
        if (res.categoryName && res.categoryId) {
          const option = new Option(
            res.categoryName,
            res.categoryId,
            true,
            true,
          );
          $("#updateProductForm #danhMuc").append(option).trigger("change");
        }

        // select2: thương hiệu
        if (res.brandName && res.brandId) {
          const option = new Option(res.brandName, res.brandId, true, true);
          $("#updateProductForm #thuongHieu").append(option).trigger("change");
        }

        // select2: size
        if (res.sizeId) {
          const option = new Option(res.sizeName, res.sizeId, true, true);
          $("#updateProductForm #size").append(option).trigger("change");
        }

        // select2: color
        if (res.colorId) {
          const option = new Option(res.colorName, res.colorId, true, true);
          $("#updateProductForm #color").append(option).trigger("change");
        }

        // clear ảnh cũ và load ảnh mới vào Dropzone update
        if (avatarDropzoneUpdate) {
          avatarDropzoneUpdate.removeAllFiles(true);
          if (res.imageUrls && res.imageUrls.length > 0) {
            res.imageUrls.forEach((url) => {
              let mockFile = { name: url.split("/").pop(), size: 12345 };
              avatarDropzoneUpdate.emit("addedfile", mockFile);
              avatarDropzoneUpdate.emit("complete", mockFile);

              // Gán ảnh thật vào background (không bị mờ)
              $(mockFile.previewElement)
                .find("img[data-dz-thumbnail]")
                .attr("src", url)
                .css({
                  width: "120px", // hoặc để Dropzone mặc định
                  height: "120px",
                  objectFit: "cover", // giữ tỉ lệ và crop cho đều
                  objectPosition: "center", // căn giữa
                });
              avatarDropzoneUpdate.files.push(mockFile);
            });
          }
        }

        $("#updateModal").modal("show");
      },
      error: function (err) {
        Swal.fire("Lỗi", "Không thể tải dữ liệu sản phẩm", "error");
      },
    });
  };

  $("#btnUpdateProduct").on("click", function () {
    if (!$("#updateProductForm").valid()) {
      return;
    }

    const id = $("#productId").val();
    const formData = new FormData();

    // Lấy giá
    formData.append("price", $("#updateProductForm #price").val());

    // Lấy ảnh (Dropzone hoặc input file)
    if (typeof avatarDropzoneUpdate !== "undefined") {
      avatarDropzoneUpdate.getAcceptedFiles().forEach((file, index) => {
        formData.append("images", file);
        console.log("Dropzone file:", file.name, file.size, file.type);
      });
    }
    for (let [key, value] of formData.entries()) {
      console.log(key, value);
    }

    Swal.fire({
      title: "Xác nhận cập nhật",
      text: "Bạn có chắc chắn muốn cập nhật giá và ảnh sản phẩm?",
      icon: "question",
      showCancelButton: true,
      confirmButtonText: "Xác nhận",
      cancelButtonText: "Hủy",
    }).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/admin/productVariant/${id}/update`,
          type: "PUT",
          data: formData,
          processData: false,
          contentType: false,
          success: function (res) {
            Swal.fire("Thành công", res.message, "success");
            $("#updateModal").modal("hide");
            $("#updateProductForm")[0].reset();
            if (typeof avatarDropzoneUpdate !== "undefined") {
              avatarDropzoneUpdate.removeAllFiles(true);
            }
            searchProductVariants(0);
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

  // Hàm khởi tạo Select2
  function initSelect2s() {
    $("#sanPham")
      .select2({
        dropdownParent: $("#myModal"),
        placeholder: "Chọn Sản Phẩm",
        allowClear: true,
        ajax: {
          url: "/admin/product/select2",
          dataType: "json",
          delay: 50,
          data: (params) => ({ q: params.term }),
          processResults: (data) => ({ results: data }),
          cache: true,
        },
      })
      .val(null)
      .trigger("change.select2");

    $("#mauSac")
      .select2({
        dropdownParent: $("#myModal"),
        placeholder: "Chọn Màu Sắc",
        allowClear: true,
        ajax: {
          url: "/admin/color/select2",
          dataType: "json",
          delay: 50,
          data: (params) => ({ q: params.term }),
          processResults: (data) => ({ results: data }),
          cache: true,
        },
      })
      .val(null)
      .trigger("change.select2");

    $("#kichCo")
      .select2({
        dropdownParent: "#myModal",
        placeholder: "Chọn Kích Thước",
        allowClear: true,
        ajax: {
          url: "/admin/size/select2",
          dataType: "json",
          delay: 50,
          data: (params) => ({ q: params.term }),
          processResults: (data) => ({ results: data }),
          cache: true,
        },
      })
      .val(null)
      .trigger("change.select2");

    // $("#status")
    //     .select2({
    //         dropdownParent: "#myModal",
    //         placeholder: "Chọn Trạng Thái",
    //         allowClear: true,
    //     })
    //     .val(null)
    //     .trigger("change.select2");
  }

  function initFilter() {
    $("#colorFilter").select2({
      placeholder: "Chọn Màu Sắc",
      allowClear: true,
      ajax: {
        url: "/admin/color/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#sizeFilter").select2({
      placeholder: "Chọn Kích Thước",
      allowClear: true,
      ajax: {
        url: "/admin/size/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#brandFilter").select2({
      placeholder: "Chọn Thương Hiệu",
      allowClear: true,
      ajax: {
        url: "/admin/brand/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#cateFilter").select2({
      placeholder: "Chọn danh mục",
      allowClear: true,
      ajax: {
        url: "/admin/category/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });

    $("#statusFilter")
      .select2({
        placeholder: "Chọn Trạng Thái",
        allowClear: true,
      })
      .val(null)
      .trigger("change.select2");
  }

  // Khởi tạo TouchSpin cho price
  $("#price").TouchSpin({
    min: 0,
    max: 999999999,
    step: 1000,
    buttondown_class: "btn btn-white",
    buttonup_class: "btn btn-white",
  });

  // Hàm thu thập dữ liệu form
  function collectFormData() {
    const formData = new FormData();
    formData.append("sku", $("#maSp").val());
    formData.append("price", $("#price").val());
    formData.append("status", $("#status").val());
    formData.append("productId", $("#sanPham").val());
    formData.append("colorId", $("#mauSac").val());

    // Thu thập kích cỡ, số lượng và SKU
    const selectedSizes = $("#kichCo").select2("data");
    selectedSizes.forEach(function (size) {
      formData.append("sizeIds[]", size.id);
      formData.append(
        `quantities[${size.id}]`,
        $(`input[name="quantity_${size.id}"]`).val(),
      );
      formData.append(
        `skus[${size.id}]`,
        $(`input[name="sku_${size.id}"]`).val(),
      );
    });

    if (avatarDropzone) {
      avatarDropzone.getAcceptedFiles().forEach((file) => {
        formData.append("images[]", file);
      });
    }

    return formData;
  }

  // Hàm submit form
  async function submitProductVariant(formData) {
    // Log FormData để debug
    for (let [key, value] of formData.entries()) {
      console.log(`${key}: ${value}`);
    }

    return new Promise((resolve, reject) => {
      $.ajax({
        url: "/admin/productVariant/add",
        method: "POST",
        processData: false,
        contentType: false,
        data: formData,
        success: function (res) {
          Swal.fire("Thành công", res.message, "success");
          resolve(res); // Hoàn tất thành công
        },
        error: function (xhr) {
          Swal.fire(
            "Lỗi",
            xhr.responseJSON?.message || "Thêm thất bại",
            "error",
          );
          reject(xhr); // Lỗi
        },
      });
    });
  }

  window.toggleStatus = function (productVariantId, isActive) {
    const title = isActive
      ? "Ngừng kinh doanh sản phẩm này?"
      : "Kích hoạt sản phẩm này?";

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
          url: `/admin/productVariant/${productVariantId}/toggle-status`,
          type: "PUT",
          success: function (data) {
            Swal.fire("Thành công", data.message, "success");
            const currentPage =
              parseInt(
                $("#paginationContainer .paginate_button.active a").text(),
              ) - 1 || 0;
            searchProductVariants(currentPage);
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

  function searchProductVariants(page) {
    const keyword = $("#searchInput").val().trim();
    const colorId = $("#colorFilter").val();
    const sizeId = $("#sizeFilter").val();
    const cateId = $("#cateFilter").val();
    const brandId = $("#brandFilter").val();
    const status = $("#statusFilter").val();

    $.ajax({
      url: "/admin/productVariant/search",
      type: "GET",
      data: {
        page: page,
        keyword: keyword,
        colorId: colorId,
        sizeId: sizeId,
        cateId: cateId,
        brandId: brandId,
        status: status,
      },
      success: function (response) {
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải danh sách sản phẩm!");
      },
    });
  }

  $("#resetFilterBtn").on("click", function () {
    $("#colorFilter").val(null).trigger("change");
    $("#sizeFilter").val(null).trigger("change");
    $("#cateFilter").val(null).trigger("change");
    $("#statusFilter").val(null).trigger("change");

    $.ajax({
      url: "/admin/productVariant/table",
      type: "GET",
      data: {
        page: 0,
        size: 5,
      },
      success: function (response) {
        // Render lại fragment bảng
        $("#productTableContainer").html(response);
      },
      error: function () {
        toastr.error("Không thể tải lại bảng");
      },
    });
    $("#searchInput").val("");
  });

  //validate updateProductForm

  $("#updateProductForm").validate({
    ignore: ":hidden:not(.select2-hidden-accessible)",
    onfocusout: false,
    onkeyup: false,
    onclick: false,
    rules: {
      price: {
        required: true,
        min: 1000,
      },
    },
    messages: {
      price: {
        required: "Vui lòng nhập giá",
        min: "Giá phải lớn hơn hoặc bằng 1000",
      },
    },
    errorPlacement: function (error, element) {
      if (element.hasClass("select2-hidden-accessible")) {
        error.insertAfter(element.next(".select2-container"));
      } else if (
        element.hasClass("touchspin2") ||
        element.hasClass("touchspin-size")
      ) {
        error.insertAfter(element.closest(".bootstrap-touchspin"));
      } else {
        error.insertAfter(element);
      }
    },
  });

  $(document).on("click", "#fastAddColor", function () {
    console.log("Mở modal fastAddColorModal");
    $("#fastAddColorModal").modal({
      backdrop: "static", // Ngăn đóng khi nhấp vào backdrop
      keyboard: false, // Ngăn đóng khi nhấn phím Esc
    });
  });

  // Sự kiện mở modal fastAddSizeModal
  $(document).on("click", "#fastAddSize", function () {
    console.log("Mở modal fastAddSizeModal");
    $("#fastAddSizeModal").modal({
      backdrop: "static", // Ngăn đóng khi nhấp vào backdrop
      keyboard: false, // Ngăn đóng khi nhấn phím Esc
    });
  });

  $(document).on("hidden.bs.modal", function () {
    const anyOpenModal = $(".modal.show").length > 0;

    // Nếu vẫn còn modal khác đang mở
    if (anyOpenModal) {
      $("body").addClass("modal-open");
    } else {
      $("body").removeClass("modal-open");
      $(".modal-backdrop").remove();
    }
  });

  window.searchProductVariants = searchProductVariants;
});

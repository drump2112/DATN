Dropzone.autoDiscover = false;
var avatarDropzone = null;
var avatarDropzoneVariants = null;


$(document).ready(function () {
    // hàm khởi tạo step
    $.validator.addMethod(
        "select2Required",
        function (value, element) {
            return value !== null && value !== "" && value.length > 0;
        },
        "Vui lòng chọn giá trị",
    );


    $.validator.addMethod(
        "notBlank",
        function (value, element) {
            return value != null && $.trim(value).length > 0;
        },
        "Vui lòng nhập nội dung hợp lệ",
    );
    // validate cho productForm
    $("#productForm").validate({
        ignore: [],
        rules: {
            tenSp: { required: true, maxlength: 100 },
            danhMuc: { select2Required: true },
            thuongHieu: { select2Required: true },
            avatar: { required: true },
            description: { required: true, minlength: 10, notBlank: true },
        },
        messages: {
            tenSp: {
                required: "Vui lòng nhập tên sản phẩm",
                maxlength: "Tên sản phẩm không quá 100 ký tự",
            },
            danhMuc: { select2Required: "Vui lòng chọn danh mục" },
            thuongHieu: { select2Required: "Vui lòng chọn thương hiệu" },
            avatar: { required: "Vui lòng chọn ảnh sản phẩm" },
            description: {
                required: "Vui lòng nhập mô tả sản phẩm",
                minlength: "Mô tả phải có ít nhất 10 ký tự",
                notBlank: "Vui lòng nhập nội dung hợp lệ",
            },
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
    // Sự kiện mở modal fastAddColorModal
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


    // sự kiện add màu sắc
    $("#btnAddColor").click(function () {
        // if (!validateColorName()) return; //   check trước khi gửi


        SwalUtils.confirm(
            "Xác nhận thêm màu sắc?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();
                formData.append("id", $("#id").val().trim());
                formData.append("colorCode", $("#code").val() || null);
                formData.append("name", $("#name").val().trim());
                formData.append("isActive", true);


                $.ajax({
                    url: "/admin/color/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (response) {
                        toastr.success("Thêm màu sắc thành công !");


                        $("#mauSac")
                            .select2({
                                // dropdownParent: $("#myModal"),
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
                        $("#fastAddColorModal").modal("hide");
                    },
                    error: function (xhr) {
                        toastr.error(xhr.responseJSON?.message || "Thêm màu sắc thất bại");
                    },
                });
            }
        });
    });
    // sự kiện add kích thước
    $("#btnAddSize").click(function () {
        // if (!validateSizeName()) return; //   check trước khi gửi


        SwalUtils.confirm(
            "Xác nhận thêm kích thước?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();


                formData.append("id", $("#id").val().trim());
                formData.append("sizeCode", $("#code").val() || null);
                formData.append("name", $("#name").val().trim());
                formData.append("isActive", true);


                $.ajax({
                    url: "/admin/size/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (response) {
                        toastr.success("Thêm kích thước thành công !");


                        $("#kichCo")
                            .select2({
                                // dropdownParent: "#myModal",
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
                        $("#fastAddSizeModal").modal("hide");
                    },
                    error: function (xhr) {
                        toastr.error(
                            xhr.responseJSON?.message || "Thêm kích thước thất bại",
                        );
                    },
                });
            }
        });
    });




    // Select2 cho #productForm
    function initSelect2s() {
        $("#danhMuc").select2({
            dropdownParent: $("#myModal"),
            placeholder: "Chọn danh mục",
            allowClear: true,
            ajax: {
                url: "/admin/categories/select2",
                dataType: "json",
                delay: 50,
                data: (params) => ({ q: params.term }),
                processResults: (data) => ({ results: data }),
                cache: true,
            },
        });


        $("#thuongHieu").select2({
            dropdownParent: $("#myModal"),
            placeholder: "Chọn thương hiệu",
            allowClear: true,
            ajax: {
                url: "/admin/brand/select2",
                dataType: "json",
                delay: 50,
                data: (params) => ({ q: params.term }),
                processResults: (data) => ({ results: data }),
                cache: true,
            },
        });
    }


    initSelect2s();


    $("#description").on("input", function () {
        $(this).valid();
    });


    // Dropzone cho #myModal #productForm
    avatarDropzone = new Dropzone("#myModal #avatarDropzone", {
        url: "/dummy-upload",
        autoProcessQueue: false,
        clickable: true,
        maxFiles: 1,
        acceptedFiles: "image/*",
        addRemoveLinks: true,
        dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
        dictRemoveFile: "Xóa ảnh",
        dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
        previewsContainer: "#myModal #avatarDropzone",
    });


    avatarDropzone.on("addedfile", function (file) {
        if (this.files.length > 1) {
            this.removeFile(this.files[0]);
        }
        if (file instanceof File) {
            const dt = new DataTransfer();
            dt.items.add(file);
            $("#myModal #avatarInput").prop("files", dt.files);
        }
    });


    // Hidden event cho #myModal
    $("#myModal").on("hidden.bs.modal", function () {
        if (avatarDropzone) {
            avatarDropzone.removeAllFiles(true);
        }
        $("#myModal #avatarInput").val("");
        $("#danhMuc, #thuongHieu").val(null).trigger("change");
    });


    // openAddModal cho #myModal
    function openAddModal() {
        $("#productForm").validate().resetForm();
        $("#modalTitle").text("Thêm Sản Phẩm");
        $("#productForm")[0].reset();
        $("#danhMuc, #thuongHieu").val(null).trigger("change");
        $("#btnAddProduct").show();
        $("#btnUpdateProduct").hide();
        if (avatarDropzone) avatarDropzone.removeAllFiles(true);
        $("#myModal").modal("show");
    }


    // openEditModal cho #myModal
    function openEditModal(product) {
        console.log("Dữ liệu sản phẩm:", product);
        $("#productForm").validate().resetForm();
        $("#modalTitle").text("Chi Tiết Và Cập Nhật Sản Phẩm");
        $("#productForm")[0].reset();


        $("#productId").val(product.id);
        $("#maSp").val(product.productCode);
        $("#tenSp").val(product.name);
        $("#description").val(product.description || "");


        if (product.categoryId && product.categoryName) {
            const option = new Option(product.categoryName || "Unknown", product.categoryId, true, true);
            $("#danhMuc").append(option).trigger("change").valid();
        }


        if (product.brandId && product.brandName) {
            const option = new Option(product.brandName || "Unknown", product.brandId, true, true);
            $("#thuongHieu").append(option).trigger("change").valid();
        }


        if (avatarDropzone) avatarDropzone.removeAllFiles(true);


        if (product.thumbnail) {
            const thumbnail = product.thumbnail.startsWith("/") ? `${window.location.origin}${product.thumbnail}` : product.thumbnail;
            const mockFile = {
                name: thumbnail.split("/").pop() || "thumbnail.jpg",
                size: 12345,
                type: "image/jpeg",
                accepted: true
            };
            avatarDropzone.emit("addedfile", mockFile);
            avatarDropzone.emit("complete", mockFile);
            avatarDropzone.createThumbnailFromUrl(mockFile, thumbnail, () => console.log("Thumbnail loaded"), () => toastr.error("Không thể tải ảnh thumbnail!"));
            avatarDropzone.files.push(mockFile);
            const dt = new DataTransfer();
            const dummyFile = new File([""], mockFile.name, { type: mockFile.type });
            dt.items.add(dummyFile);
            $("#myModal #avatarInput").prop("files", dt.files);
            $("#myModal #avatarInput").valid();
        } else {
            $("#myModal #avatarInput").val("");
        }


        $("#productForm").valid();
        $("#btnAddProduct").hide();
        $("#btnUpdateProduct").show();
        $("#myModal").modal("show");
    }


    window.handleDetailClick = function (button) {
        const id = $(button).data("id");
        const currentPage = parseInt($("#paginationContainer .paginate_button.active a").text()) - 1 || 0;
        $("#productForm").data("current-page", currentPage);


        $.ajax({
            url: `/admin/product/${id}`,
            method: "GET",
            success: function (product) {
                openEditModal(product);
            },
            error: function () {
                toastr.error("Không thể lấy thông tin sản phẩm!");
            },
        });
    };


    // btnAddProduct cho #myModal
    $("#btnAddProduct").click(function () {
        if (!$("#productForm").valid()) return;


        SwalUtils.confirm(
            "Xác Nhận Thêm Sản Phẩm",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const files = avatarDropzone.getAcceptedFiles();
                const avatarFile = files[0] || null;
                const formData = new FormData();
                formData.append("name", $("#tenSp").val());
                formData.append("categoryId", $("#danhMuc").val());
                formData.append("brandId", $("#thuongHieu").val());
                formData.append("description", $("#description").val());
                if (avatarFile) formData.append("thumbnail", avatarFile, avatarFile.name);


                $.ajax({
                    url: "/admin/product/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (response) {
                        SwalUtils.toast("success", response.message);
                        $("#myModal").modal("hide");
                        $.get("/admin/product/count").done(function (totalItems) {
                            const pageSize = 5;
                            const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
                            searchProduct(lastPage);
                        });
                    },
                    error: function (xhr) {
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Thêm thất bại");
                    },
                });
            }
        });
    });


    // btnUpdateProduct cho #myModal
    $("#btnUpdateProduct").click(function () {
        if (!$("#productForm").valid()) return;
        SwalUtils.confirm(
            "Xác Nhận Cập Nhật Sản Phẩm",
            "",
            "Cập Nhật",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();
                formData.append("name", $("#tenSp").val());
                formData.append("categoryId", $("#danhMuc").val());
                formData.append("brandId", $("#thuongHieu").val());
                formData.append("description", $("#description").val());


                const files = avatarDropzone.getAcceptedFiles();
                if (files.length > 0 && files[0] instanceof File) {
                    formData.append("thumbnail", files[0], files[0].name);
                }


                const productId = $("#productId").val();


                $.ajax({
                    url: `/admin/product/${productId}`,
                    type: "PUT",
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (response) {
                        SwalUtils.toast("success", response.message);
                        $("#myModal").modal("hide");
                        const currentPage = $("#productForm").data("current-page") || 0;
                        searchProduct(currentPage);
                    },
                    error: function (xhr) {
                        toastr.error("Cập nhật thất bại: " + xhr.responseText);
                    },
                });
            }
        });
    });


    function searchProduct(page) {
        const keyword = $("#searchInput").val().trim();
        const isActive = $("#statusFilter").val();


        $.ajax({
            url: "/admin/product/search",
            type: "GET",
            data: { page, keyword, isActive },
            success: function (response) {
                $("#productTableContainer").html(response);
            },
            error: function () {
                toastr.error("Không thể tải danh sách sản phẩm!");
            },
        });
    }


    window.toggleStatus = function (productId, isActive) {
        const title = isActive ? "Bạn có chắc muốn khóa sản phẩm này?" : "Bạn có chắc muốn kích hoạt sản phẩm này?";


        SwalUtils.confirm(
            title,
            "",
            "Xác nhận",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/admin/product/${productId}/toggle-status`,
                    type: "PUT",
                    success: function (data) {
                        SwalUtils.toast('success', data.message);
                        const currentPage = parseInt($("#paginationContainer .paginate_button.active a").text()) - 1 || 0;
                        searchProduct(currentPage);
                    },
                    error: function (xhr) {
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Có lỗi xảy ra");
                    },
                });
            }
        });
    };


    $("#productForm").valid();
    window.openAddModal = openAddModal;
    window.searchProduct = searchProduct;


    // Select2 cho #ProductVariantsForm
    function initSelect2sVariants() {
        $("#tensanPham").select2({
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
        }).val(null).trigger("change.select2");


        $("#mauSac").select2({
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
        }).val(null).trigger("change.select2");


        $("#kichCo").select2({
            placeholder: "Chọn Kích Thước",
            allowClear: true,
            multiple: true,
            ajax: {
                url: "/admin/size/select2",
                dataType: "json",
                delay: 50,
                data: (params) => ({ q: params.term }),
                processResults: (data) => ({ results: data }),
                cache: true,
            },
        }).val(null).trigger("change.select2");
    }


    avatarDropzoneVariants = new Dropzone("#myModalProductVariants #avatarDropzoneVariants", {
        url: "/dummy-upload",
        autoProcessQueue: false,
        clickable: true,
        maxFiles: 3,
        acceptedFiles: "image/*",
        addRemoveLinks: true,
        dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
        dictRemoveFile: "Xóa ảnh",
        dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
        previewsContainer: "#myModalProductVariants #avatarDropzoneVariants",
    });


    avatarDropzoneVariants.on("addedfile", function (file) {
        const dt = new DataTransfer();
        this.files.forEach(f => dt.items.add(f));
        $("#myModalProductVariants #avatarInputVariants").prop("files", dt.files);
    });


    $("#myModalProductVariants").on("shown.bs.modal", function () {
        console.log("Modal ProductVariants shown, initializing...");
        initSelect2sVariants();
        const validator = $("#ProductVariantsForm").validate();
        if (validator) validator.resetForm();


        $("#price").val("").trigger("touchspin.updatesettings", { initval: "" });


        // Reset wizard về bước 1 (manual, tránh setStep vì không được implement)
        $(".steps ul li:first a").click();
        $(".steps ul li").removeClass("error done current");
        $(".steps ul li:first").addClass("current");


        // Reset nội dung động ở bước 2
        $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();


        if (avatarDropzoneVariants) avatarDropzoneVariants.removeAllFiles(true);


    });



    // Hidden event cho #myModalProductVariants
    $("#myModalProductVariants").on("hidden.bs.modal", function () {
        $("#ProductVariantsForm")[0].reset();
        $("#tensanPham, #mauSac, #kichCo").val(null).trigger("change.select2");
        const validator = $("#ProductVariantsForm").validate();
        if (validator) validator.resetForm();
        $("#price").val("").trigger("touchspin.updatesettings", { initval: "" });
        $(".steps ul li:first a").click();
        $(".steps ul li").removeClass("error done current");
        $(".steps ul li:first").addClass("current");
        $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();
        if (avatarDropzoneVariants) avatarDropzoneVariants.removeAllFiles(true);
    });


    // TouchSpin cho #price trong #ProductVariantsForm
    $("#price").TouchSpin({
        min: 0,
        max: 999999999,
        step: 1000,
        forcestepdivisibility: "none",
        buttondown_class: "btn btn-white",
        buttonup_class: "btn btn-white",
    });


    // Khởi tạo Steps chỉ cho #ProductVariantsForm
    function initSteps() {
        $("#ProductVariantsForm")
            .steps({
                bodyTag: "fieldset",
                labels: {
                    next: "Tiếp theo",
                    previous: "Quay lại",
                    finish: "Thêm",
                },
                onStepChanging: function (event, currentIndex, newIndex) {
                    var form = $(this);
                    if (currentIndex > newIndex) return true;


                    var currentFieldset = form.find("fieldset").eq(currentIndex);
                    form.validate().settings.ignore = ":hidden:not(.select2-hidden-accessible)";


                    var isValid = true;
                    currentFieldset.find(":input").each(function () {
                        if (!$(this).valid()) isValid = false;
                    });


                    console.log("Current Index:", currentIndex, "New Index:", newIndex, "Fieldset valid:", isValid);
                    if (!isValid) {
                        console.log("Validation errors:", form.validate().errorList);
                        currentFieldset.find(":input").each(function () {
                            console.log($(this).attr("name"), "is valid:", $(this).valid());
                        });
                    }


                    if (isValid) {
                        form.parent().find(".steps ul li").eq(currentIndex).removeClass("error");
                    }


                    return isValid;
                },
                onStepChanged: function (event, currentIndex, priorIndex) {
                    var form = $(this);


                    if (priorIndex === 0 && currentIndex === 1) {
                        var selectedSizes = $("#kichCo").select2("data");
                        var step2Fieldset = form.find("fieldset").eq(1).find(".row");
                        step2Fieldset.empty();


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


                        selectedSizes.forEach(function (size) {
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


                        step2Fieldset.append(tableHtml);


                        $(".touchspin-size").TouchSpin({
                            min: 0,
                            max: 999999,
                            buttondown_class: "btn btn-white",
                            buttonup_class: "btn btn-white",
                        });


                        selectedSizes.forEach(function (size) {
                            $(`input[name="quantity_${size.id}"]`).rules("add", {
                                required: true,
                                min: 1,
                                messages: {
                                    required: "Vui lòng nhập số lượng",
                                    min: "Số lượng phải lớn hơn 0",
                                },
                            });
                        });


                        console.log("Rendered sizes in step 2:", selectedSizes);
                    }


                    // Khởi tạo/refresh Dropzone khi đến step 3 (Ảnh Sản Phẩm, index 2)
                    if (priorIndex !== 2 && currentIndex === 2) {
                        // Destroy Dropzone cũ nếu tồn tại để tránhh trùng lặp Dropzone
                        if (avatarDropzoneVariants) {
                            avatarDropzoneVariants.destroy();
                        }


                        // Khởi tạo Dropzone mới cho step 3
                        avatarDropzoneVariants = new Dropzone("#myModalProductVariants #avatarDropzoneVariants", {
                            url: "/dummy-upload",
                            autoProcessQueue: false,
                            clickable: true,
                            maxFiles: 3,
                            acceptedFiles: "image/*",
                            addRemoveLinks: true,
                            dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
                            dictRemoveFile: "Xóa ảnh",
                            dictInvalidFileType: "Chỉ chấp nhận định dạng hình ảnh!",
                            previewsContainer: "#myModalProductVariants #avatarDropzoneVariants",
                        });


                        console.log("Dropzone refreshed for step 3");
                    }


                    // Optional: Reset files khi rời step 3
                    if (priorIndex === 2 && currentIndex !== 2) {
                        if (avatarDropzoneVariants) {
                            avatarDropzoneVariants.removeAllFiles(true);
                        }
                    }
                },




                onFinishing: function (event, currentIndex) {
                    var form = $(this);
                    form.validate().settings.ignore = ":hidden:not(.select2-hidden-accessible)";
                    return form.valid();
                },
                onFinished: async function (event, currentIndex) {
                    var form = $(this);
                    const result = await SwalUtils.confirm(
                        "Xác nhận thêm sản phẩm chi tiết",
                        "Bạn có chắc chắn muốn Thêm sản phẩm chi tiết này không?",
                        "Xác nhận",
                        "Hủy"
                    );


                    if (result.isConfirmed) {
                        var formData = collectFormDataVariants();
                        try {
                            await submitaddProductVariant(formData);
                            // $("#myModalProductVariants").modal("hide");
                            $("#ProductVariantsForm")[0].reset();
                            $("#tensanPham, #mauSac, #kichCo, #status").val(null).trigger("change.select2");
                            $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();
                            const currentPage = $("#ProductVariantsForm").data("current-page") || 0;
                            searchProduct(currentPage);
                        } catch (error) {
                            console.error("Submit failed:", error);
                        }
                    }
                },
            })
            .validate({
                ignore: ":hidden:not(.select2-hidden-accessible)",
                onfocusout: false,
                onkeyup: false,
                onclick: false,
                rules: {
                    tensanPham: { required: true },
                    mauSac: { required: true },
                    "kichCo[]": { required: true },
                    price: { required: true, min: 1000 },
                    status: { required: true },
                },
                messages: {
                    tensanPham: { required: "Vui lòng chọn sản phẩm" },
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
    }


    // Gọi initSteps chỉ cho #ProductVariantsForm
    initSteps();


    // collectFormDataVariants cho #ProductVariantsForm
    function collectFormDataVariants() {
        const formData = new FormData();
        formData.append("price", Number($("#price").val()));
        formData.append("status", $("#status").val());
        formData.append("productId", $("#id").val());
        formData.append("colorId", $("#mauSac").val());




        const selectedSizes = $("#kichCo").select2("data");
        selectedSizes.forEach(function (size) {
            formData.append("sizeIds[]", size.id);
            formData.append(`quantities[${size.id}]`, $(`input[name="quantity_${size.id}"]`).val());
        });


        if (avatarDropzoneVariants) {
            avatarDropzoneVariants.getAcceptedFiles().forEach((file) => {
                formData.append("images[]", file);
            });
        }
        return formData;
    }


    // submitProductVariantUpdate cho #ProductVariantsForm
    async function submitaddProductVariant(formData) {


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
                    SwalUtils.toast("success", res.message);
                    resolve(res);
                    // load lại bảng danh sách sản phẩm chi tiết trong modal bằng id của sp
                    const idproduct = $("#productIdsp").val();
                    const variantPagination = $("#variantPagination ul.pagination");
                    let currentPage = 0;
                    if (variantPagination.length > 0) {
                        currentPage = parseInt(variantPagination.data("current-page")) || 0;
                    }
                    console.log("Reloading variants with page:", currentPage);
                    getProductVariants(idproduct, currentPage, 5);
                },
                error: function (xhr) {
                    SwalUtils.error(
                        "Lỗi!",
                        xhr.responseJSON?.message || "Thêm thất bại"
                    );
                    reject(xhr); // Lỗi
                },
            });
        });
    }


    window.handleVariantDetailClick = function (button) {
        const id = $(button).data("id");
        $("#productIdsp").val(id);
        $("#myModalProductVariants").modal("show");
        const currentPage = parseInt($("#paginationContainer .paginate_button.active a").text()) - 1 || 0;
        $("#ProductVariantsForm").data("current-page", currentPage); // Lưu trang hiện tại để reload sau


        $.ajax({
            url: `/admin/product/${id}`,
            method: "GET",
            success: function (product) {
                console.log("Dữ liệu sản phẩm cho thêm chi tiết:", product);

                // Set title modal
                $("#modalTitleProductVariants").text("Thêm Sản Phẩm Chi Tiết");

                // Reset form và wizard trước khi fill
                $("#ProductVariantsForm")[0].reset();
                $("#tensanPham, #mauSac, #kichCo").val(null).trigger("change.select2");
                $("#price").val("").trigger("touchspin.updatesettings", { initval: "" });
                $("#status").val("1"); // Default Kinh Doanh
                // Reset wizard về bước 1 (manual, tránh setStep vì không được implement)
                $(".steps ul li:first a").click();
                $(".steps ul li").removeClass("error done current");
                $(".steps ul li:first").addClass("current");
                $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();


                $("#id").val(product.id);
                $("#codeProduct").val(product.productCode || "");

                $("#nameProduct").val(product.name || "");
                getProductVariants(id, currentPage, 5);
                $("#myModalProductVariants").modal("show");
            },
            error: function () {
                toastr.error("Không thể lấy thông tin sản phẩm để thêm chi tiết!");
            },
        });
    };


    window.getProductVariants = function (productId, page = 0, size = 5) {
        console.log("Load data productVariants by id", productId, page, size);
        $.ajax({
            url: `/admin/productVariant/productvariant-list/${productId}`,
            method: "GET",
            data: { page: page, size: size },
            success: function (response) {
                const list = response.listProducts;
                const tbody = $("#variantTableBody");
                tbody.empty();


                if (list && list.length > 0) {
                    list.forEach(item => {
                        const imageUrl = item.imageUrls && item.imageUrls.length > 0
                            ? `<img src="${item.imageUrls[0]}" width="60" height="60">`
                            : `<span>Không có ảnh</span>`;


                        const formattedPrice = Number(item.price || 0).toLocaleString("vi-VN") + " VNĐ";


                        const row = `
                       <tr>
                           <td>${item.id}</td>
                           <td>${imageUrl}</td>
                           <td>${item.variantCode || ""}</td>
                           <td>${item.productName || ""}</td>
                           <td>${item.brandName || ""}</td>
                           <td>${item.categoryName || ""}</td>
                           <td>${item.colorName || ""}</td>
                           <td>${item.sizeName || ""}</td>
                           <td>${formattedPrice}</td>
                           <td>${item.quantity || 0}</td>
                       </tr>
                   `;
                        tbody.append(row);
                    });
                } else {
                    tbody.append(`<tr><td colspan="10" class="text-center">Không có biến thể nào</td></tr>`);
                }


                // 🧩 Thêm phân trang (phía ngoài bảng)
                renderVariantPagination(productId, response.currentPage, response.totalPages, response.pageSize);
            },
            error: function () {
                toastr.error("Không thể lấy danh sách biến thể sản phẩm!");
            },
        });
    }




    // hàm phân trangg
    function renderVariantPagination(productId, currentPage, totalPages, size) {
        const paginationContainer = $("#variantPagination");
        paginationContainer.empty();


        if (totalPages <= 1) return;


        let html = `
       <ul class="pagination" data-total-pages="${totalPages}" data-current-page="${currentPage}">
           <!-- Nút Previous -->
           <li class="paginate_button previous ${currentPage === 0 ? 'disabled' : ''}">
               <a href="#" onclick="getProductVariants(${productId}, ${currentPage - 1}, ${size}); return false;">Sau</a>
           </li>
   `;


        // Nút số trang
        for (let i = 0; i < totalPages; i++) {
            html += `
           <li class="paginate_button ${i === currentPage ? 'active' : ''}">
               <a href="#" onclick="getProductVariants(${productId}, ${i}, ${size}); return false;">${i + 1}</a>
           </li>
       `;
        }


        // Nút Next
        if (currentPage + 1 < totalPages) {
            html += `
           <li class="paginate_button next">
               <a href="#" onclick="getProductVariants(${productId}, ${currentPage + 1}, ${size}); return false;">Tiếp</a>
           </li>
       `;
        } else {
            html += `
           <li class="paginate_button next disabled">
               <a href="#" tabindex="0">Tiếp</a>
           </li>
       `;
        }


        html += `</ul>`;
        paginationContainer.append(html);
    }




    // nút thêm nhanh màu sắc
    $(document).on("click", "#fastAddColor", function () {
        $("#fastAddColorModal").modal({ backdrop: "static", keyboard: false });
    });
    // nút thêm nhanh kích thước
    $(document).on("click", "#fastAddSize", function () {
        $("#fastAddSizeModal").modal({ backdrop: "static", keyboard: false });
    });


    // btnAddColor và btnAddSize (giả sử có trong fast add modals)
    $("#btnAddColor").click(function () {
        // Logic thêm color (từ code trước)
        SwalUtils.confirm(
            "Xác nhận thêm màu sắc?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();
                formData.append("colorCode", $("#fastAddColorModal #code").val() || null);
                formData.append("name", $("#fastAddColorModal #name").val().trim());
                formData.append("isActive", true);


                $.ajax({
                    url: "/admin/color/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (response) {
                        toastr.success("Thêm màu sắc thành công!");
                        $("#mauSac").val(null).trigger("change.select2");
                        $("#fastAddColorModal").modal("hide");
                    },
                    error: function (xhr) {
                        toastr.error(xhr.responseJSON?.message || "Thêm màu sắc thất bại");
                    },
                });
            }
        });
    });


    $("#btnAddSize").click(function () {
        SwalUtils.confirm(
            "Xác nhận thêm kích thước?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();
                formData.append("sizeCode", $("#fastAddSizeModal #code").val() || null);
                formData.append("name", $("#fastAddSizeModal #name").val().trim());
                formData.append("isActive", true);


                $.ajax({
                    url: "/admin/size/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    success: function (response) {
                        toastr.success("Thêm kích thước thành công!");
                        $("#kichCo").val(null).trigger("change.select2");
                        $("#fastAddSizeModal").modal("hide");
                    },
                    error: function (xhr) {
                        toastr.error(xhr.responseJSON?.message || "Thêm kích thước thất bại");
                    },
                });
            }
        });
    });



});



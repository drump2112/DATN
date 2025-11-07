// === QUẢN LÝ SẢN PHẨM ===
(function() {
    'use strict';

    Dropzone.autoDiscover = false;
    var avatarDropzone = null;
    var avatarDropzoneVariants = null;
    var currentProductPage = 0;

    // === FUNCTION ĐỢI jQuery VÀ KHỞI TẠO ===
    function initializeProductModule() {
        if (typeof jQuery === 'undefined' || typeof $ === 'undefined') {
            return;
        }

        if (typeof SwalUtils === 'undefined') {
            setTimeout(initializeProductModule, 500);
            return;
        }

        setupEventListeners();
        initializeComponents();
    }

    // === SETUP EVENT LISTENERS ===
    function setupEventListeners() {
        // Event delegation for modal buttons
        $(document).off('click.productModule').on("click.productModule", "#btnAddProduct", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddProduct();
        });

        $(document).off('click.productModule').on("click.productModule", "#btnUpdateProduct", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateProduct();
        });

        // Modal-specific event delegation
        $(document).off('click.productModalModule').on("click.productModalModule", "#myModal #btnAddProduct", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddProduct();
        });

        $(document).off('click.productModalModule').on("click.productModalModule", "#myModal #btnUpdateProduct", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateProduct();
        });

        // Force binding when modal shows
        $('#myModal').off('shown.bs.modal.productModule').on('shown.bs.modal.productModule', function () {
            setTimeout(function() {
                bindModalEvents();
            }, 100);
        });

        // Other existing events
        setupOtherEvents();
    }

    // === FORCE BIND MODAL EVENTS ===
    function bindModalEvents() {
        if ($("#btnAddProduct").length > 0) {
            $("#btnAddProduct").off('click.product').on('click.product', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleAddProduct();
            });
        }

        if ($("#btnUpdateProduct").length > 0) {
            $("#btnUpdateProduct").off('click.product').on('click.product', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleUpdateProduct();
            });
        }
    }

    // === SETUP OTHER EVENTS ===
    function setupOtherEvents() {
        $(document).on("change.productFilter", "#statusFilter", function() {
            searchProduct(0); // Reset to page 0 when filter changes
        });

        $(document).on("keypress.productSearch", "#searchInput", function(e) {
            if (e.which === 13) { // Enter key
                searchProduct(0);
            }
        });

        // Reset filter button
        $(document).on("click.productReset", "#resetFilterBtn", function() {
            $("#searchInput").val('');
            $("#statusFilter").val('');
            currentProductPage = 0;
            searchProduct(0);
        });

        // Initialize current page
        currentProductPage = 0;

        // Fast add modal events
        $(document).on("click.productFastAdd", "#fastAddColor", function () {
            clearFastAddColorForm();
            $("#fastAddColorModal").modal({
                backdrop: "static",
                keyboard: false,
            });
        });

        $(document).on("click.productFastAdd", "#fastAddSize", function () {
            clearFastAddSizeForm();
            $("#fastAddSizeModal").modal({
                backdrop: "static",
                keyboard: false,
            });
        });

        // Fast add button events
        $(document).on("click.productFastAddColor", "#btnAddColor", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleFastAddColor();
        });

        $(document).on("click.productFastAddSize", "#btnAddSize", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleFastAddSize();
        });

        $(document).on("hidden.bs.modal", function () {
            const anyOpenModal = $(".modal.show").length > 0;
            if (anyOpenModal) {
                $("body").addClass("modal-open");
            } else {
                $("body").removeClass("modal-open");
                $(".modal-backdrop").remove();
            }
        });
    }

    // === CLEAR FAST ADD FORMS ===
    function clearFastAddColorForm() {
        $("#fastAddColorModal #id").val("");
        $("#fastAddColorModal #code").val("");
        $("#fastAddColorModal #name").val("");
    }

    function clearFastAddSizeForm() {
        $("#fastAddSizeModal #id").val("");
        $("#fastAddSizeModal #code").val("");
        $("#fastAddSizeModal #name").val("");
    }

    // === INITIALIZE COMPONENTS ===
    function initializeComponents() {
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

        initSelect2s();
        initDropzones();
        initTouchSpin();
        initSteps();
        setupModalEvents();
        setupDescriptionValidation();
    }

    // === HANDLE ADD PRODUCT ===
    function handleAddProduct() {
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
                        SwalUtils.success("Thành công!", response.message);
                        $("#myModal").modal("hide");
                        $.get("/admin/product/count").done(function (totalItems) {
                            const pageSize = 5;
                            const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
                            currentProductPage = lastPage;
                            searchProduct(lastPage);
                        });
                    },
                    error: function (xhr) {
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Thêm thất bại");
                    },
                });
            }
        });
    }

    // === HANDLE UPDATE PRODUCT ===
    function handleUpdateProduct() {
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
                        SwalUtils.success("Cập nhật thành công!", response.message);
                        $("#myModal").modal("hide");
                        // Stay on current page after update
                        const savedPage = $("#productForm").data("current-page") || currentProductPage;
                        searchProduct(savedPage);
                    },
                    error: function (xhr) {
                        let errorMessage = "Cập nhật thất bại";
                        if (xhr.responseJSON && xhr.responseJSON.message) {
                            errorMessage = xhr.responseJSON.message;
                        }
                        SwalUtils.error("Lỗi!", errorMessage);
                    },
                });
            }
        });
    }

    // === HANDLE FAST ADD COLOR ===
    function handleFastAddColor() {
        const name = $("#fastAddColorModal #name").val().trim();
        if (!name) {
            SwalUtils.error("Lỗi!", "Vui lòng nhập tên màu sắc!");
            return;
        }

        SwalUtils.confirm(
            "Xác nhận thêm màu sắc?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();
                formData.append("id", $("#fastAddColorModal #id").val().trim());
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
                        SwalUtils.success("Thành công!", "Thêm màu sắc thành công!");

                        $("#mauSac")
                            .select2({
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
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Thêm màu sắc thất bại");
                    },
                });
            }
        });
    }

    // === HANDLE FAST ADD SIZE ===
    function handleFastAddSize() {
        const name = $("#fastAddSizeModal #name").val().trim();
        if (!name) {
            SwalUtils.error("Lỗi!", "Vui lòng nhập tên kích thước!");
            return;
        }

        if (!/^\d+$/.test(name)) {
            SwalUtils.error("Lỗi!", "Kích thước chỉ được nhập số!");
            return;
        }

        SwalUtils.confirm(
            "Xác nhận thêm kích thước?",
            "",
            "Thêm",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData();

                formData.append("id", $("#fastAddSizeModal #id").val().trim());
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
                        SwalUtils.success("Thành công!", "Thêm kích thước thành công!");

                        $("#kichCo")
                            .select2({
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
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Thêm kích thước thất bại");
                    },
                });
            }
        });
    }

    // === INITIALIZE SELECT2s ===
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

    // === INITIALIZE DROPZONES ===
    function initDropzones() {
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

        // Dropzone for ProductVariants modal
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
    }

    // === SETUP MODAL EVENTS ===
    function setupModalEvents() {
        // Hidden event cho #myModal
        $("#myModal").on("hidden.bs.modal", function () {
            if (avatarDropzone) {
                avatarDropzone.removeAllFiles(true);
            }
            $("#myModal #avatarInput").val("");
            $("#danhMuc, #thuongHieu").val(null).trigger("change");
        });

        // ProductVariants modal events
        $("#myModalProductVariants").on("shown.bs.modal", function () {
            initSelect2sVariants();
            const validator = $("#ProductVariantsForm").validate();
            if (validator) validator.resetForm();

            $("#price").val("").trigger("touchspin.updatesettings", { initval: "" });

            // Reset wizard về bước 1
            $(".steps ul li:first a").click();
            $(".steps ul li").removeClass("error done current");
            $(".steps ul li:first").addClass("current");

            // Reset nội dung động ở bước 2
            $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();

            if (avatarDropzoneVariants) avatarDropzoneVariants.removeAllFiles(true);
        });

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
    }

    // === SETUP DESCRIPTION VALIDATION ===
    function setupDescriptionValidation() {
        $("#description").on("input", function () {
            $(this).valid();
        });
    }

    // === TOUCHSPIN INITIALIZATION ===
    function initTouchSpin() {
        $("#price").TouchSpin({
            min: 0,
            max: 999999999,
            step: 1000,
            forcestepdivisibility: "none",
            buttondown_class: "btn btn-white",
            buttonup_class: "btn btn-white",
        });
    }

    // === INITIALIZE STEPS ===
    function initSteps() {
        // Steps sẽ được khởi tạo riêng cho ProductVariants modal
        if ($("#ProductVariantsForm").length > 0) {
            initProductVariantsSteps();
        }
    }

    // === INITIALIZE PRODUCT VARIANTS STEPS ===
    function initProductVariantsSteps() {
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
                    }

                    if (priorIndex !== 2 && currentIndex === 2) {
                        if (avatarDropzoneVariants) {
                            avatarDropzoneVariants.destroy();
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
                    }

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
                            $("#ProductVariantsForm")[0].reset();
                            $("#tensanPham, #mauSac, #kichCo, #status").val(null).trigger("change.select2");
                            $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();
                            const savedPage = $("#ProductVariantsForm").data("current-page") || currentProductPage;
                            searchProduct(savedPage);
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

    // === OPEN ADD MODAL ===
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

    // === OPEN EDIT MODAL ===
    function openEditModal(product) {
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
            avatarDropzone.createThumbnailFromUrl(mockFile, thumbnail, () => console.log("Thumbnail loaded"), () => SwalUtils.error("Lỗi", "Không thể tải ảnh thumbnail!"));
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

    // === SEARCH PRODUCT ===
    function searchProduct(page) {
        // Update current page
        currentProductPage = page;

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
                SwalUtils.error("Lỗi", "Không thể tải danh sách sản phẩm!");
            },
        });
    }


    // === SELECT2 FOR VARIANTS ===
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

    // === COLLECT FORM DATA VARIANTS ===
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
                    SwalUtils.success("Thành công!", res.message);
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
        $("#ProductVariantsForm").data("current-page", currentProductPage); // Lưu trang hiện tại để reload sau


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
                SwalUtils.error("Lỗi", "Không thể lấy thông tin sản phẩm để thêm chi tiết!");
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
                SwalUtils.error("Lỗi", "Không thể lấy danh sách biến thể sản phẩm!");
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




    // === LEGACY FUNCTIONS THAT STILL NEED TO BE DEFINED ===
    // These functions are still referenced in the HTML templates

    function handleVariantDetailClick(button) {
        // Implementation moved from legacy code
        const id = $(button).data("id");
        $("#productIdsp").val(id);
        $("#myModalProductVariants").modal("show");
        $("#ProductVariantsForm").data("current-page", currentProductPage);

        $.ajax({
            url: `/admin/product/${id}`,
            method: "GET",
            success: function (product) {
                $("#modalTitleProductVariants").text("Thêm Sản Phẩm Chi Tiết");
                $("#ProductVariantsForm")[0].reset();
                $("#tensanPham, #mauSac, #kichCo").val(null).trigger("change.select2");
                $("#price").val("").trigger("touchspin.updatesettings", { initval: "" });
                $("#status").val("1");
                $(".steps ul li:first a").click();
                $(".steps ul li").removeClass("error done current");
                $(".steps ul li:first").addClass("current");
                $("#ProductVariantsForm fieldset").eq(1).find(".row").empty();

                $("#id").val(product.id);
                $("#codeProduct").val(product.productCode || "");
                $("#nameProduct").val(product.name || "");
                getProductVariants(id, currentProductPage, 5);
                $("#myModalProductVariants").modal("show");
            },
            error: function () {
                SwalUtils.error("Lỗi", "Không thể lấy thông tin sản phẩm để thêm chi tiết!");
            },
        });
    }

    function getProductVariants(productId, page = 0, size = 5) {
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

                renderVariantPagination(productId, response.currentPage, response.totalPages, response.pageSize);
            },
            error: function () {
                SwalUtils.error("Lỗi", "Không thể lấy danh sách biến thể sản phẩm!");
            },
        });
    }

    function renderVariantPagination(productId, currentPage, totalPages, size) {
        const paginationContainer = $("#variantPagination");
        paginationContainer.empty();

        if (totalPages <= 1) return;

        let html = `
       <ul class="pagination" data-total-pages="${totalPages}" data-current-page="${currentPage}">
           <li class="paginate_button previous ${currentPage === 0 ? 'disabled' : ''}">
               <a href="#" onclick="getProductVariants(${productId}, ${currentPage - 1}, ${size}); return false;">Sau</a>
           </li>
   `;

        for (let i = 0; i < totalPages; i++) {
            html += `
           <li class="paginate_button ${i === currentPage ? 'active' : ''}">
               <a href="#" onclick="getProductVariants(${productId}, ${i}, ${size}); return false;">${i + 1}</a>
           </li>
       `;
        }

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

    // === WINDOW FUNCTIONS ===
    window.handleDetailClick = function (button) {
        const id = $(button).data("id");
        // Store current page for later use
        $("#productForm").data("current-page", currentProductPage);

        $.ajax({
            url: `/admin/product/${id}`,
            method: "GET",
            success: function (product) {
                openEditModal(product);
            },
            error: function () {
                SwalUtils.error("Lỗi", "Không thể lấy thông tin sản phẩm!");
            },
        });
    };

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
                        SwalUtils.success("Thành công", data.message);
                        // Stay on current page after toggle status
                        searchProduct(currentProductPage);
                    },
                    error: function (xhr) {
                        SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Có lỗi xảy ra");
                    },
                });
            }
        });
    };

    window.openAddModal = openAddModal;
    window.searchProduct = searchProduct;
    window.handleVariantDetailClick = handleVariantDetailClick;
    window.getProductVariants = getProductVariants;



    // === WAIT FOR DEPENDENCIES AND INITIALIZE ===
    if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
        $(document).ready(function() {
            initializeProductModule();
        });
    } else {
        function waitForJQuery() {
            if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
                $(document).ready(function() {
                    initializeProductModule();
                });
            } else {
                setTimeout(waitForJQuery, 100);
            }
        }
        waitForJQuery();
    }

})(); // Đóng IIFE


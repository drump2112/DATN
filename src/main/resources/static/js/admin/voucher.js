$(document).ready(function () {

    window.handleVoucherDetailClick = function (button) {
        const voucher = {
            id: $(button).data("id"),
            code: $(button).data("code"),
            name: $(button).data("name"),
            discountType: $(button).data("discounttype"),
            discountValue: $(button).data("discountvalue"),
            minOrderAmount: $(button).data("minorderamount"),
            maxDiscountValue: $(button).data("maxdiscountvalue"),
            startDate: $(button).data("startdate"),
            endDate: $(button).data("enddate"),
            isActive: $(button).data("isActive"),
            quantity: $(button).data("quantity"),
        };

        console.log("Voucher data lấy tu day:", voucher);

        const currentPage = parseInt($("#voucherPaginationContainer .paginate_button.active a").text()) - 1 || 0;
        $("#voucherForm").data("current-page", currentPage);
        openVoucherEditModal(voucher, true);
    };

    function openVoucherEditModal(data, isEditable) {
        clearVoucherForm();

        $("#voucherId").val(data.id || "");
        $("#voucherCode").val(data.code || "").prop("readonly", true);
        $("#voucherName").val(data.name || "").prop("readonly", !isEditable);
        $("#voucherDiscountType").val(data.discountType || "PERCENT");
        $("#voucherDiscountValue").val(Math.round(data.discountValue || ""));
        $("#voucherMinOrderAmount").val(Math.round(data.minOrderAmount || ""));

        $("#voucherQuantity").val(data.quantity || "");

        $("#voucherDiscountType").prop("disabled", true);

        const discountType = $("#voucherDiscountType").val();
        const $maxDiscountInput = $("#voucherMaxDiscountValue");

        if (discountType === "FIXED") {
            console.log("Fixed");
            $maxDiscountInput.val("").prop("disabled", true);
        } else {
            $maxDiscountInput
                .val(Math.round(data.maxDiscountValue || ""))
                .prop("disabled", !isEditable);
        }

        console.log("Voucher data:", data);


        $("#voucherStartDate").val(data.startDate);
        $("#voucherEndDate").val(data.endDate);

        let activeVal = "1";
        if (data.isActive === false || data.isActive === "false" || data.isActive === "0") {
            activeVal = "0";
        }
        $("#voucherIsActive").val(activeVal);

        // Debug: Log data để check console (remove sau khi test)
        console.log("Voucher data loaded:", data);
        // console.log("Formatted dates:", { start: formattedStartDate, end: formattedEndDate });
        console.log("isActive val:", activeVal);

        // Update title và buttons
        $("#voucherModalTitle").text(isEditable ? "Cập Nhật Voucher" : "Chi Tiết Voucher");
        $("#btnSaveVoucher").hide();
        $("#btnUpdateVoucher").toggle(isEditable).show();
        $("#modalVoucher").modal("show");

        // Trigger change event để apply readonly logic ngay khi load
        $("#DiscountType").trigger("change");
    }

    function clearVoucherForm() {
        $("#voucherForm input:not(#voucherCode), #voucherForm select").val("");
        $("#voucherForm").removeData("current-page");
        $(".form-group").removeClass("has-error");

        $("#voucherMaxDiscountValue").prop("readonly", false);
        clearValidationErrors();
    }

    $("#btnOpenAddVoucher").on("click", function () {
        clearVoucherForm();
        $("#voucherCode").val("").prop("readonly", false);
        $("#voucherStartDate").val();
        $("#voucherDiscountType").val("PERCENT");
        $("#voucherIsActive").val("1");

        $("#voucherModalTitle").text("Thêm Voucher Mới");
        $("#voucherForm input, #voucherForm select").prop("readonly", false).prop("disabled", false);
        $("#btnSaveVoucher").show();
        $("#btnUpdateVoucher").hide();
        $("#modalVoucher").modal("show");

        $("#voucherDiscountType").trigger("change");
    });


    $(document).on("change", "#voucherDiscountType", function () {
        const discountType = $(this).val();
        const $maxDiscountInput = $("#voucherMaxDiscountValue");

        if (discountType === "FIXED") {
            $maxDiscountInput.prop("readonly", true).val("");  // Clear khi FIXED
            $maxDiscountInput.closest(".form-group").removeClass("has-error");

        } else if (discountType === "PERCENT") {
            $maxDiscountInput.prop("readonly", false).focus();
        }
    });

    function validateVoucher() {
        clearValidationErrors();

        const code = $("#voucherCode").val().trim();
        const name = $("#voucherName").val().trim();
        const discountType = $("#voucherDiscountType").val();
        const discountValueStr = $("#voucherDiscountValue").val().trim();
        const discountValue = parseFloat(discountValueStr);
        const maxDiscountValueStr = $("#voucherMaxDiscountValue").val().trim();
        const maxDiscountValue = parseFloat(maxDiscountValueStr) || null;
        const minOrderAmountStr = $("#voucherMinOrderAmount").val().trim();
        const minOrderAmount = parseFloat(minOrderAmountStr) || null;
        const quantityStr = $("#voucherQuantity").val().trim();
        const quantity = parseInt(quantityStr) || 0;
        const start = $("#voucherStartDate").val();
        const end = $("#voucherEndDate").val();
        const isActive = $("#voucherIsActive").val();

        let isValid = true;

        if (code) {
            if (code.length < 5) {
                showFieldError("#voucherCode", "Mã voucher phải có ít nhất 5 ký tự!");
                isValid = false;
            }
        }

        if (!name) {
            showFieldError("#voucherName", "Tên voucher không được để trống!");
            isValid = false;
        }

        if (!discountType) {
            showFieldError("#voucherDiscountType", "Vui lòng chọn loại giảm giá!");
            isValid = false;
        }

        if (!discountValueStr || isNaN(discountValue) || discountValue <= 0) {
            showFieldError("#voucherDiscountValue", "Giá trị giảm phải là số dương!");
            isValid = false;
        }

        if (!minOrderAmountStr || isNaN(minOrderAmount) || minOrderAmount <= 0) {
            showFieldError("#voucherMinOrderAmount", "Giá trị đơn hàng tối thiểu phải là số dương!");
            isValid = false;
        } else if (minOrderAmount > 9999999) {
            showFieldError("#voucherMinOrderAmount", "Giá trị đơn hàng tối thiểu không được lớn hơn 10 triệu!");
            isValid = false;
        }

        if (discountType === "PERCENT") {
            if (discountValue > 100) {
                showFieldError("#voucherDiscountValue", "Giá trị giảm không được lớn hơn 100%!");
                isValid = false;
            } else if (discountValue < 1) {
                showFieldError("#voucherDiscountValue", "Giá trị giảm không được nhỏ hơn 1%!");
                isValid = false;
            }
            if (!maxDiscountValueStr || isNaN(maxDiscountValue) || maxDiscountValue <= 0) {
                showFieldError("#voucherMaxDiscountValue", "Giá trị giảm tối đa phải là số dương!");
                isValid = false;
            } else if (maxDiscountValue > 9999999) {
                showFieldError("#voucherMaxDiscountValue", "Giá trị giảm tối đa không được lớn hơn 10 triệu!");
                isValid = false;
            }
        } else if (discountType === "FIXED") {
            if (discountValue > 9999999) {
                showFieldError("#voucherDiscountValue", "Giá trị giảm không được lớn hơn 10 triệu!");
                isValid = false;
            }
        }

        if (!validateVoucherTime(start, end)) {
            isValid = false;
        }

        if (quantity <= 0) {
            showFieldError("#voucherQuantity", "Số lượng voucher phải lớn hơn 0!");
            isValid = false;
        }

        if (!isActive) {
            showFieldError("#voucherIsActive", "Vui lòng chọn trạng thái voucher!");
            isValid = false;
        }

        return isValid;
    }

    function validateVoucherTime(start, end) {
        const currentDate = new Date();
        currentDate.setHours(0, 0, 0, 0);

        const startDate = start ? new Date(start) : null;
        const endDate = end ? new Date(end) : null;

        if (startDate) startDate.setHours(0, 0, 0, 0);
        if (endDate) endDate.setHours(0, 0, 0, 0);

        let isValid = true;

        if (!start) {
            showFieldError("#voucherStartDate", "Vui lòng chọn ngày bắt đầu!");
            isValid = false;
        } else if (startDate < currentDate) {
            showFieldError("#voucherStartDate", "Ngày bắt đầu phải lớn hơn hoặc bằng ngày hiện tại!");
            isValid = false;
        }
        if (!end) {
            showFieldError("#voucherEndDate", "Vui lòng chọn ngày kết thúc!");
            isValid = false;
        } else if (startDate && endDate < startDate) {
            showFieldError("#voucherEndDate", "Ngày kết thúc phải lớn hơn ngày bắt đầu!");
            isValid = false;
        } else if (endDate && endDate <= currentDate) {
            showFieldError("#voucherEndDate", "Ngày kết thúc phải lớn hơn ngày hiện tại!");
            isValid = false;
        }

        return isValid;
    }

    function showFieldError(fieldSelector, errorMsg) {
        const $field = $(fieldSelector);
        const $formGroup = $field.closest(".form-group");

        $formGroup.addClass("has-error");

        let $errorSpan = $formGroup.find(".help-block.error-message");
        if ($errorSpan.length === 0) {
            $errorSpan = $('<span class="help-block error-message" style="color: red; font-size: 12px;"></span>');
            $field.after($errorSpan);
        }
        $errorSpan.text(errorMsg).show();

        $field.focus();
    }

    function clearValidationErrors() {
        $(".form-group").removeClass("has-error");
        $(".help-block.error-message").remove();
    }


    $(document).on("click", "#btnSaveVoucher", function () {
        if (!validateVoucher()) return;

        Swal.fire({
            title: "Xác nhận thêm voucher?",
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Thêm",
            cancelButtonText: "Hủy",
        }).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData($("#voucherForm")[0]);
                formData.append("isActive", $("#voucherIsActive").val());

                $.ajax({
                    url: "/admin/voucher/add",
                    method: "POST",
                    processData: false,
                    contentType: false,
                    data: formData,
                    beforeSend: function () {
                        $("#btnSaveVoucher").prop("disabled", true).text("Đang thêm...");
                    },
                    success: function (response) {
                        Swal.fire("Thành công!", response.message, "success");
                        $("#modalVoucher").modal("hide");
                        refreshVoucherTable();
                        searchVoucher($("#paginationContainer").data("current-page") || 0);
                    },
                    error: function (xhr) {
                        Swal.fire("Lỗi!", xhr.responseJSON?.message || "Cập nhật thất bại!", "error");
                    },
                    complete: function () {
                        $("#btnSaveVoucher").prop("disabled", false).text("Thêm");
                    }
                });
            }
        });
    });


    // nuts cập nhật voucher
    $(document).on("click", "#btnUpdateVoucher", function () {
        if (!validateVoucher()) return;
        Swal.fire({
            title: "Xác nhận cập nhật voucher?",
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Cập nhật",
            cancelButtonText: "Hủy",
        }).then((result) => {
            if (result.isConfirmed) {
                const formData = new FormData($("#voucherForm")[0]);
                const voucherId = $("#voucherId").val();
                formData.append("isActive", $("#voucherIsActive").val());
                formData.append("discountType", $("#voucherDiscountType").val())
                $.ajax({
                    url: `/admin/voucher/${voucherId}`,
                    type: "PUT",
                    processData: false,
                    contentType: false,
                    data: formData,
                    beforeSend: function () {
                        $("#btnUpdateVoucher").prop("disabled", true).text("Đang cập nhật...");
                    },
                    success: function (response) {
                        Swal.fire("Thành công!", response.message, "success");
                        $("#modalVoucher").modal("hide");
                        searchVoucher($("#paginationContainer").data("current-page") || 0);
                    },
                    error: function (xhr) {
                        Swal.fire("Lỗi!", xhr.responseJSON?.message || "Cập nhật thất bại!", "error");
                    },
                    complete: function () {
                        $("#btnUpdateVoucher").prop("disabled", false).text("Cập Nhật");
                    }
                });
            }
        });
    });

    // Tìm kiếm voucher
    function searchVoucher(page = 0) {
        const keyword = $("#voucherSearchInput").val().trim();
        const isActive = $("#voucherStatusFilter").val() || null;

        $("#paginationContainer").attr("data-current-page", page);

        $.ajax({
            url: "/admin/voucher/search",
            type: "GET",
            data: { page, keyword, isActive },
            beforeSend: function () {
                $("#voucherTableContainer").html('<div class="text-center"><i class="fa fa-spinner fa-spin"></i> Đang tải...</div>');
            },
            success: function (response) {
                $("#voucherTableContainer").html(response);
            },
            error: function () {
                Swal.fire("Lỗi!", "Không tải được dữ liệu voucher!", "error");
                searchVoucher($("#paginationContainer").data("current-page") || 0);
            }
        });
    }

    // update trạng thái voucher
    window.toggleVoucherStatus = function (voucherId, isActive) {
        const title = isActive ? "Vô hiệu hóa voucher này?" : "Kích hoạt voucher này?";

        Swal.fire({
            title: title,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Xác nhận",
            cancelButtonText: "Hủy",
        }).then((result) => {
            if (result.isConfirmed) {
                $.ajax({
                    url: `/admin/voucher/${voucherId}/toggle-status`,
                    type: "PUT",
                    beforeSend: function () {
                        $(`button[data-id="${voucherId}"]`).prop("disabled", true);
                    },
                    success: function (data) {
                        Swal.fire("Thành công!", data.message, "success");
                        searchVoucher($("#paginationContainer").data("current-page") || 0);
                    },
                    error: function (xhr) {
                        Swal.fire("Lỗi!", xhr.responseJSON?.message || "Có lỗi xảy ra!", "error");
                    },
                    complete: function () {
                        $(`button[data-id="${voucherId}"]`).prop("disabled", false);
                    }
                });
            }
        });
    };

    // làm mơis table voucher sau add
    function refreshVoucherTable() {
        $.get("/admin/voucher/counts").done(function (totalItems) {
            const pageSize = 5;
            const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
            searchVoucher(lastPage);
        });
    }

    // reset filter
    $("#resetFilterVoucherBtn").on("click", function () {
        $("#voucherSearchInput").val("");
        $("#voucherStatusFilter").val(null).trigger("change");
        $("#voucherSearchInput").val(null).trigger("change");

        $.ajax({
            url: "/admin/voucher/search",
            type: "GET",
            data: {
                page: 0,
                size: 5,
            },
            success: function (response) {
            },
            error: function () {
                toastr.error("Không thể tải lại bảng");
            },
        });
    });

    $("#voucherSearchInput, #voucherStatusFilter").on("change keyup", function (e) {
        if (e.key === "Enter" || $(this).is("select")) {
            searchVoucher(0);
        }
    });

    window.searchVoucher =searchVoucher
});


// Xem chi tiết và mở modal edit voucher
window.handleVoucherDetailClick = function (button) {
    const voucher = {
        id: $(button).data("id"),
        code: $(button).data("code"),
        name: $(button).data("name"),
        discountType: $(button).data("discountType"),
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


// Mở modal edit/view chi tiết voucher
function openVoucherEditModal(data, isEditable) {
    clearVoucherForm();


    // Fill cơ bản (với fallback empty string)
    $("#voucherId").val(data.id || "");
    $("#voucherCode").val(data.code || "").prop("readonly", true);  // Code luôn readonly
    $("#voucherName").val(data.name || "").prop("readonly", !isEditable);
    $("#voucherDiscountType").val(data.discountType || "PERCENT");
    $("#voucherDiscountValue").val(data.discountValue || "");
    $("#voucherMinOrderAmount").val(data.minOrderAmount || "");
    $("#voucherQuantity").val(data.quantity || "");


    // Fill maxDiscountValue và set readonly dựa trên discountType (swap: PERCENT enable, FIXED readonly)
    const discountType = $("#voucherDiscountType").val();
    const $maxDiscountInput = $("#voucherMaxDiscountValue");
    if (discountType === "FIXED") {
        $maxDiscountInput.val("").prop("readonly", true);  // Clear và readonly nếu FIXED
    } else {
        $maxDiscountInput.val(data.maxDiscountValue || "").prop("readonly", !isEditable);
    }


    console.log("Voucher data:", data);


    // Fix date format: Cắt từ "YYYY-MM-DDTHH:mm:ss" → "YYYY-MM-DD" cho input date
    let formattedStartDate = "";
    let formattedEndDate = "";
    if (data.startDate) {
        formattedStartDate = data.startDate.toString().split('T')[0];  // "2025-10-14T00:00" → "2025-10-14"
    }
    if (data.endDate) {
        formattedEndDate = data.endDate.toString().split('T')[0];
    }
    $("#voucherStartDate").val(formattedStartDate);
    $("#voucherEndDate").val(formattedEndDate);


    // Fix isActive: Convert boolean/string → "1"/"0"
    let activeVal = "1";  // Default active
    if (data.isActive === false || data.isActive === "false" || data.isActive === "0") {
        activeVal = "0";
    }
    $("#voucherIsActive").val(activeVal);


    // Debug: Log data để check console (remove sau khi test)
    console.log("Voucher data loaded:", data);
    console.log("Formatted dates:", { start: formattedStartDate, end: formattedEndDate });
    console.log("isActive val:", activeVal);


    // Update title và buttons
    $("#voucherModalTitle").text(isEditable ? "Cập Nhật Voucher" : "Chi Tiết Voucher");
    $("#btnSaveVoucher").hide();
    $("#btnUpdateVoucher").toggle(isEditable).show();
    $("#modalVoucher").modal("show");


    // Trigger change event để apply readonly logic ngay khi load
    $("#voucherDiscountType").trigger("change");
}


// Clear form voucher
function clearVoucherForm() {
    $("#voucherForm input:not(#voucherCode), #voucherForm select").val("");
    $("#voucherForm").removeData("current-page");
    $(".form-group").removeClass("has-error");
    // Reset readonly cho maxDiscountValue (sẽ được handle bởi event change sau)
    $("#voucherMaxDiscountValue").prop("readonly", false);
    clearValidationErrors();
}


// Mở modal thêm mới voucher
$("#btnOpenAddVoucher").on("click", function () {
    clearVoucherForm();
    $("#voucherCode").val("").prop("readonly", false);  // Cho phép nhập code thủ công
    $("#voucherModalTitle").text("Thêm Voucher Mới");
    $("#voucherForm input, #voucherForm select").prop("readonly", false).prop("disabled", false);
    $("#btnSaveVoucher").show();
    $("#btnUpdateVoucher").hide();
    $("#modalVoucher").modal("show");


    // Trigger change để apply readonly cho PERCENT/FIXED default
    $("#voucherDiscountType").trigger("change");
});


// Event listener cho select loại giảm giá: Swap logic readonly (ngược lại: FIXED -> readonly max, PERCENT -> enable max)
$(document).on("change", "#voucherDiscountType", function () {
    const discountType = $(this).val();
    const $maxDiscountInput = $("#voucherMaxDiscountValue");


    if (discountType === "FIXED") {
        $maxDiscountInput.prop("readonly", true).val("");  // Readonly và clear value khi FIXED (optional sau swap)
        $maxDiscountInput.closest(".form-group").removeClass("has-error");  // Clear lỗi nếu có
    } else if (discountType === "PERCENT") {
        $maxDiscountInput.prop("readonly", false).focus();  // Enable và focus để nhập khi PERCENT (bắt buộc sau swap)
    }
});


// Validation voucher (swap logic giữa PERCENT và FIXED: đảo ngược check max vs check %)
function validateVoucher() {
    // Clear tất cả lỗi trước khi validate mới
    clearValidationErrors();


    // Trim tất cả input text để loại bỏ khoảng trắng đầu cuối
    $("#voucherCode").val($("#voucherCode").val().trim());
    $("#voucherName").val($("#voucherName").val().trim());
    $("#voucherDiscountValue").val($("#voucherDiscountValue").val().trim());
    $("#voucherMaxDiscountValue").val($("#voucherMaxDiscountValue").val().trim());
    $("#voucherMinOrderAmount").val($("#voucherMinOrderAmount").val().trim());
    $("#voucherQuantity").val($("#voucherQuantity").val().trim());


    const code = $("#voucherCode").val().trim();  // Optional: null OK, nhưng nếu có thì >=5 ký tự
    const name = $("#voucherName").val().trim();
    const discountType = $("#voucherDiscountType").val();  // Bắt buộc
    const discountValueStr = $("#voucherDiscountValue").val().trim();
    const discountValue = parseFloat(discountValueStr);
    const maxDiscountValueStr = $("#voucherMaxDiscountValue").val().trim();
    const maxDiscountValue = parseFloat(maxDiscountValueStr) || null;
    const minOrderAmountStr = $("#voucherMinOrderAmount").val().trim();
    const minOrderAmount = parseFloat(minOrderAmountStr) || null;
    const quantityStr = $("#voucherQuantity").val().trim();
    const quantity = parseInt(quantityStr) || 0;
    const startDate = $("#voucherStartDate").val();
    const endDate = $("#voucherEndDate").val();
    const isActive = $("#voucherIsActive").val();  // Bắt buộc


    // Check lần lượt theo thứ tự: mã -> tên -> loại -> giá trị giảm -> logic loại (đã swap) -> dates -> số lượng -> trạng thái


    // 1. Mã voucher: optional, nhưng nếu nhập thì >=5 ký tự
    if (code && code.length < 5) {
        showFieldError("#voucherCode", "Mã voucher phải có ít nhất 5 ký tự!");
        return false;
    }


    // 2. Tên không được trống
    if (!name) {
        showFieldError("#voucherName", "Tên voucher không được để trống!");
        return false;
    }


    // 3. Loại mã giảm giá bắt buộc
    if (!discountType) {
        showFieldError("#voucherDiscountType", "Vui lòng chọn loại giảm giá!");
        return false;
    }


    // 4. Giá trị giảm chung: phải >0
    if (!discountValueStr || !discountValue || discountValue <= 0) {
        showFieldError("#voucherDiscountValue", "Giá trị giảm phải là số dương!");
        return false;
    }


    // 5. Logic theo loại (đã swap: PERCENT -> check max bắt buộc & discount <= max; FIXED -> check discount <=100, max optional)
    if (discountType === "PERCENT") {
        if (discountValue > 100) {
            showFieldError("#voucherDiscountValue", "Giá trị giảm không được lớn hơn 100%!");
            return false;
        } else if (discountValue < 0) {
            showFieldError("#voucherDiscountValue", "Giá trị giảm không được nhỏ hơn 1%!");
            return false;
        } else if (!minOrderAmountStr || minOrderAmount === null || minOrderAmount <= 0) {
            showFieldError("#voucherMinOrderAmount", "Giá trị đơn hàng tối thiểu không được để trống và phải là số dương!");
            return false;
        }


        if (!maxDiscountValue) {
            showFieldError("#voucherMaxDiscountValue", "Giá trị giảm tối đa không được để trống!");
            return false;
        }


    } else if (discountType === "FIXED") {
        // Sau swap (nội dung cũ PERCENT): discount <=100 (điều chỉnh message cho FIXED), minOrder >0, max optional
        if (!discountValue) {
            showFieldError("#voucherDiscountValue", "Giá trị giảm cố định không được lớn để trống!");
            return false;
        }
        if (!minOrderAmountStr || minOrderAmount === null || minOrderAmount <= 0) {
            showFieldError("#voucherMinOrderAmount", "Giá trị đơn hàng tối thiểu không được để trống và phải là số dương!");
            return false;
        }
        // maxDiscountValue: không validate (optional)
    }


    // 6. Thời gian bắt đầu/kết thúc: bắt buộc, start > current, start <= end (chung cho cả hai loại)
    if (!startDate || !endDate) {
        showFieldError("#voucherStartDate", "Vui lòng chọn ngày bắt đầu và kết thúc!");
        return false;
    } else {
        const start = new Date(startDate);
        const end = new Date(endDate);
        const currentDate = new Date();  // Ngày hiện tại: October 18, 2025
        currentDate.setHours(0, 0, 0, 0);  // Reset giờ để so sánh ngày


        if (start <= currentDate) {  // Phải > current
            showFieldError("#voucherEndDate", "Thời gian bắt đầu phải lớn hơn thời gian hiện tại!");
            return false;
        } else if (start > end) {
            showFieldError("#voucherEndDate", "Thời gian bắt đầu không được lớn hơn thời gian kết thúc!");
            return false;
        } else if (end <= currentDate) {  // end > current để hợp lý
            showFieldError("#voucherEndDate", "Thời gian kết thúc phải sau thời gian hiện tại!");
            return false;
        }
    }


    // 7. Số lượng: bắt buộc >0 (giống PERCENT, chung)
    if (quantity <= 0) {
        showFieldError("#voucherQuantity", "Số lượng voucher phải lớn hơn 0!");
        return false;
    }


    // 8. Trạng thái không null (giống PERCENT, chung)
    if (!isActive) {
        showFieldError("#voucherIsActive", "Vui lòng chọn trạng thái voucher!");
        return false;
    }


    return true;
}


// Helper function: Hiển thị lỗi inline bên cạnh label/input (chữ đỏ)
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


// Helper function: Clear tất cả lỗi validation
function clearValidationErrors() {
    $(".form-group").removeClass("has-error");
    $(".help-block.error-message").remove();
}


// Event thêm/lưu voucher
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
                },
                error: function (xhr) {
                    Swal.fire("Lỗi!", xhr.responseJSON?.message || "Thêm thất bại!", "error");
                },
                complete: function () {
                    $("#btnSaveVoucher").prop("disabled", false).text("Thêm/Lưu");
                }
            });
        }
    });
});


// Event cập nhật voucher
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
                    searchVoucher($("#voucherForm").data("current-page") || 0);
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


// Lấy page hiện tại voucher
function getCurrentVoucherPage() {
    return parseInt($("#voucherPaginationContainer").attr("data-current-page")) ||
        parseInt($("#voucherPaginationContainer .paginate_button.active a").text()) - 1 || 0;
}


// Tìm kiếm voucher
function searchVoucher(page = 0) {
    const keyword = $("#voucherSearchInput").val().trim();
    const isActive = $("#voucherStatusFilter").val() || null;


    $("#voucherPaginationContainer").attr("data-current-page", page);


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
            // Swal.fire("Lỗi!", "Không tải được dữ liệu voucher!", "error");
            searchVoucher(0);
        }
    });
}


// Toggle status voucher
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
                    searchVoucher(getCurrentVoucherPage());
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

// Refresh table voucher sau add
function refreshVoucherTable() {
    $.get("/admin/voucher/counts").done(function (totalItems) {
        const pageSize = 10;  // Giả sử page size, điều chỉnh nếu cần
        const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
        searchVoucher(lastPage);
    });
}

// Init voucher
$(document).ready(function () {
    $("#voucherSearchInput, #voucherStatusFilter").on("change keyup", function (e) {
        if (e.key === "Enter" || $(this).is("select")) {
            searchVoucher(0);
        }
    });
});




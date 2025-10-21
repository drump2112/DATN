// admin/event.js - Quản lý Đợt Giảm Giá (Event)


// Xem chi tiết và mở modal edit event
window.handleEventDetailClick = function (button) {
   const event = {
       id: $(button).data("id"),
       code: $(button).data("code"),
       name: $(button).data("name"),
       discountType: $(button).data("discountType"),
       discountValue: $(button).data("discountvalue"),
       maxDiscountValue: $(button).data("maxdiscountvalue"),
       startDate: $(button).data("startdate"),
       endDate: $(button).data("enddate"),
       isActive: $(button).data("isActive"),
   };


   console.log("Event data lấy tu day:", event);


   const currentPage = parseInt($("#eventPaginationContainer .paginate_button.active a").text()) - 1 || 0;
   $("#eventForm").data("current-page", currentPage);
   openEventEditModal(event, true);
};


// Mở modal edit/view chi tiết event
function openEventEditModal(data, isEditable) {
   clearEventForm();


   // Fill cơ bản (với fallback empty string)
   $("#eventId").val(data.id || "");
   $("#eventCode").val(data.code || "").prop("readonly", true);  // Code luôn readonly
   $("#eventName").val(data.name || "").prop("readonly", !isEditable);
   $("#eventDiscountType").val(data.discountType || "PERCENT");
   $("#eventDiscountValue").val(data.discountValue || "");
   $("#eventMinOrderAmount").val(data.minOrderAmount || "");
   $("#eventQuantity").val(data.quantity || "");


   // Fill maxDiscountValue và set readonly dựa trên discountType (swap: PERCENT enable, FIXED readonly)
   const discountType = $("#eventDiscountType").val();
   const $maxDiscountInput = $("#eventMaxDiscountValue");
   if (discountType === "FIXED") {
       $maxDiscountInput.val("").prop("readonly", true);  // Clear và readonly nếu FIXED
   } else {
       $maxDiscountInput.val(data.maxDiscountValue || "").prop("readonly", !isEditable);
   }


   console.log("Event data:", data);


   // Fix date format: Cắt từ "YYYY-MM-DDTHH:mm:ss" → "YYYY-MM-DD" cho input date
   let formattedStartDate = "";
   let formattedEndDate = "";
   if (data.startDate) {
       formattedStartDate = data.startDate.toString().split('T')[0];  // "2025-10-14T00:00" → "2025-10-14"
   }
   if (data.endDate) {
       formattedEndDate = data.endDate.toString().split('T')[0];
   }
   $("#eventStartDate").val(formattedStartDate);
   $("#eventEndDate").val(formattedEndDate);


   // Fix isActive: Convert boolean/string → "1"/"0"
   let activeVal = "1";  // Default active
   if (data.isActive === false || data.isActive === "false" || data.isActive === "0") {
       activeVal = "0";
   }
   $("#eventIsActive").val(activeVal);


   // Debug: Log data để check console (remove sau khi test)
   console.log("Event data loaded:", data);
   console.log("Formatted dates:", {start: formattedStartDate, end: formattedEndDate});
   console.log("isActive val:", activeVal);


   // Update title và buttons
   $("#eventModalTitle").text(isEditable ? "Cập Nhật Event" : "Chi Tiết Event");
   $("#btnSaveEvent").hide();
   $("#btnUpdateEvent").toggle(isEditable).show();
   $("#modalEvent").modal("show");


   // Trigger change event để apply readonly logic ngay khi load
   $("#eventDiscountType").trigger("change");
}


// Clear form event
function clearEventForm() {
   $("#eventForm input:not(#eventCode), #eventForm select").val("");
   $("#eventForm").removeData("current-page");
   $(".form-group").removeClass("has-error");
   // Reset readonly cho maxDiscountValue (sẽ được handle bởi event change sau)
   $("#eventMaxDiscountValue").prop("readonly", false);
   clearValidationErrors();
}


// Mở modal thêm mới event
$("#btnOpenAddEvent").on("click", function () {
   clearEventForm();
   $("#eventCode").val("").prop("readonly", false);  // Cho phép nhập code thủ công
   $("#eventModalTitle").text("Thêm Event Mới");
   $("#eventForm input, #eventForm select").prop("readonly", false).prop("disabled", false);
   $("#btnSaveEvent").show();
   $("#btnUpdateEvent").hide();
   $("#modalEvent").modal("show");


   // Trigger change để apply readonly cho PERCENT/FIXED default
   $("#eventDiscountType").trigger("change");
});


// Event listener cho select loại giảm giá: Swap logic readonly (ngược lại: FIXED -> readonly max, PERCENT -> enable max)
$(document).on("change", "#eventDiscountType", function () {
   const discountType = $(this).val();
   const $maxDiscountInput = $("#eventMaxDiscountValue");


   if (discountType === "FIXED") {
       $maxDiscountInput.prop("readonly", true).val("");  // Readonly và clear value khi FIXED (optional sau swap)
       $maxDiscountInput.closest(".form-group").removeClass("has-error");  // Clear lỗi nếu có
   } else if (discountType === "PERCENT") {
       $maxDiscountInput.prop("readonly", false).focus();  // Enable và focus để nhập khi PERCENT (bắt buộc sau swap)
   }
});


// Validation event (swap logic giữa PERCENT và FIXED: đảo ngược check max vs check %)
function validateEvent() {
   // Clear tất cả lỗi trước khi validate mới
   clearValidationErrors();


   // Trim tất cả input text để loại bỏ khoảng trắng đầu cuối
   $("#eventCode").val($("#eventCode").val().trim());
   $("#eventName").val($("#eventName").val().trim());
   $("#eventDiscountValue").val($("#eventDiscountValue").val().trim());
   $("#eventMaxDiscountValue").val($("#eventMaxDiscountValue").val().trim());


   const code = $("#eventCode").val().trim();  // Optional: null OK, nhưng nếu có thì >=5 ký tự
   const name = $("#eventName").val().trim();
   const discountType = $("#eventDiscountType").val();  // Bắt buộc
   const discountValueStr = $("#eventDiscountValue").val().trim();
   const discountValue = parseFloat(discountValueStr);
   const maxDiscountValueStr = $("#eventMaxDiscountValue").val().trim();
   const maxDiscountValue = parseFloat(maxDiscountValueStr) || null;
   const startDate = $("#eventStartDate").val();
   const endDate = $("#eventEndDate").val();
   const isActive = $("#eventIsActive").val();  // Bắt buộc


   // Check lần lượt theo thứ tự: mã -> tên -> loại -> giá trị giảm -> logic loại (đã swap) -> dates -> số lượng -> trạng thái


   // 1. Mã event: optional, nhưng nếu nhập thì >=5 ký tự
   if (code && code.length < 5) {
       showFieldError("#eventCode", "Mã event phải có ít nhất 5 ký tự!");
       return false;
   }


   // 2. Tên không được trống
   if (!name) {
       showFieldError("#eventName", "Tên event không được để trống!");
       return false;
   }


   // 3. Loại mã giảm giá bắt buộc
   if (!discountType) {
       showFieldError("#eventDiscountType", "Vui lòng chọn loại giảm giá!");
       return false;
   }


   // 4. Giá trị giảm chung: phải >0
   if (!discountValueStr || !discountValue || discountValue <= 0) {
       showFieldError("#eventDiscountValue", "Giá trị giảm phải lớn hơn 0!");
       return false;
   }


   // 5. Logic theo loại (đã swap: PERCENT -> check max bắt buộc & discount <= max; FIXED -> check discount <=100, max optional)
   if (discountType === "PERCENT") {
       if (discountValue < 0) {
           showFieldError("#voucherDiscountValue", "Giá trị giảm không được nhỏ hơn 1%!");
           return false;
       } else if (discountValue > 100) {
           showFieldError("#eventDiscountValue", "Giá trị giảm cố định không được lớn hơn 100!");
           return false;
       } else if (!maxDiscountValueStr || !maxDiscountValue || maxDiscountValue <= 0) {
           showFieldError("#eventMaxDiscountValue", "Giá trị giảm tối đa phải là số dương khi chọn loại giảm giá phần trăm!");
           return false;
       }
   } else if (discountType === "FIXED") {


       // maxDiscountValue: không validate (optional)
   }


   // 6. Thời gian bắt đầu/kết thúc: bắt buộc, start > current, start <= end (chung cho cả hai loại)
   if (!startDate || !endDate) {
       showFieldError("#eventStartDate", "Vui lòng chọn ngày bắt đầu và kết thúc!");
       return false;
   } else {
       const start = new Date(startDate);
       const end = new Date(endDate);
       const currentDate = new Date();  // Ngày hiện tại
       currentDate.setHours(0, 0, 0, 0);  // Reset giờ để so sánh ngày


       if (start <= currentDate) {  // Phải > current
           showFieldError("#eventStartDate", "Thời gian bắt đầu phải lớn hơn thời gian hiện tại!");
           return false;
       } else if (start > end) {
           showFieldError("#eventEndDate", "Thời gian bắt đầu không được lớn hơn thời gian kết thúc!");
           return false;
       } else if (end <= currentDate) {  // end > current để hợp lý
           showFieldError("#eventEndDate", "Thời gian kết thúc phải sau thời gian hiện tại!");
           return false;
       }
   }


   // 8. Trạng thái không null (giống PERCENT, chung)
   if (!isActive) {
       showFieldError("#eventIsActive", "Vui lòng chọn trạng thái event!");
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
       $field.after($errorSpan);  // Đặt sau input để bên cạnh
   }
   $errorSpan.text(errorMsg).show();


   $field.focus();
}


// Helper function: Clear tất cả lỗi validation
function clearValidationErrors() {
   $(".form-group").removeClass("has-error");
   $(".help-block.error-message").remove();  // Xóa span lỗi
}


// Event thêm/lưu event
$(document).on("click", "#btnSaveEvent", function () {
   if (!validateEvent()) return;


   Swal.fire({
       title: "Xác nhận thêm event?",
       icon: "question",
       showCancelButton: true,
       confirmButtonText: "Thêm",
       cancelButtonText: "Hủy",
   }).then((result) => {
       if (result.isConfirmed) {
           const formData = new FormData($("#eventForm")[0]);
           formData.append("isActive", $("#eventIsActive").val());


           $.ajax({
               url: "/admin/event/add",
               method: "POST",
               processData: false,
               contentType: false,
               data: formData,
               beforeSend: function () {
                   $("#btnSaveEvent").prop("disabled", true).text("Đang thêm...");
               },
               success: function (response) {
                   Swal.fire("Thành công!", response.message, "success");
                   $("#modalEvent").modal("hide");
                   refreshEventTable();
               },
               error: function (xhr) {
                   Swal.fire("Lỗi!", xhr.responseJSON?.message || "Thêm thất bại!", "error");
               },
               complete: function () {
                   $("#btnSaveEvent").prop("disabled", false).text("Thêm/Lưu");
               }
           });
       }
   });
});


// Event cập nhật event
$(document).on("click", "#btnUpdateEvent", function () {
   if (!validateEvent()) return;


   Swal.fire({
       title: "Xác nhận cập nhật event?",
       icon: "question",
       showCancelButton: true,
       confirmButtonText: "Cập nhật",
       cancelButtonText: "Hủy",
   }).then((result) => {
       if (result.isConfirmed) {
           const formData = new FormData($("#eventForm")[0]);
           const eventId = $("#eventId").val();
           formData.append("isActive", $("#eventIsActive").val());


           $.ajax({
               url: `/admin/event/${eventId}`,
               type: "PUT",
               processData: false,
               contentType: false,
               data: formData,
               beforeSend: function () {
                   $("#btnUpdateEvent").prop("disabled", true).text("Đang cập nhật...");
               },
               success: function (response) {
                   Swal.fire("Thành công!", response.message, "success");
                   $("#modalEvent").modal("hide");
                   searchEvent($("#eventForm").data("current-page") || 0);
               },
               error: function (xhr) {
                   Swal.fire("Lỗi!", xhr.responseJSON?.message || "Cập nhật thất bại!", "error");
               },
               complete: function () {
                   $("#btnUpdateEvent").prop("disabled", false).text("Cập Nhật");
               }
           });
       }
   });
});


// Lấy page hiện tại event
function getCurrentEventPage() {
   return parseInt($("#eventPaginationContainer").attr("data-current-page")) ||
       parseInt($("#eventPaginationContainer .paginate_button.active a").text()) - 1 || 0;
}


// Tìm kiếm event
function searchEvent(page = 0) {
   const keyword = $("#eventSearchInput").val().trim();
   const isActive = $("#eventStatusFilter").val() || null;


   $("#eventPaginationContainer").attr("data-current-page", page);


   $.ajax({
       url: "/admin/event/search",
       type: "GET",
       data: {page, keyword, isActive},
       beforeSend: function () {
           $("#eventTableContainer").html('<div class="text-center"><i class="fa fa-spinner fa-spin"></i> Đang tải...</div>');
       },
       success: function (response) {
           $("#eventTableContainer").html(response);
       },
       error: function () {
           // Swal.fire("Lỗi!", "Không tải được dữ liệu event!", "error");
           searchEvent(0);
       }
   });
};


// Toggle status event
window.toggleEventStatus = function (eventId, isActive) {
   const title = isActive ? "Vô hiệu hóa event này?" : "Kích hoạt event này?";


   Swal.fire({
       title: title,
       icon: "warning",
       showCancelButton: true,
       confirmButtonText: "Xác nhận",
       cancelButtonText: "Hủy",
   }).then((result) => {
       if (result.isConfirmed) {
           $.ajax({
               url: `/admin/event/${eventId}/toggle-status`,
               type: "PUT",
               beforeSend: function () {
                   $(`button[data-id="${eventId}"]`).prop("disabled", true);
               },
               success: function (data) {
                   Swal.fire("Thành công!", data.message, "success");
                   searchEvent(getCurrentEventPage());
               },
               error: function (xhr) {
                   Swal.fire("Lỗi!", xhr.responseJSON?.message || "Có lỗi xảy ra!", "error");
               },
               complete: function () {
                   $(`button[data-id="${eventId}"]`).prop("disabled", false);
               }
           });
       }
   });
};


// Refresh table event sau add
function refreshEventTable() {
   $.get("/admin/event/counts").done(function (totalItems) {
       const pageSize = 10;  // Giả sử page size, điều chỉnh nếu cần
       const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
       searchEvent(lastPage);
   });
}

// Init event
$(document).ready(function () {
   $("#eventSearchInput, #eventStatusFilter").on("change keyup", function (e) {
       if (e.key === "Enter" || $(this).is("select")) {
           searchEvent(0);
       }
   });
});




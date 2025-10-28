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

// Mở modal edit
function openEventEditModal(data, isEditable) {
   clearEventForm();

   $("#eventId").val(data.id || "");
   $("#eventCode").val(data.code || "").prop("readonly", true);  // Code luôn readonly
   $("#eventName").val(data.name || "").prop("readonly", !isEditable);
   $("#eventDiscountType").val(data.discountType || "PERCENT");
   $("#eventDiscountValue").val(Math.round(data.discountValue || ""));
   $("#eventMinOrderAmount").val(Math.round(data.minOrderAmount || ""));
   $("#eventQuantity").val(data.quantity || "");

   const discountType = $("#eventDiscountType").val();
   const $maxDiscountInput = $("#eventMaxDiscountValue");
   if (discountType === "FIXED") {
       $maxDiscountInput.val("").prop("readonly", true);
   } else {
       $maxDiscountInput
           .val(Math.round(data.maxDiscountValue || ""))
           .prop("disabled", !isEditable);
   }




   console.log("Event data:", data);






   let formattedStartDate = "";
   let formattedEndDate = "";
   if (data.startDate) {
       formattedStartDate = data.startDate.toString().split('T')[0];
   }
   if (data.endDate) {
       formattedEndDate = data.endDate.toString().split('T')[0];
   }
   $("#eventStartDate").val(formattedStartDate);
   $("#eventEndDate").val(formattedEndDate);




   let activeVal = "1";  // Default active
   if (data.isActive === false || data.isActive === "false" || data.isActive === "0") {
       activeVal = "0";
   }
   $("#eventIsActive").val(activeVal);




   console.log("Event data loaded:", data);
   console.log("Formatted dates:", {start: formattedStartDate, end: formattedEndDate});
   console.log("isActive val:", activeVal);




   $("#eventModalTitle").text(isEditable ? "Cập Nhật Event" : "Chi Tiết Event");
   $("#btnSaveEvent").hide();
   $("#btnUpdateEvent").toggle(isEditable).show();
   $("#modalEvent").modal("show");




   $("#eventDiscountType").trigger("change");
}




function clearEventForm() {
   $("#eventForm input:not(#eventCode), #eventForm select").val("");
   $("#eventForm").removeData("current-page");
   $(".form-group").removeClass("has-error");
   $("#eventMaxDiscountValue").prop("readonly", false);
   clearValidationErrors();
}




$("#btnOpenAddEvent").on("click", function () {
   clearEventForm();
   $("#eventCode").val("").prop("readonly", false);  // Cho phép nhập code thủ công
   $("#eventModalTitle").text("Thêm Event Mới");
   $("#eventForm input, #eventForm select").prop("readonly", false).prop("disabled", false);
   $("#btnSaveEvent").show();
   $("#btnUpdateEvent").hide();
   $("#modalEvent").modal("show");






   $("#eventDiscountType").trigger("change");
});




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


function validateEvent() {
   clearValidationErrors();


   const code = $("#eventCode").val().trim();
   const name = $("#eventName").val().trim();
   const discountType = $("#eventDiscountType").val();
   const discountValueStr = $("#eventDiscountValue").val().trim();
   const discountValue = parseFloat(discountValueStr);
   const maxDiscountValueStr = $("#eventMaxDiscountValue").val().trim();
   const maxDiscountValue = parseFloat(maxDiscountValueStr) || null;
   const startDate = $("#eventStartDate").val();
   const endDate = $("#eventEndDate").val();
   const isActive = $("#eventIsActive").val();


   let isValid = true;


 if (code) {
     if (code.length < 5) {
         showFieldError("#eventCode", "Mã event phải có ít nhất 5 ký tự!");
         isValid = false;
     }
 }


   if (!name) {
       showFieldError("#eventName", "Tên event không được để trống!");
       isValid = false;
   }


   if (!discountType) {
       showFieldError("#eventDiscountType", "Vui lòng chọn loại giảm giá!");
       isValid = false;
   }


   if (!discountValueStr || isNaN(discountValue) || discountValue <= 0) {
       showFieldError("#eventDiscountValue", "Giá trị giảm phải lớn hơn 0!");
       isValid = false;
   }
   if (discountType === "PERCENT") {
       if (discountValue < 1) {
           showFieldError("#eventDiscountValue", "Giá trị giảm không được nhỏ hơn 1%!");
           isValid = false;
       } else if (discountValue > 100) {
           showFieldError("#eventDiscountValue", "Giá trị giảm không được lớn hơn 100%!");
           isValid = false;
       }


       if (!maxDiscountValueStr || isNaN(maxDiscountValue) || maxDiscountValue <= 0) {
           showFieldError("#eventMaxDiscountValue", "Giá trị giảm tối đa không để trống khi chọn loại giảm giá phần trăm và phải là số dương!");
           isValid = false;
       } else if (maxDiscountValue > 9999999) {
           showFieldError("#eventMaxDiscountValue", "Giá trị giảm tối đa không được lớn hơn 10 triệu!");
           isValid = false;
       }
   } else if (discountType === "FIXED") {
       if (discountValue > 9999999) {
           showFieldError("#eventDiscountValue", "Giá trị giảm không được lớn hơn 10 triệu!");
           isValid = false;
       }
   }


   if (!startDate || !endDate) {
       if (!startDate) {
           showFieldError("#eventStartDate", "Vui lòng chọn ngày bắt đầu!");
           isValid = false;
       }
       if (!endDate) {
           showFieldError("#eventEndDate", "Vui lòng chọn ngày kết thúc!");
           isValid = false;
       }
   } else {
       const start = new Date(startDate);
       const end = new Date(endDate);
       const currentDate = new Date();
       currentDate.setHours(0, 0, 0, 0);
       start.setHours(0, 0, 0, 0);
       end.setHours(0, 0, 0, 0);


       if (start < currentDate) {
           showFieldError("#eventStartDate", "Thời gian bắt đầu phải lớn hơn thời gian hiện tại!");
           isValid = false;
       }
       if (start > end) {
           showFieldError("#eventStartDate", "Thời gian bắt đầu không được lớn hơn thời gian kết thúc!");
           isValid = false;
       } else if (end <= currentDate) {
           showFieldError("#eventEndDate", "Thời gian kết thúc phải sau thời gian hiện tại!");
           isValid = false;
       }
   }


   if (!isActive) {
       showFieldError("#eventIsActive", "Vui lòng chọn trạng thái event!");
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
       $field.after($errorSpan);  // Đặt sau input để bên cạnh
   }
   $errorSpan.text(errorMsg).show();




   $field.focus();
}




function clearValidationErrors() {
   $(".form-group").removeClass("has-error");
   $(".help-block.error-message").remove();  // Xóa span lỗi
}




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
                   searchEvent($("#paginationContainer").data("current-page") || 0);
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
                   searchEvent($("#paginationContainer").data("current-page") || 0);
                   $("#modalEvent").modal("hide");
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
                   searchEvent($("#paginationContainer").data("current-page") || 0);
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


function refreshEventTable() {
   $.get("/admin/event/counts").done(function (totalItems) {
       const pageSize = 10;  // Giả sử page size, điều chỉnh nếu cần
       const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
       searchEvent(lastPage);
   });
}

$("#resetFilterBtn").on("click", function () {
   $("#eventSearchInput").val("");
   $("#eventStatusFilter").val(null).trigger("change");
   $("#eventSearchInput").val(null).trigger("change");
   console.log("reset filter");
   $.ajax({
       url: "/admin/event/search",
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

$(document).ready(function () {
   $("#eventSearchInput, #eventStatusFilter").on("change keyup", function (e) {
       if (e.key === "Enter" || $(this).is("select")) {
           searchEvent(0);
       }
   });
});








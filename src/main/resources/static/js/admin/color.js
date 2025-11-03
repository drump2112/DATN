// color.js
// xem chi tiết
window.handleDetailClick = function (button) {
    const color = {
        id: $(button).data("id"),
        code: $(button).data("code"),
        name: $(button).data("name"),
        isActive: $(button).data("isActive"),
    };
    const currentPage =
        parseInt($("#paginationContainer .paginate_button.active a").text()) -
        1 || 0;
    $("#colorForm").data("current-page", currentPage);
    openEditModal(color, true);
};


// mở modal và hiển thị chi tiêt color
function openEditModal(data, isEditable) {
    clearErrors();
    $("#id").val(data.id);
    $("#code").val(data.code).prop("readonly", true);
    $("#name").val(data.name).prop("readonly", !isEditable);


    $("#modalTitle").text("Chi tiết và cập nhật thông tin màu sắc");
    $("#btnAddColor").hide();
    $("#btnUpdate").toggle(isEditable);
    $("#myModal").modal("show");
}


// Hàm làm mới input
function clearErrors() {
    $("#code").val("");
    $("#name").val("");
}


// mở modal nhập thêm
function openAddModal() {
    console.log(" oke đã  tới đây")
    clearErrors();
    $("#modalTitle").text("Thêm màu sắc");
    $("#colorForm input, #colorForm select")
        .prop("readonly", false)
        .prop("disabled", false);
    $("#colorForm #maColor").prop("readonly", true).prop("disabled", true);
    $("#btnAddColor").show();
    $("#btnUpdate").hide();
    $("#myModal").modal("show");
}


//  Hàm validation tên màu sắc
function validateColorName() {
    const name = $("#name").val().trim();
    let errorMessage = "";


    // Không để trống
    if (!name) {
        errorMessage = "Màu sắc không được để trống!";
    }
    else if (name.length < 2 || name.length > 30) {
        errorMessage = "Tên màu sắc phải từ 2 đến 30 ký tự!";
    }

    if (errorMessage) {
        SwalUtils.error("Lỗi!", errorMessage);
        return false;
    }
    return true;
}


// thêm màu sắc
$("#btnAddColor").click(function () {
    if (!validateColorName()) return; s//   check trước khi gửi

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
                    SwalUtils.success("Thành công!", response.message);
                    $("#myModal").modal("hide");
                    $.get("/admin/color/counts").done(function (totalItems) {
                        const pageSize = 5;
                        const lastPage = Math.max(
                            0,
                            Math.ceil(totalItems / pageSize) - 1,
                        );
                        searchColor(lastPage);
                    });
                },
                error: function (xhr) {
                    SwalUtils.error(
                        "Lỗi!",
                        xhr.responseJSON?.message || "Thêm thất bại"
                    );
                },
            });
        }
    });
});

// get trang hiện tại
function getCurrentPage() {
    return parseInt($("#paginationContainer").attr("data-current-page")) || 0;
}


// Tìm kiếm Color
function searchColor(page) {
    var keyword = $("#searchInput").val().trim();
    var isActive = $("#statusFilter").val() || null;

    $.ajax({
        url: "/admin/color/search",
        type: "GET",
        data: {
            page: page,
            keyword: keyword,
            isActive: isActive,
        },
        success: function (response) {
            $("#colorTableContainer").html(response);
        },
        error: function () {
            searchColor(0);
        },
    });
}

// Chuyển đổi trạng thái
window.toggleStatus = function (userId, isActive) {
    const title = isActive
        ? "Bạn có chắc muốn vô hiệu hóa màu sắc này?"
        : "Bạn có chắc muốn kích hoạt màu sắc này?";


    SwalUtils.confirm(
        title,
        "",
        "Xác nhận",
        "Hủy",
        {
            icon: "warning",
            customClass: {
                popup: "swal-pop-zindex",
            },
            backdrop: `rgba(0, 0, 0, 0.4)`,
        }
    ).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `/admin/color/${userId}/toggle-status`,
                type: "PUT",
                success: function (data) {
                    SwalUtils.success("Thành công", data.message);
                    const currentPage = getCurrentPage();
                    searchColor(currentPage);
                },
                error: function (xhr) {
                    SwalUtils.error(
                        "Lỗi",
                        xhr.responseJSON?.message || "Có lỗi xảy ra"
                    );
                },
            });
        }
    });
};

$("#btnUpdate").on("click", function () {
    if (!validateColorName(false)) return;

    SwalUtils.confirm(
        "Xác nhận cập nhật màu sắc?",
        "",
        "Cập nhật",
        "Hủy"
    ).then((result) => {
        if (result.isConfirmed) {
            const formData = new FormData();
            formData.append("colorCode", $("#code").val().trim());
            formData.append("name", $("#name").val().trim());
            console.log(formData.get("colorCode" + "code"))
            console.log(formData.get("name"))
            const colorId = $("#id").val();


            $.ajax({
                url: `/admin/color/${colorId}`,
                type: "PUT",
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    SwalUtils.success("Cập nhật thành công!", response.message);
                    $("#myModal").modal("hide");
                    const currentPage = $("#colorForm").data("current-page") || 0;
                    searchColor(currentPage);
                },
                error: function (xhr) {
                    toastr.error("Cập nhật thất bại: " + xhr.responseText);
                },
            });
        }
    });
});


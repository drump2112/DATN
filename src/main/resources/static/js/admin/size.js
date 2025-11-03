// xem chi tiết
window.handleDetailClick = function (button) {
    const size = {
        id: $(button).data("id"),
        code: $(button).data("code"),
        name: $(button).data("name"),
        isActive: $(button).data("isActive"),
    };
    const currentPage =
        parseInt($("#paginationContainer .paginate_button.active a").text()) -
        1 || 0;
    $("#sizeForm").data("current-page", currentPage);
    openEditModal(size, true);
};


// mở modal và hiển thị chi tiêt size
function openEditModal(data, isEditable) {
    clearErrors();
    $("#id").val(data.id);
    $("#code").val(data.code).prop("readonly", true);
    $("#name").val(data.name).prop("readonly", !isEditable);


    $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Kích thước");
    $("#btnAddSize").hide();
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
    $("#modalTitle").text("Thêm kích thước");
    $("#sizeForm input, #sizeForm select")
        .prop("readonly", false)
        .prop("disabled", false);
    $("#sizeForm #maSize").prop("readonly", true).prop("disabled", true);
    $("#btnAddSize").show();
    $("#btnUpdate").hide();
    $("#myModal").modal("show");
}


//  Hàm validation tên kích thước
function validateSizeName() {
    const name = $("#name").val().trim();
    let errorMessage = "";

    if (!name) {
        errorMessage = "Kích thước không được để trống!";
    } else if (!/^\d+$/.test(name)) {
        errorMessage = "Kích thước chỉ được nhập số!";
    }

    if (errorMessage) {
        SwalUtils.showErrorAlert("Lỗi!", errorMessage);
        return false;
    }
    return true;
}


// thêm kích thước
$("#btnAddSize").click(function () {
    if (!validateSizeName()) return; //   check trước khi gửi

    SwalUtils.showConfirmDialog(
        "Xác nhận thêm kích thước?",
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
                    SwalUtils.showSuccessToast(response.message);

                    $("#myModal").modal("hide");

                    $.get("/admin/size/counts").done(function (totalItems) {
                        const pageSize = 5;
                        const lastPage = Math.max(
                            0,
                            Math.ceil(totalItems / pageSize) - 1,
                        );
                        searchSize(lastPage);
                    });
                },
                error: function (xhr) {
                    SwalUtils.showErrorAlert(
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


// Tìm kiếm size
function searchSize(page) {

    var keyword = $("#searchInput").val().trim();
    var isActive = $("#statusFilter").val() || null;


    $.ajax({
        url: "/admin/size/search",
        type: "GET",
        data: {
            page: page,
            keyword: keyword,
            isActive: isActive,
        },
        success: function (response) {
            $("#sizeTableContainer").html(response);
        },
        error: function () {
            searchSize(0);
        },
    });
}


// Chuyển đổi trạng thái
window.toggleStatus = function (userId, isActive) {
    const title = isActive
        ? "Bạn có chắc muốn vô hiệu hóa kích thước này?"
        : "Bạn có chắc muốn kích hoạt kích thước này?";


    SwalUtils.showWarningConfirmDialog(
        title,
        "Xác nhận",
        "Hủy"
    ).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                url: `/admin/size/${userId}/toggle-status`,
                type: "PUT",
                success: function (data) {
                    SwalUtils.showSuccessToast(data.message);
                    const currentPage = getCurrentPage();
                    searchSize(currentPage);
                },
                error: function (xhr) {
                    SwalUtils.showErrorAlert(
                        "Lỗi!",
                        xhr.responseJSON?.message || "Có lỗi xảy ra"
                    );
                },
            });
        }
    });
};
$("#btnUpdate").on("click", function () {
    console.log("oke")
    if (!validateSizeName(false)) return;


    SwalUtils.showConfirmDialog(
        "Xác nhận cập nhật kích thước",
        "Cập nhật",
        "Hủy"
    ).then((result) => {
        if (result.isConfirmed) {
            const formData = new FormData();
            formData.append("sizeCode", $("#code").val().trim());
            formData.append("name", $("#name").val().trim());
            const sizeId = $("#id").val();


            $.ajax({
                url: `/admin/size/${sizeId}`,
                type: "PUT",
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    SwalUtils.showSuccessToast(response.message);
                    $("#myModal").modal("hide");
                    const currentPage = $("#colorForm").data("current-page") || 0;
                    searchSize(currentPage);
                },
                error: function (xhr) {
                    toastr.error("Cập nhật thất bại: " + xhr.responseText);
                },
            });
        }
    });
});


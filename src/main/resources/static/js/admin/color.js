
// === QUẢN LÝ MÀU SẮC ===
(function() {
    'use strict';

    let currentColorPage = 0;

// Tìm kiếm Color - Internal function
function doSearchColor(page) {
    currentColorPage = page;
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
            doSearchColor(0);
        },
    });
}

// === XEM CHI TIẾT ===
window.handleDetailClick = function (button) {
    const color = {
        id: $(button).data("id"),
        code: $(button).data("code"),
        name: $(button).data("name"),
        isActive: $(button).data("isActive"),
    };

    const currentPage = getCurrentPage();
    currentColorPage = currentPage;
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
    clearErrors();
    $("#id").val("");
    $("#code").val("");
    $("#name").val("");

    $("#modalTitle").text("Thêm màu sắc");
    $("#colorForm input, #colorForm select")
        .prop("readonly", false)
        .prop("disabled", false);
    $("#colorForm #code").prop("readonly", true).prop("disabled", true);
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


// === XỬ LÝ THÊM MÀU SẮC ===
function handleAddColor() {
    if (!validateColorName()) return;

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
                        doSearchColor(lastPage);
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
}

// get trang hiện tại
function getCurrentPage() {
    const fromContainer = parseInt($("#paginationContainer").attr("data-current-page"));
    const fromActive = parseInt($("#paginationContainer .paginate_button.active a").text()) - 1;
    const fromPagination = parseInt($(".pagination .active a").text()) - 1;

    return fromContainer || fromActive || fromPagination || currentColorPage || 0;
}


// === CHUYỂN ĐỔI TRẠNG THÁI ===
function doToggleStatus(userId, isActive) {
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
                    doSearchColor(currentColorPage);
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
}

// === PUBLIC FUNCTIONS ===
window.searchColor = function (page) { doSearchColor(page); };
window.openAddModal = openAddModal;
window.toggleStatus = function (userId, isActive) { doToggleStatus(userId, isActive); };

// === XỬ LÝ CẬP NHẬT MÀU SẮC ===
function handleUpdateColor() {
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
            const colorId = $("#id").val();
            const savedPage = $("#colorForm").data("current-page") || getCurrentPage() || currentColorPage || 0;

            $.ajax({
                url: `/admin/color/${colorId}`,
                type: "PUT",
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    SwalUtils.success("Cập nhật thành công!", response.message);
                    $("#myModal").modal("hide");
                    doSearchColor(savedPage);
                },
                error: function (xhr) {
                    SwalUtils.error("Cập nhật thất bại", xhr.responseJSON?.message || "Có lỗi xảy ra");
                },
            });
        }
    });
    }

    // === FORCE BIND MODAL EVENTS ===
    function bindModalEvents() {
        if ($("#btnAddColor").length > 0) {
            $("#btnAddColor").off('click.color').on('click.color', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleAddColor();
            });
        }

        if ($("#btnUpdate").length > 0) {
            $("#btnUpdate").off('click.color').on('click.color', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleUpdateColor();
            });
        }
    }

    // === FUNCTION ĐỢI jQuery VÀ KHỞI TẠO ===
    function initializeColorModule() {
        if (typeof jQuery === 'undefined' || typeof $ === 'undefined') {
            return;
        }

        if (typeof SwalUtils === 'undefined') {
            setTimeout(initializeColorModule, 500);
            return;
        }

        setupEventListeners();
    }

    // === SETUP EVENT LISTENERS ===
    function setupEventListeners() {
        $(document).off('click.colorModule').on("click.colorModule", "#btnAddColor", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddColor();
        });

        $(document).off('click.colorModule').on("click.colorModule", "#btnUpdate", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateColor();
        });

        $(document).off('click.colorModalModule').on("click.colorModalModule", "#myModal #btnAddColor", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddColor();
        });

        $(document).off('click.colorModalModule').on("click.colorModalModule", "#myModal #btnUpdate", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateColor();
        });

        $(document).on('click.colorPagination', '.pagination a', function (e) {
            const pageText = $(this).text();
            if (!isNaN(pageText)) {
                currentColorPage = parseInt(pageText) - 1;
            }
        });

        $(document).on('click.colorSearch', '[onclick*="searchColor"]', function () {
            const onclickAttr = $(this).attr('onclick');
            const match = onclickAttr.match(/searchColor\((\d+)\)/);
            if (match) {
                currentColorPage = parseInt(match[1]);
            }
        });

        $(document).on("change.colorFilter", "#statusFilter", function () {
            doSearchColor(0);
        });

        $(document).on("keypress.colorSearch", "#searchInput", function (e) {
            if (e.which === 13) {
                doSearchColor(0);
            }
        });

        $('#myModal').off('shown.bs.modal.colorModule').on('shown.bs.modal.colorModule', function () {
            setTimeout(function() {
                bindModalEvents();
            }, 100);
        });
    }

    // === WAIT FOR DEPENDENCIES AND INITIALIZE ===
    if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
        $(document).ready(function() {
            initializeColorModule();
        });
    } else {
        function waitForJQuery() {
            if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
                $(document).ready(function() {
                    initializeColorModule();
                });
            } else {
                setTimeout(waitForJQuery, 100);
            }
        }
        waitForJQuery();
    }

})(); // Đóng IIFE


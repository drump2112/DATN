// === QUẢN LÝ KÍCH THƯỚC ===
(function() {
    'use strict';

    let currentSizePage = 0;

// === XEM CHI TIẾT ===
window.handleDetailClick = function (button) {
    const size = {
        id: $(button).data("id"),
        code: $(button).data("code"),
        name: $(button).data("name"),
        isActive: $(button).data("isActive"),
    };

    const currentPage = getCurrentPage();
    currentSizePage = currentPage;
    $("#sizeForm").data("current-page", currentPage);

    openEditModal(size, true);
};

// === MỞ MODAL CHỈNH SỬA ===
function openEditModal(data, isEditable) {
    clearErrors();
    $("#id").val(data.id);
    $("#code").val(data.code).prop("readonly", true);
    $("#name").val(data.name).prop("readonly", !isEditable);

    $("#modalTitle").text("Chi Tiết Và Cập Nhật Thông Tin Kích thước");

    // Show/hide buttons
    $("#btnAddSize").hide();
    $("#btnUpdate").toggle(isEditable);

    $("#myModal").modal("show");
}

// === MỞ MODAL THÊM MỚI ===
function openAddModal() {
    clearErrors();

    $("#id").val("");
    $("#code").val("");
    $("#name").val("");

    $("#modalTitle").text("Thêm kích thước");
    $("#sizeForm input, #sizeForm select")
        .prop("readonly", false)
        .prop("disabled", false);
    $("#sizeForm #code").prop("readonly", true).prop("disabled", true);

    $("#btnAddSize").show();
    $("#btnUpdate").hide();

    $("#myModal").modal("show");
}

// === LÀM SẠCH LỖI ===
function clearErrors() {
    $("#code").val("");
    $("#name").val("");
}

// === VALIDATE TÊN KÍCH THƯỚC ===
function validateSizeName(isEdit = false) {
    return new Promise((resolve) => {
        const name = $("#name").val().trim();
        let errorMessage = "";

        // Kiểm tra định dạng cơ bản
        if (!name) {
            errorMessage = "Kích thước không được để trống!";
        } else if (!/^\d+$/.test(name)) {
            errorMessage = "Kích thước chỉ được nhập số!";
        }

        if (errorMessage) {
            SwalUtils.error("Lỗi!", errorMessage);
            resolve(false);
            return;
        }

        // Kiểm tra trùng tên với server
        const sizeId = isEdit ? $("#id").val() : null;
        checkSizeNameExists(name, sizeId).then(exists => {
            if (exists) {
                SwalUtils.error("Lỗi!", "Kích thước đã tồn tại!");
                resolve(false);
            } else {
                resolve(true);
            }
        }).catch(() => {
            // Nếu lỗi API thì vẫn cho phép submit (server sẽ validate lại)
            resolve(true);
        });
    });
}

// Hàm kiểm tra kích thước có tồn tại không
function checkSizeNameExists(name, excludeId = null) {
    return new Promise((resolve, reject) => {
        // Tạo URL với query parameters
        let url = `/admin/size/check-duplicate?name=${encodeURIComponent(name)}`;
        if (excludeId) {
            url += `&excludeId=${excludeId}`;
        }

        $.ajax({
            url: url,
            method: "GET",
            success: function(response) {
                resolve(response.exists || false);
            },
            error: function() {
                reject(new Error("Không thể kiểm tra trùng kích thước"));
            }
        });
    });
}

// === XỬ LÝ THÊM KÍCH THƯỚC ===
async function handleAddSize() {
    const isValid = await validateSizeName(false);
    if (!isValid) return;

    SwalUtils.confirm("Xác nhận thêm kích thước?", "", "Thêm", "Hủy")
        .then((result) => {
            if (!result.isConfirmed) return;

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
                    SwalUtils.success("Thành công!", response.message);
                    $("#myModal").modal("hide");

                    $.get("/admin/size/counts").done(function (totalItems) {
                        const pageSize = 5;
                        const lastPage = Math.max(0, Math.ceil(totalItems / pageSize) - 1);
                        doSearchSize(lastPage);
                    });
                },
                error: function (xhr) {
                    SwalUtils.error("Lỗi!", xhr.responseJSON?.message || "Thêm thất bại");
                },
            });
        });
}

// === XỬ LÝ CẬP NHẬT ===
async function handleUpdateSize() {
    const isValid = await validateSizeName(true);
    if (!isValid) return;

    SwalUtils.confirm("Xác nhận cập nhật kích thước", "", "Cập nhật", "Hủy")
        .then((result) => {
            if (!result.isConfirmed) return;

            const formData = new FormData();
            formData.append("sizeCode", $("#code").val().trim());
            formData.append("name", $("#name").val().trim());
            const sizeId = $("#id").val();
            const savedPage = $("#sizeForm").data("current-page") || getCurrentPage() || currentSizePage || 0;

            $.ajax({
                url: `/admin/size/${sizeId}`,
                type: "PUT",
                data: formData,
                processData: false,
                contentType: false,
                success: function (response) {
                    SwalUtils.success("Cập nhật thành công!", response.message);
                    $("#myModal").modal("hide");
                    doSearchSize(savedPage);
                },
                error: function (xhr) {
                    SwalUtils.error("Lỗi", xhr.responseJSON?.message || "Cập nhật thất bại");
                },
            });
        });
}

// === LẤY TRANG HIỆN TẠI ===
function getCurrentPage() {
    let page = 0;

    // 1. Từ data attribute
    page = parseInt($("#paginationContainer").attr("data-current-page"));
    if (!isNaN(page)) return page;

    // 2. Từ nút active
    page = parseInt($("#paginationContainer .paginate_button.active a").text()) - 1;
    if (!isNaN(page) && page >= 0) return page;

    // 3. Từ biến global
    if (currentSizePage >= 0) return currentSizePage;

    return 0;
}

// === TÌM KIẾM NỘI BỘ ===
function doSearchSize(page) {
    const keyword = $("#searchInput").val().trim();
    const isActive = $("#statusFilter").val() || null;

    currentSizePage = page || 0;

    $.ajax({
        url: "/admin/size/search",
        type: "GET",
        data: { page: currentSizePage, keyword, isActive },
        success: function (response) {
            $("#sizeTableContainer").html(response);
            $("#paginationContainer").attr("data-current-page", currentSizePage);
        },
        error: function () {
            if (currentSizePage !== 0) {
                currentSizePage = 0;
                doSearchSize(0);
            }
        },
    });
}

// === TOGGLE TRẠNG THÁI ===
function doToggleStatus(userId, isActive) {
    const title = isActive
        ? "Bạn có chắc muốn vô hiệu hóa kích thước này?"
        : "Bạn có chắc muốn kích hoạt kích thước này?";

    SwalUtils.confirm(title, "", "Xác nhận", "Hủy", {
        icon: "warning",
        customClass: { popup: "swal-pop-zindex" },
        backdrop: `rgba(0, 0, 0, 0.4)`,
    }).then((result) => {
        if (!result.isConfirmed) return;

        const currentPage = getCurrentPage();
        currentSizePage = currentPage;

        $.ajax({
            url: `/admin/size/${userId}/toggle-status`,
            type: "PUT",
            success: function (data) {
                SwalUtils.success("Thành công", data.message);
                doSearchSize(currentSizePage);
            },
            error: function (xhr) {
                SwalUtils.error("Lỗi", xhr.responseJSON?.message || "Có lỗi xảy ra");
            },
        });
    });
}

// === PUBLIC FUNCTIONS ===
window.searchSize = function (page) { doSearchSize(page); };
window.openAddModal = openAddModal;
window.toggleStatus = function (userId, isActive) { doToggleStatus(userId, isActive); };



    // === FORCE BIND MODAL EVENTS ===
    function bindModalEvents() {
        if ($("#btnAddSize").length > 0) {
            $("#btnAddSize").off('click.size').on('click.size', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleAddSize();
            });
        }

        if ($("#btnUpdate").length > 0) {
            $("#btnUpdate").off('click.size').on('click.size', function (e) {
                e.preventDefault();
                e.stopPropagation();
                handleUpdateSize();
            });
        }
    }

    // === FUNCTION ĐỢI jQuery VÀ KHỞI TẠO ===
    function initializeSizeModule() {
        if (typeof jQuery === 'undefined' || typeof $ === 'undefined') {
            return;
        }

        if (typeof SwalUtils === 'undefined') {
            setTimeout(initializeSizeModule, 500);
            return;
        }

        setupEventListeners();
    }

    // === SETUP EVENT LISTENERS ===
    function setupEventListeners() {
        $(document).off('click.sizeModule').on("click.sizeModule", "#btnAddSize", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddSize();
        });

        $(document).off('click.sizeModule').on("click.sizeModule", "#btnUpdate", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateSize();
        });

        $(document).off('click.sizeModalModule').on("click.sizeModalModule", "#myModal #btnAddSize", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleAddSize();
        });

        $(document).off('click.sizeModalModule').on("click.sizeModalModule", "#myModal #btnUpdate", function (e) {
            e.preventDefault();
            e.stopPropagation();
            handleUpdateSize();
        });

        $(document).on('click.sizePagination', '.pagination a', function (e) {
            const pageText = $(this).text();
            if (!isNaN(pageText)) {
                currentSizePage = parseInt(pageText) - 1;
            }
        });

        $(document).on('click.sizeSearch', '[onclick*="searchSize"]', function () {
            const onclickAttr = $(this).attr('onclick');
            const match = onclickAttr.match(/searchSize\((\d+)\)/);
            if (match) {
                currentSizePage = parseInt(match[1]);
            }
        });

        $(document).on("change.sizeFilter", "#statusFilter", function () {
            doSearchSize(0);
        });

        $(document).on("keypress.sizeSearch", "#searchInput", function (e) {
            if (e.which === 13) {
                doSearchSize(0);
            }
        });

        $('#myModal').off('shown.bs.modal.sizeModule').on('shown.bs.modal.sizeModule', function () {
            setTimeout(function() {
                bindModalEvents();
            }, 100);
        });
    }

    // === WAIT FOR DEPENDENCIES AND INITIALIZE ===
    if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
        $(document).ready(function() {
            initializeSizeModule();
        });
    } else {
        function waitForJQuery() {
            if (typeof jQuery !== 'undefined' && typeof $ !== 'undefined') {
                $(document).ready(function() {
                    initializeSizeModule();
                });
            } else {
                setTimeout(waitForJQuery, 100);
            }
        }
        waitForJQuery();
    }

})(); // Đóng IIFE
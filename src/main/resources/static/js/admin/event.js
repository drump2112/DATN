$(document).ready(function () {
    $("#btnAddEvent").click(function () {
        $("#modalEvent").modal("show");
        $("#modalEvent .modal-title").html("Thêm Sự Kiện Giảm Giá");
        // $("#modalEvent .modal-body").load("/admin/voucher/event-form");
    });
});

$(document).ready(function () {
    $("#btnAddVoucher").click(function () {
        $("#modalVoucher").modal("show");
        $("#modalVoucher .modal-title").html("Thêm Voucher");
        // $("#modalVoucher .modal-body").load("/admin/voucher/form");
    });
});

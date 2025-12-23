window.addProductToEvent = function (button) {
    const productId = $(button).data("id");
    const eventId = $(button).data("idevent");
    console.log(productId, eventId);

    Swal.fire({
        title: "Xác nhận thêm sản phẩm này vào sự kiện giảm giá",
        icon: "warning",
        showCancelButton: true,
        showConfirmButton: "Xác nhận",
        cancelButtonText: true,
    }).then(result => {
        if (result) {
            $.ajax({
                url: `/admin/event/add-product`,
                method: "Put",
                contentType: "application/json",
                beforeSend: function () {
                    $(button).prop("disabled", true);
                },
                data: JSON.stringify({productId, eventId}),
                success: function (response) {
                    Swal.fire("Thành công!", data.message, "success");
                    // Làm mới danh sách (tùy bạn dùng)
                    // searchEvent($("#paginationContainer").data("current-page") || 0);
                },
                error: function (xhr) {
                    Swal.fire("Lỗi!", xhr.responseJSON?.message || "Có lỗi xảy ra!", "error");
                },
                complete: function () {
                    $(button).prop("disabled", false);
                }
            })
        }
    })
};
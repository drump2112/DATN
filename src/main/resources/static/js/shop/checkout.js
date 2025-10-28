$(document).ready(function () {
    // Load giỏ hàng

    let cartItems = [];

    function loadCheckoutItems() {

        $.get("/cart/items")
            .done(function (cart) {
                cartItems = cart;
                renderCheckoutItems(cart);
                calculateTotals(cart);
            })
            .fail(function (xhr) {
                console.error("Error loading cart:", xhr);
            });
    }

    // Hiển thị sản phẩm trong giỏ hàng
    function renderCheckoutItems(cart) {
        const $tbody = $(".table-hover tbody");
        $tbody.empty();

        cart.forEach((item) => {
            $tbody.append(`
        <tr>
          <td width="90">
            <img src="${item.image}" alt="${item.name}" style="width:80px;height:auto;"/>
          </td>
          <td>
            <h4>${item.name}</h4>
            <p><small>Màu: ${item.colorName} | Size: ${item.sizeName}</small></p>
          </td>
          <td>${item.price.toLocaleString()} VNĐ</td>
          <td>${item.quantity}</td>
          <td>${(item.price * item.quantity).toLocaleString()} VNĐ</td>
        </tr>
      `);
        });
    }

    let appliedVoucher = null;

    function calculateTotals(cart) {
        const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        const shipping = 30000;
        const discount = appliedVoucher ? appliedVoucher.discount : 0;
        const total = Math.max(0, subtotal + shipping - discount);

        // Cập nhật giá trị hiển thị
        $("#finalTotal").text(subtotal.toLocaleString() + " VNĐ");
        $("#shippingValue").text(shipping.toLocaleString() + " VNĐ");
        $("#discountValue").text(discount.toLocaleString() + " VNĐ");
        $("#totalPayment").text(total.toLocaleString() + " VNĐ");
        $("#appliedVoucherCode").val(appliedVoucher ? appliedVoucher.code : "");

        if (discount > 0) {
            $("#clearVoucherInline").show();
        } else {
            $("#clearVoucherInline").hide();
        }
    }

    function loadVouchers(subtotal) {
        $.get("/api/vouchers/available", { orderTotal: subtotal })
            .done(function (data) {
                renderVouchers(data, subtotal);
            })
            .fail(function () {
                const sample = [
                    {
                        code: "SALE10",
                        discountType: "PERCENT",
                        discountValue: 10,
                        maxDiscountValue: 50000,
                        minOrderAmount: 100000,
                        name: "Giảm 10% (tối đa 50k)",
                        discountAmount: 50000,
                    },
                    {
                        code: "FLAT50",
                        discountType: "AMOUNT",
                        discountValue: 50000,
                        minOrderAmount: 200000,
                        name: "Giảm 50.000 VNĐ",
                        discountAmount: 50000,
                    },
                    {
                        code: "WELCOME20",
                        discountType: "PERCENT",
                        discountValue: 20,
                        maxDiscountValue: 100000,
                        minOrderAmount: 300000,
                        name: "Giảm 20% (tối đa 100k)",
                        discountAmount: 100000,
                    },
                ];
                renderVouchers(sample, subtotal);
            });
    }

    function renderVouchers(vouchers, subtotal) {
        const $grid = $("#voucherGrid");
        if ($grid.length === 0) return;
        $grid.empty();

        let best = null;
        vouchers.forEach((v) => {
            if (!best || v.discountAmount > best.discountAmount) best = v;
        });

        vouchers.forEach((v) => {
            const disabled = v.discountAmount <= 0;
            const isBest = best && v.code === best.code;

            const card = $(`
        <div class="voucher-card ${disabled ? "disabled" : ""}" data-code="${v.code}">
          ${isBest ? '<span class="best-badge">Gợi ý tốt nhất</span>' : ""}
          <div class="voucher-header mb-2">
            <b>${v.code}</b>
            <div class="small text-muted">${v.name || ""}</div>
            ${v.minOrderAmount
                    ? `<div class='small text-muted'>Yêu cầu tối thiểu: ${v.minOrderAmount.toLocaleString()} VNĐ</div>`
                    : ""
                }
          </div>
          <div class="voucher-footer d-flex justify-content-between align-items-center">
            <span class="badge">${v.discountAmount.toLocaleString()} VNĐ</span>
            <button class="btn btn-sm btn-success apply-voucher" ${disabled ? "disabled" : ""}>Áp dụng</button>
          </div>
        </div>
      `);

            $grid.append(card);
        });

        // Sự kiện "Áp dụng"
        $(".apply-voucher")
            .off("click")
            .on("click", function () {
                const $card = $(this).closest(".voucher-card");
                const code = $card.data("code");
                const voucher = vouchers.find((x) => x.code === code);

                if (voucher) {
                    appliedVoucher = {
                        id: voucher.id,
                        code: voucher.code,
                        discount: voucher.discountAmount
                    };
                    $('.voucher-card').removeClass('active');
                    $card.addClass('active');

                    $('#clearVoucherInline').show();

                    $('#suggestionText').text(
                        `Đã áp dụng: ${appliedVoucher.code} — Tiết kiệm ${appliedVoucher.discount.toLocaleString()} VNĐ`
                    );

                    $.get('/cart/items').done(function (cart) {
                        calculateTotals(cart);
                    });
                }

            });

    }

    // Hủy mã
    $('#clearVoucherInline').off('click').on('click', function () {
        appliedVoucher = null;
        $('#discountValue').text('0 VNĐ');
        $('#clearVoucherInline').hide();
        $('#voucherList li').removeClass('active');
        $('#suggestionText').text('Không có mã phù hợp');
        $.get('/cart/items').done(function (cart) { calculateTotals(cart); });
    });

    // Mở modal chọn mã
    $("#openVoucherModal").click(function () {
        $("#voucherModal").modal("show");

        setTimeout(() => {
            $(".voucher-card").removeClass("selected");
            if (appliedVoucher) {
                $(`.voucher-card[data-code="${appliedVoucher.code}"]`).addClass("selected");
            }
        }, 100);
    });

    // ===== ĐẶT HÀNG =====
    // Khởi chạy
    loadCheckoutItems();
    $.get("/cart/items")
        .done(function (cart) {
            const subtotal = cart.reduce((s, it) => s + it.price * it.quantity, 0);
            loadVouchers(subtotal);
        })
        .fail(function () {
            loadVouchers(0);
        });

    function enableEdit(id) {
        const span = document.getElementById(id);
        const parent = span.parentNode;
        const icon = parent.querySelector(".edit-btn");

        const currentValue = span.textContent.trim();
        const input = document.createElement("input");
        input.type = "text";
        input.value = currentValue;
        input.className = "edit-input form-control";

        span.style.display = "none";
        if (icon) icon.style.display = "none";

        input.addEventListener("blur", function () {
            span.textContent = input.value;
            span.style.display = "inline";
            if (icon) icon.style.display = "inline-block";
            input.remove();
        });

        parent.insertBefore(input, span);
        input.focus();
    }

    function enableTextareaEdit(id) {
        const textarea = document.getElementById(id);
        const parent = textarea.parentNode;
        const icon = parent.querySelector(".edit-btn");

        textarea.removeAttribute("readonly");
        textarea.focus();

        if (icon) icon.style.display = "none";

        textarea.addEventListener("blur", function handleBlur() {
            textarea.setAttribute("readonly", true);
            if (icon) icon.style.display = "inline-block";
            textarea.removeEventListener("blur", handleBlur);
        });
    }

    function parseMoney(str) {
        return parseFloat(str.replace(/[^\d]/g, "")) || 0;
    }

    $("#btnConfirmOrder").on("click", function () {
        Swal.fire({
            title: "Xác nhận đặt hàng?",
            text: "Bạn có chắc chắn muốn đặt đơn hàng này không?",
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Đặt hàng",
            cancelButtonText: "Hủy",
            reverseButtons: true
        }).then((result) => {
            if (result.isConfirmed) {
                const orderData = {
                    userId: $("#userId").val(),
                    customerName: $("#userFullName").text().trim(),
                    paymentMethod: $("input[name='paymentMethod']:checked").val(),
                    voucherCode: $("#appliedVoucherCode").val() || null,
                    shippingFee: parseMoney($("#shippingValue").text()),
                    totalAmount: parseMoney($("#totalAmount").text()),
                    discountAmount: parseMoney($("#discountValue").text()),
                    finalAmount: parseMoney($("#totalPayment").text()),
                    shippingAddress: $("#shippingAddress").val(),
                    shippingPhone: $("#shippingPhone").text().trim(),
                    voucherId: appliedVoucher ? appliedVoucher.id : null,
                    items: typeof cartItems !== "undefined" ? cartItems : []
                };

                $.ajax({
                    url: "/api/orders/create",
                    type: "POST",
                    contentType: "application/json",
                    data: JSON.stringify(orderData),
                    success: function (res) {
                        if (res.status === "WAITING_OTP") {
                            // Hiển thị thông báo đã gửi OTP
                            Swal.fire({
                                title: "OTP đã được gửi!",
                                text: "Vui lòng kiểm tra email của bạn để lấy mã OTP.",
                                icon: "info",
                                confirmButtonText: "OK"
                            }).then(() => {
                                // Lưu thông tin và mở modal OTP
                                $("#otpOrderId").val(res.orderId);
                                $("#otpEmail").val(res.email);
                                $("#otpCode").val("");
                                $("#otpModal").modal("show");
                            });
                        } else if (res.status === "SUCCESS") {
                            // Đơn hàng thành công (thanh toán chuyển khoản)
                            Swal.fire({
                                title: "Thành công!",
                                text: "Đơn hàng của bạn đã được đặt thành công.",
                                icon: "success",
                                confirmButtonText: "OK"
                            }).then(() => {
                                window.location.href = "/orders";
                            });
                        }
                    },
                    error: function (err) {
                        console.error(err);
                        const errorMsg = err.responseJSON?.message || "Đặt hàng thất bại. Vui lòng thử lại.";
                        Swal.fire({
                            title: "Lỗi!",
                            text: errorMsg,
                            icon: "error",
                            confirmButtonText: "Đóng"
                        });
                    }
                });
            }
        });
    });    $("#btnVerifyOtp").on("click", function () {
        const orderId = $("#otpOrderId").val();
        const email = $("#otpEmail").val();
        const otp = $("#otpCode").val();

        if (!otp || otp.length !== 6) {
            Swal.fire({
                title: "Thông báo",
                text: "Vui lòng nhập mã OTP gồm 6 ký tự.",
                icon: "warning",
                confirmButtonText: "Đóng"
            });
            return;
        }

        $.ajax({
            url: "/api/orders/confirm-otp",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({ orderId, email, otp }),
            success: function (res) {
                if (res.success) {
                    $("#otpModal").modal("hide");
                    Swal.fire({
                        title: "Thành công!",
                        text: "Đơn hàng của bạn đã được xác nhận thành công.",
                        icon: "success",
                        confirmButtonText: "OK"
                    }).then(() => {
                        window.location.href = "/orders";
                    });
                } else {
                    Swal.fire({
                        title: "Sai OTP",
                        text: res.message || "Mã OTP không hợp lệ hoặc đã hết hạn.",
                        icon: "error",
                        confirmButtonText: "Đóng"
                    });
                }
            },
            error: function (err) {
                console.error(err);
                const errorMsg = err.responseJSON?.message || "Không thể xác nhận OTP. Vui lòng thử lại.";
                Swal.fire({
                    title: "Lỗi!",
                    text: errorMsg,
                    icon: "error",
                    confirmButtonText: "Đóng"
                });
            }
        });
    });


    window.enableTextareaEdit = enableTextareaEdit;
    window.enableEdit = enableEdit;
});

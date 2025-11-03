$(document).ready(function () {
    // Chỉ chạy khi đang ở trang checkout
    if ($("#btnConfirmOrder").length === 0) {
        return;
    }

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

    // ===== PAYMENT METHOD HANDLING =====
    // Xử lý radio button trong payment methods
    $('.payment-radio-label input[type="radio"]').change(function() {
        // Remove active state from all payment options
        $('.payment-option').removeClass('active');

        // Add active state to selected payment option
        if (this.checked) {
            $(this).closest('.payment-option').addClass('active');

            // Update button text based on payment method
            const buttonText = $('#btnOrderText');
            const buttonIcon = $('#btnConfirmOrder i');

            switch($(this).val()) {
                case 'CASH':
                    buttonText.text('Đặt Hàng (COD)');
                    buttonIcon.attr('class', 'fa fa-money');
                    break;
                case 'TRANSFER':
                    buttonText.text('Đặt Hàng (Chuyển khoản)');
                    buttonIcon.attr('class', 'fa fa-university');
                    break;
                case 'VNPAY':
                    buttonText.text('Thanh toán VNPay');
                    buttonIcon.attr('class', 'fa fa-credit-card');
                    break;
                default:
                    buttonText.text('Đặt Hàng');
                    buttonIcon.attr('class', 'fa fa-shopping-cart');
            }
        }

        console.log('Payment method selected:', $(this).val());
    });

    // Trigger change event for initially checked radio
    $('.payment-radio-label input[type="radio"]:checked').trigger('change');

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
        const $btn = $(this);

        // Kiểm tra phương thức thanh toán được chọn
        const paymentMethod = $("input[name='paymentMethod']:checked").val();

        if (!paymentMethod) {
            SwalUtils.warning("Chưa chọn phương thức thanh toán", "Vui lòng chọn phương thức thanh toán");
            return;
        }

        // Kiểm tra giỏ hàng có sản phẩm không
        if (!cartItems || cartItems.length === 0) {
            SwalUtils.warning("Giỏ hàng trống", "Vui lòng thêm sản phẩm vào giỏ hàng");
            return;
        }

        // Customize confirm message based on payment method
        let confirmTitle = "Xác nhận đặt hàng?";
        let confirmText = "Bạn có chắc chắn muốn đặt đơn hàng này không?";

        if (paymentMethod === 'VNPAY') {
            confirmTitle = "Xác nhận thanh toán VNPay?";
            confirmText = "Bạn sẽ được chuyển đến trang thanh toán VNPay để hoàn tất giao dịch.";
        }

        SwalUtils.confirm(
            confirmTitle,
            confirmText,
            "Xác nhận",
            "Hủy"
        ).then((result) => {
            if (result.isConfirmed) {
                // Show loading state
                $btn.prop('disabled', true);
                const originalText = $('#btnOrderText').text();
                $('#btnOrderText').text('Đang xử lý...');
                $btn.find('i').attr('class', 'fa fa-spinner fa-spin');

                // Lấy thông tin địa chỉ giao hàng
                let shippingAddressData = getShippingAddressData();

                const orderData = {
                    userId: $("#userId").val(),
                    customerName: $("#userFullName").text().trim(),
                    paymentMethod: $("input[name='paymentMethod']:checked").val(),
                    voucherCode: $("#appliedVoucherCode").val() || null,
                    shippingFee: parseMoney($("#shippingValue").text()),
                    totalAmount: parseMoney($("#totalAmount").text()),
                    discountAmount: parseMoney($("#discountValue").text()),
                    finalAmount: parseMoney($("#totalPayment").text()),
                    shippingAddress: shippingAddressData.fullAddress,
                    shippingProvinceCode: shippingAddressData.provinceCode,
                    shippingCommuneCode: shippingAddressData.communeCode,
                    shippingSpecificAddress: shippingAddressData.specificAddress,
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
                            SwalUtils.info(
                                "OTP đã được gửi!",
                                "Vui lòng kiểm tra email của bạn để lấy mã OTP."
                            ).then(() => {
                                $("#otpOrderId").val(res.orderId);
                                $("#otpEmail").val(res.email);
                                $("#otpCode").val("");
                                $("#otpModal").modal("show");
                            });
                        } else if (res.status === "SUCCESS") {
                            clearCartAndRedirect(res.orderId, "Đơn hàng của bạn đã được đặt thành công.");
                        } else if (res.status === "VNPAY_REDIRECT") {
                            const paymentUrl = res.paymentUrl || res.vnpayUrl;
                            if (paymentUrl) {
                                SwalUtils.info(
                                    "Chuyển hướng thanh toán",
                                    "Đang chuyển hướng đến VNPay..."
                                );
                                setTimeout(() => {
                                    window.location.href = paymentUrl;
                                }, 1500);
                            } else {
                                console.error("VNPay response:", res);
                                SwalUtils.error("Lỗi!", "Không thể tạo liên kết thanh toán VNPay. Vui lòng thử lại.");
                            }
                        }
                    },
                    error: function (err) {
                        console.error("Order creation error:", err);
                        let errorMsg = "Đặt hàng thất bại. Vui lòng thử lại.";

                        if (err.responseJSON && err.responseJSON.message) {
                            errorMsg = err.responseJSON.message;

                            // Nếu lỗi VNPay, suggest fallback
                            if (errorMsg.includes("VNPay") || errorMsg.includes("thanh toán")) {
                                errorMsg += "\n\nBạn có thể chọn phương thức 'Thanh toán khi nhận hàng' để hoàn tất đơn hàng.";
                            }
                        }

                        SwalUtils.error("Lỗi!", errorMsg);
                    },
                    complete: function() {
                        // Restore button state
                        $btn.prop('disabled', false);
                        $('#btnOrderText').text(originalText);
                        // Restore icon based on payment method
                        $('.payment-radio-label input[type="radio"]:checked').trigger('change');
                    }
                });
            }
        });
    });

    // Helper function to clear cart and redirect
    function clearCartAndRedirect(orderId, message) {
        $.post("/cart/clear").always(function() {
            // Cập nhật icon giỏ hàng
            if (typeof window.updateCartCount === 'function') {
                window.updateCartCount();
            } else {
                $("#cart-count").text(0);
            }
            SwalUtils.success("Thành công!", message).then(() => {
                window.location.href = "/orders/thank-you?orderId=" + orderId;
            });
        });
    }

    $("#btnVerifyOtp").on("click", function () {
        const orderId = $("#otpOrderId").val();
        const email = $("#otpEmail").val();
        const otp = $("#otpCode").val();

        if (!otp || otp.length !== 6) {
            SwalUtils.warning("Thông báo", "Vui lòng nhập mã OTP gồm 6 ký tự.");
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
                    clearCartAndRedirect(orderId, "Đơn hàng của bạn đã được xác nhận thành công.");
                } else {
                    SwalUtils.error(
                        "Sai OTP",
                        res.message || "Mã OTP không hợp lệ hoặc đã hết hạn."
                    );
                }
            },
            error: function (err) {
                console.error(err);
                const errorMsg = err.responseJSON?.message || "Không thể xác nhận OTP. Vui lòng thử lại.";
                SwalUtils.error("Lỗi!", errorMsg);
            }
        });
    });


    window.enableTextareaEdit = enableTextareaEdit;
    window.enableEdit = enableEdit;

    // === Xử lý địa chỉ giao hàng ===
    loadShippingProvinces();

    // Sự kiện thay đổi loại địa chỉ với animation
    $('input[name="addressType"]').change(function() {
        if ($(this).val() === 'new') {
            $('#newAddressFields').slideDown(400);
            $('#shippingProvince').prop('required', true);
            $('#shippingCommune').prop('required', true);
            $('#shippingSpecificAddress').prop('required', true);
        } else {
            $('#newAddressFields').slideUp(400);
            $('#shippingFeePreview').fadeOut(300);
            $('#shippingProvince').prop('required', false);
            $('#shippingCommune').prop('required', false);
            $('#shippingSpecificAddress').prop('required', false);
            // Reset giá phí ship về mặc định khi dùng địa chỉ hiện tại
            calculateShippingFee(null, null);
        }
    });    // Sự kiện thay đổi tỉnh để load phường/xã và tính phí ship
    $('#shippingProvince').change(function() {
        const provinceCode = $(this).val();
        const communeSelect = $('#shippingCommune');

        communeSelect.empty().append('<option value="">-- Chọn Phường/Xã --</option>');

        if (provinceCode) {
            loadShippingCommunes(provinceCode);
            communeSelect.prop('disabled', false);
        } else {
            communeSelect.prop('disabled', true);
        }
    });

    // Sự kiện thay đổi phường/xã để tính phí ship
    $('#shippingCommune').change(function() {
        const provinceCode = $('#shippingProvince').val();
        const communeCode = $(this).val();

        if (provinceCode && communeCode) {
            calculateShippingFee(provinceCode, communeCode);
        }
    });
});

// === Các function xử lý địa chỉ ===
function toggleAddressEdit() {
    const editForm = $('#addressEditForm');
    const currentView = $('#currentAddressView');

    if (editForm.is(':visible')) {
        editForm.slideUp(400, function() {
            currentView.fadeIn(300);
        });
    } else {
        currentView.fadeOut(300, function() {
            editForm.slideDown(400);
        });
        // Reset form
        $('input[name="addressType"][value="current"]').prop('checked', true);
        $('#newAddressFields').hide();
        $('#shippingFeePreview').hide();
        $('#shippingAddressForm')[0].reset();
    }
}function cancelAddressEdit() {
    $('#addressEditForm').slideUp(400, function() {
        $('#currentAddressView').fadeIn(300);
    });
}

function loadShippingProvinces() {
    $.get('/api/provinces')
        .done(function(provinces) {
            const select = $('#shippingProvince');
            select.empty().append('<option value="">-- Chọn Tỉnh/Thành phố --</option>');

            provinces.forEach(province => {
                select.append(`<option value="${province.code}">${province.name}</option>`);
            });
        })
        .fail(function(xhr) {
            console.error('Error loading provinces:', xhr);
            SwalUtils.error('Lỗi!', 'Không thể tải danh sách tỉnh/thành phố.');
        });
}

function loadShippingCommunes(provinceCode) {
    $.get(`/api/communes?provinceCode=${provinceCode}`)
        .done(function(communes) {
            const select = $('#shippingCommune');
            select.empty().append('<option value="">-- Chọn Phường/Xã --</option>');

            if (communes && communes.length > 0) {
                communes.forEach(commune => {
                    select.append(`<option value="${commune.code}">${commune.name}</option>`);
                });
            }
        })
        .fail(function(xhr) {
            console.error('Error loading communes:', xhr);
            const select = $('#shippingCommune');
            select.empty().append('<option value="">Lỗi tải dữ liệu</option>');
        });
}

function calculateShippingFee(provinceCode, communeCode) {
    let shippingFee = 30000; // Phí mặc định

    if (provinceCode && communeCode) {
        // Lấy thông tin đơn hàng để tính phí chính xác
        const totalValue = parseFloat($('#totalAmount').text().replace(/[^\d]/g, '')) || 0;
        const estimatedWeight = calculateEstimatedWeight(); // Ước tính trọng lượng từ giỏ hàng

        // Gọi API GHN tính phí ship
        const params = new URLSearchParams({
            provinceCode: provinceCode,
            communeCode: communeCode,
            weight: estimatedWeight,
            totalValue: totalValue
        });

        $.get(`/api/shipping-fee?${params.toString()}`)
            .done(function(response) {
                shippingFee = response.fee || 30000;
                console.log(`GHN shipping fee: ${shippingFee} VNĐ for ${provinceCode}-${communeCode}`);
                updateShippingFee(shippingFee);
                updateShippingFeePreview(shippingFee);
            })
            .fail(function(xhr) {
                console.warn('Error calculating shipping fee with GHN, using fallback:', xhr);
                updateShippingFee(30000);
                updateShippingFeePreview(30000);
            });
    } else {
        // Sử dụng phí mặc định
        updateShippingFee(30000);
    }
}

// Ước tính trọng lượng dựa trên giỏ hàng
function calculateEstimatedWeight() {
    if (typeof cartItems !== 'undefined' && cartItems.length > 0) {
        // Giả sử mỗi sản phẩm khoảng 200-500g tùy loại
        const totalQuantity = cartItems.reduce((sum, item) => sum + item.quantity, 0);
        return Math.max(500, totalQuantity * 300); // Tối thiểu 500g, mỗi món 300g
    }
    return 500; // Mặc định 500g
}function updateShippingFee(fee) {
    $('#shippingValue').text(fee.toLocaleString() + ' VNĐ');

    // Tính lại tổng thanh toán
    const subtotal = parseFloat($('#totalAmount').text().replace(/[^\d]/g, '')) || 0;
    const discount = parseFloat($('#discountValue').text().replace(/[^\d]/g, '')) || 0;
    const total = subtotal + fee - discount;

    $('#totalPayment').text(total.toLocaleString() + ' VNĐ');
}

function getShippingAddressData() {
    const addressType = $('input[name="addressType"]:checked').val();

    if (addressType === 'new') {
        // Sử dụng địa chỉ mới
        const provinceCode = $('#shippingProvince').val();
        const communeCode = $('#shippingCommune').val();
        const specificAddress = $('#shippingSpecificAddress').val().trim();
        const provinceName = $('#shippingProvince option:selected').text();
        const communeName = $('#shippingCommune option:selected').text();
        const fullAddress = `${specificAddress}, ${communeName}, ${provinceName}`;

        return {
            fullAddress: fullAddress,
            provinceCode: provinceCode,
            communeCode: communeCode,
            specificAddress: specificAddress
        };
    } else {
        // Sử dụng địa chỉ hiện tại
        return {
            fullAddress: $('#currentFullAddress').text(),
            provinceCode: null,
            communeCode: null,
            specificAddress: null
        };
    }
}

function saveShippingAddress() {
    const addressType = $('input[name="addressType"]:checked').val();

    if (addressType === 'new') {
        // Validate form cho địa chỉ mới
        const provinceCode = $('#shippingProvince').val();
        const communeCode = $('#shippingCommune').val();
        const specificAddress = $('#shippingSpecificAddress').val().trim();

        if (!provinceCode) {
            SwalUtils.error('Lỗi!', 'Vui lòng chọn Tỉnh/Thành phố');
            return;
        }

        if (!communeCode) {
            SwalUtils.error('Lỗi!', 'Vui lòng chọn Phường/Xã');
            return;
        }

        if (!specificAddress) {
            SwalUtils.error('Lỗi!', 'Vui lòng nhập địa chỉ cụ thể');
            return;
        }

        // Tạo địa chỉ đầy đủ để hiển thị
        const provinceName = $('#shippingProvince option:selected').text();
        const communeName = $('#shippingCommune option:selected').text();
        const fullAddress = `${specificAddress}, ${communeName}, ${provinceName}`;

        // Cập nhật hiển thị địa chỉ giao hàng
        $('#currentFullAddress').text(fullAddress);

        // Tính phí ship cho địa chỉ mới
        calculateShippingFee(provinceCode, communeCode);

        SwalUtils.success('Thành công!', 'Đã cập nhật địa chỉ giao hàng');
    }

    // Ẩn form chỉnh sửa
    cancelAddressEdit();
}

// Cập nhật preview phí ship trong form
function updateShippingFeePreview(fee) {
    const preview = $('#shippingFeePreview');
    const feeAmount = $('#previewFeeAmount');

    if (fee && fee > 0) {
        feeAmount.text(fee.toLocaleString() + ' VNĐ');
        preview.fadeIn(300);
    } else {
        preview.fadeOut(300);
    }
}

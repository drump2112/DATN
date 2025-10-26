
$(document).ready(function () {
    // Load giỏ hàng
    function loadCheckoutItems() {
        $.get("/cart/items")
            .done(function (cart) {
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

    // ===== VOUCHER LOGIC =====
    let appliedVoucher = null;

    function calculateTotals(cart) {
        const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        const shipping = 30000;
        const discount = appliedVoucher ? appliedVoucher.discount : 0;
        const total = Math.max(0, subtotal + shipping - discount);

        // Cập nhật giá trị hiển thị
        $("#finalTotal").text(subtotal.toLocaleString() + " VNĐ");
        $("#shippingValue").text(shipping.toLocaleString() + " VNĐ");
        $("#discountValue").text(
            (discount > 0 ? "-" : "") + discount.toLocaleString() + " VNĐ"
        );
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
                    appliedVoucher = { code: voucher.code, discount: voucher.discountAmount };
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
    $(".btn-primary.dim").click(function () {
        const address = $("#deliveryAddress").val();
        if (!address) {
            alert("Vui lòng nhập địa chỉ giao hàng!");
            return;
        }

        const paymentMethod = $('input[name="paymentMethod"]:checked').val();
        if (!paymentMethod) {
            alert("Vui lòng chọn phương thức thanh toán!");
            return;
        }

        // createOrder(address, paymentMethod);
    });

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

    window.enableTextareaEdit = enableTextareaEdit;
    window.enableEdit = enableEdit;
});

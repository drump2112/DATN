$(document).ready(function () {
    // Chỉ chạy khi đang ở trang checkout
    if ($("#btnConfirmOrder").length === 0) {
        return;
    }

    // Load giỏ hàng
    let cartItems = [];

    // OTP state management
    let otpState = {
        isWaiting: false,
        orderId: null,
        email: null,
        startTime: null,
        duration: 10 * 60 * 1000, // 10 minutes in milliseconds
        timer: null
    };

    function loadCheckoutItems() {

        $.get("/cart/items")
            .done(function (cart) {
                cartItems = cart;
                renderCheckoutItems(cart);
                calculateTotals(cart);

                // Thiết lập trạng thái ban đầu cho voucher area
                initializeVoucherState();
            })
            .fail(function (xhr) {
                console.error("Error loading cart:", xhr);
            });
    }

    // Thiết lập trạng thái ban đầu cho voucher area
    function initializeVoucherState() {
        if (!appliedVoucher) {
            $("#appliedVoucherArea").hide();
            $("#defaultDiscountText").show();
        } else {
            $("#appliedVoucherArea").show();
            $("#defaultDiscountText").hide();
        }
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
    let suggestedVoucher = null; // Biến lưu voucher được gợi ý

    // === VOUCHER SUGGESTION FUNCTIONS ===

    // Hàm load voucher tốt nhất khi trang được tải
    function loadBestVoucherSuggestion() {
        const subtotal = parseFloat($("#totalAmount").text().replace(/[^\d]/g, '')) || 0;
        console.log('Loading voucher suggestion for subtotal:', subtotal);

        // Gọi API để lấy voucher tốt nhất
        $.ajax({
            url: '/api/vouchers/available',
            method: 'GET',
            data: { orderTotal: subtotal },
            success: function(vouchers) {
                console.log('Available vouchers:', vouchers);
                if (vouchers && vouchers.length > 0) {
                    // Tìm voucher tốt nhất giống logic trong modal
                    let best = null;
                    vouchers.forEach((v) => {
                        if (v.discountAmount > 0 && (!best || v.discountAmount > best.discountAmount)) {
                            best = v;
                        }
                    });

                    if (best) {
                        console.log('Best voucher found:', best);
                    // Convert format để phù hợp với function showVoucherSuggestion
                        const suggestionVoucher = {
                            id: best.id,
                            code: best.code,
                            discountType: best.discountType,
                            discountValue: best.discountValue,
                            maxDiscountAmount: best.maxDiscountValue || best.maxDiscountAmount,
                            minOrderAmount: best.minOrderAmount,
                            discountAmount: best.discountAmount
                        };
                        showVoucherSuggestion(suggestionVoucher);
                    }
                } else {
                    // Fallback với sample data
                    useFallbackVoucher(subtotal);
                }
            },
            error: function(xhr) {
                console.log('Không thể tải voucher, dùng fallback:', xhr);
                useFallbackVoucher(subtotal);
            }
        });
    }

    // Function riêng cho fallback voucher - không sử dụng fake data nữa
    function useFallbackVoucher(subtotal) {
        // Không hiển thị gợi ý voucher nếu không có dữ liệu thực từ API
        console.log('No vouchers available from API for subtotal:', subtotal);
        hideVoucherSuggestion();
    }

    // Hàm hiển thị gợi ý voucher
    function showVoucherSuggestion(voucher) {
        suggestedVoucher = voucher;

        // Hiển thị phần gợi ý
        const voucherCode = $("#suggestedVoucherCode");
        const voucherDesc = $("#suggestedVoucherDesc");

        voucherCode.text(voucher.code);

        // Tạo mô tả voucher - xử lý cả PERCENT và PERCENTAGE
        let description = '';
        const discountType = voucher.discountType || voucher.type;

        if (discountType === 'PERCENTAGE' || discountType === 'PERCENT') {
            description = `Giảm ${voucher.discountValue}%`;
            const maxDiscount = voucher.maxDiscountAmount || voucher.maxDiscountValue;
            if (maxDiscount && maxDiscount > 0) {
                description += ` (tối đa ${maxDiscount.toLocaleString()} VNĐ)`;
            }
        } else {
            description = `Giảm ${voucher.discountValue.toLocaleString()} VNĐ`;
        }

        if (voucher.minOrderAmount > 0) {
            description += ` cho đơn hàng từ ${voucher.minOrderAmount.toLocaleString()} VNĐ`;
        }

        voucherDesc.text(description);
        $("#voucherSuggestion").fadeIn(400);
        console.log('Voucher suggestion displayed:', voucher);
    }    // Hàm ẩn gợi ý voucher
    function hideVoucherSuggestion() {
        $("#voucherSuggestion").fadeOut(300);
        suggestedVoucher = null;
    }

    // Hàm áp dụng voucher được gợi ý
    function applySuggestedVoucher() {
        if (!suggestedVoucher) return;

        console.log('Applying suggested voucher:', suggestedVoucher);

        // Áp dụng voucher trực tiếp giống logic trong modal
        const discountAmount = suggestedVoucher.discountAmount || calculateDiscountAmount(suggestedVoucher);

        // Cập nhật appliedVoucher global trước
        appliedVoucher = {
            id: suggestedVoucher.id,
            code: suggestedVoucher.code,
            discount: discountAmount
        };

        // Ẩn gợi ý và hiển thị voucher đã áp dụng
        hideVoucherSuggestion();

        // Hiển thị voucher đã áp dụng với format chuẩn
        $("#discountValue").text(discountAmount.toLocaleString() + " VNĐ");
        $("#appliedVoucherArea").show();
        $("#defaultDiscountText").hide();

        // Cập nhật lại totals
        $.get('/cart/items').done(function (cart) {
            calculateTotals(cart);
        });

        // Hiển thị thông báo thành công
        if (typeof SwalUtils !== 'undefined') {
            SwalUtils.success('Thành công!', 'Áp dụng mã giảm giá thành công!');
        } else {
            alert('Áp dụng mã giảm giá thành công!');
        }
    }

    // Hàm tính toán discount amount nếu chưa có
    function calculateDiscountAmount(voucher) {
        if (voucher.discountAmount) return voucher.discountAmount;

        const subtotal = parseFloat($("#totalAmount").text().replace(/[^\d]/g, '')) || 0;
        const discountType = voucher.discountType || voucher.type;

        if (discountType === 'PERCENTAGE' || discountType === 'PERCENT') {
            let discount = subtotal * (voucher.discountValue / 100);
            const maxDiscount = voucher.maxDiscountAmount || voucher.maxDiscountValue;
            if (maxDiscount && discount > maxDiscount) {
                discount = maxDiscount;
            }
            return discount;
        } else {
            return voucher.discountValue;
        }
    }

    // Hàm hiển thị voucher đã áp dụng
    function showAppliedVoucher(discountAmount, voucherCode) {
        // Ẩn gợi ý
        $("#voucherSuggestion").hide();

        // Chỉ cập nhật appliedVoucher nếu chưa có id (để tránh ghi đè id)
        if (!appliedVoucher || !appliedVoucher.id) {
            appliedVoucher = {
                code: voucherCode,
                discount: discountAmount
            };
        } else {
            // Nếu đã có id, chỉ cập nhật discount
            appliedVoucher.discount = discountAmount;
        }

        // Hiển thị voucher đã áp dụng với format chuẩn
        $("#discountValue").text(discountAmount.toLocaleString() + " VNĐ");
        $("#appliedVoucherArea").show();
        $("#defaultDiscountText").hide();
    }

    // Hàm cập nhật tổng đơn hàng khi áp dụng voucher
    function updateOrderSummaryFromVoucher(data) {
        if (data.newTotal) {
            $("#totalPayment").text(formatCurrency(data.newTotal));
        }
        if (data.shippingFee) {
            $("#shippingValue").text(formatCurrency(data.shippingFee));
        }
    }

    // Hàm format tiền tệ
    function formatCurrency(amount) {
        return new Intl.NumberFormat('vi-VN').format(amount) + ' VNĐ';
    }

    function calculateTotals(cart) {
        const subtotal = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);
        // Lấy phí vận chuyển hiện tại (có thể được tính từ GHN hoặc mặc định 30000)
        const currentShipping = parseFloat($("#shippingValue").text().replace(/[^\d]/g, '')) || 30000;
        const discount = appliedVoucher ? appliedVoucher.discount : 0;
        const total = Math.max(0, subtotal + currentShipping - discount);

        // Cập nhật giá trị hiển thị
        $("#totalAmount").text(subtotal.toLocaleString() + " VNĐ");
        // Chỉ update shippingValue nếu nó chưa được set bởi GHN (giá trị mặc định)
        if (!$("#shippingValue").text().match(/\d+/)) {
            $("#shippingValue").text(currentShipping.toLocaleString() + " VNĐ");
        }
        $("#totalPayment").text(total.toLocaleString() + " VNĐ");
        $("#appliedVoucherCode").val(appliedVoucher ? appliedVoucher.code : "");

        // Cập nhật UI phần discount
        if (discount > 0) {
            $("#discountValue").text(discount.toLocaleString() + " VNĐ");
            $("#appliedVoucherArea").show();
            $("#clearVoucherInline").show();
            $("#voucherSuggestion").hide();
        } else {
            $("#discountValue").text("0 VNĐ");
            $("#appliedVoucherArea").hide();
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
                        id: 1,
                        code: "SALE10",
                        discountType: "PERCENT",
                        discountValue: 10,
                        maxDiscountValue: 50000,
                        minOrderAmount: 100000,
                        name: "Giảm 10% (tối đa 50k)",
                        discountAmount: 50000,
                    },
                    {
                        id: 2,
                        code: "FLAT50",
                        discountType: "AMOUNT",
                        discountValue: 50000,
                        minOrderAmount: 200000,
                        name: "Giảm 50.000 VNĐ",
                        discountAmount: 50000,
                    },
                    {
                        id: 3,
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
                    // Ẩn gợi ý voucher nếu có
                    hideVoucherSuggestion();

                    appliedVoucher = {
                        id: voucher.id,
                        code: voucher.code,
                        discount: voucher.discountAmount
                    };

                    $('.voucher-card').removeClass('active');
                    $card.addClass('active');

                    // Hiển thị voucher đã áp dụng
                    showAppliedVoucher(voucher.discountAmount, voucher.code);

                    $('#suggestionText').text(
                        `Đã áp dụng: ${appliedVoucher.code} — Tiết kiệm ${appliedVoucher.discount.toLocaleString()} VNĐ`
                    );

                    $.get('/cart/items').done(function (cart) {
                        calculateTotals(cart);
                    });

                    // Đóng modal
                    $('#voucherModal').modal('hide');
                }
            });

    }

    // Hủy mã
    $('#clearVoucherInline').off('click').on('click', function () {
        clearSelectedVoucher();
    });

    // Hàm clear voucher (tách riêng để có thể gọi từ nơi khác)
    function clearSelectedVoucher() {
        appliedVoucher = null;
        $('#discountValue').text('0 VNĐ');
        $('#appliedVoucherArea').hide();
        $('#defaultDiscountText').show();
        $('#voucherList li').removeClass('active');
        $('.voucher-card').removeClass('active selected');
        $('#suggestionText').text('Không có mã phù hợp');

        $("#voucherSuggestion").hide();

        $.get('/cart/items').done(function (cart) {
            calculateTotals(cart);
            // Load lại gợi ý voucher sau khi clear
            setTimeout(loadBestVoucherSuggestion, 500);
        });
    }

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

    // Load vouchers và suggestion
    $.get("/cart/items")
        .done(function (cart) {
            const subtotal = cart.reduce((s, it) => s + it.price * it.quantity, 0);
            loadVouchers(subtotal);
            // Load voucher suggestion khi trang được tải
            setTimeout(loadBestVoucherSuggestion, 1000);
        })
        .fail(function () {
            loadVouchers(0);
        });

    // Tính phí ship cho địa chỉ hiện tại khi load trang
    loadInitialShippingFee();

    // Event listener cho nút áp dụng voucher gợi ý
    $(document).on('click', '#applySuggestedVoucher', applySuggestedVoucher);

    // Expose clearSelectedVoucher function globally để có thể gọi từ nơi khác
    window.clearSelectedVoucher = clearSelectedVoucher;

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

        // Kiểm tra thay đổi giá trước khi đặt hàng
        checkPriceChangesForCheckout().then(priceChanged => {
            if (priceChanged) {
                // Có giá thay đổi, đã hiển thị modal cảnh báo
                return; // Dừng lại, chờ user quyết định
            }

            // Không có giá thay đổi hoặc user đã quyết định tiếp tục
            proceedWithOrder($btn, paymentMethod);
        }).catch(error => {
            console.error("Error checking price changes:", error);
            // Nếu lỗi check giá, vẫn cho phép đặt hàng
            proceedWithOrder($btn, paymentMethod);
        });
    });

    // Function để kiểm tra thay đổi giá trước khi đặt hàng
    function checkPriceChangesForCheckout() {
        return new Promise((resolve, reject) => {
            if (!cartItems || cartItems.length === 0) {
                resolve(false);
                return;
            }

            // Lấy danh sách variant codes từ giỏ hàng
            const variantCodes = cartItems.map(item => item.variantCode).filter(code => code);

            if (variantCodes.length === 0) {
                resolve(false);
                return;
            }

            // Gọi API kiểm tra giá
            $.ajax({
                url: "/api/product-variants/check-prices",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({ variantCodes: variantCodes }),
                success: function (currentPrices) {
                    let hasPriceChange = false;
                    let changedItems = [];

                    // So sánh giá hiện tại với giá trong giỏ hàng
                    cartItems.forEach(item => {
                        const currentPrice = currentPrices[item.variantCode];
                        if (currentPrice !== undefined && currentPrice !== item.price) {
                            hasPriceChange = true;
                            changedItems.push({
                                name: item.name,
                                oldPrice: item.price,
                                newPrice: currentPrice,
                                quantity: item.quantity
                            });
                        }
                    });

                    if (hasPriceChange) {
                        // Hiển thị modal cảnh báo thay đổi giá
                        showPriceChangeAlertForCheckout(changedItems);
                        resolve(true); // Có thay đổi giá
                    } else {
                        resolve(false); // Không có thay đổi
                    }
                },
                error: function (xhr) {
                    console.error("Error checking prices:", xhr);
                    reject(xhr);
                }
            });
        });
    }

    // Function hiển thị modal cảnh báo thay đổi giá cho checkout
    function showPriceChangeAlertForCheckout(changedItems) {
        // Tạo HTML content cho modal
        let htmlContent = '<div class="price-change-alert">';
        htmlContent += '<p class="alert-description"><i class="fa fa-exclamation-triangle text-warning"></i> Giá của một số sản phẩm trong giỏ hàng đã thay đổi:</p>';
        htmlContent += '<div class="price-change-list">';

        changedItems.forEach(item => {
            const oldTotal = item.oldPrice * item.quantity;
            const newTotal = item.newPrice * item.quantity;
            const priceDiff = newTotal - oldTotal;
            const diffClass = priceDiff > 0 ? 'price-increase' : 'price-decrease';
            const diffIcon = priceDiff > 0 ? 'fa-arrow-up' : 'fa-arrow-down';
            const diffText = priceDiff > 0 ? 'Tăng' : 'Giảm';

            htmlContent += `
                <div class="price-change-item">
                    <div class="product-name">
                        <strong>${item.name}</strong>
                    </div>
                    <div class="price-comparison">
                        <div class="old-price">
                            <span class="label">Giá cũ:</span>
                            <span class="value">${item.oldPrice.toLocaleString()} VNĐ x ${item.quantity}</span>
                            <span class="total">= ${oldTotal.toLocaleString()} VNĐ</span>
                        </div>
                        <div class="new-price">
                            <span class="label">Giá mới:</span>
                            <span class="value">${item.newPrice.toLocaleString()} VNĐ x ${item.quantity}</span>
                            <span class="total">= ${newTotal.toLocaleString()} VNĐ</span>
                        </div>
                        <div class="price-difference ${diffClass}">
                            <i class="fa ${diffIcon}"></i>
                            <span>${diffText} ${Math.abs(priceDiff).toLocaleString()} VNĐ</span>
                        </div>
                    </div>
                </div>
            `;
        });

        htmlContent += '</div>';
        htmlContent += '<p class="alert-question"><strong>Bạn có muốn tiếp tục đặt hàng với giá mới không?</strong></p>';
        htmlContent += '</div>';

        // Thêm CSS cho modal
        const customCSS = `
            <style>
                .price-change-alert {
                    text-align: left;
                    max-height: 400px;
                    overflow-y: auto;
                }
                .alert-description {
                    color: #856404;
                    background-color: #fff3cd;
                    border: 1px solid #ffeaa7;
                    padding: 12px 16px;
                    border-radius: 6px;
                    margin-bottom: 20px;
                    font-size: 14px;
                }
                .alert-description i {
                    margin-right: 8px;
                }
                .price-change-list {
                    margin-bottom: 20px;
                }
                .price-change-item {
                    border: 1px solid #e9ecef;
                    border-radius: 8px;
                    padding: 15px;
                    margin-bottom: 12px;
                    background-color: #f8f9fa;
                }
                .product-name {
                    margin-bottom: 12px;
                    color: #495057;
                    font-size: 16px;
                }
                .price-comparison {
                    display: flex;
                    flex-direction: column;
                    gap: 8px;
                }
                .old-price, .new-price {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    padding: 8px 12px;
                    border-radius: 4px;
                    font-size: 14px;
                }
                .old-price {
                    background-color: #f1aeb5;
                    color: #721c24;
                }
                .new-price {
                    background-color: #d4edda;
                    color: #155724;
                }
                .price-difference {
                    text-align: center;
                    padding: 8px;
                    border-radius: 4px;
                    font-weight: bold;
                    font-size: 14px;
                }
                .price-increase {
                    background-color: #f8d7da;
                    color: #721c24;
                }
                .price-decrease {
                    background-color: #d1ecf1;
                    color: #0c5460;
                }
                .label {
                    font-weight: bold;
                    min-width: 60px;
                }
                .value {
                    flex: 1;
                    text-align: center;
                }
                .total {
                    font-weight: bold;
                    min-width: 100px;
                    text-align: right;
                }
                .alert-question {
                    text-align: center;
                    color: #495057;
                    font-size: 16px;
                    margin-top: 20px;
                    padding: 15px;
                    background-color: #e9ecef;
                    border-radius: 6px;
                    border: 1px solid #ced4da;
                }
                .price-change-footer {
                    display: flex;
                    justify-content: space-between;
                    gap: 15px;
                    padding: 20px 0 0 0;
                    border-top: 1px solid #e9ecef;
                    margin-top: 20px;
                }
                .modern-btn-secondary {
                    background: linear-gradient(135deg, #6c757d, #5a6268);
                    border: none;
                    color: white;
                    padding: 12px 25px;
                    border-radius: 15px;
                    font-weight: 600;
                    transition: all 0.3s ease;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    flex: 1;
                    justify-content: center;
                    cursor: pointer;
                }
                .modern-btn-secondary:hover {
                    background: linear-gradient(135deg, #5a6268, #495057);
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
                    color: white;
                }
                .modern-btn-primary {
                    background: linear-gradient(135deg, #28a745, #20c997);
                    border: none;
                    color: white;
                    padding: 12px 25px;
                    border-radius: 15px;
                    font-weight: 600;
                    transition: all 0.3s ease;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    flex: 1;
                    justify-content: center;
                    cursor: pointer;
                    box-shadow: 0 4px 15px rgba(40, 167, 69, 0.3);
                }
                .modern-btn-primary:hover {
                    background: linear-gradient(135deg, #218838, #17a2b8);
                    transform: translateY(-2px);
                    box-shadow: 0 6px 20px rgba(40, 167, 69, 0.4);
                    color: white;
                }
                @media (max-width: 768px) {
                    .price-change-footer {
                        flex-direction: column;
                    }
                    .modern-btn-secondary,
                    .modern-btn-primary {
                        width: 100%;
                        margin: 5px 0;
                    }
                }
            </style>
        `;

        // Tạo custom footer với buttons đồng bộ style
        const footerHtml = `
            <div class="price-change-footer">
                <button type="button" class="btn modern-btn-secondary" id="btnCancelOrder">
                    <i class="fa fa-times"></i>
                    <span>Hủy đặt hàng</span>
                </button>
                <button type="button" class="btn modern-btn-primary" id="btnContinueOrder">
                    <i class="fa fa-check"></i>
                    <span>Tiếp tục với giá mới</span>
                </button>
            </div>
        `;

        // Hiển thị modal với custom footer
        Swal.fire({
            title: '<i class="fa fa-exclamation-triangle text-warning"></i> Giá sản phẩm đã thay đổi',
            html: customCSS + htmlContent,
            showConfirmButton: false,
            showCancelButton: false,
            width: '600px',
            customClass: {
                popup: 'price-change-modal',
                title: 'price-change-title',
                content: 'price-change-content'
            },
            showClass: {
                popup: 'animate__animated animate__fadeInDown'
            },
            hideClass: {
                popup: 'animate__animated animate__fadeOutUp'
            },
            footer: footerHtml,
            didOpen: () => {
                // Bind events cho custom buttons
                document.getElementById('btnCancelOrder').addEventListener('click', () => {
                    // Cập nhật giá mới trong giỏ hàng và UI, sau đó đóng modal
                    updateCartWithNewPrices(changedItems).then(() => {
                        Swal.close();
                        SwalUtils.info("Đã cập nhật giá", "Giá sản phẩm trong giỏ hàng đã được cập nhật theo giá mới.");
                    }).catch(error => {
                        console.error("Error updating cart prices:", error);
                        Swal.close();
                        SwalUtils.error("Lỗi!", "Không thể cập nhật giá trong giỏ hàng. Vui lòng thử lại.");
                    });
                });

                document.getElementById('btnContinueOrder').addEventListener('click', () => {
                    // User đồng ý với giá mới, cập nhật giỏ hàng và tiếp tục đặt hàng
                    updateCartWithNewPrices(changedItems).then(() => {
                        Swal.close();
                        // Sau khi cập nhật xong, tự động trigger lại nút đặt hàng
                        $("#btnConfirmOrder").trigger("click");
                    }).catch(error => {
                        console.error("Error updating cart prices:", error);
                        Swal.close();
                        SwalUtils.error("Lỗi!", "Không thể cập nhật giá trong giỏ hàng. Vui lòng thử lại.");
                    });
                });
            }
        });
    }

    // Function cập nhật giá trong giỏ hàng
    function updateCartWithNewPrices(changedItems) {
        return new Promise((resolve, reject) => {
            // Cập nhật giá trong cartItems
            changedItems.forEach(changedItem => {
                const cartItem = cartItems.find(item =>
                    item.name === changedItem.name &&
                    item.price === changedItem.oldPrice
                );
                if (cartItem) {
                    cartItem.price = changedItem.newPrice;
                }
            });

            // Cập nhật hiển thị trên UI
            renderCheckoutItems(cartItems);
            calculateTotals(cartItems);

            resolve();
        });
    }

    // Function xử lý đặt hàng sau khi đã kiểm tra giá
    function proceedWithOrder($btn, paymentMethod) {
        // Nếu có voucher được áp dụng, validate trước
        if (appliedVoucher && appliedVoucher.id) {
            validateVoucherBeforeOrder(appliedVoucher.id).then(result => {
                if (result.valid) {
                    // Voucher hợp lệ, tiếp tục đặt hàng
                    confirmAndPlaceOrder($btn, paymentMethod);
                } else {
                    // Voucher không hợp lệ, thông báo và yêu cầu chọn voucher khác
                    SwalUtils.warning(
                        "Voucher không khả dụng!",
                        result.message + "\n\nBạn có muốn tiếp tục đặt hàng không có voucher không?"
                    ).then((swalResult) => {
                        if (swalResult.isConfirmed) {
                            // Xóa voucher đã áp dụng và tiếp tục đặt hàng
                            clearSelectedVoucher();
                            confirmAndPlaceOrder($btn, paymentMethod);
                        }
                    });
                }
            }).catch(error => {
                console.error("Error validating voucher:", error);
                // Nếu lỗi validate, vẫn cho phép đặt hàng (backend sẽ check lại)
                confirmAndPlaceOrder($btn, paymentMethod);
            });
        } else {
            // Không có voucher, tiếp tục đặt hàng bình thường
            confirmAndPlaceOrder($btn, paymentMethod);
        }
    }

    // Function validate voucher trước khi đặt hàng
    function validateVoucherBeforeOrder(voucherId) {
        return new Promise((resolve, reject) => {
            const orderTotal = parseMoney($("#totalAmount").text());

            $.ajax({
                url: "/api/vouchers/validate",
                type: "GET",
                data: {
                    voucherId: voucherId,
                    orderTotal: orderTotal
                },
                success: function(response) {
                    resolve(response);
                },
                error: function(xhr) {
                    console.error("Voucher validation error:", xhr);
                    reject(xhr);
                }
            });
        });
    }

    // Function confirm và thực hiện đặt hàng
    function confirmAndPlaceOrder($btn, paymentMethod) {
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
                    paymentMethod: paymentMethod,
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
                                startOtpWaiting(res.orderId, res.email);
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

                            // Nếu lỗi voucher hết lượt sử dụng
                            if (errorMsg.includes("hết lượt sử dụng") || errorMsg.includes("Voucher")) {
                                SwalUtils.warning("Voucher không khả dụng!", errorMsg).then(() => {
                                    // Xóa voucher đã áp dụng
                                    clearSelectedVoucher();
                                });
                                return;
                            }

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
    }

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

    // === OTP Management Functions ===
    function startOtpWaiting(orderId, email) {
        otpState.isWaiting = true;
        otpState.orderId = orderId;
        otpState.email = email;
        otpState.startTime = Date.now();

        // Ẩn nút đặt hàng và hiện khu vực chờ OTP
        $("#btnConfirmOrder").fadeOut(300, function() {
            $("#otpWaitingArea").fadeIn(400);
            startOtpTimer();
        });
    }

    function stopOtpWaiting() {
        otpState.isWaiting = false;
        otpState.orderId = null;
        otpState.email = null;
        otpState.startTime = null;

        if (otpState.timer) {
            clearInterval(otpState.timer);
            otpState.timer = null;
        }

        // Ẩn khu vực chờ OTP và hiện lại nút đặt hàng
        $("#otpWaitingArea").fadeOut(300, function() {
            $("#btnConfirmOrder").fadeIn(400);
        });
    }

    function startOtpTimer() {
        if (otpState.timer) {
            clearInterval(otpState.timer);
        }

        updateTimerDisplay();

        otpState.timer = setInterval(function() {
            updateTimerDisplay();
        }, 1000);
    }

    function updateTimerDisplay() {
        if (!otpState.isWaiting || !otpState.startTime) {
            return;
        }

        const elapsed = Date.now() - otpState.startTime;
        const remaining = Math.max(0, otpState.duration - elapsed);

        if (remaining <= 0) {
            // Hết thời gian
            clearInterval(otpState.timer);
            otpState.timer = null;
            $("#otpTimeRemaining").text("Hết hạn").addClass("urgent");
            $("#btnResendOtp").prop("disabled", false).text("Gửi lại OTP");
            SwalUtils.warning("Hết thời gian", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
            return;
        }

        const minutes = Math.floor(remaining / 60000);
        const seconds = Math.floor((remaining % 60000) / 1000);
        const timeString = `${minutes}:${seconds.toString().padStart(2, '0')}`;

        const $timerElement = $("#otpTimeRemaining");
        $timerElement.text(timeString);

        // Thêm hiệu ứng urgent khi còn ít hơn 2 phút
        if (remaining < 2 * 60 * 1000) {
            $timerElement.addClass("urgent");
        } else {
            $timerElement.removeClass("urgent");
        }
    }

    function reopenOtpModal() {
        if (!otpState.isWaiting || !otpState.orderId) {
            SwalUtils.error("Lỗi", "Không có phiên OTP nào đang chờ xử lý.");
            return;
        }

        // Kiểm tra thời gian còn lại
        const elapsed = Date.now() - otpState.startTime;
        const remaining = Math.max(0, otpState.duration - elapsed);

        if (remaining <= 0) {
            SwalUtils.warning("Hết thời gian", "Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.");
            return;
        }

        // Điền thông tin và mở modal
        $("#otpOrderId").val(otpState.orderId);
        $("#otpEmail").val(otpState.email);
        $("#otpCode").val("");
        $("#otpModal").modal("show");
    }

    function resendOtp() {
        if (!otpState.orderId) {
            SwalUtils.error("Lỗi", "Không tìm thấy thông tin đơn hàng.");
            return;
        }

        // Disable nút để tránh spam
        $("#btnResendOtp").prop("disabled", true).html('<i class="fa fa-spinner fa-spin"></i> Đang gửi...');

        $.ajax({
            url: "/api/orders/resend-otp",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                orderId: otpState.orderId,
                email: otpState.email
            }),
            success: function(res) {
                if (res.success) {
                    // Reset timer
                    otpState.startTime = Date.now();
                    startOtpTimer();
                    $("#otpTimeRemaining").removeClass("urgent");

                    SwalUtils.success("Thành công", "Mã OTP mới đã được gửi đến email của bạn.");
                } else {
                    SwalUtils.error("Lỗi", res.message || "Không thể gửi lại OTP.");
                }
            },
            error: function(err) {
                console.error("Resend OTP error:", err);
                SwalUtils.error("Lỗi", "Không thể gửi lại OTP. Vui lòng thử lại.");
            },
            complete: function() {
                $("#btnResendOtp").prop("disabled", false).html('<i class="fa fa-refresh"></i> <span>Gửi lại OTP</span>');
            }
        });
    }

    $("#btnVerifyOtp").on("click", function () {
        const $btn = $(this);
        const $cancelBtn = $("#otpModal .modern-btn-secondary");
        const orderId = $("#otpOrderId").val();
        const email = $("#otpEmail").val();
        const otp = $("#otpCode").val();

        if (!otp || otp.length !== 6) {
            SwalUtils.warning("Thông báo", "Vui lòng nhập mã OTP gồm 6 ký tự.");
            return;
        }

        // Disable buttons và hiển thị spinner
        $btn.prop("disabled", true).html('<i class="fa fa-spinner fa-spin"></i> Đang xử lý...');
        $cancelBtn.prop("disabled", true);
        $("#otpCode").prop("disabled", true);

        $.ajax({
            url: "/api/orders/confirm-otp",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({ orderId, email, otp }),
            success: function (res) {
                if (res.success) {
                    $("#otpModal").modal("hide");
                    stopOtpWaiting(); // Dọn dẹp trạng thái OTP
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
            },
            complete: function() {
                // Khôi phục trạng thái nút sau khi hoàn thành (thành công hoặc thất bại)
                $btn.prop("disabled", false).html('<i class="fa fa-check-circle"></i> Xác nhận OTP');
                $cancelBtn.prop("disabled", false);
                $("#otpCode").prop("disabled", false);
            }
        });
    });

    // === Event Listeners cho OTP ===
    $("#btnReopenOtpModal").on("click", function() {
        reopenOtpModal();
    });

    $("#btnResendOtp").on("click", function() {
        resendOtp();
    });

    // Xử lý khi modal OTP đóng để không mất trạng thái
    $("#otpModal").on('hidden.bs.modal', function () {
        // Không làm gì cả - giữ trạng thái OTP để có thể mở lại
        // Modal chỉ được clear khi OTP thành công hoặc hết hạn
    });

    // Cleanup khi rời khỏi trang
    $(window).on('beforeunload', function() {
        if (otpState.timer) {
            clearInterval(otpState.timer);
        }
    });

    window.enableTextareaEdit = enableTextareaEdit;
    window.enableEdit = enableEdit;

    // === Xử lý địa chỉ giao hàng ===
    loadShippingProvinces();
    updateMapWithCurrentAddress(); // Load map với địa chỉ hiện tại khi tải trang

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

            // Tính lại phí ship theo địa chỉ hiện tại khi chọn "Giao đến địa chỉ hiện tại"
            const currentAddress = $('#currentFullAddress');
            // Dùng .attr() để lấy string
            const provinceCode = currentAddress.attr('data-province-code');
            const communeCode = currentAddress.attr('data-commune-code');

            if (provinceCode && communeCode) {
                console.log('Recalculating shipping fee for current address:', {provinceCode, communeCode});
                calculateShippingFee(provinceCode, communeCode);
            } else {
                console.log('No current address, using default fee');
                calculateShippingFee(null, null);
            }

            // Cập nhật map về địa chỉ hiện tại
            updateMapWithCurrentAddress();
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
            // Cập nhật map với địa chỉ mới
            updateMapWithNewAddress();
        }
    });

    // Sự kiện thay đổi địa chỉ cụ thể để cập nhật map
    $('#shippingSpecificAddress').on('blur', function() {
        const specificAddress = $(this).val().trim();
        if (specificAddress && $('#shippingProvince').val() && $('#shippingCommune').val()) {
            updateMapWithNewAddress();
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
        console.log('calculateShippingFee() called with:', {provinceCode, communeCode});

        // Lấy thông tin đơn hàng để tính phí chính xác
        const totalValue = parseFloat($('#totalAmount').text().replace(/[^\d]/g, '')) || 0;
        const estimatedWeight = calculateEstimatedWeight(); // Ước tính trọng lượng từ giỏ hàng

        console.log('Request payload:', {provinceCode, communeCode, weight: estimatedWeight, totalValue});

        // Gọi API tính phí ship qua GHN
        $.ajax({
            url: '/api/orders/calculate-shipping-fee',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                provinceCode: provinceCode,
                communeCode: communeCode,
                weight: estimatedWeight,
                totalValue: totalValue
            }),
            success: function(response) {
                shippingFee = response.fee || 30000;
                console.log(`GHN shipping fee received: ${shippingFee.toLocaleString()} VNĐ for ${provinceCode}-${communeCode}`, response);
                updateShippingFee(shippingFee);
                updateShippingFeePreview(shippingFee);
            },
            error: function(xhr) {
                console.error('Error calculating shipping fee with GHN:', xhr);
                console.log('Response text:', xhr.responseText);
                updateShippingFee(30000);
                updateShippingFeePreview(30000);
            }
        });
    } else {
        // Sử dụng phí mặc định
        console.log('No address selected (Province:', provinceCode, ', Commune:', communeCode, '), using default shipping fee (30,000 VNĐ)');
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

        // Cập nhật map với địa chỉ mới được lưu
        updateMapWithAddress(fullAddress);

        SwalUtils.success('Thành công!', 'Đã cập nhật địa chỉ giao hàng');
    }

    // Ẩn form chỉnh sửa
    cancelAddressEdit();
}

// === Các function xử lý map ===

// Cập nhật map với địa chỉ hiện tại
function updateMapWithCurrentAddress() {
    const currentAddress = $('#currentFullAddress').text().trim();
    if (currentAddress && currentAddress !== 'Chưa có địa chỉ') {
        updateMapWithAddress(currentAddress);
    } else {
        // Fallback về Hà Nội nếu không có địa chỉ
        updateMapWithAddress('Hanoi, Vietnam');
    }
}

// Cập nhật map với địa chỉ mới đang nhập
function updateMapWithNewAddress() {
    const provinceName = $('#shippingProvince option:selected').text();
    const communeName = $('#shippingCommune option:selected').text();
    const specificAddress = $('#shippingSpecificAddress').val().trim();

    let searchAddress = '';
    if (specificAddress) {
        searchAddress = `${specificAddress}, ${communeName}, ${provinceName}, Vietnam`;
    } else if (communeName && communeName !== '-- Chọn Phường/Xã --') {
        searchAddress = `${communeName}, ${provinceName}, Vietnam`;
    } else if (provinceName && provinceName !== '-- Chọn Tỉnh/Thành phố --') {
        searchAddress = `${provinceName}, Vietnam`;
    }

    if (searchAddress) {
        updateMapWithAddress(searchAddress);
    }
}

// Function chính để cập nhật map
function updateMapWithAddress(address) {
    const mapFrame = $('#deliveryMap');
    if (mapFrame.length && address) {
        // Encode địa chỉ để đảm bảo URL hợp lệ
        const encodedAddress = encodeURIComponent(address);
        const mapUrl = `https://www.google.com/maps?q=${encodedAddress}&output=embed`;

        console.log('Updating map with address:', address);
        mapFrame.attr('src', mapUrl);

        // Optional: Hiệu ứng loading cho map
        mapFrame.css('opacity', '0.5');
        setTimeout(() => {
            mapFrame.css('opacity', '1');
        }, 1000);
    }
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

// Tính phí ship ban đầu khi load trang
function loadInitialShippingFee() {
    // Lấy provinceCode và communeCode từ địa chỉ hiện tại
    const currentAddress = $('#currentFullAddress');
    // Dùng .attr() thay vì .data() để lấy string, không bị convert sang number
    const provinceCode = currentAddress.attr('data-province-code');
    const communeCode = currentAddress.attr('data-commune-code');

    console.log('Loading initial shipping fee with current address:');
    console.log('   - Province Code:', provinceCode, '(type:', typeof provinceCode, ')');
    console.log('   - Commune Code:', communeCode, '(type:', typeof communeCode, ')');

    if (provinceCode && communeCode) {
        // Tính phí ship cho địa chỉ hiện tại
        calculateShippingFee(provinceCode, communeCode);
    } else {
        console.log('No current address found, using default shipping fee (30,000 VNĐ)');
        updateShippingFee(30000);
    }
}

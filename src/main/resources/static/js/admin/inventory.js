// Inventory Management JavaScript

let currentPage = 0;
const pageSize = 15;

// Load inventory data
function loadInventory(page = 0) {
    currentPage = page;
    const search = $('#searchInput').val();
    const stockFilter = $('#stockFilter').val();
    const categoryFilter = $('#categoryFilter').val();
    const brandFilter = $('#brandFilter').val();

    const params = new URLSearchParams({
        page: page,
        size: pageSize
    });

    if (search) params.append('search', search);
    if (stockFilter) params.append('stockFilter', stockFilter);
    if (categoryFilter) params.append('categoryFilter', categoryFilter);
    if (brandFilter) params.append('brandFilter', brandFilter);

    $.ajax({
        url: `/api/inventory?${params.toString()}`,
        method: 'GET',
        success: function(data) {
            renderInventoryTable(data);
            updateStats();
        },
        error: function(xhr, status, error) {
            toastr.error('Không thể tải dữ liệu kho: ' + error);
        }
    });
}

// Render inventory table (mock data for now)
function renderInventoryTable(data) {
    // This is a mock implementation
    // In real implementation, you would have an API endpoint that returns inventory data
    const mockData = {
        content: [
            {
                id: 1,
                variantCode: 'SP001-RED-M',
                productName: 'Áo thun nam',
                colorName: 'Đỏ',
                sizeName: 'M',
                quantity: 50,
                price: 250000,
                status: 'in-stock'
            },
            {
                id: 2,
                variantCode: 'SP002-BLUE-L',
                productName: 'Áo sơ mi nữ',
                colorName: 'Xanh',
                sizeName: 'L',
                quantity: 5,
                price: 350000,
                status: 'low-stock'
            },
            {
                id: 3,
                variantCode: 'SP003-WHITE-S',
                productName: 'Quần jean',
                colorName: 'Trắng',
                sizeName: 'S',
                quantity: 0,
                price: 450000,
                status: 'out-of-stock'
            }
        ],
        totalPages: 1,
        number: 0,
        totalElements: 3
    };

    let tableHtml = `
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Mã SP</th>
                    <th>Tên Sản Phẩm</th>
                    <th>Màu/Size</th>
                    <th>Số Lượng</th>
                    <th>Giá</th>
                    <th>Trạng Thái</th>
                    <th>Thao Tác</th>
                </tr>
            </thead>
            <tbody>
    `;

    mockData.content.forEach(item => {
        const statusBadge = getStatusBadge(item.status, item.quantity);
        const quantityColor = getQuantityColor(item.quantity);

        tableHtml += `
            <tr>
                <td>${item.variantCode}</td>
                <td>${item.productName}</td>
                <td>${item.colorName} / ${item.sizeName}</td>
                <td class="${quantityColor}"><strong>${item.quantity}</strong></td>
                <td>${formatCurrency(item.price)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="openQuickStockModal(${item.id}, '${item.variantCode}', '${item.productName}', '${item.colorName}', '${item.sizeName}', ${item.quantity})">
                        <i class="fa fa-edit"></i> Cập nhật
                    </button>
                    <button class="btn btn-sm btn-info" onclick="viewStockHistory(${item.id})">
                        <i class="fa fa-history"></i> Lịch sử
                    </button>
                </td>
            </tr>
        `;
    });

    if (mockData.content.length === 0) {
        tableHtml += '<tr><td colspan="7" class="text-center"><em>Không có dữ liệu</em></td></tr>';
    }

    tableHtml += '</tbody></table>';

    // Add pagination
    if (mockData.totalPages > 1) {
        tableHtml += generatePagination(mockData.number, mockData.totalPages);
    }

    $('#inventoryTableContainer').html(tableHtml);
}

// Update statistics
function updateStats() {
    // Mock stats - in real implementation, get from API
    $('#totalProducts').text('150');
    $('#lowStockCount').text('12');
    $('#outOfStockCount').text('3');
    $('#inStockCount').text('135');
}

// Open quick stock modal
function openQuickStockModal(variantId, variantCode, productName, colorName, sizeName, currentQuantity) {
    $('#selectedVariantId').val(variantId);
    $('#selectedProductInfo').html(`
        <strong>Sản phẩm:</strong> ${productName}<br>
        <strong>Mã:</strong> ${variantCode}<br>
        <strong>Màu/Size:</strong> ${colorName} / ${sizeName}<br>
        <strong>Số lượng hiện tại:</strong> ${currentQuantity}
    `);

    // Reset form
    $('#quickStockForm')[0].reset();
    $('#quickQuantityGroup').show();
    $('#quickNewQuantityGroup').hide();

    $('#quickStockModal').modal('show');
}

// Execute quick stock action
function executeQuickStockAction() {
    const variantId = $('#selectedVariantId').val();
    const actionType = $('#quickActionType').val();
    const quantity = $('#quickQuantity').val();
    const newQuantity = $('#quickNewQuantity').val();
    const note = $('#quickNote').val();

    if (!actionType || !variantId) {
        toastr.error('Vui lòng nhập đầy đủ thông tin');
        return;
    }

    let endpoint = '';
    let data = {
        variantId: parseInt(variantId),
        note: note,
        createdBy: 'admin' // TODO: Get from session
    };

    switch (actionType) {
        case 'stock-in':
            if (!quantity) {
                toastr.error('Vui lòng nhập số lượng');
                return;
            }
            endpoint = '/api/stock-movements/stock-in';
            data.quantity = parseInt(quantity);
            break;

        case 'stock-out':
            if (!quantity) {
                toastr.error('Vui lòng nhập số lượng');
                return;
            }
            endpoint = '/api/stock-movements/stock-out';
            data.quantity = parseInt(quantity);
            break;

        case 'update-stock':
            if (newQuantity === '') {
                toastr.error('Vui lòng nhập số lượng mới');
                return;
            }
            endpoint = '/api/stock-movements/update-stock';
            data.newQuantity = parseInt(newQuantity);
            break;
    }

    // Show loading
    $('#quickStockModal .btn-primary').prop('disabled', true).text('Đang xử lý...');

    $.ajax({
        url: endpoint,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(response) {
            if (response.success) {
                toastr.success(response.message);
                $('#quickStockModal').modal('hide');
                loadInventory(currentPage); // Reload current page
                updateStats(); // Update statistics
            } else {
                toastr.error(response.message);
            }
        },
        error: function(xhr, status, error) {
            const response = xhr.responseJSON;
            if (response && response.message) {
                toastr.error(response.message);
            } else {
                toastr.error('Có lỗi xảy ra: ' + error);
            }
        },
        complete: function() {
            $('#quickStockModal .btn-primary').prop('disabled', false).text('Cập nhật');
        }
    });
}

// View stock history
function viewStockHistory(variantId) {
    window.open(`/admin/stock-management/history?variantId=${variantId}`, '_blank');
}

// Export inventory to Excel
function exportInventory() {
    toastr.info('Tính năng xuất Excel đang được phát triển...');
}

// Utility functions
function getStatusBadge(status, quantity) {
    if (quantity === 0) {
        return '<span class="badge badge-danger">Hết hàng</span>';
    } else if (quantity <= 10) {
        return '<span class="badge badge-warning">Sắp hết</span>';
    } else {
        return '<span class="badge badge-success">Còn hàng</span>';
    }
}

function getQuantityColor(quantity) {
    if (quantity === 0) return 'text-danger';
    if (quantity <= 10) return 'text-warning';
    return 'text-success';
}

function formatCurrency(amount) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(amount);
}

function generatePagination(currentPage, totalPages) {
    let pagination = '<div class="text-center"><ul class="pagination">';

    // Previous button
    if (currentPage > 0) {
        pagination += `<li><a href="#" onclick="loadInventory(${currentPage - 1})"><i class="fa fa-angle-left"></i> Trước</a></li>`;
    } else {
        pagination += '<li class="disabled"><span><i class="fa fa-angle-left"></i> Trước</span></li>';
    }

    // Page numbers
    for (let i = 0; i < totalPages; i++) {
        const activeClass = i === currentPage ? 'active' : '';
        pagination += `<li class="${activeClass}"><a href="#" onclick="loadInventory(${i})">${i + 1}</a></li>`;
    }

    // Next button
    if (currentPage < totalPages - 1) {
        pagination += `<li><a href="#" onclick="loadInventory(${currentPage + 1})">Sau <i class="fa fa-angle-right"></i></a></li>`;
    } else {
        pagination += '<li class="disabled"><span>Sau <i class="fa fa-angle-right"></i></span></li>';
    }

    pagination += '</ul></div>';
    return pagination;
}

// Event handlers
$(document).ready(function() {
    // Load initial data
    loadInventory(0);

    // Search on Enter key
    $('#searchInput').keypress(function(e) {
        if (e.which == 13) {
            loadInventory(0);
        }
    });

    // Action type change handler for quick modal
    $('#quickActionType').change(function() {
        const actionType = $(this).val();
        if (actionType === 'update-stock') {
            $('#quickQuantityGroup').hide();
            $('#quickNewQuantityGroup').show();
        } else {
            $('#quickQuantityGroup').show();
            $('#quickNewQuantityGroup').hide();
        }
    });
});

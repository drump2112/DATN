// Stock Movements Management JavaScript

// Search and filter functions
function searchMovements(page = 0) {
    const searchInput = $('#searchInput').val();
    const movementType = $('#movementTypeFilter').val();
    const startDate = $('#dateStartFilter').val();
    const endDate = $('#dateEndFilter').val();

    const params = new URLSearchParams({
        page: page,
        size: 10
    });

    if (searchInput) params.append('search', searchInput);
    if (movementType) params.append('movementType', movementType);
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);

    $.ajax({
        url: `/admin/stock-management/history/table?${params.toString()}`,
        method: 'GET',
        success: function(data) {
            $('#movementTableContainer').html(data);
        },
        error: function(xhr, status, error) {
            toastr.error('Không thể tải dữ liệu: ' + error);
        }
    });
}

// Execute stock action from modal
function executeStockAction() {
    const actionType = $('#actionType').val();
    const variantId = $('#variantId').val();
    const quantity = $('#quantity').val();
    const newQuantity = $('#newQuantity').val();
    const note = $('#note').val();

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

        case 'damage':
            if (!quantity) {
                toastr.error('Vui lòng nhập số lượng');
                return;
            }
            endpoint = '/api/stock-movements/damage';
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

        default:
            toastr.error('Loại thao tác không hợp lệ');
            return;
    }

    // Show loading
    $('#stockActionModal .btn-primary').prop('disabled', true).text('Đang xử lý...');

    $.ajax({
        url: endpoint,
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function(response) {
            if (response.success) {
                toastr.success(response.message);
                $('#stockActionModal').modal('hide');
                resetStockActionForm();
                // Reload table
                searchMovements(0);
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
            $('#stockActionModal .btn-primary').prop('disabled', false).text('Thực hiện');
        }
    });
}

// Reset form when modal is hidden
function resetStockActionForm() {
    $('#stockActionForm')[0].reset();
    $('#quantityGroup').show();
    $('#newQuantityGroup').hide();
}

// Event handlers
$(document).ready(function() {
    // Load initial data
    searchMovements(0);

    // Filter change events
    $('#movementTypeFilter').change(function() {
        searchMovements(0);
    });

    $('#dateStartFilter, #dateEndFilter').change(function() {
        searchMovements(0);
    });

    // Search on Enter key
    $('#searchInput').keypress(function(e) {
        if (e.which == 13) {
            searchMovements(0);
        }
    });

    // Reset form when modal is hidden
    $('#stockActionModal').on('hidden.bs.modal', function() {
        resetStockActionForm();
    });

    // Action type change handler
    $('#actionType').change(function() {
        const actionType = $(this).val();
        if (actionType === 'update-stock') {
            $('#quantityGroup').hide();
            $('#newQuantityGroup').show();
        } else {
            $('#quantityGroup').show();
            $('#newQuantityGroup').hide();
        }
    });
});

// Utility function to format number with thousands separator
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// Utility function to get movement type badge color
function getMovementTypeBadge(type) {
    const badges = {
        'IN': 'badge-success',
        'OUT': 'badge-warning',
        'SALE': 'badge-primary',
        'RETURN': 'badge-info',
        'DAMAGE': 'badge-danger',
        'MANUAL': 'badge-default'
    };
    return badges[type] || 'badge-default';
}
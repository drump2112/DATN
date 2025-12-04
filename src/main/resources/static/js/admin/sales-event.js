// Global variables to track current state
var currentPage = 0;
var currentKeyword = '';
var currentStatus = '';

function searchEvent(page) {
  currentPage = page;

  var data = {
    page: page,
    size: 5,
  };

  var searchInput = document.getElementById("searchInput");
  var statusFilter = document.getElementById("statusFilter");

  var keyword = searchInput ? searchInput.value.trim() : '';
  var status = statusFilter ? statusFilter.value : '';

  if (keyword && keyword.length > 0) {
    data.keyword = keyword;
    currentKeyword = keyword;
  }
  if (status !== null && status !== undefined && status !== "") {
    data.isActive = status;
    currentStatus = status;
  }

  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/admin/sales-event/search?' + new URLSearchParams(data).toString(), true);
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var container = document.getElementById("salesEventTableContainer");
        if (container) {
          container.innerHTML = xhr.responseText;
        }
      } else {
        console.error("Không thể tải dữ liệu đợt giảm giá");
      }
    }
  };
  xhr.send();
}

window.searchEvent = searchEvent;

function triggerSearch() {
  var searchInput = document.getElementById("searchInput");
  var statusFilter = document.getElementById("statusFilter");

  currentKeyword = searchInput ? searchInput.value.trim() : '';
  currentStatus = statusFilter ? statusFilter.value : '';

  searchEvent(0);
}

window.triggerSearch = triggerSearch;

function openAddModal() {
  clearForm();
  loadProductVariants();
  document.getElementById("modalTitle").innerText = "Thêm Đợt Giảm Giá";
  document.getElementById("btnAddEvent").style.display = "inline-block";
  document.getElementById("btnUpdateEvent").style.display = "none";
  $('#myModal').modal('show');
}

function handleDetailClick(button) {
  var data = button.dataset;
  clearForm();
  loadProductVariants();

  document.getElementById("eventId").value = data.id || "";
  document.getElementById("eventCode").value = data.code || "";
  document.getElementById("eventName").value = data.name || "";
  document.getElementById("discountType").value = data.discounttype || "";
  document.getElementById("discountValue").value = data.discountvalue || "";
  document.getElementById("maxDiscountValue").value = data.maxdiscountvalue || "";
  document.getElementById("startDate").value = data.startdate || "";
  document.getElementById("endDate").value = data.enddate || "";

  // Set selected product variants
  var selectedVariants = (data.productvariants || "").split(",").filter(id => id.trim() !== "");
  $('#productVariants').val(selectedVariants).trigger('change');

  document.getElementById("modalTitle").innerText = "Cập Nhật Đợt Giảm Giá";
  document.getElementById("btnAddEvent").style.display = "none";
  document.getElementById("btnUpdateEvent").style.display = "inline-block";
  $('#myModal').modal('show');
}

function loadProductVariants() {
  $('#productVariants').select2({
    placeholder: 'Chọn sản phẩm',
    ajax: {
      url: '/admin/sales-event/product-variants',
      dataType: 'json',
      delay: 250,
      data: function (params) {
        return {
          q: params.term
        };
      },
      processResults: function (data) {
        return {
          results: data
        };
      },
      cache: true
    },
    minimumInputLength: 0,
    multiple: true
  });
}

function clearForm() {
  document.getElementById("salesEventForm").reset();
  document.getElementById("eventId").value = "";
  $('#productVariants').val(null).trigger('change');
}

function toggleStatus(id, currentStatus) {
  if (!confirm("Bạn có chắc chắn muốn thay đổi trạng thái?")) {
    return;
  }

  var xhr = new XMLHttpRequest();
  xhr.open('PUT', '/admin/sales-event/' + id + '/toggle-status', true);
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var response = JSON.parse(xhr.responseText);
        alert(response.message);
        searchEvent(currentPage);
      } else {
        alert("Có lỗi xảy ra khi thay đổi trạng thái");
      }
    }
  };
  xhr.send();
}

$(document).ready(function() {
  $("#btnAddEvent").on("click", function() {
    var formData = new FormData(document.getElementById("salesEventForm"));
    // Add selected product variants
    var selectedVariants = $('#productVariants').val() || [];
    selectedVariants.forEach(function(variantId) {
      formData.append('productVariantIds', variantId);
    });

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/admin/sales-event/add', true);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          var response = JSON.parse(xhr.responseText);
          alert(response.message);
          $('#myModal').modal('hide');
          searchEvent(0);
        } else {
          var response = JSON.parse(xhr.responseText);
          alert(response.message || "Có lỗi xảy ra");
        }
      }
    };
    xhr.send(formData);
  });

  $("#btnUpdateEvent").on("click", function() {
    var id = document.getElementById("eventId").value;
    var formData = new FormData(document.getElementById("salesEventForm"));
    // Add selected product variants
    var selectedVariants = $('#productVariants').val() || [];
    selectedVariants.forEach(function(variantId) {
      formData.append('productVariantIds', variantId);
    });

    var xhr = new XMLHttpRequest();
    xhr.open('PUT', '/admin/sales-event/' + id, true);
    xhr.onreadystatechange = function() {
      if (xhr.readyState === 4) {
        if (xhr.status === 200) {
          var response = JSON.parse(xhr.responseText);
          alert(response.message);
          $('#myModal').modal('hide');
          searchEvent(currentPage);
        } else {
          var response = JSON.parse(xhr.responseText);
          alert(response.message || "Có lỗi xảy ra");
        }
      }
    };
    xhr.send(formData);
  });

  $("#resetFilterBtn").on("click", function() {
    document.getElementById("searchInput").value = "";
    document.getElementById("statusFilter").value = "";
    currentKeyword = '';
    currentStatus = '';
    searchEvent(0);
  });
});
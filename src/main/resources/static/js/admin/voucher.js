$(document).ready(function () {
  $(document).on("change", "#voucherType", function () {
    console.log("voucherType change trigger");
    let type = $(this).val();
    if (type === "PERCENT") {
      $("#discountUnit").text("%");
      $("#discountValue").attr({
        min: 1,
        max: 100,
        placeholder: "Nhập % giảm",
      });
    } else {
      $("#discountUnit").text("₫");
      $("#discountValue").attr({
        min: 1000,
        max: "",
        placeholder: "Nhập số tiền giảm",
      });
    }
  });

  // Khi modal mở thì trigger luôn để cập nhật giao diện ban đầu
  $("#myModal").on("shown.bs.modal", function () {
    $("#voucherType").trigger("change");
  });
  function openAddModal() {
    $("#modalTitle").text("Thêm Voucher");

    $("#myModal").modal("show");
  }
  $("#targetType").on("change", function () {
    let type = $(this).val();
    $(
      "#targetProductGroup, #targetCategoryGroup, #targetUserGroup, #targetBrandGroup",
    ).hide();

    if (type === "PRODUCT") {
      initSelect2Product();
      $("#targetProductGroup").show();
    } else if (type === "CATEGORY") {
      initSelect2Category();
      $("#targetCategoryGroup").show();
    } else if (type === "BRAND") {
      initSelect2Brand();
      $("#targetBrandGroup").show();
    } else if (type === "USER") {
      $("#targetUserGroup").show();
    }
  });

  function initSelect2Category() {
    $("#targetCategory").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn Danh Mục",
      allowClear: true,
      ajax: {
        url: "/admin/category/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });
  }

  function initSelect2Product() {
    $("#targetProduct").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn Sản Phẩm",
      allowClear: true,
      ajax: {
        url: "/admin/product/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });
  }

  function initSelect2Brand() {
    $("#targetBrand").select2({
      dropdownParent: $("#myModal"),
      placeholder: "Chọn Thương Hiệu",
      allowClear: true,
      ajax: {
        url: "/admin/brand/select2",
        dataType: "json",
        delay: 250,
        data: (params) => ({ q: params.term }),
        processResults: (data) => ({ results: data }),
        cache: true,
      },
    });
  }

  window.openAddModal = openAddModal;
});

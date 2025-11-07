Dropzone.autoDiscover = false;
var avatarDropzone = null;

// Global variables to track current state
var currentPage = 0;
var currentKeyword = '';
var currentStatus = '';

// Define searchBrand function in multiple ways to ensure it's accessible
function searchBrand(page) {
  // Update current page
  currentPage = page;

  var data = {
    page: page,
    size: 5,
  };

  // Get current filter values from DOM elements (with null checks)
  var searchInput = document.getElementById("searchInput");
  var statusFilter = document.getElementById("statusFilter");

  var keyword = searchInput ? searchInput.value.trim() : '';
  var status = statusFilter ? statusFilter.value : '';

  // Use current filter values
  if (keyword && keyword.length > 0) {
    data.keyword = keyword;
    currentKeyword = keyword;
  }
  if (status !== null && status !== undefined && status !== "") {
    data.isActive = status;
    currentStatus = status;
  }

  // Use vanilla JavaScript for AJAX to avoid jQuery dependency issues
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/admin/brand/search?' + new URLSearchParams(data).toString(), true);
  xhr.onreadystatechange = function() {
    if (xhr.readyState === 4) {
      if (xhr.status === 200) {
        var container = document.getElementById("brandTableContainer");
        if (container) {
          container.innerHTML = xhr.responseText;
        }
      } else {
        console.error("Không thể tải dữ liệu thương hiệu");
      }
    }
  };
  xhr.send();
}

// Expose to window object immediately
window.searchBrand = searchBrand;

// Debug: Log to console to verify function is loaded
console.log("searchBrand function loaded:", typeof window.searchBrand);

// Also expose through direct assignment (backup method)
if (typeof window !== 'undefined') {
  window['searchBrand'] = searchBrand;
  window['triggerSearch'] = triggerSearch;
}

// Alternative global assignment
var globalSearchBrand = searchBrand;
var globalTriggerSearch = triggerSearch;

// Function to manually trigger search (for search button)
function triggerSearch() {
  var searchInput = document.getElementById("searchInput");
  var statusFilter = document.getElementById("statusFilter");

  currentKeyword = searchInput ? searchInput.value.trim() : '';
  currentStatus = statusFilter ? statusFilter.value : '';

  searchBrand(0); // Reset to first page when searching
}

// Expose to window object
window.triggerSearch = triggerSearch;

// Double-check function exposure when DOM is ready
$(document).ready(function () {
  console.log("DOM ready - searchBrand type:", typeof window.searchBrand);
  console.log("DOM ready - searchBrand exists:", 'searchBrand' in window);

  // Force re-assignment in case of any issues
  window.searchBrand = searchBrand;
  window.triggerSearch = triggerSearch;

  console.log("After re-assignment - searchBrand type:", typeof window.searchBrand);

  $("#brandForm").validate({
    ignore: [],
    rules: {
      nameBrand: { required: true, maxlength: 20 },
      avatar: { required: true },
    },
    messages: {
      nameBrand: {
        required: "Vui lòng nhập tên thương hiệu",
        maxlength: "Tên thương hiệu không quá 20 ký tự",
      },
      avatar: { required: "Vui lòng chọn logo" },
    },
    errorPlacement: function (error, element) {
      error.insertAfter(element); // lỗi hiển thị dưới input
    },
  });

  // Add event listener for status filter change
  $("#statusFilter").change(function() {
    currentStatus = $(this).val();
    searchBrand(0); // Reset to page 0 when filter changes
  });

  // Add event listener for search input (optional: search on enter)
  $("#searchInput").keypress(function(e) {
    if (e.which === 13) { // Enter key
      currentKeyword = $(this).val().trim();
      searchBrand(0);
    }
  });

  // Reset filter button
  $("#resetFilterBtn").click(function() {
    $("#searchInput").val('');
    $("#statusFilter").val('');
    currentKeyword = '';
    currentStatus = '';
    currentPage = 0;
    searchBrand(0);
  });

  // Initialize filters from current page values if any
  function initializeFilters() {
    currentKeyword = $("#searchInput").val() || '';
    currentStatus = $("#statusFilter").val() || '';
    currentPage = 0;
  }

  // Initialize on page load
  initializeFilters();

  if (!avatarDropzone) {
    avatarDropzone = new Dropzone("#avatarDropzone", {
      url: "/dummy-upload", // dummy, không upload
      autoProcessQueue: false,
      clickable: true,
      maxFiles: 1,
      acceptedFiles: "image/*",
      addRemoveLinks: true,
      dictDefaultMessage: "Kéo ảnh vào đây hoặc click để chọn",
      previewsContainer: "#avatarDropzone",
    });

    avatarDropzone.on("addedfile", function (file) {
      if (this.files.length > 1) {
        this.removeFile(this.files[0]);
      }

      if (file instanceof File) {
        const dt = new DataTransfer();
        dt.items.add(file);
        document.getElementById("avatarInput").files = dt.files;
      }
    });
  }

  function openAddModal() {
    $("#brandForm").validate().resetForm();

    $("#modalTitle").text("Thêm Thương Hiệu");
    $("#brandForm")[0].reset();
    $("#brandForm").validate().resetForm();
    $("#brandId").val("");

    if (avatarDropzone) {
      avatarDropzone.removeAllFiles(true);
    }

    $("#btnAddBrand").show();
    $("#btnUpdateBrand").hide();
    $("#myModal").modal("show");
  }

  function setFakeFileForInput(fileName, mimeType) {
    const dt = new DataTransfer();
    const dummyFile = new File([""], fileName, { type: mimeType });
    dt.items.add(dummyFile);
    document.getElementById("avatarInput").files = dt.files;
    $("#avatarInput").valid();
  }

  window.handleDetailClick = function (button) {
    const id = $(button).data("id");

    $.ajax({
      url: `/admin/brand/${id}`,
      type: "GET",
      success: function (res) {
        $("#brandId").val(res.id);
        $("#brandCode").val(res.brandCode);
        $("#nameBrand").val(res.name);

        if (avatarDropzone) {
          avatarDropzone.removeAllFiles(true);

          if (res.logoUrl && res.logoUrl.length > 0) {
            // Lấy ảnh đầu tiên thôi
            let url = res.logoUrl;
            let mockFile = { name: url.split("/").pop(), size: 12345 };

            avatarDropzone.emit("addedfile", mockFile);
            avatarDropzone.emit("complete", mockFile);

            // Gán ảnh thật vào thumbnail
            $(mockFile.previewElement)
              .find("img[data-dz-thumbnail]")
              .attr("src", url)
              .css({
                width: "120px",
                height: "120px",
                objectFit: "cover",
                objectPosition: "center",
              });

            avatarDropzone.files.push(mockFile);

            // 👉 THÊM DÒNG NÀY NGAY DƯỚI ĐÂY 👇
            const dt = new DataTransfer();
            const dummyFile = new File([""], mockFile.name, {
              type: "image/jpeg",
            });
            dt.items.add(dummyFile);
            document.getElementById("avatarInput").files = dt.files;
            $("#avatarInput").valid();
          }
        }
        openEditModal(true);
      },
      error: function (err) {
        SwalUtils.error("Lỗi", "Không thể tải dữ liệu sản phẩm");
      },
    });
  };

  function openEditModal(isEditable) {
    $("#brandForm").validate().resetForm();
    $("#modalTitle").text("Chi Tiết Và Cập Nhật");
    $("#btnAddBrand").hide();
    $("#btnUpdate").show();
    $("#myModal").modal("show");
  }

  $("#btnAddBrand").click(function () {
    if (!$("#brandForm").valid()) {
      return;
    }

    SwalUtils.confirm(
      "Xác nhận thêm thương hiệu ?",
      "",
      "Thêm",
      "Hủy"
    ).then((result) => {
      if (result.isConfirmed) {
        const dz = Dropzone.forElement("#avatarDropzone");
        const files = dz.getAcceptedFiles();
        console.log(files[0]);
        const avatarFile = files.length > 0 ? files[0] : null;

        const formData = new FormData();

        formData.append("name", $("#nameBrand").val());

        if (avatarFile) {
          formData.append("logoUrl", avatarFile, avatarFile.name);
          console.log("Name File:", avatarFile.name);
        } else {
          console.log("No file select");
          s;
        }

        $.ajax({
          url: "/admin/brand/add",
          method: "POST",
          processData: false,
          contentType: false,
          data: formData,
          success: function (response) {
            SwalUtils.success("Thành công!", response.message);

            $("#myModal").modal("hide");

            if (avatarDropzone) {
              avatarDropzone.removeAllFiles(true);
            }

            // Stay on current page or go to last page if added
            $.get("/admin/brand/counts").done(function (totalItems) {
              const pageSize = 5;
              const lastPage = Math.max(
                0,
                Math.ceil(totalItems / pageSize) - 1,
              );
              currentPage = lastPage;
              searchBrand(lastPage);
            });
          },
          error: function (xhr) {
            let errorMessage = "Thêm thất bại";
            if (xhr.responseJSON && xhr.responseJSON.message) {
              errorMessage = xhr.responseJSON.message;
            }
            SwalUtils.error("Lỗi!", errorMessage);
          },
        });
      }
    });
  });

  $("#btnUpdateBrand").click(function () {
    if (!$("#brandForm").valid()) {
      return;
    }

    SwalUtils.confirm(
      "Xác Nhận Cập Nhật Thương Hiệu",
      "",
      "Cập Nhật",
      "Hủy"
    ).then((result) => {
      if (result.isConfirmed) {
        const formData = new FormData();

        formData.append("brandCode", $("#brandCode").val().trim());
        formData.append("name", $("#nameBrand").val().trim());
        const brandId = $("#brandId").val();

        const files = avatarDropzone.getAcceptedFiles();
        if (files.length > 0 && files[0] instanceof File) {
          console.log("logoUrl:", files[0]); // Debug
          formData.append("logoUrl", files[0], files[0].name);
        } else {
          console.log("No valid file selected, skipping logoUrl"); // Debug
        }

        $.ajax({
          url: `/admin/brand/${brandId}`,
          type: "PUT",
          data: formData,
          processData: false,
          contentType: false,
          success: function (response) {
            SwalUtils.success("Cập nhật thành công!", response.message);

            $("#myModal").modal("hide");

            // Stay on current page after update
            searchBrand(currentPage);
          },
          error: function (xhr) {
            let errorMessage = "Cập nhật thất bại";
            if (xhr.responseJSON && xhr.responseJSON.message) {
              errorMessage = xhr.responseJSON.message;
            }
            SwalUtils.error("Lỗi!", errorMessage);
          },
        });
      }
    });
  });

  window.toggleStatus = function (id, isActive) {
    const title = isActive
      ? "Bạn có chắc muốn khóa thuong hiệu này?"
      : "Bạn có chắc muốn mở khóa thương hiệu này?";

    SwalUtils.confirm(
      title,
      "",
      "Xác nhận",
      "Hủy",
      {
        icon: "warning",
        customClass: { popup: "swal-pop-zindex" },
        backdrop: `rgba(0, 0, 0, 0.4)`,
      }
    ).then((result) => {
      if (result.isConfirmed) {
        $.ajax({
          url: `/admin/brand/${id}/toggle-status`,
          type: "PUT",
          success: function (data) {
            SwalUtils.success("Thành công", data.message);
            // Stay on current page after status toggle
            searchBrand(currentPage);
          },
          error: function (xhr) {
            SwalUtils.error(
              "Lỗi",
              xhr.responseJSON?.message || "Có lỗi xảy ra"
            );
          },
        });
      }
    });
  };

  // Expose functions to global scope
  window.openAddModal = openAddModal;
});

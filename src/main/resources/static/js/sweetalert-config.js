const SwalConfig = {
  colors: {
    primary: '#1ab394',
    secondary: '#6c757d',
    success: '#28a745',
    error: '#dc3545',
    warning: '#ffc107',
    info: '#17a2b8'
  },

  // Default configuration
  defaults: {
    confirmButtonColor: '#1ab394',
    cancelButtonColor: '#6c757d',
    timer: 2000,
    timerProgressBar: true,
    allowOutsideClick: false,
    allowEscapeKey: true,
    buttonsStyling: true,
    showClass: {
      popup: 'animate__animated animate__fadeInDown'
    },
    hideClass: {
      popup: 'animate__animated animate__fadeOutUp'
    }
  }
};

const SweetAlertUtils = {

  success: function(title, text = '', options = {}) {
    return Swal.fire({
      icon: 'success',
      title: title,
      text: text,
      confirmButtonColor: SwalConfig.colors.primary,
      timer: options.timer || SwalConfig.defaults.timer,
      timerProgressBar: options.timerProgressBar !== false,
      showConfirmButton: options.showConfirmButton !== false,
      ...options
    });
  },

  // Error notification
  error: function(title, text = '', options = {}) {
    return Swal.fire({
      icon: 'error',
      title: title,
      text: text,
      confirmButtonColor: SwalConfig.colors.primary,
      confirmButtonText: options.confirmButtonText || '<i class="fa fa-check"></i> OK',
      ...options
    });
  },

  // Warning notification
  warning: function(title, text = '', options = {}) {
    return Swal.fire({
      icon: 'warning',
      title: title,
      text: text,
      confirmButtonColor: SwalConfig.colors.primary,
      confirmButtonText: options.confirmButtonText || '<i class="fa fa-check"></i> OK',
      ...options
    });
  },

  // Info notification
  info: function(title, text = '', options = {}) {
    return Swal.fire({
      icon: 'info',
      title: title,
      text: text,
      confirmButtonColor: SwalConfig.colors.primary,
      confirmButtonText: options.confirmButtonText || '<i class="fa fa-check"></i> OK',
      showCancelButton: options.showCancelButton || false,
      cancelButtonColor: SwalConfig.colors.secondary,
      cancelButtonText: options.cancelButtonText || '<i class="fa fa-times"></i> Đóng',
      ...options
    });
  },

  confirm: function(title, text = '', confirmText = 'Xác nhận', cancelText = 'Hủy', options = {}) {
    return Swal.fire({
      title: title,
      text: text,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: SwalConfig.colors.primary,
      cancelButtonColor: SwalConfig.colors.secondary,
      confirmButtonText: `<i class="fa fa-check"></i> ${confirmText}`,
      cancelButtonText: `<i class="fa fa-times"></i> ${cancelText}`,
      reverseButtons: true,
      ...options
    });
  },

  confirmDelete: function(title, text = '', confirmText = 'Xóa', cancelText = 'Hủy', options = {}) {
    return Swal.fire({
      title: title,
      text: text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: SwalConfig.colors.error,
      cancelButtonColor: SwalConfig.colors.secondary,
      confirmButtonText: `<i class="fa fa-trash"></i> ${confirmText}`,
      cancelButtonText: `<i class="fa fa-times"></i> ${cancelText}`,
      reverseButtons: true,
      focusCancel: true,
      ...options
    });
  },

  confirmDelete: function(title = 'Xác nhận xóa', text = 'Bạn có chắc chắn muốn xóa? Hành động này không thể hoàn tác!', options = {}) {
    return Swal.fire({
      title: title,
      text: text,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: SwalConfig.colors.error,
      cancelButtonColor: SwalConfig.colors.secondary,
      confirmButtonText: '<i class="fa fa-trash"></i> Xóa',
      cancelButtonText: '<i class="fa fa-times"></i> Hủy',
      reverseButtons: false,
      focusCancel: true,
      ...options
    });
  },

  loading: function(title = 'Đang xử lý...', text = 'Vui lòng chờ trong giây lát') {
    return Swal.fire({
      title: title,
      text: text,
      allowOutsideClick: false,
      allowEscapeKey: false,
      showConfirmButton: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });
  },

  close: function() {
    return Swal.close();
  },

  input: function(title, inputType = 'text', inputPlaceholder = '', options = {}) {
    return Swal.fire({
      title: title,
      input: inputType,
      inputPlaceholder: inputPlaceholder,
      showCancelButton: true,
      confirmButtonColor: SwalConfig.colors.primary,
      cancelButtonColor: SwalConfig.colors.secondary,
      confirmButtonText: '<i class="fa fa-check"></i> Xác nhận',
      cancelButtonText: '<i class="fa fa-times"></i> Hủy',
      inputValidator: options.validator || null,
      ...options
    });
  },

  toast: function(type, message, options = {}) {
    const Toast = Swal.mixin({
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: options.timer || 3000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer);
        toast.addEventListener('mouseleave', Swal.resumeTimer);
      }
    });

    return Toast.fire({
      icon: type,
      title: message,
      ...options
    });
  },

  showConfirmDialog: function(title, confirmText = 'Xác nhận', cancelText = 'Hủy', options = {}) {
    return this.confirm(title, '', confirmText, cancelText, options);
  },

  showWarningConfirmDialog: function(title, confirmText = 'Xác nhận', cancelText = 'Hủy', options = {}) {
    return Swal.fire({
      title: title,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: SwalConfig.colors.warning,
      cancelButtonColor: SwalConfig.colors.secondary,
      confirmButtonText: `<i class="fa fa-check"></i> ${confirmText}`,
      cancelButtonText: `<i class="fa fa-times"></i> ${cancelText}`,
      reverseButtons: true,
      ...options
    });
  },

  showSuccessToast: function(message, options = {}) {
    return this.toast('success', message, options);
  },

  showErrorAlert: function(title, text = '', options = {}) {
    return this.error(title, text, options);
  }
};

const SwalUtils = SweetAlertUtils;

if (typeof Swal !== 'undefined') {
  Swal.mixin(SwalConfig.defaults);
}

if (typeof module !== 'undefined' && module.exports) {
  module.exports = { SwalConfig, SweetAlertUtils, SwalUtils };
}
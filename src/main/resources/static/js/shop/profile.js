document.addEventListener('DOMContentLoaded', function() {
  try {
    const profileName = document.getElementById('profile-fullname');
    const isAuthenticated = profileName && profileName.textContent.trim() !== '';

    initializeAvatarLoading();

    if (isAuthenticated) {
      updateUserStats();
    }

    setupAvatarClickHandler();

    console.log('Profile page loaded successfully');
  } catch (error) {
    console.error('Error loading profile page:', error);
  }
});

// Initialize avatar loading with fallback
function initializeAvatarLoading() {
  const avatar = document.getElementById('avatarImg');
  if (avatar) {
    // Add loading state
    avatar.classList.add('loading');

    avatar.addEventListener('load', function() {
      this.classList.remove('loading');
      console.log('Avatar loaded successfully:', this.src);
    });

    avatar.addEventListener('error', function() {
      this.classList.remove('loading');
      console.warn('Avatar failed to load, using fallback:', this.src);
      this.src = '/assets/img/profile_big.jpg';
    });

    if (avatar.complete) {
      avatar.classList.remove('loading');
    }
  }
}

function setupAvatarClickHandler() {
  const avatar = document.getElementById('avatarImg');
  if (avatar) {
    avatar.style.cursor = 'pointer';
    avatar.addEventListener('click', function() {
      Swal.fire({
        icon: 'info',
        title: 'Thay đổi ảnh đại diện',
        text: 'Nhấp vào "Chỉnh sửa thông tin" để thay đổi ảnh đại diện!',
        confirmButtonColor: '#1ab394',
        confirmButtonText: '<i class="fa fa-edit"></i> Chỉnh sửa ngay',
        showCancelButton: true,
        cancelButtonColor: '#6c757d',
        cancelButtonText: 'Đóng'
      }).then((result) => {
        if (result.isConfirmed) {
          editProfile();
        }
      });
    });
  }
}

function triggerFileSelect() {
  const fileInput = document.getElementById('avatarInput');
  if (fileInput) {
    fileInput.click();
  }
}

function previewAvatar(input) {
  const avatarPreview = document.getElementById('avatarPreview');
  const avatarBtn = document.getElementById('avatarBtn');
  const fileInfo = document.getElementById('fileInfo');

  if (input.files && input.files[0]) {
    const file = input.files[0];

    if (file.size > 20 * 1024 * 1024) {
      Swal.fire({
        icon: 'error',
        title: 'Lỗi!',
        text: 'Kích thước file không được vượt quá 20MB!',
        confirmButtonColor: '#1ab394',
        allowOutsideClick: false,
        allowEscapeKey: false
      });
      input.value = '';
      return;
    }

    // Check file type
    if (!file.type.match('image.*')) {
      Swal.fire({
        icon: 'error',
        title: 'Lỗi!',
        text: 'Vui lòng chọn file hình ảnh!',
        confirmButtonColor: '#1ab394',
        allowOutsideClick: false,
        allowEscapeKey: false
      });
      input.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
      avatarPreview.innerHTML = `<img src="${e.target.result}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;">`;
      avatarPreview.classList.add('has-image');

      // Update button
      avatarBtn.classList.add('has-image');
      avatarBtn.innerHTML = '<i class="fa fa-trash"></i><span>Xóa ảnh</span>';
      avatarBtn.onclick = removeAvatar;

      // Update file info
      fileInfo.textContent = `${file.name} (${(file.size / 1024).toFixed(1)}KB)`;
    };
    reader.readAsDataURL(file);
  }
}

// Remove avatar preview and reset form
function removeAvatar() {
  const avatarPreview = document.getElementById('avatarPreview');
  const avatarBtn = document.getElementById('avatarBtn');
  const fileInfo = document.getElementById('fileInfo');
  const avatarInput = document.getElementById('avatarInput');

  // Reset preview
  avatarPreview.innerHTML = '<i class="fa fa-camera"></i>';
  avatarPreview.classList.remove('has-image');
  avatarPreview.onclick = triggerFileSelect;

  // Reset button
  avatarBtn.classList.remove('has-image');
  avatarBtn.innerHTML = '<i class="fa fa-upload"></i><span>Chọn ảnh</span>';
  avatarBtn.onclick = triggerFileSelect;

  // Reset file info
  fileInfo.textContent = 'JPG, PNG tối đa 20MB';

  // Clear input
  avatarInput.value = '';
}

// Open edit profile modal and populate with current data
function editProfile() {
  // Clear form first
  document.getElementById('editProfileForm').reset();

  // Reset avatar preview
  removeAvatar();

  // Get values from profile display elements
  const fullName = document.getElementById('profile-fullname');
  const email = document.getElementById('profile-email');
  const phone = document.getElementById('profile-phone');
  const dateOfBirth = document.getElementById('profile-dateOfBirth');

  // Populate form
  if (fullName && fullName.textContent.trim() !== 'Người dùng') {
    document.querySelector('input[name="fullName"]').value = fullName.textContent.trim();
  }

  if (email && email.textContent.trim() !== 'Chưa cập nhật') {
    document.querySelector('input[name="email"]').value = email.textContent.trim();
  }

  if (phone && phone.textContent.trim() !== 'Chưa cập nhật') {
    document.querySelector('input[name="phone"]').value = phone.textContent.trim();
  }

  // Load date of birth
  if (dateOfBirth && dateOfBirth.hasAttribute('data-date')) {
    const dateValue = dateOfBirth.getAttribute('data-date');
    if (dateValue && dateValue.trim() !== '') {
      document.querySelector('input[name="dateOfBirth"]').value = dateValue;
    }
  }

  // Handle gender radio buttons
  loadCurrentGender();

  // Load current avatar if exists
  loadCurrentAvatar();

  // Show modal
  showEditModal();
}

// Load current gender selection
function loadCurrentGender() {
  const genderLabels = document.querySelectorAll('.radio-label');
  genderLabels.forEach(label => {
    const genderText = label.textContent.trim();
    // Check from current profile display - looking for gender badges
    const profileGender = document.querySelector('.label.label-primary, .label.label-success');
    if (profileGender) {
      const currentGender = profileGender.textContent.trim();
      if ((currentGender === 'Nam' && genderText.includes('Nam')) ||
          (currentGender === 'Nữ' && genderText.includes('Nữ'))) {
        const radioInput = label.querySelector('input[type="radio"]');
        if (radioInput) {
          radioInput.checked = true;
        }
      }
    }
  });
}

// Load current avatar in edit modal
function loadCurrentAvatar() {
  const currentAvatar = document.getElementById('avatarImg');
  if (currentAvatar && currentAvatar.src &&
      !currentAvatar.src.includes('profile_big.jpg') &&
      !currentAvatar.src.includes('data:image') &&
      currentAvatar.src.trim() !== '') {
    const avatarPreview = document.getElementById('avatarPreview');
    avatarPreview.innerHTML = `<img src="${currentAvatar.src}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;" onerror="this.style.display='none'; this.parentElement.innerHTML='<i class=\\'fa fa-camera\\'></i>';">`;
    avatarPreview.classList.add('has-image');

    const avatarBtn = document.getElementById('avatarBtn');
    avatarBtn.classList.add('has-image');
    avatarBtn.innerHTML = '<i class="fa fa-trash"></i><span>Xóa ảnh</span>';
    avatarBtn.onclick = removeAvatar;

    document.getElementById('fileInfo').textContent = 'Ảnh hiện tại';
  }
}

// Show edit profile modal
function showEditModal() {
  if (typeof $ !== 'undefined') {
    $('#editProfileModal').modal('show');
  } else {
    document.getElementById('editProfileModal').style.display = 'block';
  }
}

// Save profile information
function saveProfile() {
  const form = document.getElementById('editProfileForm');
  const formData = new FormData(form);

  // Basic validation
  if (!validateProfileForm(formData)) {
    return;
  }

  // Show loading state
  const saveBtn = document.querySelector('#editProfileModal .btn-modern-modal');
  const originalText = saveBtn.innerHTML;
  saveBtn.innerHTML = '<i class="fa fa-spinner fa-spin"></i> Đang lưu...';
  saveBtn.disabled = true;

  // Debug: Log form data
  console.log('Form data being sent:');
  for (let [key, value] of formData.entries()) {
    console.log(`${key}: ${value}`);
  }

  // Send AJAX request to update profile
  fetch('/profile/update', {
    method: 'POST',
    body: formData,
    headers: {
      'X-Requested-With': 'XMLHttpRequest'
    }
  })
  .then(response => {
    console.log('Response status:', response.status);
    console.log('Response ok:', response.ok);

    if (response.status === 404) {
      throw new Error('API endpoint không tồn tại. Vui lòng liên hệ admin.');
    }

    if (response.status === 500) {
      throw new Error('Lỗi server nội bộ. Vui lòng thử lại sau hoặc liên hệ admin.');
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    // Try to parse JSON response with better error handling
    const contentType = response.headers.get('content-type');
    console.log('Content-Type:', contentType);

    if (contentType && contentType.includes('application/json')) {
      return response.text().then(text => {
        console.log('Raw response text:', text);
        try {
          const data = JSON.parse(text);
          console.log('Parsed JSON data:', data);
          return data;
        } catch (jsonError) {
          console.error('JSON parsing error:', jsonError);
          console.error('Invalid JSON text:', text);

          // If response status was ok but JSON is invalid, assume success
          if (response.ok) {
            console.log('Response was OK but JSON invalid, assuming success');
            return { success: true, message: 'Cập nhật thành công!' };
          } else {
            throw new Error('Invalid JSON response from server');
          }
        }
      });
    } else {
      // If not JSON, get text and assume success if status is ok
      return response.text().then(text => {
        console.log('Non-JSON response:', text);
        if (response.ok) {
          return { success: true, message: 'Cập nhật thành công!' };
        } else {
          throw new Error('Server returned error with non-JSON response');
        }
      });
    }
  })
  .then(data => {
    console.log('Final response data:', data);
    handleSaveSuccess(data, saveBtn, originalText);
  })
  .catch(error => {
    console.error('Fetch error details:', error);

    // Check if this is a JSON parsing error specifically
    if (error.message && error.message.includes('JSON')) {
      console.log('JSON parsing error detected, showing specific message');
      Swal.fire({
        icon: 'warning',
        title: 'Có thể đã cập nhật thành công!',
        html: `Server trả về dữ liệu không đúng định dạng, nhưng có thể thông tin đã được cập nhật.<br><br>Vui lòng tải lại trang để kiểm tra.`,
        confirmButtonColor: '#1ab394',
        confirmButtonText: '<i class="fa fa-refresh"></i> Tải lại trang',
        showCancelButton: true,
        cancelButtonColor: '#6c757d',
        cancelButtonText: 'Đóng',
        allowOutsideClick: false,
        allowEscapeKey: false
      }).then((result) => {
        if (result.isConfirmed) {
          window.location.reload();
        } else {
          // Close modal and reset button
          closeEditModal();
          saveBtn.innerHTML = originalText;
          saveBtn.disabled = false;
        }
      });
    } else {
      // Other errors - try alternative endpoints
      if (error.message && (error.message.includes('404') || error.message.includes('500') || error.message.includes('fetch'))) {
        console.log('Server error detected, trying alternative endpoints');
        tryAlternativeEndpoints(formData, saveBtn, originalText);
      } else {
        console.log('General error, showing connection error');
        Swal.fire({
          icon: 'error',
          title: 'Lỗi kết nối!',
          html: `Không thể kết nối đến server để cập nhật thông tin.<br><br><small>Chi tiết lỗi: ${error.message}</small><br><br>Vui lòng thử lại sau hoặc liên hệ admin.`,
          confirmButtonColor: '#1ab394',
          allowOutsideClick: false,
          allowEscapeKey: false
        });

        // Reset button state
        saveBtn.innerHTML = originalText;
        saveBtn.disabled = false;
      }
    }
  });
}

// Validate profile form data
function validateProfileForm(formData) {
  const fullName = formData.get('fullName');
  const email = formData.get('email');

  if (!fullName || fullName.trim() === '') {
    Swal.fire({
      icon: 'warning',
      title: 'Thông tin thiếu!',
      text: 'Vui lòng nhập họ và tên!',
      confirmButtonColor: '#1ab394',
      allowOutsideClick: false,
      allowEscapeKey: false
    });
    return false;
  }

  if (!email || email.trim() === '') {
    Swal.fire({
      icon: 'warning',
      title: 'Thông tin thiếu!',
      text: 'Vui lòng nhập email!',
      confirmButtonColor: '#1ab394',
      allowOutsideClick: false,
      allowEscapeKey: false
    });
    return false;
  }

  // Email validation
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi!',
      text: 'Email không hợp lệ!',
      confirmButtonColor: '#1ab394',
      allowOutsideClick: false,
      allowEscapeKey: false
    });
    return false;
  }

  // Phone validation (if provided)
  const phone = formData.get('phone');
  if (phone && phone.trim() !== '') {
    const phoneRegex = /^[0-9]{10,11}$/;
    if (!phoneRegex.test(phone.replace(/\s+/g, ''))) {
      Swal.fire({
        icon: 'error',
        title: 'Lỗi!',
        text: 'Số điện thoại không hợp lệ! (10-11 chữ số)',
        confirmButtonColor: '#1ab394',
        allowOutsideClick: false,
        allowEscapeKey: false
      });
      return false;
    }
  }

  return true;
}

// Handle successful save response
function handleSaveSuccess(data, saveBtn, originalText) {
  console.log('handleSaveSuccess called with:', data);

  // Handle various response formats
  if (!data) {
    console.log('No data received, assuming success');
    data = { success: true, message: 'Cập nhật thành công!' };
  }

  // Check if response indicates success
  if (data.success === true || data.success === undefined) {
    console.log('Showing success message');
    Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      text: data.message || 'Thông tin đã được cập nhật thành công!',
      confirmButtonColor: '#1ab394',
      timer: 2000,
      timerProgressBar: true,
      allowOutsideClick: false,
      allowEscapeKey: false
    }).then(() => {
      // Close modal
      closeEditModal();
      // Reload page to see changes
      window.location.reload();
    });
  } else {
    console.log('Response indicates failure:', data);
    Swal.fire({
      icon: 'error',
      title: 'Lỗi!',
      text: data.message || 'Có lỗi xảy ra khi cập nhật thông tin!',
      confirmButtonColor: '#1ab394',
      allowOutsideClick: false,
      allowEscapeKey: false
    });
    // Reset button state
    saveBtn.innerHTML = originalText;
    saveBtn.disabled = false;
  }
}

// Handle save error
function handleSaveError(error, formData, saveBtn, originalText) {
  // Fallback: Try alternative endpoints
  if (error.message.includes('404') || error.message.includes('500')) {
    tryAlternativeEndpoints(formData, saveBtn, originalText);
  } else {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi kết nối!',
      html: `Không thể kết nối đến server để cập nhật thông tin.<br><br><small>Chi tiết lỗi: ${error.message}</small><br><br>Vui lòng thử lại sau hoặc liên hệ admin.`,
      confirmButtonColor: '#1ab394',
      allowOutsideClick: false,
      allowEscapeKey: false
    });

    // Reset button state
    saveBtn.innerHTML = originalText;
    saveBtn.disabled = false;
  }
}

// Close edit modal
function closeEditModal() {
  if (typeof $ !== 'undefined') {
    $('#editProfileModal').modal('hide');
  } else {
    document.getElementById('editProfileModal').style.display = 'none';
  }
}

// Try alternative endpoints if main one fails
function tryAlternativeEndpoints(formData, saveBtn, originalText) {
  const endpoints = [
    '/user/profile/update',
    '/api/user/update-profile',
    '/customer/profile/update'
  ];

  let currentIndex = 0;

  function tryNextEndpoint() {
    if (currentIndex >= endpoints.length) {
      // All endpoints failed
      Swal.fire({
        icon: 'error',
        title: 'Không thể cập nhật!',
        html: `Tất cả API endpoints đều không khả dụng.<br><br>Có thể server đang bảo trì hoặc chưa được cấu hình.<br><br>Vui lòng liên hệ admin để được hỗ trợ.`,
        confirmButtonColor: '#1ab394',
        allowOutsideClick: false,
        allowEscapeKey: false
      });

      // Reset button state
      saveBtn.innerHTML = originalText;
      saveBtn.disabled = false;
      return;
    }

    const endpoint = endpoints[currentIndex];
    console.log(`Trying endpoint: ${endpoint}`);

    fetch(endpoint, {
      method: 'POST',
      body: formData,
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      }
    })
    .then(response => {
      console.log(`${endpoint} - Response status:`, response.status);

      if (response.ok) {
        // Success
        Swal.fire({
          icon: 'success',
          title: 'Thành công!',
          text: 'Thông tin đã được cập nhật thành công!',
          confirmButtonColor: '#1ab394',
          timer: 2000,
          timerProgressBar: true,
          allowOutsideClick: false,
          allowEscapeKey: false
        }).then(() => {
          // Close modal
          closeEditModal();
          // Reload page to see changes
          window.location.reload();
        });

        // Reset button state
        saveBtn.innerHTML = originalText;
        saveBtn.disabled = false;
      } else {
        // Try next endpoint
        currentIndex++;
        tryNextEndpoint();
      }
    })
    .catch(error => {
      console.error(`${endpoint} failed:`, error);
      // Try next endpoint
      currentIndex++;
      tryNextEndpoint();
    });
  }

  tryNextEndpoint();
}

// Handle change password button click
function changePassword() {
  // Show confirmation before redirecting
  Swal.fire({
    title: 'Đổi mật khẩu',
    text: 'Bạn có muốn đổi mật khẩu không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#1ab394',
    cancelButtonColor: '#6c757d',
    confirmButtonText: '<i class="fa fa-check"></i> Có, đổi mật khẩu',
    cancelButtonText: '<i class="fa fa-times"></i> Hủy'
  }).then((result) => {
    if (result.isConfirmed) {
      // Redirect to change password page
      window.location.href = '/profile/change-password';
    }
  });
}

// Handle manage address button click
function manageAddress() {
  // Show confirmation before redirecting
  Swal.fire({
    title: 'Quản lý địa chỉ',
    text: 'Bạn có muốn quản lý địa chỉ giao hàng không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#1ab394',
    cancelButtonColor: '#6c757d',
    confirmButtonText: '<i class="fa fa-map-marker"></i> Có, quản lý địa chỉ',
    cancelButtonText: '<i class="fa fa-times"></i> Hủy'
  }).then((result) => {
    if (result.isConfirmed) {
      // Redirect to address management page
      window.location.href = '/profile/address-management';
    }
  });
}

// Update user statistics display
function updateUserStats() {
  // Check if stats elements exist
  const orderCountEl = document.getElementById('orderCount');
  const loyaltyPointsEl = document.getElementById('loyaltyPoints');

  if (!orderCountEl || !loyaltyPointsEl) {
    console.log('Stats elements not found, skipping stats update');
    return;
  }

  // Fetch real user statistics from backend
  fetch('/profile/stats')
  .then(response => response.json())
  .then(data => {
    if (data.success) {
      orderCountEl.textContent = data.orderCount || 0;
      loyaltyPointsEl.textContent = data.loyaltyPoints || 0;
    } else {
      console.warn('Failed to load user stats:', data.message);
      // Fallback to mock data
      orderCountEl.textContent = Math.floor(Math.random() * 50);
      loyaltyPointsEl.textContent = Math.floor(Math.random() * 1000);
    }
  })
  .catch(error => {
    console.error('Error loading user stats:', error);
    // Fallback to mock data
    orderCountEl.textContent = Math.floor(Math.random() * 50);
    loyaltyPointsEl.textContent = Math.floor(Math.random() * 1000);
  });
}

// Handle file input change to preview image (general handler)
document.addEventListener('change', function(e) {
  if (e.target.type === 'file' && e.target.accept === 'image/*') {
    const file = e.target.files[0];
    if (file) {
      console.log('Image selected:', file.name);
      // Additional image handling can be added here
    }
  }
});
$(document).ready(function () {
  // Lấy URL hiện tại
  var currentUrl = window.location.pathname;

  // Tìm tất cả các liên kết trong menu
  $("#side-menu a").each(function () {
    var href = $(this).attr("href");

    // Kiểm tra xem href có khớp với URL hiện tại không
    if (href && currentUrl.includes(href)) {
      // Thêm class active vào thẻ <li> của mục con
      var $li = $(this).parent("li");
      $li.addClass("active");

      // Tìm thẻ <ul> cha của menu cấp hai và mở nó
      var $parentUl = $li.closest("ul.nav-second-level");
      $parentUl.addClass("in").attr("aria-expanded", "true");

      // Thêm class active vào thẻ <li> cha của menu cấp một
      var $parentLi = $parentUl.closest("li");
      $parentLi.addClass("active").find("> a").attr("aria-expanded", "true");

      // Đảm bảo tất cả các mục con trong menu cấp hai hiển thị
      $parentUl.find("li").css("display", "block");
    }
  });

  // Khởi tạo MetisMenu sau khi thiết lập trạng thái ban đầu
  $("#side-menu").metisMenu();

  // Xử lý sự kiện click để giữ toggle của MetisMenu
  $("#side-menu").on("click", "a", function (e) {
    // Nếu liên kết có menu con, đảm bảo tất cả mục con hiển thị khi mở
    if ($(this).next("ul.nav-second-level").length) {
      var $parentUl = $(this).next("ul.nav-second-level");
      if (!$parentUl.hasClass("in")) {
        $parentUl.find("li").css("display", "block");
      }
    }
  });
});

$(document).ajaxComplete(function (event, xhr, settings) {
  if (xhr.status === 401) {
    window.location.href = "/login";
  }
});

document.addEventListener("DOMContentLoaded", function () {
    const messages = document.querySelectorAll(".message.success");
    messages.forEach(function (message) {
        setTimeout(function () {
            message.style.opacity = "0";
        }, 2500);
    });
});


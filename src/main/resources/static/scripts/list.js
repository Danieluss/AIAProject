$(document).ready(function($) {
    $(".clickableRow").click(function() {
        window.location = this.getAttribute('href');
    });
});

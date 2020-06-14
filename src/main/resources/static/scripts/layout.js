$(document).ready(() => {
    $('button[type="submit"]').on('click', () => {
        $(this).prop('disabled', true);
        setTimeout(() => {
            $(this).prop('disabled', false);
        }, 200);
    })
});

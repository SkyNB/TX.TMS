$(function () {
    // Initialize the jQuery File Upload widget:
    var validate = $('#fileupload').validate();
    $('#fileupload').fileupload({
        disableImageResize: false,
        autoUpload: false,
        disableImageResize: /Android(?!.*Chrome)|Opera/.test(window.navigator.userAgent),
        maxFileSize: 1000000,
        singleFileUploads: true,
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
        submit: function (e) {
            if (!$("#customerCode").val()) {
                e.preventDefault();
                var files = document.getElementsByName("files[]");
                $("#customerCode").closest("div.form-group").addClass("has-error")
            }
        },
        success: function(result) {
            if(!result.success && result.message)
                App.toastr(result.message);
        }
    });
    $("#customerCode").on("change", function () {
        $("button.start.btn").removeAttr("disabled");
        if ($("#customerCode").val()) {
            $("#customerCode").closest("div.form-group").removeClass("has-error")
        }
    });
});
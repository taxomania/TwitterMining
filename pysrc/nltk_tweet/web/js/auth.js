$(document).ready(function(){
    $('.input_control').attr('checked',true);
    $('.controlled').attr('disabled', true);
    $('.input_control').click(function(){
        if ($('.controlled').attr('disabled') == true){
            $('.controlled').attr('disabled', false);
        } else {
            $('.controlled').attr('disabled', true);
        }
    });
});
 

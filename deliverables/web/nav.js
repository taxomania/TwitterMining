$(document).ready(function()
{
	$(".content").css("display","none");
	$("#home").css("display","block");
	showViaLink($("nav a"));
});

function showViaLink(array)
{
	array.each(function(i)
	{
		$(this).click(function()
		{
			$(".content").css("display","none");
			var target = $(this).attr("href");
			$(target).slideDown("slow");
		});
	});
}
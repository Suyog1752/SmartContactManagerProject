console.log("this a is script file")
const toggleSidebar=()=>{
 if ($(".sidebar").is(":visible")) {
  //true
  //band karan hai
$(".sidebar").css("display","none");
$(".content").css("margin-left","0%");


 }else{
  //false
  $(".sidebar").css("display","block");
  $(".content").css("margin-left","20%");
 }

};

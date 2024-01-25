console.log("this a is script file");
const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    //true
    //band karan hai
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    //false
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  // console.log("searching...");
  let query = $("#search-input").val();

  if (query == "") {
    $(".search-result").hide();
  } else {
    

    //sending request to sever
    let url = `http://localhost:8181/search/${query}`;
    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        //data access...
        
        let text = `<div class='list-group'>`;

        data.forEach((contact) => {
          text += `<a  href='/user/${contact.cid}/contact' class='list-group-item list-group-item-action'>${contact.name}</a>`;
        });

        text += `</div>`;

        $(".search-result").html(text);
        $(".search-result").show();
      });
  }
};

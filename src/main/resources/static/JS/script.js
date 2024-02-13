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


//first request to server to create order
const paymentStart=()=>{
 console.log("payment started...");
 let amount=$("#payment_field").val();
 console.log(amount);
 if (amount=='' || amount==null) {
// alert("Amount is required ?")
swal("Failed !", "Amount is required ?", "error");
return;
 }

 //code for send request to server
 //we will use ajox to send request to sever to create order
 
 $.ajax({
  url: '/user/create_order',
  data: JSON.stringify({ amount: amount, info: 'order_request' }),
  contentType: 'application/json',
  type: 'POST',
  dataType: 'json',
  success: function(response) {
    console.log(response);
    if (response.status == 'created') {
      let options = {
        key: 'rzp_test_VKLrVJm4KS74R1',
        amount: response.amount,
        currency: 'INR',
        name: 'Smart Contact Manager',
        description: 'Donation',
        image: 'https://www.learncodewithdurgesh.com/_next/image?url=%2F_next%2Fstatic%2Fmedia%2Flcwd_logo.45da3818.png&w=1080&q=75',
        order_id: response.id,
        handler: function(response) {
          console.log(response.razorpay_payment_id);
          console.log(response.razorpay_order_id);
          console.log(response.razorpay_signature);
          console.log('Payment is successful !!');
          // alert('Congratulations! Your Payment is Successful !!');
          swal("Good job!", "Your Payment is Successful !!", "success");
        },
        prefill: {
          name: '',
          email: '',
          contact: ''
        },
        notes: {
          address: 'LearnCodeWithDurgesh'
        },
        theme: {
          color: '#3399cc'
        },
        payment_method: {
          method: 'upi'
        }
      };

      let rzp = new Razorpay(options);
      rzp.on('payment.failed', function(response) {
        console.log(response.error.code);
        console.log(response.error.description);
        console.log(response.error.source);
        console.log(response.error.step);
        console.log(response.error.reason);
        console.log(response.error.metadata.order_id);
        console.log(response.error.metadata.payment_id);
        alert('Oops! Your payment failed !!');
        swal("Failed !", "Oops! Your payment failed !!", "error");
      });
      rzp.open();
    }
  },
  error: function(error) {
    console.log(error);
    alert('Something went wrong !!');
  }
});



};

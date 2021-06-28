
function handlePaymentResult(resultDataString) {
    //let resultData = JSON.parse(resultDataString);
    console.log("handle Payment response");
    console.log(resultDataString["message"]);

    if (resultDataString["message"] === "Success") {
        window.location.replace("confirm.html");
    }
    else {
        console.log("show error message");
        console.log(resultDataString["message"]);
        // $("#payment_error_message").text(resultDataString["message"]);
        alert(resultDataString["message"]);
    }
}

function submitPayment(formSubmitEvent) {
    console.log("submit Payment");
    formSubmitEvent.preventDefault();
    $.post(
        "api/payment",
        // Serialize the login form to the data sent by POST request
        $("#payment").serialize(),
        (resultDataString) => handlePaymentResult(resultDataString)
    );
}

$("#payment").submit((event) => submitPayment(event));

function print_total_price(resultDataString) {
    total_price = resultDataString["total_price"];
    document.getElementById("price").innerHTML = resultDataString["total_price"];
}

$.ajax({
    dataType: "json",
    method: "POST",
    url: "api/payment",
    success: (resultData) => print_total_price(resultData)
});
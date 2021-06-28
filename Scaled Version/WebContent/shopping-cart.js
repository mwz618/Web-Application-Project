let total = 0;

function handleCartResult(resultData) {
    let shopping_cart = $("#Shopping_cart_Body");
    shopping_cart.empty();

    total = parseInt(resultData[resultData.length - 1]["total"]);

    // text id = title !!
    let rowHtml = "<tr>";
    for (let i = 0; i < resultData.length - 1; i++) {
        rowHtml += "<td>" + resultData[i]["title"] + "</td>";
        rowHtml += "<td>" + "$ 20" + "</td>";
        rowHtml += "<td>" + "<button class='minus' onclick='minus(\"" + resultData[i]["movieId"] + "\")'>-</button>" +
            "<input type='text' id=\'" + resultData[i]["movieId"] + "\' type='number' maxlength='2' value=\'" + resultData[i]["qty"] + "\'>" +
            "<button class='plus' onclick='plus(\"" + resultData[i]["movieId"] + "\")'>+</button>" + "</td>";
        rowHtml += "<td>" + "<span>$ </span><span id='" + "price_"+resultData[i]["movieId"] + "'>" + resultData[i]["price"] +"</span>" + "</td>";
        rowHtml += "<td>" + "<p onclick='remove_item(\"" + resultData[i]["movieId"] + "\")'>Delete</p>" + "</td>";
        rowHtml += "</tr>";
    }

    shopping_cart.append(rowHtml);
    shopping_cart.append("<tr><td colspan=\"5\"><label id=\"price\">Total is: <label id='totalPrice'>0</label></label></td></tr>" +
        "<tr><td colspan=\"5\"><button id='checkout_button'><a style='text-decoration:none' href='payment.html'>Proceed to payment</a></button></td></tr>");
    document.getElementById("totalPrice").innerHTML = total;
}

function minus(movieId) {
    console.log("minus " + movieId);
    let qty = parseInt(document.getElementById(movieId).valueOf().value) - 1;
    if (qty < 0) { qty = 0; document.getElementById(movieId).valueOf().value = qty; }
    else {
        document.getElementById(movieId).valueOf().value = qty;
        document.getElementById("price_" + movieId).innerText = parseInt(document.getElementById("price_" + movieId).innerText) - 20;
        total -= 20;
        document.getElementById("totalPrice").innerHTML = total;
        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/shopping-cart?minusItem=" + movieId,
            success: (resultData) => handleCartResult(resultData)
        });
    }
}

function plus(movieId) {
    console.log("plus " + movieId);
    let qty = parseInt(document.getElementById(movieId).valueOf().value) + 1;
    document.getElementById(movieId).valueOf().value = qty;
    document.getElementById("price_" + movieId).innerText = parseInt(document.getElementById("price_" + movieId).innerText) + 20;
    total += 20;
    document.getElementById("totalPrice").innerHTML = total;
    $.ajax({
        dataType: "json",
        method: "GET",
        url: "api/shopping-cart?addItem=" + movieId,
        success: (resultData) => handleCartResult(resultData)
    });
}

function remove_item(movieId) {
    console.log("remove " + movieId);
    if (!confirm("Do you want to delete this item?")) {
        window.event.returnValue = false;
    }
    else {
        $.ajax({
            dataType: "json",
            method: "GET",
            url: "api/shopping-cart?removeItem=" + movieId,
            success: (resultData) => handleCartResult(resultData)
        });
    }
}

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/shopping-cart",
    success: (resultData) => handleCartResult(resultData)
});
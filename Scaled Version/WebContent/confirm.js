
function confirm(resultData) {
    console.log("display confirm page\n");
    console.log(resultData);
    let confirm = jQuery("#Confirm_Body");
    confirm.empty();

    let rowHTML = "";
    for (let i = 0; i < resultData.length - 1; i++) {
        rowHTML += "<tr>"
                + "<td style=\"text-align:center\">" + resultData[i]['sale_id'] + "</td>"
                + "<td style=\"text-align:center\">" + resultData[i]['movie_id'] + "</td>"
                + "<td style=\"text-align:center\">" + resultData[i]['title'] + "</td>"
                + "<td style=\"text-align:center\">" + resultData[i]['qty'] + "</td>"
                + "<td style=\"text-align:center\">" + resultData[i]['price'] + "</td>";
        rowHTML += "</tr>";
    }
    confirm.append(rowHTML);

    document.getElementById("price").innerHTML = resultData[resultData.length - 1]['total_price'];
}


$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/confirm-page",
    success: (resultData) => confirm(resultData)
});
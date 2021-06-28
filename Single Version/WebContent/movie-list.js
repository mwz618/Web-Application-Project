let currentPage = getParameterByName("currentPage") == "" ? 0 : getParameterByName("currentPage");
let numRecords = getParameterByName("numRecords");

function handleStarResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");
    let movieListElement = jQuery("#Movie_List_Body");
    movieListElement.empty();

    // {movieId : set(genres)}
    let genresMap = new Map();
    // {movieId : set(starId)
    let movieStarsMap = new Map();
    // {starId : [starName, starMoviesNum]}
    let starsNamesMap = new Map();
    // {movieId : [title, year, director, rating}
    let singleAttrs = new Map();

    for (let i = 0; i < resultData.length - 1; i++) {
        let m = resultData[i]['movieId'];
        if (!genresMap.has(m)) genresMap.set(m, new Set())
        genresMap.set(m, genresMap.get(m).add(resultData[i]['genreName']))
        if (!movieStarsMap.has(m)) movieStarsMap.set(m, new Set())
        movieStarsMap.set(m, movieStarsMap.get(m).add(resultData[i]['starId']))
        starsNamesMap.set(resultData[i]['starId'], [resultData[i]['starName'], resultData[i]['starMoviesNum']]);
        if (singleAttrs.has(m)) continue;
        singleAttrs.set(m, [resultData[i]['title'], resultData[i]['year'], resultData[i]['director'], resultData[i]['rating']]);
    }

    for (let [key, value] of singleAttrs){
        let rowHTML = "";

        rowHTML += "<tr>"
            + "<td style=\"text-align:center\"><a href=\"single-movie.html?id=" + key + "\">" + value[0] + "</td>"
            + "<td style=\"text-align:center\">" + value[1] + "</td>"
            + "<td style=\"text-align:center\">" + value[2] + "</td>";

        rowHTML += "<td><ul>";
        let sortedGenresArray = Array.from(genresMap.get(key)).sort((a, b) => a.localeCompare(b));
        for (let i = 0; i < Math.min(3, sortedGenresArray.length); i++){
            rowHTML += "<li>" + sortedGenresArray[i] + "</li>";
        }

        // genresMap.get(key).forEach(g => rowHTML += "<li>" + g + "</li>");
        rowHTML += "</ul></td>";

        rowHTML += "<td><ul>";

        let sortedStarsArray = Array.from(movieStarsMap.get(key)).sort((a, b) => starsNamesMap.get(a)[0].localeCompare(starsNamesMap.get(b)[0])).sort((a, b) => starsNamesMap.get(b)[1] -starsNamesMap.get(a)[1]);
        for (let i = 0; i < Math.min(3, sortedStarsArray.length); i++){
            rowHTML += "<li>" + "<a href=\"single-star.html?id=" + sortedStarsArray[i] + "\">" + starsNamesMap.get(sortedStarsArray[i])[0] + "</a>" + "</li>" ;
        }
        // movieStarsMap.get(key).forEach( s => rowHTML += "<li>" + "<a href=\"single-star.html?id=" + s + "\">" + starsNamesMap.get(s) + "</a>" + "</li>" );
        rowHTML += "</ul></td>";

        rowHTML += "<td style=\"text-align:center\">" + value[3] + "</td>";

        rowHTML += "<td><button class='add_to_cart' align='center' onclick='Add_to_cart(\"" + key + "###" + value[0] + "\")'>Add to Cart</button></td>";

        rowHTML += "</tr>";

        movieListElement.append(rowHTML);
    }

    let temp = resultData[resultData.length - 1];
    $("#firstSort").val(temp["firstSort"]);
    $("#firstSortOrder").val(temp["firstSortOrder"]);
    $("#secondSort").val(temp["secondSort"]);
    $("#secondSortOrder").val(temp["secondSortOrder"]);
    $("#numRecords").val(temp["numRecords"]);
}

// paras: movieId, title
function Add_to_cart(item) {
    var m = item.split("###");
    console.log("add movieId: " + m[0] + " title: " + m[1] + " to cart");
    alert("Add " + m[1] + " to cart Successfully");
    $.ajax("api/top-rating-movies", {
        method: "get",
        data: {addMovieId: m[0], addTitle: m[1]}
    });
}

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return "";  // change null to "";
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function showing(showing_request) {
    showing_request.preventDefault();
    queryParameters = "";
    numRecords = $("#numRecords").find(":selected").text();
    queryParameters += "?currentPage=" + currentPage;
    console.log("current_page is: " + currentPage);
    queryParameters += getParameterByName("title") == "" ? "" : "&title=" + getParameterByName("title");
    queryParameters += getParameterByName("year") == "" ? "" : "&year=" + getParameterByName("year");
    queryParameters += getParameterByName("director") == "" ? "" : "&director=" + getParameterByName("director");
    queryParameters += getParameterByName("starName") == "" ? "" : "&starName=" + getParameterByName("starName");
    queryParameters += getParameterByName("genre") ==  "" ? "" : "&genre=" + getParameterByName("genre");
    queryParameters += getParameterByName("acronym") == "" ? "" : "&acronym=" + getParameterByName("acronym");
    queryParameters += "&numRecords=" + numRecords;
    queryParameters +=  "&sortBy=" + $("#firstSort").find(":selected").text() + " " + $("#firstSortOrder").find(":selected").text() + ", "
        + $("#secondSort").find(":selected").text() + " " + $("#secondSortOrder").find(":selected").text();
    //alert(queryParameters);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/top-rating-movies" + queryParameters,
        success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

$("#showing").submit((event) => showing(event));

$(document).ready(function(){
    $("#prev").click(function(){
        if (parseInt(currentPage) - 1 < 0) {
           return; 
        }
        queryParameters = "";
        currentPage = parseInt(currentPage) - 1 + "";
        queryParameters += "?currentPage=" + currentPage;
        console.log("current_page is: " + (parseInt(currentPage - 1) + ""));
        queryParameters += getParameterByName("title") == "" ? "" : "&title=" + getParameterByName("title");
        queryParameters += getParameterByName("year") == "" ? "" : "&year=" + getParameterByName("year");
        queryParameters += getParameterByName("director") == "" ? "" : "&director=" + getParameterByName("director");
        queryParameters += getParameterByName("starName") == "" ? "" : "&starName=" + getParameterByName("starName");
        queryParameters += getParameterByName("genre") ==  "" ? "" : "&genre=" + getParameterByName("genre");
        queryParameters += getParameterByName("acronym") == "" ? "" : "&acronym=" + getParameterByName("acronym");
        queryParameters += "&numRecords=" + numRecords;
        queryParameters +=  "&sortBy=" + $("#firstSort").find(":selected").text() + " " + $("#firstSortOrder").find(":selected").text() + ", "
            + $("#secondSort").find(":selected").text() + " " + $("#secondSortOrder").find(":selected").text();
        //alert(queryParameters);
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/top-rating-movies" + queryParameters,
            success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    });
});

$(document).ready(function(){
    $("#next").click(function() {
        queryParameters = "";
        currentPage = parseInt(currentPage) + 1 + "";
        queryParameters += "?currentPage=" + currentPage;
        console.log("current_page is: " + (parseInt(currentPage) + 1 + ""));
        queryParameters += getParameterByName("title") == "" ? "" : "&title=" + getParameterByName("title");
        queryParameters += getParameterByName("year") == "" ? "" : "&year=" + getParameterByName("year");
        queryParameters += getParameterByName("director") == "" ? "" : "&director=" + getParameterByName("director");
        queryParameters += getParameterByName("starName") == "" ? "" : "&starName=" + getParameterByName("starName");
        queryParameters += getParameterByName("genre") ==  "" ? "" : "&genre=" + getParameterByName("genre");
        queryParameters += getParameterByName("acronym") == "" ? "" : "&acronym=" + getParameterByName("acronym");
        queryParameters += "&numRecords=" + numRecords;
        // title is enough?
        queryParameters +=  "&sortBy=" + $("#firstSort").find(":selected").text() + " " + $("#firstSortOrder").find(":selected").text() + ", "
            + $("#secondSort").find(":selected").text() + " " + $("#secondSortOrder").find(":selected").text();
        //alert(queryParameters);
        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/top-rating-movies" + queryParameters,
            success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    });
});

$(document).ready(function(){
    $("#submitOrder").click(function() {
        queryParameters = "";
        queryParameters += "?currentPage=" + currentPage;
        console.log("current_page is: " + currentPage);
        queryParameters += getParameterByName("title") == "" ? "" : "&title=" + getParameterByName("title");
        queryParameters += getParameterByName("year") == "" ? "" : "&year=" + getParameterByName("year");
        queryParameters += getParameterByName("director") == "" ? "" : "&director=" + getParameterByName("director");
        queryParameters += getParameterByName("starName") == "" ? "" : "&starName=" + getParameterByName("starName");
        queryParameters += getParameterByName("genre") ==  "" ? "" : "&genre=" + getParameterByName("genre");
        queryParameters += getParameterByName("acronym") == "" ? "" : "&acronym=" + getParameterByName("acronym");
        queryParameters += "&numRecords=" + numRecords
        queryParameters +=  "&sortBy=" + $("#firstSort").find(":selected").text() + " " + $("#firstSortOrder").find(":selected").text() + ", "
            + $("#secondSort").find(":selected").text() + " " + $("#secondSortOrder").find(":selected").text();

        jQuery.ajax({
            dataType: "json", // Setting return data type
            method: "GET", // Setting request method
            url: "api/top-rating-movies" + queryParameters,
            success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
        });
    });
});


let queryParameters = "";
// queryParameters += "?currentPage=" + currentPage;
queryParameters += getParameterByName("currentPage") == "" ? "?currentPage=0" : "?currentPage=" + getParameterByName("currentPage");
queryParameters += getParameterByName("title") == "" ? "" : "&title=" + getParameterByName("title");
queryParameters += getParameterByName("year") == "" ? "" : "&year=" + getParameterByName("year");
queryParameters += getParameterByName("director") == "" ? "" : "&director=" + getParameterByName("director");
queryParameters += getParameterByName("starName") == "" ? "" : "&starName=" + getParameterByName("starName");
queryParameters += getParameterByName("genre") ==  "" ? "" : "&genre=" + getParameterByName("genre");
queryParameters += getParameterByName("acronym") == "" ? "" : "&acronym=" + getParameterByName("acronym");
queryParameters += "&numRecords=" + getParameterByName("numRecords");
console.log("current_page is: " + currentPage);

// title is enough?
if (getParameterByName("sortBy") == null || getParameterByName("sortBy") == "") {
    queryParameters +=  "&sortBy=" + $("#firstSort").find(":selected").text() + " " + $("#firstSortOrder").find(":selected").text() + ", "
        + $("#secondSort").find(":selected").text() + " " + $("#secondSortOrder").find(":selected").text();
}
else { queryParameters +=  "&sortBy=" + getParameterByName("sortBy"); }

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-rating-movies" + queryParameters,
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

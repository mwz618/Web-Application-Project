/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */


/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let SingleStarElement = jQuery("#Single_Star_Body");
    SingleStarElement.empty();
    // moives = [[moiveId, movieTitle, movieYear, director]...]
    let movies = [];

    for (let i = 0; i < resultData.length - 1; i++){
        movies.push([resultData[i]["movieId"], resultData[i]["movieTitle"], resultData[i]["movieYear"], resultData[i]["director"]]);
    }

    let rowHTML = "";
    rowHTML += "<tr>"
        + "<td style=\"text-align:center\">" + resultData[0]['starName'] + "</td>"
        + "<td style=\"text-align:center\">" + resultData[0]['birthYear'] + "</td>";

    rowHTML += "<td><ul>";
    movies.forEach(m => rowHTML += "<li>" + "<a href=\"single-movie.html?id=" + m[0] + "\">" + m[1] + "</a>" + "</li>");
    rowHTML += "</ul></td>";

    rowHTML += "<td><ul>";
    movies.forEach(m => rowHTML += "<li>" + m[2] + "</li>");
    rowHTML += "</ul></td>";

    rowHTML += "<td><ul>";
    movies.forEach(m => rowHTML += "<li>" + m[3] + "</li>");
    rowHTML += "</ul></td>";

    rowHTML += "</tr>";

    SingleStarElement.append(rowHTML);

    let prevPage = resultData[resultData.length - 1];
    $("#Back_to_Movie_List").html("<a href = 'movie-list.html?currentPage=" + prevPage["currentPage"] +
                                                            "&numRecords=" + prevPage["numRecords"] +
                                                            "&title=" + prevPage["title"] +
                                                            "&year=" + prevPage["year"] +
                                                            "&director=" + prevPage["director"] +
                                                            "&starName=" + prevPage["starName"] +
                                                            "&genre=" + prevPage["genre"] +
                                                            "&acronym=" + prevPage["acronym"] +
                                                            "&sortBy=" + prevPage["sortBy"] + "'>Back to Movie List</a>");
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let starId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-star?id=" + starId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let browseInfo = jQuery("#BrowseType");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length - 1; i+=5) {
        let row = '<div class="box-wrap">';
        for (let j = 0; j < 5; j++){

            if (i + j <  resultData.length - 1){
                row += '<div class="box">';
                row += "<a style='font-size:30px' href=\"movie-list.html?currentPage=0&numRecords=" + numRecords + "&" + browseType + "=" + resultData[i + j] + "\">" + resultData[i + j] + "</a>";
                row += '</div>';
            }
        }
        row += '</div>';
        // Append the row created to the table body, which will refresh the page
        browseInfo.append(row);
    }

    if (resultData[resultData.length - 1] === "employee") {
        document.getElementById("employee").innerHTML = "<li class=\"nav-item\"><a class=\"nav-link\" href=\"_dashboard.html\">Dashboard</a></li>";
    }
}

let browseType = getParameterByName("browseType");
let numRecords = document.getElementById("numRecords").value;
console.log("numRecords: ", numRecords);

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/browse?browseType=" + getParameterByName("browseType"), // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});



// auto-complete

/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here
    if (sessionStorage.getItem(query) != null){
        console.log("using cached result");
        handleLookupAjaxSuccess(sessionStorage.getItem(query), query, doneCallback);
        return;
    }

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data
    console.log("sending AJAX request to backend Java Servlet")
    jQuery.ajax({
        "method": "GET",
        // generate the request url from the query.
        // escape the query string to avoid errors caused by special characters
        "url": "title-suggestion?query=" + escape(query),
        "success": function(data) {
            // pass the data, query, and doneCallback function into the success handler
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup successful")

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here
    if (sessionStorage.getItem(query) == null){
        sessionStorage.setItem(query, data);
    }

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movieId"])
    window.location.replace("single-movie.html?id=" + suggestion["data"]["movieId"]);
}


/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3
});


/*
 * do normal full text search if no suggestion is selected
 */
function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here
    window.location.replace("single-movie.html?id=" + query);
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button

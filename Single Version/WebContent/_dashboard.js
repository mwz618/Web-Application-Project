function handleStarResult(resultDataString) {
    //let resultData = JSON.parse(resultDataString);
    console.log("handle adding star response");
    console.log(resultDataString["message"]);

    $("#add_star").text(resultDataString["message"]);
}

function handleMovieResult(resultDataString) {
    //let resultData = JSON.parse(resultDataString);
    console.log("handle adding movie response");
    console.log(resultDataString["message"]);

    $("#add_movie").text(resultDataString["message"]);
}

function addStar(formSubmitEvent) {
    console.log("add a star");
    formSubmitEvent.preventDefault();
    $.post(
        "api/dashboard",
        // Serialize the login form to the data sent by POST request
        $("#add_star_form").serialize() + "&addType=star",
        (resultDataString) => handleStarResult(resultDataString)
    );
}

function addMovie(formSubmitEvent) {
    console.log("add a movie");
    formSubmitEvent.preventDefault();
    $.post(
        "api/dashboard",
        // Serialize the login form to the data sent by POST request
        $("#add_movie_form").serialize() + "&addType=movie",
        (resultDataString) => handleMovieResult(resultDataString)
    );
}

function handleResult(resultData){
    if (resultData["access"] === "no"){
        window.location.replace("main-page.html?numRecords=10&browseType=genre");
    }
}

$("#add_star_form").submit((event) => addStar(event));
$("#add_movie_form").submit((event) => addMovie(event));

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/dashboard",
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "ConfirmPage", urlPatterns = "/api/confirm-page")
public class ConfirmPage extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            User userInfo = (User)request.getSession().getAttribute("user");
            HashMap<String, Integer> cart = userInfo.getCart();
            HashMap<String, String> movieIdTitle = userInfo.getMovieIdTitle();
            int total_price = userInfo.totalPrice();
            JsonArray jsonArray = new JsonArray();

            synchronized (cart) {
                for (String movieId : cart.keySet()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("sale_id", userInfo.getSaleId());
                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("title", userInfo.getTitle(movieId));
                    jsonObject.addProperty("qty", String.valueOf(cart.get(movieId)));
                    jsonObject.addProperty("price", String.valueOf(cart.get(movieId) * 20));

                    jsonArray.add(jsonObject);

                    userInfo.setSaleId(String.valueOf(Integer.parseInt(userInfo.getSaleId()) + 1));
                }
            }
            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("total_price", total_price);

            jsonArray.add(jsonObject1);
            out.write(jsonArray.toString());
            response.setStatus(200);

            userInfo.clearCart();

        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
}

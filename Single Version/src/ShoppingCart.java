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
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCart extends HttpServlet {

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            User userInfo = (User)request.getSession().getAttribute("user");
            String remove_item = request.getParameter("removeItem");
            String add_item = request.getParameter("addItem");
            String minus_item = request.getParameter("minusItem");

            System.out.println("remove item is " + remove_item);
            System.out.println("add item is " + add_item);
            System.out.println("minus item is " + minus_item);

            if (remove_item != null) {
                userInfo.removeFromCart(remove_item);
            }
            if (add_item != null) {
                String id = userInfo.getMovieIdTitle().get(add_item);
                userInfo.addToCart(add_item, id);
                System.out.println(userInfo.totalPrice());
                out.close();
                return;
            }
            if (minus_item != null) {
                userInfo.minusFromCart(minus_item);
                System.out.println(userInfo.totalPrice());
                out.close();
            }

            JsonArray jsonArray = new JsonArray();
            for (String movieId : userInfo.getCart().keySet()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movieId", movieId);
                jsonObject.addProperty("title", userInfo.getTitle(movieId));
                jsonObject.addProperty("qty", String.valueOf(userInfo.getCart().get(movieId)));
                jsonObject.addProperty("price", String.valueOf(userInfo.getCart().get(movieId) * 20));
                jsonArray.add(jsonObject);
            }

            JsonObject jsonObject1 = new JsonObject();
            jsonObject1.addProperty("total", String.valueOf(userInfo.totalPrice()));
            System.out.println("Total is : " + String.valueOf(userInfo.totalPrice()));
            jsonArray.add(jsonObject1);
            // write JSON string to output
            out.write(jsonArray.toString());
            // set response status to 200 (OK)
            response.setStatus(200);
            
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

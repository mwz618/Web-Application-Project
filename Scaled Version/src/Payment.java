

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "Payment", urlPatterns = "/api/payment")
public class Payment extends HttpServlet{

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb_write");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        String first_name = request.getParameter("first_name");
        String last_name = request.getParameter("last_name");
        String card_number = request.getParameter("card_number");
        String expire = request.getParameter("expire");

        System.out.println(first_name);
        System.out.println(last_name);
        System.out.println(card_number);
        System.out.println(expire);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            User userInfo = (User)request.getSession().getAttribute("user");
            int total_price = userInfo.totalPrice();

            if (first_name == null && last_name == null && card_number == null && expire == null) {
                System.out.println(total_price);
                JsonObject jsonObject1 = new JsonObject();
                jsonObject1.addProperty("total_price", total_price);
                out.write(jsonObject1.toString());
                // set response status to 200 (OK)
                response.setStatus(200);
                out.close();
                return;
            }

            // Get a connection from dataSource
            Connection conn = dataSource.getConnection();
            // Construct a query with parameter represented by "?"
            String query = "select cu.id as c_id " +
                    "from creditcards cc, customers cu " +
                    "where cc.id = ? and cc.firstName = ? and cc.lastName = ? and cc.expiration = ? and cu.ccid = cc.id;";
            System.out.println(query);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, card_number);
            statement.setString(2, first_name);
            statement.setString(3, last_name);
            statement.setString(4, expire);
            ResultSet rs = statement.executeQuery();
            JsonObject jsonobject = new JsonObject();

            if (rs.next()) {
                jsonobject.addProperty("message", "Success");

                // get max id from sales table and customer id from customers table
                PreparedStatement statement2 = conn.prepareStatement("select max(id) as max_id from sales;");
                ResultSet rs2 = statement2.executeQuery();
                rs2.next();
                int saleId = rs2.getInt("max_id");
                int customerId = rs.getInt("c_id");

                // get current date
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                ZoneId zid = ZoneId.of("America/Los_Angeles");
                LocalDateTime now = LocalDateTime.now(zid);
                String currentDate = dtf.format(now);
                userInfo.setSaleId(String.valueOf(saleId + 1));

                // insert into sales table, waiting for your scrutiny
                HashMap<String, Integer> cart = userInfo.getCart();
                synchronized (cart) {
                    for (String movieId : cart.keySet()) {
                        saleId += 1;
                        String query3 = "insert into sales values (?, ?, ?, ?);";

                        PreparedStatement statement3 = conn.prepareStatement(query3);
                        statement3.setInt(1, saleId);
                        statement3.setInt(2, customerId);
                        statement3.setString(3, movieId);
                        statement3.setString(4, currentDate);
                        statement3.executeUpdate();
                        statement3.close();

                        System.out.println(query3);
                    }
                }

                rs2.close();
                statement2.close();
            }
            else {
                jsonobject.addProperty("message", "Payment Failed!");
            }
            System.out.println(total_price);
            jsonobject.addProperty("total_price", total_price);
            out.write(jsonobject.toString());
            // set response status to 200 (OK)
            response.setStatus(200);

            rs.close();
            statement.close();
            conn.close();
        } catch (Exception e) {
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("message", e.getMessage());
            out.write(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.doPost(request, response);
    }
}

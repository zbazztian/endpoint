package org.github.endpoint;

import org.github.endpoint.DataSource;
import org.github.endpoint.EndpointBase;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.*;

@RestController
public class Endpoint extends EndpointBase {

    private final DataSource dataSource;

    public Endpoint(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostMapping("/endpoints/userinfo")
    @ResponseBody
    public String completed(@RequestParam String userid, @RequestParam String login_count, HttpServletRequest request) throws IOException {
        return getInfo(login_count, userid);
    }

    protected String getInfo(String login_count, String accountName) {
        String queryString = "SELECT * From user_data WHERE Login_Count = ? and userid= " + accountName;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement query = connection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            int count = 0;
            try {
                count = Integer.parseInt(login_count);
            } catch (Exception e) {
                return "Could not parse: " + login_count + " to a number"
                        + "<br> Your query was: " + queryString.replace("?", login_count);
            }

            query.setInt(1, count);
            try {
                ResultSet results = query.executeQuery();

                if ((results != null) && (results.first() == true)) {
                    ResultSetMetaData resultsMetaData = results.getMetaData();
                    StringBuilder output = new StringBuilder();

                    output.append(Utils.writeTable(results, resultsMetaData));
                    results.last();

                    if (results.getRow() >= 6) {
                        return "Your query was: " + queryString.replace("?", login_count) + ":  " + output.toString();
                    } else {
                        return output.toString() + "<br> Your query was: " + queryString.replace("?", login_count);
                    }

                } else {
                    return "Your query was: " + queryString.replace("?", login_count);
                }
            } catch (SQLException sqle) {
                return sqle.getMessage() + "<br> Your query was: " + queryString.replace("?", login_count);
            }
        } catch (Exception e) {
            return this.getClass().getName() + " : " + e.getMessage() + "<br> Your query was: " + queryString.replace("?", login_count);
        }
    }
}

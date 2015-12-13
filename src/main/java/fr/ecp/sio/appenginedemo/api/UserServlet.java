package fr.ecp.sio.appenginedemo.api;

import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * A servlet to handle all the requests on a specific user
 * All requests with path matching "/users/*" where * is the id of the user are handled here.
 */
public class UserServlet extends JsonServlet {

    public static final Logger LOG = Logger.getLogger(UserServlet.class.getSimpleName());

    // A GET request should simply return the user
    @Override
    protected User doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        LOG.info("User Servlet");
        return findUserOfRequest(req);

    }

    // A POST request could be used by a user to edit its own account
    // It could also handle relationship parameters like "followed=true"
    @Override
    protected User doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Get the user as below
        // TODO: Apply some changes on the user (after checking for the connected user)
        // TODO: Handle special parameters like "followed=true" to create or destroy relationships
        // TODO: Return the modified user

        User user = findUserOfRequest(req);

        // user to follow
        String path = req.getPathInfo();
        String[] parts = path.split("/");
        long id =  Long.parseLong(parts[1]);

        if(req.getParameterNames().nextElement().toString() == "followed") {
            // parse the bool (true or false)
            boolean parameter = Boolean.parseBoolean(req.getParameter("followed"));
            // 1er id : follower / 2eme id : followed
            UsersRepository.setUserFollowed(user.id, id, parameter);
        }

        return user;
    }

    // A user can DELETE its own account
    @Override
    protected Void doDelete(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Security checks
        // TODO: Delete the user, the messages, the relationships
        // A DELETE request shall not have a response body
        return null;
    }

    private static User findUserOfRequest(HttpServletRequest req) throws ApiException {

        // TODO: Extract the id of the user from the last part of the path of the request
        String path = req.getPathInfo();
        String[] parts = path.split("/");

        // TODO: Check if this id is syntactically correct
        // TODO: Not found?
        //String para = req.getParameterNames().nextElement().toString();
        //idUser = Integer.parseInt(req.getParameter(para));

        if (parts[1].contains("me")) { // authorization needed
            return getAuthenticatedUser(req);
        }

        long id =  Long.parseLong(parts[1]);

        // if url finish with an id auth
        if (UsersRepository.getUser(id) == getAuthenticatedUser(req)) {
            return getAuthenticatedUser(req);
        }
        else {
            // return the user of the id specified with hide private info
            User modifiedUser = UsersRepository.getUser(id);
            modifiedUser.email = "";
            modifiedUser.password = "";
            return modifiedUser;
        }


    }

}
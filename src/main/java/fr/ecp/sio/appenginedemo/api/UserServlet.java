package fr.ecp.sio.appenginedemo.api;



import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.blobstore.BlobKey;



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
        User user = findUserOfRequest(req);
            return user;
    }

    // A POST request could be used by a user to edit its own account
    // It could also handle relationship parameters like "followed=true"
    @Override
    protected User doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        // TODO: Get the user as below

        // TODO: Apply some changes on the user (after checking for the connected user)
        // TODO: Handle special parameters like "followed=true" to create or destroy relationships
        // TODO: Return the modified user

        User user = findUserOfRequest(req); // token needed to be authentificated

        // si l'id de l'url et l'id du user authentifié sont diff, le user enregistré est celui de l'url
        // Problème à régler.

        // retrieve the user to follow ( users/id )
        String path = req.getPathInfo();
        String[] parts = path.split("/");
        long id =  Long.parseLong(parts[1]);

        if(req.getParameter("followed") != null ){
        //if(req.getParameterNames().nextElement().toString() == "followed") {
            // parse the bool (true or false)
            boolean parameter = Boolean.parseBoolean(req.getParameter("followed"));
            // 1er id : follower / 2eme id : followed
            UsersRepository.setUserFollowed(user.id, id, parameter);
        }

        /*if(req.getParameter("newAvatar") != null ){

            BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

            Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
            List<BlobKey> blobKeys = blobs.get("myFile");

            if (blobKeys == null || blobKeys.isEmpty()) {
                res.sendRedirect("/");
            } else {
                res.sendRedirect("/serve?blob-key=" + blobKeys.get(0).getKeyString());
            }

            String urlImage =
        }
        */



        return user;
    }

    // A user can DELETE its own account
    @Override
    protected Void doDelete(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Security checks
        // TODO: Delete the user, the messages, the relationships
        // A DELETE request shall not have a response body


        /*GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        Plus plus = new Plus.builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("Google-PlusSample/1.0")
                .build();
        */

        return null;
    }

    private static User findUserAuth(HttpServletRequest req) throws  ApiException {

        String path = req.getPathInfo();
        String[] parts = path.split("/");
        User user = getAuthenticatedUser(req);
       return  null;

    }


    private static User findUserOfRequest(HttpServletRequest req) throws ApiException {

        // TODO: Extract the id of the user from the last part of the path of the request
        // TODO: Check if this id is syntactically correct
        // TODO: Not found?
        String path = req.getPathInfo();
        String[] parts = path.split("/");

        User user = getAuthenticatedUser(req);
        if (user == null) {
            // return the user of the id specified with hide private info
            if (ValidationUtils.validateIdString(parts[1])) {
                long id = Long.parseLong(parts[1]);
                user = UsersRepository.getUser(id);
                user.email = "";
                user.password = "";
                return user;
            }
            // if parsing error
            return null;

        } else {
        // if authentification succeed

            // if id ="me" => return the auth user
            if (ValidationUtils.validateIdMe(parts[1])) {
                return user;
            }
            // check id and parse it
            if (ValidationUtils.validateIdString(parts[1])) {
                long id = Long.parseLong(parts[1]);
                // user wants edit its own account if the id of the url and id of the authentificated user is the same.
                if (UsersRepository.getUser(id).id == user.id) {
                    return getAuthenticatedUser(req);
                }
                // return the user of the id specified with hide private info
                user = UsersRepository.getUser(id);
                user.email = "";
                user.password = "";
                return user;
            }
        }
        return null;
    }
}


    //String para = req.getParameterNames().nextElement().toString();
    //idUser = Integer.parseInt(req.getParameter(para));
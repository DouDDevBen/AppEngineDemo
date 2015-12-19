package fr.ecp.sio.appenginedemo.api;



import com.google.appengine.api.images.Image;
import fr.ecp.sio.appenginedemo.data.MessagesRepository;
import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.Message;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import com.google.appengine.api.blobstore.*;
import com.google.appengine.api.blobstore.BlobKey;
import org.apache.commons.codec.digest.DigestUtils;


/**
 * A servlet to handle all the requests on a specific user
 * All requests with path matching "/users/*" where * is the id of the user are handled here.
 */
public class UserServlet extends JsonServlet {

    public static final Logger LOG = Logger.getLogger(UserServlet.class.getSimpleName());

    // A GET request should simply return the user
    @Override
    protected User doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {

        // return the user Auth if authentification match with url Id or "me" patern
        // else return user of url Id with hide info (no password and email)
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

        User userAuth = getAuthenticatedUser(req); // token needed to be authentificated
        if (userAuth == null) throw new ApiException(500, "accessDenied", "authorization required");

        // Manage a follow action ( or not follow)------------------------------
        if(req.getParameter(ValidationUtils.PARAMETER_FOLLOWED) != null ){

            // retrieve the user set in the url ( users/id )
            String path = req.getPathInfo();
            String[] parts = path.split("/");
            long urlId =  Long.parseLong(parts[1]);

            // parse the bool (true or false)
            boolean parameter = Boolean.parseBoolean(req.getParameter("followed"));
            // Update relationship between both users
            // 1er id : follower / 2eme id : followed
            UsersRepository.setUserFollowed(userAuth.id, urlId, parameter);
        }

        // Manage password update -----------------------------------------------
        if(req.getParameter(ValidationUtils.PARAMETER_UPDATE_PWD) != null) {
            //Update passWord
            String password = req.getParameter(ValidationUtils.PARAMETER_UPDATE_PWD);

            if (!ValidationUtils.validatePassword(password)) {
                throw new ApiException(400, "invalidPassword", "Password did not match the specs");
            }
            userAuth.password = DigestUtils.sha256Hex(password + userAuth.id);
        }

        // Manage email update --------------------------------------------------
        if(req.getParameter(ValidationUtils.PARAMETER_UPDATE_EMAIL) != null) {

            //Update email if User is authentificated
            String mail = req.getParameter(ValidationUtils.PARAMETER_UPDATE_EMAIL);

            if (!ValidationUtils.validateEmail(mail)) {
                throw new ApiException(400, "invalidEmail", "Invalid email");
            }
            if (UsersRepository.getUserByEmail(mail) != null) {
                throw new ApiException(400, "duplicateEmail", "Duplicate email");
            }

            userAuth.email = mail;
        }

        UsersRepository.saveUser(userAuth);
        return userAuth;
    }

    // A user can DELETE its own account
    // Check the Auth user
    // Deleted all his messages
    // Deleted user in repository
    // Return the user information to confirm.
    @Override
    protected User doDelete(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: Security checks
        // TODO: Delete the user, the messages, the relationships
        // A DELETE request shall not have a response body
        User authUser = findUserOfRequest(req);
        //To be sure that the user return is Auth and not the url-id
        if (!authUser.password.isEmpty()) {

            // For instance we can just iterate on a whole Repository Message list
            // to delete all messages of the user.
            // We could create a list of Reference of all Messages in the User
            // model to simplify the edition
            List<Message> messages = MessagesRepository.getMessages();
            for (Message message : messages) {
            User matchedUser = message.user.get();
                if (matchedUser.id == authUser.id) {
                    MessagesRepository.deleteMessage(authUser.id);
                }
            }
            // user deleted once all messages have been deleted.
            UsersRepository.deleteUser(authUser.id);
        }
        return authUser;

    }

    // Identification method of a user //
    // Return the full user if Authentification is validated AND matched with the id on the url (users/id/...)
    // If id of the url is substituated by "me", Authentification succed : Returr also Auth User
    // For all other configuration : RETURN the user with hide personal information
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
            // Id not found
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
                    return user;
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

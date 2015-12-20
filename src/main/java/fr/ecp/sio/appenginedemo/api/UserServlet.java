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

        // DONE: Get the user as below
        // DONE: Apply some changes on the user (after checking for the connected user)
        // DONE: Handle special parameters like "followed=true" to create or destroy relationships
        // DONE: Return the modified user

        User userAuth = getAuthenticatedUser(req);
        if (userAuth == null) throw new ApiException(500, "accessDenied", "authorization required");

        // Manage a follow action ( or not follow)------------------------------
        if(req.getParameter(ValidationUtils.PARAMETER_FOLLOWED) != null ){

            // retrieve the user set in the url ( users/id )
            // id = "me" is not allowed here : we can't follow ourself
            long urlId = getIdUrl(req);
            if (urlId == 0) throw new ApiException(400, "Url ID not found ", "Url Id not found");

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
            if (!ValidationUtils.validateEmail(mail)) throw new ApiException(400, "invalidEmail", "Invalid email");
            if (UsersRepository.getUserByEmail(mail) != null) throw new ApiException(400, "duplicateEmail", "Duplicate email");
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
        // DONE: Security checks
        // DONE: Delete the user, the messages, the relationships
        // A DELETE request shall not have a response body
        User authUser = getAuthenticatedUser(req);
        if (authUser == null ) throw new ApiException(500, "User Not found", "User Not found");

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
        // return null if delete action succeed
        return null;
    }
}

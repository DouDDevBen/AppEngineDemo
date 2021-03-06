package fr.ecp.sio.appenginedemo.api;

import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.MD5Utils;
import fr.ecp.sio.appenginedemo.utils.TokenUtils;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;
import org.apache.commons.codec.digest.DigestUtils;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A servlet to handle all the requests on a list of users
 * All requests on the exact path "/users" are handled here.
 */
public class UsersServlet extends JsonServlet {

    public static final Logger LOG = Logger.getLogger(UsersServlet.class.getSimpleName());

    // A GET request should return a list of users
    @Override
    protected Object doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // DONE: define parameters to search/filter users by login, with limit, order...
        // DONE: define parameters to get the followings and the followers of a user given its id
        // Init default values
        long idUser = 0;
        String continuationToken = "";
        int limit = 5;
        User user = null;
        String followedBy = req.getParameter(ValidationUtils.PARAMETER_FOLLOWEDBY);
        String followerOf = req.getParameter(ValidationUtils.PARAMETER_FOLLOWEROF);

        // By default
        if (followedBy == null && followerOf == null) {
            List<User> usersList = UsersRepository.getUsers().users;
            return hideInfoForListUser(usersList);
        }

        // Check limit parameter
        // default limit = 5 users
        if (req.getParameter(ValidationUtils.PARAMETER_LIMIT) != null) {
            limit = Integer.parseInt(req.getParameter(ValidationUtils.PARAMETER_LIMIT));
        }
        // check continuation Token
        // If empty, we will return the top list (followers or followed users)
        if (req.getParameter(ValidationUtils.PARAMETER_CONTINUATION_TOKEN) != null) {
            continuationToken = req.getParameter(ValidationUtils.PARAMETER_CONTINUATION_TOKEN);
        }

        // If no authentification , use id url
        if (ValidationUtils.validateIdString(followedBy)) {
            idUser = Long.parseLong(followedBy);
            user = UsersRepository.getUser(idUser);
            if (user == null ) throw new ApiException(400, "User Id not found ", "Url Id not found");
            return handleRequiereList(followedBy, continuationToken, user, limit);
        } else if (ValidationUtils.validateIdString(followerOf)) {
            idUser = Long.parseLong(followerOf);
            user = UsersRepository.getUser(idUser);
            if (user == null ) throw new ApiException(400, "User Id not found ", "Url Id not found");
            return handleRequiereList(followerOf, continuationToken, user, limit);

            //  If url-id didn't match Pattern ID string
        } else if (ValidationUtils.validateIdMe(followerOf)){
            user = getAuthenticatedUser(req);
            if (user == null ) throw new ApiException(400, "User Auth not found ", "User Auth not found");
            return handleRequiereList(followerOf, continuationToken, user, limit);
        } else if (ValidationUtils.validateIdMe(followedBy)) {
            user = getAuthenticatedUser(req);
            if (user == null) throw new ApiException(400, "User Auth not found ", "User Auth not found");
            return handleRequiereList(followedBy, continuationToken, user, limit);
        }
        // By default
        return UsersRepository.getUsers().users;
    }

    // Method in charge of calling the appropriate list of Users
    // PARA type : PARAMETER_FOLLOWEDBY or PARAMETER_FOLLOWEROF
    // PARA token : continuation token ( cursor on the list)
    // PARA user : user auth
    // PARA limit : limitation of users members returned
    // RETURN : List of (Cursor + List of Users)
    protected Object handleRequiereList(String type, String token, User user, int limit ) {

        //List<Object> maList = new ArrayList<>();

        List<User> usersList;
        if (token == "") {
            token = TokenUtils.generateToken(user.id);
            if (type == ValidationUtils.PARAMETER_FOLLOWEDBY ) {
                usersList = UsersRepository.getUserFollowers(user.id, limit).users;
            } else {
                usersList =  UsersRepository.getUserFollowed(user.id, limit).users;
            }
        } else { // If no continuation token , return the top list
            if (type == ValidationUtils.PARAMETER_FOLLOWEROF ) {
                usersList =  UsersRepository.getUserFollowers(token, limit).users;
            } else {
                usersList =  UsersRepository.getUserFollowed(token, limit).users;
            }
            // Generate a new token
            token = TokenUtils.generateToken(user.id);
        }
        // Return a list working with the first element : Cursor token for further GET
        // and a second element with the whole UserList found.
        //maList.add(token);
        // hide private info
        //maList.add(hideInfoForListUser(usersList));
        //return maList;

        UsersRepository.UsersList usersAndCursorList  = new UsersRepository.UsersList(usersList, token);
        return  usersAndCursorList;


    }

    // A POST request can be used to create a user
    // We can use it as a "register" endpoint; in this case we return a token to the client.
    @Override
    protected String doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        // The request should be a JSON object describing a new user
        User user = getJsonRequestBody(req, User.class);
        if (user == null) {
            throw new ApiException(400, "invalidRequest", "Invalid JSON body");
        }

        // Perform all the usul checkings
        if (!ValidationUtils.validateLogin(user.login)) {
            throw new ApiException(400, "invalidLogin", "Login did not match the specs");
        }
        if (!ValidationUtils.validatePassword(user.password)) {
            throw new ApiException(400, "invalidPassword", "Password did not match the specs");
        }
        if (!ValidationUtils.validateEmail(user.email)) {
            throw new ApiException(400, "invalidEmail", "Invalid email");
        }
        if (UsersRepository.getUserByLogin(user.login) != null) {
            throw new ApiException(400, "duplicateLogin", "Duplicate login");
        }
        if (UsersRepository.getUserByEmail(user.email) != null) {
            throw new ApiException(400, "duplicateEmail", "Duplicate email");
        }

        // Explicitly give a fresh id to the user (we need it for next step)
        user.id = UsersRepository.allocateNewId();

        // DONE: find a solution to receive an store profile pictures
        // The avatar cannot be upload when the account is created
        // It will be managed by the BlobServlet ( in 2 phases)
        // Simulate an avatar image using Gravatar API
        user.avatar = "http://www.gravatar.com/avatar/" + MD5Utils.md5Hex(user.email) + "?d=wavatar";

        // Hash the user password with the id a a salt
        user.password = DigestUtils.sha256Hex(user.password + user.id);

        // Persist the user into the repository
        UsersRepository.saveUser(user);

        // Create and return a token for the new user
        return TokenUtils.generateToken(user.id);

    }

    protected static List<User> hideInfoForListUser(List<User> userList) {
        for (User u : userList) {
            u.password = "";
            u.email = "";
        }
        return userList;
    }


}

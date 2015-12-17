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
    protected List<User> doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // TODO: define parameters to search/filter users by login, with limit, order...
        // TODO: define parameters to get the followings and the followers of a user given its id

        long idUser = 0;
        String continuationToken = "";
        int limit = 5;

        // Constant String are for the most defining in ValidationUtils
        if (req.getParameter(ValidationUtils.PARAMETER_FOLLOWEDBY) != null) {
            idUser = Integer.parseInt(req.getParameter(ValidationUtils.PARAMETER_FOLLOWEDBY));
        } else if ( req.getParameter(ValidationUtils.PARAMETER_FOLLOWEROF) != null ) {
            idUser = Integer.parseInt(req.getParameter(ValidationUtils.PARAMETER_FOLLOWEROF));
        } else {
            // Get all Users if there is not one of the below parameters
            return UsersRepository.getUsers().users;
        }

        // Retrieve User by id
        User user = UsersRepository.getUser(idUser);

        // Check limit parameter
        // default limit = 5 users
        if (req.getParameter(ValidationUtils.PARAMETER_LIMIT) != null) {
            limit = Integer.parseInt(req.getParameter(ValidationUtils.PARAMETER_LIMIT));
        }

        // check continuation Token
        // If empty, we will return the top list.
        // si il est vide et n'est pas renseigné , on retournera le début de la liste voulue
        if (req.getParameter(ValidationUtils.PARAMETER_CONTINUATION_TOKEN) != null) {
            continuationToken = req.getParameter(ValidationUtils.PARAMETER_CONTINUATION_TOKEN);
        }

        // Calling appropriate Users list.
        if (req.getParameter(ValidationUtils.PARAMETER_FOLLOWEDBY) != null) {
            return handleRequiereList(ValidationUtils.PARAMETER_FOLLOWEDBY, continuationToken, user, limit);
        } else {
            return handleRequiereList(ValidationUtils.PARAMETER_FOLLOWEROF, continuationToken, user, limit);
        }

    }


    // Method in charge of calling the appropriate list of Users
    // PARA type : PARAMETER_FOLLOWEDBY or PARAMETER_FOLLOWEROF
    // PARA token : continuation token ( cursor on the list)
    // PARA user : user auth
    // PARA limit : limitation of users members returned
    // RETURN : List of Users
    protected List<User> handleRequiereList(String type, String token, User user, int limit ) {
        List<User> users = null;

        if (token == "") {   //renvoie d'un token en typeOfListUser pour la prochaine pagination

            token = TokenUtils.generateToken(user.id);
            if (type == ValidationUtils.PARAMETER_FOLLOWEDBY ) {
                return UsersRepository.getUserFollowers(user.id, limit).users;
            } else {
                return UsersRepository.getUserFollowed(user.id, limit).users;
            }

        } else {      //renvoie la suite de la liste d'utilisateur suivi

            if (type == ValidationUtils.PARAMETER_FOLLOWEROF ) {
                users = UsersRepository.getUserFollowers(token, limit).users;
            } else {
                users = UsersRepository.getUserFollowed(token, limit).users;
            }
            token = TokenUtils.generateToken(user.id);
            // envoyer ce nouveau token avec les users...
            return users;
        }
    }


    // A POST request can be used to create a user
    // We can use it as a "register" endpoint; in this case we return a token to the client.
    @Override
    protected String doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        LOG.info("User Servlet");

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

        // TODO: find a solution to receive an store profile pictures
        // Simulate an avatar image using Gravatar API
        user.avatar = "http://www.gravatar.com/avatar/" + MD5Utils.md5Hex(user.email) + "?d=wavatar";

        // Hash the user password with the id a a salt
        user.password = DigestUtils.sha256Hex(user.password + user.id);

        // Persist the user into the repository
        UsersRepository.saveUser(user);

        // Create and return a token for the new user
        return TokenUtils.generateToken(user.id);

    }

}

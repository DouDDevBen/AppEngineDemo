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
import java.util.logging.Level;
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

        int idUser;
        //req.getParameter("followedBy")
        //req.getParameter("followerOf")
        //attention Le premier paramètre traité doit être followedBy ou followerOf
        String para = req.getParameterNames().nextElement().toString();
        idUser = Integer.parseInt(req.getParameter(para));
        User user = UsersRepository.getUser(idUser);

        int limit = Integer.parseInt(req.getParameter("limit"));
        String continuationToken = "";

        if (!req.getParameter("continuationToken").isEmpty()) {
            user.avatar = "continuationToken retrieve";
            continuationToken = req.getParameter("continuationToken");
        }

        if (para.contains("followedBy")) {
            if (continuationToken == "") {
                //renvoie d'un token en para pour la prochaine pagination
                continuationToken = TokenUtils.generateToken(idUser);
                return UsersRepository.getUserFollowed(user.id, limit).users;
            } else {

                //renvoie la suite de la liste d'utilisateur suivi
                List<User> users = UsersRepository.getUserFollowed(continuationToken, limit).users;
                //génération d'un nouveau ContinuationToken a renvoyer avec la réponse
                continuationToken = TokenUtils.generateToken(idUser);
                // envoyer ce nouveau token avec les users...
                return users;
            }
        } else if (para.contains("followerOf")) {
            if (continuationToken == "") {
                //renvoie d'un token en para pour la prochaine pagination
                continuationToken = TokenUtils.generateToken(idUser);

                return UsersRepository.getUserFollowers(user.id, limit).users;
            } else {

                //renvoie la suite de la liste d'utilisateur suivi
                List<User> users = UsersRepository.getUserFollowers(continuationToken, limit).users;
                continuationToken = TokenUtils.generateToken(idUser);
                return users;
            }
        } else {

            // renvoie tous les utilisateurs par défaut
            List<User> test = new ArrayList<>();
            user.email = para;
            user.password = "Merde";
            test.add(user);
            return test;
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

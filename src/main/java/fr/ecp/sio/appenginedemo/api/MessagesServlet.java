package fr.ecp.sio.appenginedemo.api;

import com.googlecode.objectify.Ref;
import fr.ecp.sio.appenginedemo.data.MessagesRepository;
import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.Message;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A servlet to handle all the requests on a list of messages
 * All requests on the exact path "/messages" are handled here.
 */
public class MessagesServlet extends JsonServlet {

    // A GET request should return a list of messages
    @Override
    protected List<Message> doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // DONE: filter the messages that the user can see (security!)
        // DOne: filter the list based on some parameters (order, limit, scope...)
        // DOne: e.g. add a parameter to get the messages of a user given its id (i.e. /messages?author=256439)

        User userAuth = getAuthenticatedUser(req);
        if (userAuth == null) {
            throw new ApiException(500, "accessDenied", "authorization required");
        }

        String urlId = req.getParameter("author");
        // return all messages if the user of urlID is followed by AuthUser.
        if (ValidationUtils.validateIdString(urlId) &&
                UsersRepository.isUserFollowUser(userAuth.id, Long.parseLong(urlId))){
            List<Message> messageList = MessagesRepository.getMessagesForId(Long.parseLong(urlId));
            return hideInfoForListMessage(messageList);
        }

        // By default, if no author parameter, we return all messages of all Users followed.
        List<User> userList = UsersRepository.getUserFollowed(userAuth.id, 100).users;
        List<Message> messageList = new ArrayList<> ();
        for (User user : userList) {
            messageList.addAll(MessagesRepository.getMessagesForId(user.id));
        }
        return hideInfoForListMessage(messageList);
    }

    // A POST request on a collection endpoint should create an entry and return it
    @Override
    protected Message doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        User userAuth = getAuthenticatedUser(req);

        // The request should be a JSON object describing a new message
        Message message = getJsonRequestBody(req, Message.class);
        if (message == null) {
            throw new ApiException(400, "invalidRequest", "Invalid JSON body");
        }

        // DONE: validate the message here (minimum length, etc.)
        if (!ValidationUtils.validateMessage(message.text)) {
            throw new ApiException(400, "invalidMessage", "Text too long or not found");
        }

        // Some values of the Message should not be sent from the client app
        // Instead, we give them here explicit value
        message.user = Ref.create(getAuthenticatedUser(req));
        message.date = new Date();
        message.id = null;

        // Our message is now ready to be persisted into our repository
        // After this call, our repository should have given it a non-null id
        MessagesRepository.insertMessage(message);

        return message;
    }

    protected static List<Message> hideInfoForListMessage(List<Message> messageList) {
        for (Message u : messageList) {
            u.user.get().password = "";
            u.user.get().email = "";
        }
        return messageList;
    }

}

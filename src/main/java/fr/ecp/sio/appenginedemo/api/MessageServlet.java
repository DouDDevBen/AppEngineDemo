package fr.ecp.sio.appenginedemo.api;

import fr.ecp.sio.appenginedemo.data.MessagesRepository;
import fr.ecp.sio.appenginedemo.data.UsersRepository;
import fr.ecp.sio.appenginedemo.model.Message;
import fr.ecp.sio.appenginedemo.model.User;
import fr.ecp.sio.appenginedemo.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A servlet to handle all the requests on a specific message
 * All requests with path matching "/messages/*" where * is the id of the message are handled here.
 */
public class MessageServlet extends JsonServlet {

    // A GET request should simply return the message
    @Override
    protected Message doGet(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // DONE: Extract the id of the message from the last part of the path of the request
        User userAuth = getAuthenticatedUser(req);
        if (userAuth == null ) throw new ApiException(500, "accessDenied", "authorization required");

        // DONE: Check if this id is syntactically correct
        long messageId = getIdMessageFromReq(req);
        // Lookup in repository
        Message message = MessagesRepository.getMessage(messageId);
        if (message != null && UsersRepository.isUserFollowUser(message.user.get().id, userAuth.id)) {
            message.user.get().password = "";
            message.user.get().email= "";
            return message;
        }
        // DONE: Not found?
        throw new ApiException(500, "ErrorMessage", "Message not found or not allowed to see");


    }

    protected static long getIdMessageFromReq(HttpServletRequest req) throws ApiException {

        String path = req.getPathInfo();
        String[] parts = path.split("/");
        if (ValidationUtils.validateIdString(parts[1])) {
            return Long.parseLong(parts[1]);
        } else {
            return 0;
        }
    }

    // A POST request could be made to modify some properties of a message after it is created
    @Override
    protected Message doPost(HttpServletRequest req) throws ServletException, IOException, ApiException {

        User userAuth = getAuthenticatedUser(req);
        if (userAuth == null) throw new ApiException(500, "accessDenied", "authorization required");
        // DONE: Get the message as below
        long messageId = getIdMessageFromReq(req);
        Message message = MessagesRepository.getMessage(messageId);

        // DONE: verify if user is the author
        if (userAuth.id != message.user.get().id) throw new ApiException(500, "accessDenied", "not allowed to midify this message");
        Message newMessage = getJsonRequestBody(req, Message.class);

        if (newMessage.text != null) {
            // DONE: Apply the changes
            message.text = newMessage.text;
            MessagesRepository.insertMessage(message);
            // DONE: Return the modified message
            return message;
        }
        return null;
    }

    // A DELETE request should delete a message (if the user Auth is the same )
    @Override
    protected Void doDelete(HttpServletRequest req) throws ServletException, IOException, ApiException {
        // DONE: Get the message
        // DONE: Check that the calling user is the author of the message (security!)
        // DONE: Delete the message
        // A DELETE request shall not have a response body

        User userAuth = getAuthenticatedUser(req);

        if (userAuth == null) throw new ApiException(500, "accessDenied", "authorization required");
        Message message = MessagesRepository.getMessage(getIdMessageFromReq(req));
        if (userAuth.id != message.user.get().id) throw new ApiException(500, "accessDenied", "Message has a different owner");

        MessagesRepository.deleteMessage(message.id);
        return null;

    }

}

package fr.ecp.sio.appenginedemo.utils;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * Some utils to validate user inputs.
 * Never relies on the client applications, always re-validate.
 */
public class ValidationUtils {

    // Regex patterns for the login and the password
    private static final String LOGIN_PATTERN = "^[A-Za-z0-9_-]{4,12}$";
    private static final String PASSWORD_PATTERN = "^\\w{4,12}$";
    private static final String ID_ME = "^[Mm][Ee]$";
    private static final String ID_PATTERN = "^[0-9]*$";
    public static final String PARAMETER_FOLLOWEDBY = "followedBy";
    public static final String PARAMETER_FOLLOWEROF = "followerOf";
    public static final String PARAMETER_LIMIT = "limit";
    public static final String PARAMETER_CONTINUATION_TOKEN = "continuationToken";
    public static final String PARAMETER_FOLLOWED = "followed";
    public static final String PARAMETER_UPDATE_PWD = "updatePwd";
    public static final String PARAMETER_UPDATE_EMAIL = "updateEmail";

    public static boolean validateLogin(String login) {
        return login != null && login.matches(LOGIN_PATTERN);
    }

    public static boolean validatePassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }

    public static boolean validateEmail(String email) {
        // Here we use a library from Apache Commons to do the validation
        return EmailValidator.getInstance(false).isValid(email);
    }

    public static boolean validateMessage(String message) {
        // Here we use a library from Apache Commons to do the validation
        return message != null && message.length() > 5 && message.length() < 400;
    }

    public static boolean validateIdString(String idString) {
        return idString != null && idString.matches(ID_PATTERN);
    }

    public static boolean validateIdMe(String idMe) {
        return idMe != null && idMe.matches(ID_ME);
    }

}

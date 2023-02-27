package core.data.signinpage;

import com.google.common.base.Preconditions;

/**
 * User Credentials containing username and password.
 */
public class UserCredentials {

    public final String userName;
    public final String userPassword;

    public UserCredentials(String userName, String userPassword) {
        this.userName = Preconditions.checkNotNull(userName, "Username/ID cannot be null");
        this.userPassword = Preconditions.checkNotNull(userPassword, "User password cannot be null");
    }
}

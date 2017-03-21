package com.alex.mirash.mirashmessengerdemo_quickblox.login.helper;

/**
 * @author Mirash
 */

public class UserDataHolder {
    private String login;
    private String password;
    private String confirmPassword;

    public UserDataHolder(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public UserDataHolder(String login, String password, String confirmPassword) {
        this(login, password);
        this.confirmPassword = confirmPassword;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }
}

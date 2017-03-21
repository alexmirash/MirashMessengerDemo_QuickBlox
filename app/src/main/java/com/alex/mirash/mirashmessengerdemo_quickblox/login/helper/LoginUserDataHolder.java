package com.alex.mirash.mirashmessengerdemo_quickblox.login.helper;

/**
 * @author Mirash
 */

public class LoginUserDataHolder {
    private String login;
    private String password;
    private String confirmPassword;

    public LoginUserDataHolder(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public LoginUserDataHolder(String login, String password, String confirmPassword) {
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

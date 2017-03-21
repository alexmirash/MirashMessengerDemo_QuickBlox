package com.alex.mirash.mirashmessengerdemo_quickblox.login.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.LoginUserDataHolder;

/**
 * @author Mirash
 */

public class SignUpView extends SignBaseView {
    protected EditText confirmPasswordEditText;

    public SignUpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        confirmPasswordEditText = (EditText) findViewById(R.id.password_confirm_edittext);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_sign_up;
    }

    public boolean checkData(LoginUserDataHolder userData) {
        String login = userData.getLogin();
        String password = userData.getPassword();
        String confirm = userData.getConfirmPassword();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(userData.getPassword()) || TextUtils.isEmpty(confirm)) {
            if (TextUtils.isEmpty(login)) {
                loginEditText.setError(getResources().getString(R.string.error_field_is_empty));
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError(getResources().getString(R.string.error_field_is_empty));
            }
            if (TextUtils.isEmpty(confirm)) {
                confirmPasswordEditText.setError(getResources().getString(R.string.error_field_is_empty));
            }
            return false;
        }

        if (!TextUtils.equals(password, confirm)) {
            confirmPasswordEditText.setError(getResources().getString(R.string.confirm_error));
            return false;
        }
        return true;
    }

    @Override
    public LoginUserDataHolder getUserData() {
        return new LoginUserDataHolder(loginEditText.getText().toString(),
                passwordEditText.getEditableText().toString(),
                confirmPasswordEditText.getEditableText().toString());
    }
}

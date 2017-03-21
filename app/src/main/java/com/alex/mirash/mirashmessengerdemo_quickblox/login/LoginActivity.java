package com.alex.mirash.mirashmessengerdemo_quickblox.login;

import android.os.Bundle;
import android.view.View;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.helper.DataHolder;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.ActionProvider;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.LoginUserDataHolder;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.view.SignInView;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.view.SignUpView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

/**
 * @author Mirash
 */

public class LoginActivity extends BaseActivity {

    private SignInView signInView;
    private SignUpView signUpView;

    private View testAccountLoginView;

    private Mode mode = Mode.LOGIN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInView = _findViewById(R.id.sign_in_view);
        signUpView = _findViewById(R.id.sign_up_view);

        testAccountLoginView = findViewById(R.id.login_test_account_button);
        testAccountLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(new LoginUserDataHolder("TestUser", "qwerty123"));
            }
        });

        signInView.setActionProvider(new ActionProvider() {
            @Override
            public void onDone(LoginUserDataHolder userData) {
                signIn(userData);
            }

            @Override
            public void onSwitch() {
                showSignUpView();

            }
        });
        signUpView.setActionProvider(new ActionProvider() {
            @Override
            public void onDone(LoginUserDataHolder userData) {
                signUp(userData);
            }

            @Override
            public void onSwitch() {
                showSignInView();
            }
        });
    }

    public void signIn(final LoginUserDataHolder userData) {
        showProgressDialog();
        QBUser qbUser = new QBUser(userData.getLogin(), userData.getPassword());
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                dismissProgressDialog();
                DataHolder.getInstance().setSignInQbUser(qbUser);
                Toaster.longToast(R.string.user_successfully_sign_in);
            }

            @Override
            public void onError(QBResponseException errors) {
                progressDialog.dismiss();
                View rootLayout = findViewById(R.id.activity_login_root);
                showSnackbarError(rootLayout, R.string.errors, errors, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signIn(userData);
                    }
                });
            }
        });
    }

    public void signUp(final LoginUserDataHolder userData) {
        if (signUpView.checkData(userData)) {
            return;
        }
        progressDialog.show();

        QBUser qbUser = new QBUser();
        qbUser.setLogin(userData.getLogin());
        qbUser.setPassword(userData.getPassword());
        QBUsers.signUpSignInTask(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                progressDialog.dismiss();
                DataHolder.getInstance().addQbUser(qbUser);
                DataHolder.getInstance().setSignInQbUser(qbUser);
            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.dismiss();
                View rootLayout = findViewById(R.id.activity_login_root);
                showSnackbarError(rootLayout, R.string.errors, error, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signUp(userData);
                    }
                });
            }
        });
    }


    public void showSignUpView() {
        signInView.hide();
        signUpView.show();

    }

    public void showSignInView() {
        signUpView.hide();
        signInView.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (hideSnackbar()) {
            return;
        }
        super.onBackPressed();
    }

    private enum Mode {
        LOGIN,
        REGISTER
    }
}

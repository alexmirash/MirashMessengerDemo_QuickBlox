package com.alex.mirash.mirashmessengerdemo_quickblox.login;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.ActionProvider;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.DataHolder;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.UserDataHolder;
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
    private Button doneButton;


    private Mode mode = Mode.LOGIN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInView = _findViewById(R.id.sign_in_view);
        signUpView = _findViewById(R.id.sign_up_view);

        signInView.setActionProvider(new ActionProvider() {
            @Override
            public void onDone(UserDataHolder userData) {
                signIn(userData);
            }

            @Override
            public void onSwitch() {
                showSignUpView();

            }
        });
        signUpView.setActionProvider(new ActionProvider() {
            @Override
            public void onDone(UserDataHolder userData) {
                signUp(userData);
            }

            @Override
            public void onSwitch() {
                showSignInView();
            }
        });

        doneButton = _findViewById(R.id.login_done_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == Mode.LOGIN) {
                    signIn(signInView.getUserData());
                }
            }
        });
    }

    public void signIn(final UserDataHolder userData) {
        showProgressDialog();
        QBUser qbUser = new QBUser(userData.getLogin(), userData.getPassword());
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                dismissProgressDialog();
                DataHolder.getInstance().setSignInQbUser(qbUser);
                Toaster.longToast(R.string.user_successfully_sign_in);
                finish();
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

    public void signUp(final UserDataHolder userData) {
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
        doneButton.setText("REGISTER");

    }

    public void showSignInView() {
        signUpView.hide();
        signInView.show();
        doneButton.setText("LOG IN");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private enum Mode {
        LOGIN,
        REGISTER
    }
}

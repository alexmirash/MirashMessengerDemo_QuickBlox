package com.alex.mirash.mirashmessengerdemo_quickblox.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.DataHolder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpUserActivity extends BaseActivity {

    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
    }

    @SuppressLint("ShowToast")
    protected void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(this, R.string.error, Toast.LENGTH_LONG);
        loginEditText = _findViewById(R.id.login_up_edittext);
        passwordEditText = _findViewById(R.id.password_up_edittext);
        confirmPasswordEditText = _findViewById(R.id.password_confirm_edittext);
    }

    public void signUp() {
        String login = loginEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

   /*     if (!checkData(login, password, confirmPassword)) {
            return;
        }*/

        progressDialog.show();

        QBUser qbUser = new QBUser();
        qbUser.setLogin(login);
        qbUser.setPassword(password);
        QBUsers.signUpSignInTask(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                progressDialog.dismiss();

                DataHolder.getInstance().addQbUser(qbUser);
                DataHolder.getInstance().setSignInQbUser(qbUser);

                setResult(RESULT_OK, new Intent());
                finish();
            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.dismiss();
                View rootLayout = findViewById(R.id.activity_sign_up);
                showSnackbarError(rootLayout, R.string.errors, error, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signUp();
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sign_in_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_in_up:
                signUp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
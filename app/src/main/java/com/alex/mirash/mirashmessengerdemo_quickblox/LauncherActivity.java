package com.alex.mirash.mirashmessengerdemo_quickblox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseUserSessionActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.chat.ChatHelper;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.LoginActivity;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

public class LauncherActivity extends BaseUserSessionActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proceedToTheNextActivity();
    }

    protected void proceedToTheNextActivity() {
        if (checkSignIn()) {
            restoreChatSession();
        } else {
            startLoginActivity();
        }
    }


    private void restoreChatSession() {
        if (ChatHelper.getInstance().isLogged()) {
            startChatActivity();
            finish();
        } else {
            QBUser currentUser = getUserFromSession();
            loginToChat(currentUser);
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

package com.alex.mirash.mirashmessengerdemo_quickblox.login.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.ActionProvider;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.helper.UserDataHolder;


/**
 * @author Mirash
 */

public abstract class SignBaseView extends FrameLayout {

    protected EditText loginEditText;
    protected EditText passwordEditText;

    protected View switchButton;

    protected ActionProvider actionProvider;

    public SignBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        inflate(getContext(), getLayoutId(), this);
        loginEditText = (EditText) findViewById(R.id.login_in_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);

        switchButton = findViewById(R.id.login_switch_button);
        switchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionProvider != null) {
                    actionProvider.onSwitch();
                }
            }
        });
    }

    protected abstract int getLayoutId();

    public void hide() {
        if (getVisibility() == VISIBLE) {
            setVisibility(INVISIBLE);
        }
    }

    public void show() {
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }
    }

    public void setActionProvider(ActionProvider actionProvider) {
        this.actionProvider = actionProvider;
    }

    public UserDataHolder getUserData() {
        return new UserDataHolder(loginEditText.getText().toString(), passwordEditText.getEditableText().toString());
    }
}

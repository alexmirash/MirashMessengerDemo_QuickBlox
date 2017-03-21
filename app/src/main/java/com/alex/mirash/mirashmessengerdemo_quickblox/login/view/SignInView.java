package com.alex.mirash.mirashmessengerdemo_quickblox.login.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;

/**
 * @author Mirash
 */

public class SignInView extends SignBaseView {

    public SignInView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_sign_in;
    }

}

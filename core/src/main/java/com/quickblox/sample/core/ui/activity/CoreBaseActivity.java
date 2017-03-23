package com.quickblox.sample.core.ui.activity;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.R;
import com.quickblox.sample.core.utils.ErrorUtils;

import java.lang.reflect.Field;

public class CoreBaseActivity extends AppCompatActivity {
    protected ActionBar actionBar;

    protected Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBar = getSupportActionBar();

        // Hack. Forcing overflow button on actionbar on devices with hardware menu button
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        actionBar.setTitle(getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    public void setActionBarTitle(int title) {
        if (actionBar != null) {
            actionBar.setTitle(getClass().getSimpleName());
        }
    }

    public void setActionBarTitle(CharSequence title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    protected void fillField(TextView textView, String value) {
        textView.setText(value);
    }

    protected void showSnackbarError(View rootLayout, @StringRes int resId, QBResponseException e, View.OnClickListener clickListener) {
        snackbar = ErrorUtils.showSnackbar(rootLayout, resId, e, R.string.dlg_retry, clickListener);
        snackbar.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSnackbar();
            }
        });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                onSnackbarDismiss(snackbar, event);
            }

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                onSnackbarShown(snackbar);
            }
        });
    }

    protected void showSnackbarError(@StringRes int resId, QBResponseException e, View.OnClickListener clickListener) {
        View rootLayout = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        showSnackbarError(rootLayout, resId, e, clickListener);
    }

    protected void onSnackbarDismiss(Snackbar snackbar, int event) {

    }

    protected void onSnackbarShown(Snackbar snackbar) {

    }

    protected boolean hideSnackbar() {
        if (snackbar != null) {
            snackbar.dismiss();
            snackbar = null;
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

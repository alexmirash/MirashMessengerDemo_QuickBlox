package com.alex.mirash.mirashmessengerdemo_quickblox.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.helper.DataHolder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.QBUsers;

public class UsersListActivity extends BaseActivity {

    private static final int REQUEST_CODE_SIGN_UP = 100;
    private int currentPage = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        DataHolder.getInstance().clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SIGN_UP) {
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setActionBarTitle(DataHolder.getInstance().isSignedIn() ? DataHolder.getInstance().getSignInQbUser().getLogin()
                : getString(R.string.not_signed_in));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataHolder.getInstance().setSignInQbUser(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!DataHolder.getInstance().isSignedIn()) {
            setActionBarTitle(R.string.not_signed_in);
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setEnabled(false);
        } else {
            setActionBarTitle(DataHolder.getInstance().getSignInQbUser().getLogin());
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.sign_in:
//                intent = new Intent(this, SignInActivity.class);
//                startActivity(intent);
                return true;

            case R.id.sign_up:
                intent = new Intent(this, SignUpUserActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SIGN_UP);
                return true;

            case R.id.profile:
//                ShowUserActivity.start(this, DataHolder.getInstance().getSignInQbUser());
                return true;

            case R.id.logout:
                progressDialog.show();
                QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        progressDialog.dismiss();
                        Toaster.longToast(R.string.user_log_out_msg);
                        updateDataAfterLogOut();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.dismiss();
                        Toaster.longToast(e.getErrors().toString());
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDataAfterLogOut() {
        DataHolder.getInstance().setSignInQbUser(null);
        invalidateOptionsMenu();
    }

}
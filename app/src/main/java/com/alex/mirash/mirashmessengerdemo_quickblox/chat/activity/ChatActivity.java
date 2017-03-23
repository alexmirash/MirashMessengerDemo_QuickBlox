package com.alex.mirash.mirashmessengerdemo_quickblox.chat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.base.BaseActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.managers.DialogsManager;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.tool.DialogsAdapter;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.chat.ChatHelper;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.QbChatDialogMessageListenerImp;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.QbDialogHolder;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.callback.QbEntityCallbackImpl;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.view.ChatView;
import com.alex.mirash.mirashmessengerdemo_quickblox.login.LoginActivity;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mirash
 */

public class ChatActivity extends BaseActivity implements DialogsManager.ManagingDialogsCallbacks {
    private static final String TAG = "LOL";
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;

    private DrawerLayout drawer;

    private ActionMode currentActionMode;

    private QBRequestGetBuilder requestBuilder;

    private BroadcastReceiver pushBroadcastReceiver;
    private GooglePlayServicesHelper googlePlayServicesHelper;
    private DialogsAdapter dialogsAdapter;
    private QBChatDialogMessageListener allDialogsMessagesListener;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private DialogsManager dialogsManager;
    private QBUser currentUser;

    private View logOutButton;

    private boolean isProcessingResultInProgress;


    private Map<QBChatDialog, ChatView> chatViews = new HashMap<>();
    private ChatView activeChatView;

    private ViewGroup chatScreenContainer;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        pushBroadcastReceiver = new PushBroadcastReceiver();
        allDialogsMessagesListener = new AllDialogsMessageListener();
        systemMessagesListener = new SystemMessagesListener();
        dialogsManager = new DialogsManager();
        currentUser = ChatHelper.getCurrentUser();
        requestBuilder = new QBRequestGetBuilder();
        setActionBarTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));

        initDialogList();
        initDrawer();

        registerQbChatListeners();

        loadDialogsFromQb(QbDialogHolder.getInstance().getDialogs().size() > 0, true);

        logOutButton = findViewById(R.id.chat_drawer_logout_button);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        chatScreenContainer = (ViewGroup) findViewById(R.id.content_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkPlayServicesAvailable(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
        for (ChatView chat : chatViews.values()) {
            chat.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
        for (ChatView chat : chatViews.values()) {
            chat.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterQbChatListeners();
        for (ChatView chat : chatViews.values()) {
            chat.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void initDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }
        });
    }


    private void logOut() {
        ChatHelper.getInstance().destroy();
        SubscribeService.unSubscribeFromPushes(ChatActivity.this);
        SharedPrefsHelper.getInstance().removeQbUser();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        QbDialogHolder.getInstance().clear();
        ProgressDialogFragment.hide(getSupportFragmentManager());
        finish();
    }

    private void initDialogList() {
        ListView dialogListView = _findViewById(R.id.chat_drawer_list_view);
        dialogListView.setEmptyView(findViewById(R.id.layout_chat_empty));
        dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                if (currentActionMode == null) {
                    showChatDialogView(selectedDialog);
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
                }
            }
        });
        dialogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                startSupportActionMode(new DeleteActionModeCallback());
                dialogsAdapter.selectItem(selectedDialog);
                return true;
            }
        });
        dialogsAdapter = new DialogsAdapter(this, new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values()));
        dialogListView.setAdapter(dialogsAdapter);
    }

    public void selectUsersToChat(View view) {
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    private void showChatDialogView(QBChatDialog chatDialog) {
        //TODO
        //ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);
        loadUpdatedDialog(chatDialog.getDialogId());
        if (activeChatView != null) {
            activeChatView.hide();
        }
        activeChatView = chatViews.get(chatDialog);
        if (activeChatView == null) {
            activeChatView = new ChatView(this);
            activeChatView.setVisibility(View.INVISIBLE);
            chatViews.put(chatDialog, activeChatView);
            chatScreenContainer.addView(activeChatView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            activeChatView.show(chatDialog);
        } else {
            activeChatView.show(chatDialog);
        }
    }

    private void registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener != null
                    ? allDialogsMessagesListener : new AllDialogsMessageListener());
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
                    ? systemMessagesListener : new SystemMessagesListener());
        }

        dialogsManager.addManagingDialogsCallbackListener(this);
    }

    private void unregisterQbChatListeners() {
        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }

        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        isProcessingResultInProgress = true;
        if (!silentUpdate) {
            showProgressDialog();
        }

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                isProcessingResultInProgress = false;
                dismissProgressDialog();

                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
                dismissProgressDialog();
                Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDialogsAdapter() {
        dialogsAdapter.updateList(new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values()));
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        updateDialogsAdapter();
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        updateDialogsAdapter();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        updateDialogsAdapter();
    }


    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            isProcessingResultInProgress = true;
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);
                if (isPrivateDialogExist(selectedUsers)) {
                    selectedUsers.remove(ChatHelper.getCurrentUser());
                    QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                    isProcessingResultInProgress = false;
                    showChatDialogView(existingPrivateDialog);
                } else {
                    ProgressDialogFragment.show(getSupportFragmentManager(), R.string.create_chat);
                    createDialog(selectedUsers);
                }
            } else if (requestCode == REQUEST_DIALOG_ID_FOR_UPDATE) {
                //TODO
//                if (data != null) {
//                    String dialogId = data.getStringExtra(ChatActivity.EXTRA_DIALOG_ID);
//                    loadUpdatedDialog(dialogId);
//                } else {
//                    isProcessingResultInProgress = false;
//                    updateDialogsList();
//                }
            }
        } else {
            updateDialogsAdapter();
        }
    }

    private void loadUpdatedDialog(String dialogId) {
        ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle bundle) {
                isProcessingResultInProgress = false;
                QbDialogHolder.getInstance().addDialog(result);
                updateDialogsAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
            }
        });
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        isProcessingResultInProgress = false;
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        showChatDialogView(dialog);

                        ProgressDialogFragment.hide(getSupportFragmentManager());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isProcessingResultInProgress = false;
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        showSnackbarError(R.string.dialogs_creation_error, null, null);
                    }
                }
        );
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }


    private class DeleteActionModeCallback implements ActionMode.Callback {

        public DeleteActionModeCallback() {
//            fab.hide();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_dialogs, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_dialogs_action_delete:
                    deleteSelectedDialogs();
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            dialogsAdapter.clearSelection();
//            fab.show();
        }

        private void deleteSelectedDialogs() {
            final Collection<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            ChatHelper.getInstance().deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    updateDialogsAdapter();
                }

                @Override
                public void onError(QBResponseException e) {
                }
            });
        }
    }

    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.v(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
            requestBuilder.setSkip(0);
            loadDialogsFromQb(true, true);
        }
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
            if (!senderId.equals(ChatHelper.getCurrentUser().getId())) {
                dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
            }
        }
    }
}

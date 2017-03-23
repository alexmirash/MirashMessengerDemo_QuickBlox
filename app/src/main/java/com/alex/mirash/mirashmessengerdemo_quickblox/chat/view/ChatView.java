package com.alex.mirash.mirashmessengerdemo_quickblox.chat.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.tool.AttachmentPreviewAdapter;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.tool.ChatAdapter;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.chat.ChatHelper;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.PaginationHistoryListener;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.QbChatDialogMessageListenerImp;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.VerboseQbChatConnectionListener;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.view.widget.AttachmentPreviewAdapterView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * @author Mirash
 */

public class ChatView extends ScreenView {
    private static final String TAG = "LOL";
    private static final int REQUEST_CODE_ATTACHMENT = 721;

    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final String EXTRA_DIALOG_ID = "dialogId";

    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;

    private LinearLayout attachmentPreviewContainerLayout;
    private Snackbar snackbar;

    private ChatAdapter chatAdapter;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;
    private ConnectionListener chatConnectionListener;

    private QBChatDialog qbChatDialog;
    private ArrayList<QBChatMessage> unShownMessages;
    private int skipPagination;
    private ChatMessageListener chatMessageListener;

    public ChatView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void init() {
        inflate(getContext(), R.layout.view_chat, this);
        chatMessageListener = new ChatMessageListener();
        initChatConnectionListener();
        initViews();
    }

    @Override
    public void onCreate() {
    }

    public void show(QBChatDialog dialog) {
        if (!isScreenShown()) {
            qbChatDialog = dialog;
            qbChatDialog.initForChat(QBChatService.getInstance());
            qbChatDialog.addMessageListener(chatMessageListener);
            initChat();
        }
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
//        qbChatDialog.removeMessageListrener(chatMessageListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseChat();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onSendChatClick(View view) {
        int totalAttachmentsCount = attachmentPreviewAdapter.getCount();
        Collection<QBAttachment> uploadedAttachments = attachmentPreviewAdapter.getUploadedAttachments();
        if (!uploadedAttachments.isEmpty()) {
            if (uploadedAttachments.size() == totalAttachmentsCount) {
                for (QBAttachment attachment : uploadedAttachments) {
                    sendChatMessage(null, attachment);
                }
            } else {
                Toaster.shortToast(R.string.chat_wait_for_attachments_to_upload);
            }
        }

        String text = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }
    }

    public void onAttachmentsClick(View view) {
//        new ImagePickHelper().pickAnImage(getActivity(), REQUEST_CODE_ATTACHMENT);
    }

    public void showMessage(QBChatMessage message) {
        if (chatAdapter != null) {
            chatAdapter.add(message);
            scrollMessageListDown();
        } else {
            if (unShownMessages == null) {
                unShownMessages = new ArrayList<>();
            }
            unShownMessages.add(message);
        }
    }

    private void initViews() {
        messagesListView = (StickyListHeadersListView) findViewById(R.id.list_chat_messages);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat);
        attachmentPreviewContainerLayout = (LinearLayout) findViewById(R.id.layout_attachment_preview_container);

        attachmentPreviewAdapter = new AttachmentPreviewAdapter(getContext(),
                new AttachmentPreviewAdapter.OnAttachmentCountChangedListener() {
                    @Override
                    public void onAttachmentCountChanged(int count) {
                        attachmentPreviewContainerLayout.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    }
                },
                new AttachmentPreviewAdapter.OnAttachmentUploadErrorListener() {
                    @Override
                    public void onAttachmentUploadError(QBResponseException e) {
          /*              showErrorSnackbar(0, e, new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onAttachmentsClick(v);
                            }
                        });*/
                    }
                });
        AttachmentPreviewAdapterView previewAdapterView = (AttachmentPreviewAdapterView) findViewById(R.id.adapter_view_attachment_preview);
        previewAdapterView.setAdapter(attachmentPreviewAdapter);

        findViewById(R.id.button_chat_send).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendChatClick(v);
            }
        });

        findViewById(R.id.button_chat_attachment).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onAttachmentsClick(v);
            }
        });
    }

    private void sendChatMessage(String text, QBAttachment attachment) {
        QBChatMessage chatMessage = new QBChatMessage();
        if (attachment != null) {
            chatMessage.addAttachment(attachment);
        } else {
            chatMessage.setBody(text);
        }
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);
        chatMessage.setMarkable(true);

        if (!QBDialogType.PRIVATE.equals(qbChatDialog.getType()) && !qbChatDialog.isJoined()) {
            Toaster.shortToast("You're still joining a group chat, please wait a bit");
            return;
        }

        try {
            qbChatDialog.sendMessage(chatMessage);

            if (QBDialogType.PRIVATE.equals(qbChatDialog.getType())) {
                showMessage(chatMessage);
            }

            if (attachment != null) {
                attachmentPreviewAdapter.remove(attachment);
            } else {
                messageEditText.setText("");
            }
        } catch (SmackException.NotConnectedException e) {
            Log.w(TAG, e);
            Toaster.shortToast("Can't send a message, You are not connected to chat");
        }
    }

    private void initChat() {
        switch (qbChatDialog.getType()) {
            case GROUP:
            case PUBLIC_GROUP:
                joinGroupChat();
                break;

            case PRIVATE:
                loadDialogUsers();
                break;

            default:
                Toaster.shortToast(String.format("%s %s", getContext().getString(R.string.chat_unsupported_type), qbChatDialog.getType().name()));
                break;
        }
    }

    private void joinGroupChat() {
        progressBar.setVisibility(View.VISIBLE);
        ChatHelper.getInstance().join(qbChatDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                if (snackbar != null) {
                    snackbar.dismiss();
                }
                loadDialogUsers();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
//                snackbar = showErrorSnackbar(R.string.connection_error, e, null);
            }
        });
    }

    private void leaveGroupDialog() {
        try {
            ChatHelper.getInstance().leaveChatDialog(qbChatDialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private void releaseChat() {
        qbChatDialog.removeMessageListrener(chatMessageListener);
        if (!QBDialogType.PRIVATE.equals(qbChatDialog.getType())) {
            leaveGroupDialog();
        }
    }

    private void loadDialogUsers() {
        ChatHelper.getInstance().getUsersFromDialog(qbChatDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                loadChatHistory();
            }

            @Override
            public void onError(QBResponseException e) {
//                showErrorSnackbar(R.string.chat_load_users_error, e,
//                        new OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                loadDialogUsers();
//                            }
//                        });
            }
        });
    }

    private void loadChatHistory() {
        ChatHelper.getInstance().loadChatHistory(qbChatDialog, skipPagination, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);
                if (chatAdapter == null) {
                    chatAdapter = new ChatAdapter(getContext(), qbChatDialog, messages);
                    chatAdapter.setPaginationHistoryListener(new PaginationHistoryListener() {
                        @Override
                        public void downloadMore() {
                            loadChatHistory();
                        }
                    });
                    chatAdapter.setOnItemInfoExpandedListener(new ChatAdapter.OnItemInfoExpandedListener() {
                        @Override
                        public void onItemInfoExpanded(final int position) {
                            if (isLastItem(position)) {
                                // HACK need to allow info textview visibility change so posting it via handler
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        messagesListView.setSelection(position);
                                    }
                                });
                            } else {
                                messagesListView.smoothScrollToPosition(position);
                            }
                        }

                        private boolean isLastItem(int position) {
                            return position == chatAdapter.getCount() - 1;
                        }
                    });
                    if (unShownMessages != null && !unShownMessages.isEmpty()) {
                        List<QBChatMessage> chatList = chatAdapter.getList();
                        for (QBChatMessage message : unShownMessages) {
                            if (!chatList.contains(message)) {
                                chatAdapter.add(message);
                            }
                        }
                    }
                    messagesListView.setAdapter(chatAdapter);
                    messagesListView.setAreHeadersSticky(false);
                    messagesListView.setDivider(null);
                } else {
                    chatAdapter.addList(messages);
                    messagesListView.setSelection(messages.size());
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
//                snackbar = showErrorSnackbar(R.string.connection_error, e, null);
            }
        });
        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private void initChatConnectionListener() {
        chatConnectionListener = new VerboseQbChatConnectionListener((View) getParent()) {
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                skipPagination = 0;
                switch (qbChatDialog.getType()) {
                    case GROUP:
                        chatAdapter = null;
                        // Join active room if we're in Group Chat
                        ((Activity) getContext()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                joinGroupChat();
                            }
                        });
                        break;
                }
            }
        };
    }

    public class ChatMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
            showMessage(qbChatMessage);
        }
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (qbChatDialog != null) {
            outState.putString(EXTRA_DIALOG_ID, qbChatDialog.getDialogId());
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (qbChatDialog == null) {
            qbChatDialog = QbDialogHolder.getInstance().getChatDialogById(savedInstanceState.getString(EXTRA_DIALOG_ID));
        }
    }*/
}

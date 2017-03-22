package com.alex.mirash.mirashmessengerdemo_quickblox.chat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;

import com.alex.mirash.mirashmessengerdemo_quickblox.chat.managers.DialogsManager;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.chat.ChatHelper;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.QbChatDialogMessageListenerImp;
import com.alex.mirash.mirashmessengerdemo_quickblox.chat.utils.qb.QbDialogHolder;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.users.model.QBUser;

/**
 * @author Mirash
 */

public class ChatListController {
}

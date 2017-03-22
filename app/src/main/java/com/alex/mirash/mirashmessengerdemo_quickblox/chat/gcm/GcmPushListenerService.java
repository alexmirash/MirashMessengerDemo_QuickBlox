package com.alex.mirash.mirashmessengerdemo_quickblox.chat.gcm;

import com.alex.mirash.mirashmessengerdemo_quickblox.LauncherActivity;
import com.alex.mirash.mirashmessengerdemo_quickblox.R;
import com.quickblox.sample.core.gcm.CoreGcmPushListenerService;
import com.quickblox.sample.core.utils.NotificationUtils;
import com.quickblox.sample.core.utils.ResourceUtils;

public class GcmPushListenerService extends CoreGcmPushListenerService {
    private static final int NOTIFICATION_ID = 1;

    @Override
    protected void showNotification(String message) {
        NotificationUtils.showNotification(this, LauncherActivity.class,
                ResourceUtils.getString(R.string.notification_title), message,
                R.mipmap.ic_launcher, NOTIFICATION_ID);
    }
}
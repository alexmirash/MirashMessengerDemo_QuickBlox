package com.alex.mirash.mirashmessengerdemo_quickblox.chat.view;

import android.app.Activity;

/**
 * @author Mirash
 */

public interface ActivityActionProvider<T extends Activity> {
    T getActivity();
}

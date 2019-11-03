package com.minhui.vpn;

import java.nio.channels.SelectionKey;

public interface KeyHandler {
    void onKeyReady(SelectionKey key);
}

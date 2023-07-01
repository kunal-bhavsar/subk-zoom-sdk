package com.subk.zoomsdk.cmd;


import com.subk.zoomsdk.meeting.interfaces.IListener;

public interface CmdHandler extends IListener {
    void onCmdReceived(CmdRequest request);
}

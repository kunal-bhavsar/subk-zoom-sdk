package co.subk.zoomsdk.cmd;


import co.subk.zoomsdk.meeting.interfaces.IListener;

public interface CmdHandler extends IListener {
    void onCmdReceived(CmdRequest request);
}

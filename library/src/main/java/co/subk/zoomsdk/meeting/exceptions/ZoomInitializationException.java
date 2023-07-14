package co.subk.zoomsdk.meeting.exceptions;

import co.subk.zoomsdk.meeting.util.ErrorMsgUtil;

public class ZoomInitializationException extends Exception {
    public ZoomInitializationException(int errorCode) {
        super(ErrorMsgUtil.getMsgByErrorCode(errorCode));
    }
}

package intf;

import java.util.List;

public interface ContentServices {
    List<byte[]> extractContents(byte[] data);
    byte[] handleContent(byte[] data);
    void handleMessage(String message);

}

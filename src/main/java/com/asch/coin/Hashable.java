package com.asch.coin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public abstract class Hashable implements Serializable {
    // Convert the entire object to a byte array then hash it!
    public byte[] hash() {
        System.out.println(this.getClass().getName() + "::hash() called");
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream outputStream = new ObjectOutputStream(byteStream)) {
            outputStream.writeObject(this);
            outputStream.flush();
            byte[] ret = Util.hashBuffer(byteStream.toByteArray());
            System.out.printf("%s::hash() result: %s\n", this.getClass().getName(), Util.bytesToHex(ret));
            outputStream.close();
            byteStream.close();
            return ret;
        } catch (IOException e) {
            System.out.println("IOException: Failed to hash");
            e.printStackTrace(System.err);
        }
        return null;
    }

    public abstract int getSerializedSize();

    public abstract ByteBuffer serialize();
}

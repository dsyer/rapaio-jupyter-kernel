package org.rapaio.jupyter.kernel.core.java.io;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.rapaio.jupyter.kernel.channels.ReplyEnv;

public class JShellInputStream extends InputStream {
    private static final Charset encoding = StandardCharsets.UTF_8;

    private final ByteBuffer buffer = new ByteBuffer();
    private ReplyEnv env;
    private boolean enabled;

    public JShellInputStream() {
    }

    public void bindEnv(ReplyEnv env, boolean enabled) {
        this.env = env;
        this.enabled = enabled;
    }

    public void unbindEnv() {
        this.env = null;
        this.enabled = false;
    }

    private void readFromFrontend() {
        if (enabled) {
            byte[] read = env.readFromStdIn().getBytes(encoding);
            buffer.feed(read, read.length);
        }
    }

    @Override
    public synchronized int read() {
        if (!buffer.canTake(1)) {
            readFromFrontend();
        }
        if (buffer.canTake(1)) {
            byte[] data = buffer.take(1);
            return data[0];
        }
        return -1;
    }

    @Override
    public int read(byte[] into, int intoOffset, int len) {
        Objects.requireNonNull(into, "Target buffer cannot be null");
        if (intoOffset < 0) {
            throw new IndexOutOfBoundsException("intoOffset must be semipositive");
        }
        if (len < 0) {
            throw new IndexOutOfBoundsException("len must be semipositive");
        }
        if (len > into.length - intoOffset) {
            throw new IndexOutOfBoundsException("There is no space to read.");
        }
        if (len == 0) {
            return 0;
        }

        if (!buffer.canTake(len)) {
            readFromFrontend();
        }

        int available = buffer.availableToTake();
        if(available==0) {
            return -1;
        }

        int actualLen = Math.min(available, len);
        byte[] data = buffer.take(actualLen);
        System.arraycopy(data, 0, into, intoOffset, actualLen);
        return actualLen;
    }

    @Override
    public int available() {
        return buffer.availableToTake();
    }
}

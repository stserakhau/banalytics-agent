package com.banalytics.box.module.media.task;

import com.banalytics.box.module.MediaCaptureCallbackSupport.Data;
import org.bytedeco.javacv.Seekable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SeekableOutputStream extends ByteArrayOutputStream implements Seekable {

    public SeekableOutputStream(int size) {
        super(size);
    }

    long position;

    @Override
    public void seek(long position, int whence) {
        if (position < 0 || position > count || whence != 0)
            throw new IllegalArgumentException();
        this.position = position;
//        System.out.println("    Seek: " + position + " / " + whence);
    }

    @Override
    public void flush() throws IOException {
//        System.out.println(">>>>>>>Flush");
        super.flush();
    }

    @Override
    public synchronized void reset() {
        super.reset();
//        System.out.println("<<<<< Reset");
    }

    @Override
    public synchronized void write(int b) {
        if (position < count) {
            buf[(int) position] = (byte) b; // position < count <= MAX_INT
        } else {
            super.write(b);
        }
        position++;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (position < count) {
            for (int i = 0; i < len; i++) {
                write(b[off + i]); // should be changed for bigegr arrays
            }
        } else {
            super.write(b, off, len);
            position = count;
        }
    }

    public synchronized Data getData(int mediaWidth, int mediaHeight) {
        return new Data(buf, count, mediaWidth, mediaHeight);
    }
}
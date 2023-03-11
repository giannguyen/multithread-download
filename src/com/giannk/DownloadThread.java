package com.giannk;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {
    private final Object downloadLock;
    private final int startByte;
    private final int endByte;
    private final URL url;
    private final byte[] buffer;
    private volatile int bytesRead;

    public DownloadThread(int startByte, int endByte, URL url, Object downloadLock) {
        this.startByte = startByte;
        this.endByte = endByte;
        this.url = url;
        buffer = new byte[endByte - startByte + 1];
        this.downloadLock = downloadLock;
        this.bytesRead = 0;
    }

    public synchronized int getBytesRead() {
        return this.bytesRead;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    @Override
    public void run() {
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Range", "bytes=" + startByte + "-" + endByte);
            InputStream input = conn.getInputStream();
            int bytesReadBlock = 0;
            while (true) {
                bytesReadBlock = input.read(buffer, bytesRead, buffer.length - bytesRead);
                if (bytesReadBlock == -1) { // end of input stream
                    break;
                }
                bytesRead += bytesReadBlock;
                synchronized (downloadLock) {
                    downloadLock.notify();
                }
            }
            input.close();
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

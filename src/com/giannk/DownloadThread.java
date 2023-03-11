package com.giannk;


import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class DownloadThread extends Thread {
    private final String fileUrl;
    private final String outputFilePath;
    private final int bufferSize;
    private final long fileSize;
    private final Object progressMonitor;
    private long bytesDownloaded;

    public DownloadThread(String fileUrl, String outputFilePath, int bufferSize, long fileSize, Object progressMonitor) {
        this.fileUrl = fileUrl;
        this.outputFilePath = outputFilePath;
        this.bufferSize = bufferSize;
        this.fileSize = fileSize;
        this.progressMonitor = progressMonitor;
        this.bytesDownloaded = 0;
    }

    @Override
    public void run() {
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL(fileUrl).openStream());
             FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                bytesDownloaded += bytesRead;
                synchronized (progressMonitor) {
                    ((ProcessPrintThread)progressMonitor).updateProgress(bytesDownloaded, fileSize);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


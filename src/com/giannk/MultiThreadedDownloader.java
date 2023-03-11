package com.giannk;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedDownloader {
    private static final int MAX_THREADS = 4;
    private static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/zhaofei01/book/master/Java/Effective%20Java%2C%20Third%20Edition.pdf";
    private static final String OUTPUT_FILE = "abc.pdf";

    public static void main(String[] args) {
        try {
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int fileSize = conn.getContentLength();
            conn.disconnect();

            Object downloadLock = new Object();

            List<DownloadThread> threads = new ArrayList<>();
            int blockSize = fileSize / MAX_THREADS;
            for (int i = 0; i < MAX_THREADS; i++) {
                int startByte = i * blockSize;
                int endByte = (i == MAX_THREADS - 1) ? fileSize - 1 : startByte + blockSize - 1;
                DownloadThread thread = new DownloadThread(startByte, endByte, url, downloadLock);
                threads.add(thread);
                thread.start();
            }
            ProgressPrinter progressPrinter = new ProgressPrinter(threads, fileSize, downloadLock);
            progressPrinter.start();
            for (DownloadThread thread : threads) {
                thread.join();
            }

            try (FileOutputStream output = new FileOutputStream(OUTPUT_FILE)) {
                for (DownloadThread thread : threads) {
                    output.write(thread.getBuffer());
                }
            }

            System.out.println("Download complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

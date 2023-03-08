package com.giannk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MultiThreadedDownloader {
    private static final int MAX_THREADS = 4;
    private static final String DOWNLOAD_URL = "https://www.iconsdb.com/icons/download/white/calendar-8-32.png";
    private static final String OUTPUT_FILE = "calendar-8-32.png";

    public static void main(String[] args) {
        try {
            URL url = new URL(DOWNLOAD_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int fileSize = conn.getContentLength();
            conn.disconnect();

            List<DownloadThread> threads = new ArrayList<>();
            int blockSize = fileSize / MAX_THREADS;
            for (int i = 0; i < MAX_THREADS; i++) {
                int startByte = i * blockSize;
                int endByte = (i == MAX_THREADS - 1) ? fileSize - 1 : startByte + blockSize - 1;
                DownloadThread thread = new DownloadThread(startByte, endByte, url);
                threads.add(thread);
                thread.start();
            }

            for (DownloadThread thread : threads) {
                thread.join();
            }

            try(FileOutputStream output = new FileOutputStream(OUTPUT_FILE)) {
                for (DownloadThread thread : threads) {
                    output.write(thread.getBuffer());
                }
            }

            System.out.println("Download complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DownloadThread extends Thread {
        private int startByte;
        private int endByte;
        private URL url;
        private byte[] buffer;

        public DownloadThread(int startByte, int endByte, URL url) {
            this.startByte = startByte;
            this.endByte = endByte;
            this.url = url;
            buffer = new byte[endByte - startByte + 1];
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
                int bytesRead = input.read(buffer);
                while (bytesRead != -1) {
                    bytesRead = input.read(buffer, bytesRead, buffer.length - bytesRead);
                }
                input.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

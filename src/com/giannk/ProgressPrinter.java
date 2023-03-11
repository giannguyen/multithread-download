package com.giannk;

import java.util.List;

public class ProgressPrinter extends Thread {
    private final static int PROGRESS_BAR_WIDTH = 80;

    private final List<DownloadThread> threads;
    private final int fileSize;
    private final Object lock;

    public ProgressPrinter(List<DownloadThread> threads, int fileSize, Object lock) {
        this.threads = threads;
        this.fileSize = fileSize;
        this.lock = lock;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (lock) {
                int bytesDownloaded = 0;
                for (DownloadThread thread : threads) {
                    bytesDownloaded += thread.getBytesRead();
                }
                double percentComplete = (double) bytesDownloaded / fileSize;
                int progressBarLength = (int) (percentComplete * PROGRESS_BAR_WIDTH);
                String progressBar = "[" + "=".repeat(progressBarLength) + " ".repeat(PROGRESS_BAR_WIDTH - progressBarLength) + "]";
                System.out.printf("\r%s %d%% - %s/%s", progressBar, (int) (percentComplete * 100), formatSize(bytesDownloaded), formatSize(fileSize));
                if (bytesDownloaded == fileSize) {
                    System.out.println();
                    break;
                }
                try {
                    lock.wait(); // wait until all threads notify that they have downloaded more bytes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String formatSize(long bytes) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        return String.format("%.1f %s", size, units[unitIndex]);
    }

}


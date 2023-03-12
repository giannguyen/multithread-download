package com.giannk;

public class MultiThreadedDownloaderTest {

    public static void main(String[] args) {
        final String[] testArgs = {"-url", "https://res-download-pc-te-vnno-pt-4.zadn.vn/mac/ZaloSetup-23.2.1.dmg", "-n", "10"};
        MultiThreadedDownloader.main(testArgs);
    }

}

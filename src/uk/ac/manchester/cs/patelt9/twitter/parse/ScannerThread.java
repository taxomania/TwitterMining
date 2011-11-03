package uk.ac.manchester.cs.patelt9.twitter.parse;

import java.util.Scanner;

public abstract class ScannerThread extends Thread {
    private final Scanner scanner;

    public ScannerThread() {
        this("Scanner");
    } // ScannerThread()

    public ScannerThread(final String s) {
        super(s);
        scanner = new Scanner(System.in);
    } // ScannerThread(String)

    @Override
    public final void run() {
        while (!isInterrupted()) {
            if (scanner.nextLine().contains("exit")) {
                performTask();
                break;
            } // if
        } // while
        close();
    } // run()

    private void close(){
        scanner.close();
    } // close()
    @Override
    public void interrupt() {
        super.interrupt();
        performTask();
    } // close()

    protected abstract void performTask();
} // ScannerThread

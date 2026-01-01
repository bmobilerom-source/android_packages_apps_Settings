/*
 * Copyright (C) 2025 BashaMobile
 *
 * BShare - LocalSend-inspired file sharing using WiFi HTTP server
 * Works with MicroG - no Google Play Services required
 * Uses local WiFi network for fast file transfers
 */

package com.android.settings.bshare;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * BShare Manager - WiFi HTTP server for file sharing
 * Inspired by LocalSend: https://github.com/localsend/localsend
 * 
 * Uses simple HTTP server on local WiFi network
 * No Google Play Services required - works with MicroG
 */
public class BShareManager {
    private static final String TAG = "BShareManager";
    private static final int DEFAULT_PORT = 53317; // LocalSend uses 53317
    
    private final Context mContext;
    private Handler mHandler;
    private BShareCallback mCallback;
    private ServerSocket mServerSocket;
    private ExecutorService mExecutor;
    private boolean mIsRunning = false;
    
    public interface BShareCallback {
        void onServerStarted(String ipAddress, int port);
        void onServerStopped();
        void onDeviceFound(String deviceName, String ipAddress);
        void onConnectionResult(boolean success);
        void onTransferProgress(int percent);
        void onTransferComplete();
        void onError(String error);
    }
    
    public BShareManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newCachedThreadPool();
    }
    
    public void setCallback(BShareCallback callback) {
        mCallback = callback;
    }
    
    /**
     * Start HTTP server for receiving files
     * Similar to LocalSend's approach
     */
    public void startServer() {
        if (mIsRunning) {
            return;
        }
        
        mExecutor.execute(() -> {
            try {
                mServerSocket = new ServerSocket(DEFAULT_PORT);
                mIsRunning = true;
                
                String ipAddress = getLocalIpAddress();
                if (mCallback != null && ipAddress != null) {
                    mHandler.post(() -> mCallback.onServerStarted(ipAddress, DEFAULT_PORT));
                }
                
                Log.d(TAG, "BShare server started on " + ipAddress + ":" + DEFAULT_PORT);
                
                // Accept connections
                while (mIsRunning && !mServerSocket.isClosed()) {
                    try {
                        Socket clientSocket = mServerSocket.accept();
                        handleClientConnection(clientSocket);
                    } catch (IOException e) {
                        if (mIsRunning) {
                            Log.e(TAG, "Error accepting connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to start server", e);
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Failed to start server: " + e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Stop HTTP server
     */
    public void stopServer() {
        mIsRunning = false;
        if (mServerSocket != null && !mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server", e);
            }
        }
        if (mCallback != null) {
            mHandler.post(() -> mCallback.onServerStopped());
        }
    }
    
    /**
     * Send file to device via HTTP POST
     * Similar to LocalSend's REST API approach
     */
    public void sendFile(String targetIp, int port, File file) {
        mExecutor.execute(() -> {
            try {
                // Simple HTTP POST with multipart form data
                // POST /api/v1/files
                // Content-Type: multipart/form-data
                sendFileViaHttp(targetIp, port, file);
                
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onTransferComplete());
                }
            } catch (Exception e) {
                Log.e(TAG, "File send failed", e);
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError(e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Discover devices on local network
     * Scans local network for other BShare servers
     */
    public void discoverDevices() {
        mExecutor.execute(() -> {
            String localIp = getLocalIpAddress();
            if (localIp == null) {
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Not connected to WiFi"));
                }
                return;
            }
            
            // Simple network scan - check common IP range
            // In production, use mDNS/Bonjour for proper discovery
            scanLocalNetwork(localIp);
        });
    }
    
    /**
     * Get local IP address
     */
    private String getLocalIpAddress() {
        try {
            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) {
                return null;
            }
            
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            
            if (ipAddress == 0) {
                return null;
            }
            
            return String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff));
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address", e);
            return null;
        }
    }
    
    /**
     * Handle incoming client connection
     */
    private void handleClientConnection(Socket socket) {
        mExecutor.execute(() -> {
            try {
                // Simple HTTP request handler
                // Parse HTTP request and handle file upload
                handleHttpRequest(socket);
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error handling connection", e);
            }
        });
    }
    
    /**
     * Simple HTTP request handler
     * Handles POST /api/v1/files for file uploads
     */
    private void handleHttpRequest(Socket socket) throws IOException {
        // Basic HTTP request parsing
        // In production, use proper HTTP library
        java.io.BufferedReader reader = new java.io.BufferedReader(
            new java.io.InputStreamReader(socket.getInputStream()));
        
        String line = reader.readLine();
        if (line != null && line.contains("POST")) {
            // Handle file upload
            // Simplified - would need proper multipart parsing
            Log.d(TAG, "Received file upload request");
        }
    }
    
    /**
     * Send file via HTTP POST
     */
    private void sendFileViaHttp(String targetIp, int port, File file) throws IOException {
        Socket socket = new Socket(targetIp, port);
        java.io.OutputStream out = socket.getOutputStream();
        java.io.PrintWriter writer = new java.io.PrintWriter(out);
        
        // Simple HTTP POST request
        writer.println("POST /api/v1/files HTTP/1.1");
        writer.println("Host: " + targetIp + ":" + port);
        writer.println("Content-Type: multipart/form-data; boundary=----BShareBoundary");
        writer.println("Content-Length: " + (file.length() + 200)); // Approximate
        writer.println();
        writer.flush();
        
        // Send file data
        java.io.FileInputStream fileIn = new java.io.FileInputStream(file);
        byte[] buffer = new byte[8192];
        long totalBytes = file.length();
        long sentBytes = 0;
        int bytesRead;
        
        while ((bytesRead = fileIn.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
            sentBytes += bytesRead;
            
            int percent = (int) ((sentBytes * 100) / totalBytes);
            if (mCallback != null) {
                mHandler.post(() -> mCallback.onTransferProgress(percent));
            }
        }
        
        fileIn.close();
        out.flush();
        socket.close();
    }
    
    /**
     * Scan local network for BShare devices
     * Simplified - checks common IP range
     */
    private void scanLocalNetwork(String localIp) {
        // Extract network prefix (e.g., 192.168.1 from 192.168.1.100)
        String[] parts = localIp.split("\\.");
        if (parts.length != 4) {
            return;
        }
        
        String networkPrefix = parts[0] + "." + parts[1] + "." + parts[2] + ".";
        
        // Scan common IP range (1-254)
        for (int i = 1; i <= 254; i++) {
            final String testIp = networkPrefix + i;
            if (testIp.equals(localIp)) {
                continue; // Skip own IP
            }
            
            // Check if BShare server is running on this IP
            checkBShareServer(testIp, DEFAULT_PORT);
        }
    }
    
    /**
     * Check if BShare server exists on given IP
     */
    private void checkBShareServer(String ip, int port) {
        mExecutor.execute(() -> {
            try {
                Socket testSocket = new Socket();
                testSocket.connect(new java.net.InetSocketAddress(ip, port), 500); // 500ms timeout
                testSocket.close();
                
                // Server found!
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onDeviceFound("BShare Device", ip));
                }
            } catch (IOException e) {
                // No server on this IP - ignore
            }
        });
    }
    
    public boolean isServerRunning() {
        return mIsRunning;
    }
    
    public String getServerIp() {
        return getLocalIpAddress();
    }
    
    public int getServerPort() {
        return DEFAULT_PORT;
    }
}


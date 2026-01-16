/*
 * Copyright (C) 2025 BashaMobile
 *
 * BShare - LocalSend-inspired file sharing using WiFi HTTP server
 * Works with MicroG - no Google Play Services required
 * Uses local WiFi network for fast file transfers
 */

package com.android.settings.bshare;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.SoftApConfiguration;
import android.net.wifi.WifiManager.LocalOnlyHotspotCallback;
import android.net.wifi.WifiManager.LocalOnlyHotspotReservation;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private LocalOnlyHotspotReservation mHotspotReservation;
    private WifiManager mWifiManager;
    private boolean mUseHotspotMode = false;
    private PowerManager.WakeLock mWakeLock;
    private NotificationManager mNotificationManager;
    private static final String NOTIFICATION_CHANNEL_ID = "bshare_server_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    public interface BShareCallback {
        void onServerStarted(String ipAddress, int port);
        void onServerStopped();
        void onDeviceFound(String deviceName, String ipAddress);
        void onConnectionResult(boolean success);
        void onTransferProgress(int percent);
        void onTransferComplete();
        void onError(String error);
        void onHotspotStarted(String ssid, String password, String ipAddress);
        void onHotspotStopped();
    }
    
    public BShareManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newCachedThreadPool();
        mWifiManager = (WifiManager) context.getApplicationContext()
            .getSystemService(Context.WIFI_SERVICE);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
        setInstance(this); // Register instance for notification receiver
    }
    
    /**
     * Create notification channel for Android O+
     * Inspired by prim-ftpd: https://github.com/wolpi/prim-ftpd
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "BShare Server",
                NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows when BShare server is running");
            channel.setShowBadge(false);
            mNotificationManager.createNotificationChannel(channel);
        }
    }
    
    /**
     * Acquire wakelock to prevent device from sleeping during transfers
     * Inspired by prim-ftpd: https://github.com/wolpi/prim-ftpd
     */
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BShare::WakeLock");
            mWakeLock.setReferenceCounted(false);
        }
        if (!mWakeLock.isHeld()) {
            mWakeLock.acquire();
            Log.d(TAG, "WakeLock acquired");
        }
    }
    
    /**
     * Release wakelock
     */
    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            Log.d(TAG, "WakeLock released");
        }
    }
    
    /**
     * Show statusbar notification with connection info
     * Inspired by prim-ftpd: https://github.com/wolpi/prim-ftpd
     */
    private void showNotification(String ipAddress, int port, String ssid, String password) {
        try {
            // Create intent to open BShare settings
            // Use Settings activity with BShare fragment
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            intent.putExtra(":settings:fragment_args_key", "bshare_settings");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            String title = "BShare Server Running";
            String contentText;
            if (ssid != null && password != null) {
                contentText = String.format("Hotspot: %s\nIP: %s:%d", ssid, ipAddress, port);
            } else {
                contentText = String.format("IP: %s:%d", ipAddress, port);
            }
            
            Notification.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(mContext, NOTIFICATION_CHANNEL_ID);
            } else {
                builder = new Notification.Builder(mContext);
            }
            
            builder.setContentTitle(title)
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.ic_menu_share)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setShowWhen(false);
            
            // Add stop action
            Intent stopIntent = new Intent(mContext, BShareStopReceiver.class);
            stopIntent.setAction("com.android.settings.bshare.STOP_SERVER");
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(mContext, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, 
                    "Stop", stopPendingIntent);
            }
            
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Notification shown: " + contentText);
        } catch (Exception e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }
    
    /**
     * Hide statusbar notification
     */
    private void hideNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
        Log.d(TAG, "Notification hidden");
    }
    
    /**
     * Set whether to use hotspot mode (ad-hoc WiFi) or regular WiFi network
     * @param useHotspot true to create LocalOnlyHotspot, false to use existing WiFi
     */
    public void setUseHotspotMode(boolean useHotspot) {
        mUseHotspotMode = useHotspot;
    }
    
    public void setCallback(BShareCallback callback) {
        mCallback = callback;
    }
    
    /**
     * Start HTTP server for receiving files
     * Similar to LocalSend's approach, with optional LocalOnlyHotspot support (FlyingCarpet-inspired)
     */
    public void startServer() {
        if (mIsRunning) {
            return;
        }
        
        if (mUseHotspotMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use LocalOnlyHotspot (ad-hoc WiFi) - inspired by FlyingCarpet
            startServerWithHotspot();
        } else {
            // Use existing WiFi network
            startServerOnNetwork();
        }
    }
    
    /**
     * Start server with LocalOnlyHotspot (ad-hoc WiFi)
     * Inspired by FlyingCarpet: https://github.com/spieglt/FlyingCarpet
     */
    private void startServerWithHotspot() {
        if (mWifiManager == null) {
            if (mCallback != null) {
                mHandler.post(() -> mCallback.onError("WiFi manager not available"));
            }
            return;
        }
        
        try {
            // Request LocalOnlyHotspot
            // Note: Android generates SSID and password automatically for LocalOnlyHotspot
            // We can't set custom SSID/password, but we can read them from the reservation
            // API signature: startLocalOnlyHotspot(callback, handler)
            mWifiManager.startLocalOnlyHotspot(
                new LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(LocalOnlyHotspotReservation reservation) {
                        mHotspotReservation = reservation;
                        SoftApConfiguration config = reservation.getSoftApConfiguration();
                        String ssid = config.getSsid();
                        String pass = config.getPassphrase();
                        
                        // Get IP address from hotspot
                        String ipAddress = getHotspotIpAddress();
                        if (ipAddress == null) {
                            ipAddress = "192.168.43.1"; // Default hotspot IP
                        }
                        
                        mExecutor.execute(() -> {
                            try {
                                // Start HTTP server
                                mServerSocket = new ServerSocket();
                                mServerSocket.setReuseAddress(true);
                                mServerSocket.bind(new InetSocketAddress(DEFAULT_PORT), 50);
                                mServerSocket.setSoTimeout(1000);
                                mIsRunning = true;
                                
                                // Acquire wakelock to prevent sleep during transfers
                                acquireWakeLock();
                                
                                // Show notification
                                showNotification(ipAddress, DEFAULT_PORT, ssid, pass);
                                
                                if (mCallback != null) {
                                    mHandler.post(() -> {
                                        mCallback.onHotspotStarted(ssid, pass, ipAddress);
                                        mCallback.onServerStarted(ipAddress, DEFAULT_PORT);
                                    });
                                }
                                
                                Log.d(TAG, "BShare hotspot started: " + ssid + " @ " + ipAddress);
                                
                                // Accept connections
                                while (mIsRunning && !mServerSocket.isClosed()) {
                                    try {
                                        Socket clientSocket = mServerSocket.accept();
                                        handleClientConnection(clientSocket);
                                    } catch (SocketTimeoutException e) {
                                        continue;
                                    } catch (IOException e) {
                                        if (mIsRunning) {
                                            Log.e(TAG, "Error accepting connection", e);
                                        }
                                    }
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Failed to start server on hotspot", e);
                                mIsRunning = false;
                                if (mCallback != null) {
                                    mHandler.post(() -> mCallback.onError("Failed to start server: " + e.getMessage()));
                                }
                            }
                        });
                    }
                    
                    @Override
                    public void onStopped() {
                        mHotspotReservation = null;
                        mIsRunning = false;
                        if (mCallback != null) {
                            mHandler.post(() -> mCallback.onHotspotStopped());
                        }
                        Log.d(TAG, "Hotspot stopped");
                    }
                    
                    @Override
                    public void onFailed(int reason) {
                        mIsRunning = false;
                        String errorMsg = "Failed to start hotspot: ";
                        switch (reason) {
                            case LocalOnlyHotspotCallback.ERROR_NO_CHANNEL:
                                errorMsg += "No channel available";
                                break;
                            case LocalOnlyHotspotCallback.ERROR_GENERIC:
                            default:
                                errorMsg += "Generic error (code " + reason + ")";
                                break;
                        }
                        if (mCallback != null) {
                            mHandler.post(() -> mCallback.onError(errorMsg));
                        }
                        Log.e(TAG, errorMsg);
                    }
                },
                new android.os.Handler(Looper.getMainLooper()));
        } catch (Exception e) {
            Log.e(TAG, "Exception starting hotspot", e);
            if (mCallback != null) {
                mHandler.post(() -> mCallback.onError("Exception: " + e.getMessage()));
            }
        }
    }
    
    /**
     * Start server on existing WiFi network
     */
    private void startServerOnNetwork() {
        mExecutor.execute(() -> {
            try {
                String ipAddress = getLocalIpAddress();
                if (ipAddress == null) {
                    if (mCallback != null) {
                        mHandler.post(() -> mCallback.onError("Not connected to WiFi"));
                    }
                    return;
                }
                
                // Bind to all interfaces (0.0.0.0) so it's accessible from network
                mServerSocket = new ServerSocket();
                mServerSocket.setReuseAddress(true);
                mServerSocket.bind(new InetSocketAddress(DEFAULT_PORT), 50);
                mServerSocket.setSoTimeout(1000); // 1 second timeout for accept
                mIsRunning = true;
                
                // Acquire wakelock to prevent sleep during transfers
                acquireWakeLock();
                
                // Show notification
                showNotification(ipAddress, DEFAULT_PORT, null, null);
                
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onServerStarted(ipAddress, DEFAULT_PORT));
                }
                
                Log.d(TAG, "BShare server started on " + ipAddress + ":" + DEFAULT_PORT);
                
                // Accept connections
                while (mIsRunning && !mServerSocket.isClosed()) {
                    try {
                        Socket clientSocket = mServerSocket.accept();
                        handleClientConnection(clientSocket);
                    } catch (SocketTimeoutException e) {
                        // Timeout is expected, continue loop
                        continue;
                    } catch (IOException e) {
                        if (mIsRunning) {
                            Log.e(TAG, "Error accepting connection", e);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to start server", e);
                mIsRunning = false;
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Failed to start server: " + e.getMessage()));
                }
            }
        });
    }
    
    /**
     * Generate random password for hotspot
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Avoid ambiguous chars
        StringBuilder password = new StringBuilder(8);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
    
    /**
     * Get IP address from LocalOnlyHotspot
     */
    private String getHotspotIpAddress() {
        try {
            // LocalOnlyHotspot typically uses 192.168.43.1 or similar
            // Try to get actual IP from network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getName().contains("wlan") || 
                    networkInterface.getName().contains("ap")) {
                    Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();
                        if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                            String ip = address.getHostAddress();
                            if (ip.startsWith("192.168.") || ip.startsWith("10.0.")) {
                                return ip;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting hotspot IP", e);
        }
        return "192.168.43.1"; // Default fallback
    }
    
    /**
     * Stop HTTP server and hotspot if running
     */
    public void stopServer() {
        mIsRunning = false;
        
        // Release wakelock
        releaseWakeLock();
        
        // Hide notification
        hideNotification();
        
        // Close server socket
        if (mServerSocket != null && !mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing server", e);
            }
        }
        
        // Stop hotspot if running
        if (mHotspotReservation != null) {
            try {
                mHotspotReservation.close();
                mHotspotReservation = null;
            } catch (Exception e) {
                Log.e(TAG, "Error closing hotspot", e);
            }
        }
        
        if (mCallback != null) {
            mHandler.post(() -> {
                mCallback.onServerStopped();
                if (mHotspotReservation == null) {
                    mCallback.onHotspotStopped();
                }
            });
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
     * Get local IP address - improved method that works on all Android versions
     */
    private String getLocalIpAddress() {
        try {
            // Method 1: Try WiFiManager (works on older Android)
            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                
                if (ipAddress != 0) {
                    return String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
                }
            }
            
            // Method 2: Use NetworkInterface (works on all Android versions)
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                        String ip = address.getHostAddress();
                        // Filter out link-local addresses
                        if (!ip.startsWith("169.254.")) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting IP address", e);
        }
        return null;
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
     * Handle HTTP request - supports GET for discovery and POST for file uploads
     */
    private void handleHttpRequest(Socket socket) throws IOException {
        java.io.InputStream rawInputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(rawInputStream, StandardCharsets.UTF_8));
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true);
        
        try {
            String requestLine = reader.readLine();
            if (requestLine == null) {
                return;
            }
            
            Log.d(TAG, "HTTP Request: " + requestLine);
            
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                sendHttpError(writer, outputStream, 400, "Bad Request");
                return;
            }
            
            String method = requestParts[0];
            String path = requestParts[1];
            
            // Read headers
            String line;
            int contentLength = 0;
            String boundary = null;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                String lowerLine = line.toLowerCase();
                if (lowerLine.startsWith("content-length:")) {
                    try {
                        contentLength = Integer.parseInt(line.substring(15).trim());
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                } else if (lowerLine.contains("boundary=")) {
                    int boundaryStart = lowerLine.indexOf("boundary=") + 9;
                    boundary = line.substring(boundaryStart).trim();
                }
            }
            
            // Handle GET request (discovery/info)
            if ("GET".equals(method)) {
                if (path.equals("/") || path.equals("/api/info") || path.startsWith("/api/info")) {
                    sendHttpInfo(writer, outputStream);
                } else {
                    sendHttpError(writer, outputStream, 404, "Not Found");
                }
            }
            // Handle POST request (file upload)
            else if ("POST".equals(method) && path.startsWith("/api/v1/files")) {
                handleFileUpload(rawInputStream, outputStream, writer, contentLength, boundary);
            } else {
                sendHttpError(writer, outputStream, 405, "Method Not Allowed");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling HTTP request", e);
            try {
                sendHttpError(new PrintWriter(outputStream, true), outputStream, 500, "Internal Server Error");
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
    
    /**
     * Send HTTP info response for discovery
     */
    private void sendHttpInfo(PrintWriter writer, OutputStream outputStream) throws IOException {
        String deviceName = Build.MODEL;
        String ipAddress = getLocalIpAddress();
        
        String jsonResponse = String.format(
            "{\"name\":\"%s\",\"ip\":\"%s\",\"port\":%d,\"type\":\"bshare\"}",
            deviceName, ipAddress != null ? ipAddress : "unknown", DEFAULT_PORT);
        
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Content-Length: " + jsonResponse.length());
        writer.println();
        writer.flush();
        
        outputStream.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
        
        Log.d(TAG, "Sent info response: " + jsonResponse);
    }
    
    /**
     * Handle file upload via POST
     * Properly handles binary file data from multipart form
     */
    private void handleFileUpload(java.io.InputStream inputStream, OutputStream outputStream, 
                                  PrintWriter writer, int contentLength, String boundary) throws IOException {
        // Create downloads directory if needed
        File downloadsDir = new File(mContext.getExternalFilesDir(null), "BShare");
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        
        String filename = "bshare_" + System.currentTimeMillis() + ".bin";
        
        // Read multipart body - find filename in headers
        byte[] lineBuffer = new byte[1024];
        int linePos = 0;
        boolean inHeaders = true;
        boolean foundFilename = false;
        
        // Read until we find the file data (after empty line following headers)
        while (inHeaders && linePos < 2048) {
            int b = inputStream.read();
            if (b == -1) break;
            
            lineBuffer[linePos++] = (byte) b;
            
            // Check for newline
            if (b == '\n' && linePos > 1 && lineBuffer[linePos - 2] == '\r') {
                String line = new String(lineBuffer, 0, linePos - 2, StandardCharsets.UTF_8);
                
                // Check for filename
                if (!foundFilename && line.toLowerCase().contains("filename=")) {
                    int start = line.indexOf('"') + 1;
                    int end = line.indexOf('"', start);
                    if (end > start) {
                        filename = line.substring(start, end);
                        foundFilename = true;
                    }
                }
                
                // Empty line means end of headers, start of file data
                if (line.trim().isEmpty()) {
                    inHeaders = false;
                    break;
                }
                
                linePos = 0;
            }
        }
        
        // Read file data (binary) - read until we hit boundary or contentLength
        File outputFile = new File(downloadsDir, filename);
        FileOutputStream fileOut = new FileOutputStream(outputFile);
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalRead = 0;
        long maxRead = contentLength > 0 ? contentLength - linePos - 200 : Long.MAX_VALUE; // Reserve space for boundary
        
        // Read file data
        while (totalRead < maxRead && (bytesRead = inputStream.read(buffer, 0, 
                (int) Math.min(buffer.length, maxRead - totalRead))) > 0) {
            // Simple boundary check - look for boundary marker at start of buffer
            if (boundary != null && bytesRead > boundary.length() + 4) {
                String check = new String(buffer, 0, Math.min(boundary.length() + 4, bytesRead), StandardCharsets.UTF_8);
                if (check.contains("\r\n--" + boundary)) {
                    // Found boundary, write up to boundary
                    int boundaryPos = check.indexOf("\r\n--");
                    if (boundaryPos > 0) {
                        fileOut.write(buffer, 0, boundaryPos);
                        totalRead += boundaryPos;
                    }
                    break;
                }
            }
            
            fileOut.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }
        
        fileOut.close();
        
        // Send success response
        String response = "{\"status\":\"ok\",\"file\":\"" + filename + "\",\"size\":" + totalRead + "}";
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Content-Length: " + response.length());
        writer.println();
        writer.println(response);
        writer.flush();
        
        Log.d(TAG, "File received: " + outputFile.getAbsolutePath() + " (" + totalRead + " bytes)");
        
        if (mCallback != null) {
            mHandler.post(() -> mCallback.onTransferComplete());
        }
    }
    
    /**
     * Send HTTP error response
     */
    private void sendHttpError(PrintWriter writer, OutputStream outputStream, 
                               int code, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\"}";
        writer.println("HTTP/1.1 " + code + " " + message);
        writer.println("Content-Type: application/json");
        writer.println("Content-Length: " + response.length());
        writer.println();
        writer.println(response);
        writer.flush();
    }
    
    /**
     * Send file via HTTP POST with proper multipart form data
     */
    private void sendFileViaHttp(String targetIp, int port, File file) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(targetIp, port), 5000);
        socket.setSoTimeout(30000); // 30 second timeout
        
        OutputStream out = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);
        
        String boundary = "----BShareBoundary" + System.currentTimeMillis();
        String filename = file.getName();
        long fileSize = file.length();
        
        // Calculate content length
        String header = "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n" +
            "Content-Type: application/octet-stream\r\n\r\n";
        String footer = "\r\n--" + boundary + "--\r\n";
        long contentLength = header.length() + fileSize + footer.length();
        
        // Send HTTP POST request
        writer.println("POST /api/v1/files HTTP/1.1");
        writer.println("Host: " + targetIp + ":" + port);
        writer.println("Content-Type: multipart/form-data; boundary=" + boundary);
        writer.println("Content-Length: " + contentLength);
        writer.println();
        writer.flush();
        
        // Send multipart header
        out.write(header.getBytes(StandardCharsets.UTF_8));
        
        // Send file data
        FileInputStream fileIn = new FileInputStream(file);
        byte[] buffer = new byte[8192];
        long totalBytes = fileSize;
        long sentBytes = 0;
        int bytesRead;
        
        while ((bytesRead = fileIn.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
            sentBytes += bytesRead;
            
            int percent = (int) ((sentBytes * 100) / totalBytes);
            if (mCallback != null && percent % 10 == 0) { // Update every 10%
                mHandler.post(() -> mCallback.onTransferProgress(percent));
            }
        }
        
        fileIn.close();
        
        // Send multipart footer
        out.write(footer.getBytes(StandardCharsets.UTF_8));
        out.flush();
        
        // Read response
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        String responseLine = reader.readLine();
        if (responseLine != null && responseLine.contains("200")) {
            Log.d(TAG, "File sent successfully");
        }
        
        socket.close();
    }
    
    /**
     * Scan local network for BShare devices
     * Uses parallel scanning with HTTP GET /api/info for discovery
     */
    private void scanLocalNetwork(String localIp) {
        // Extract network prefix (e.g., 192.168.1 from 192.168.1.100)
        String[] parts = localIp.split("\\.");
        if (parts.length != 4) {
            if (mCallback != null) {
                mHandler.post(() -> mCallback.onError("Invalid IP address format"));
            }
            return;
        }
        
        String networkPrefix = parts[0] + "." + parts[1] + "." + parts[2] + ".";
        
        // Use parallel scanning for faster discovery
        List<String> ipList = new ArrayList<>();
        for (int i = 1; i <= 254; i++) {
            String testIp = networkPrefix + i;
            if (!testIp.equals(localIp)) {
                ipList.add(testIp);
            }
        }
        
        // Scan in parallel batches
        int batchSize = 20; // Scan 20 IPs at a time
        CountDownLatch latch = new CountDownLatch(ipList.size());
        
        for (String ip : ipList) {
            mExecutor.execute(() -> {
                try {
                    checkBShareServer(ip, DEFAULT_PORT);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Wait for all scans to complete (with timeout)
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Discovery interrupted", e);
        }
        
        Log.d(TAG, "Discovery scan completed");
    }
    
    /**
     * Check if BShare server exists on given IP by sending HTTP GET /api/info
     */
    private void checkBShareServer(String ip, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000); // 1 second timeout
            socket.setSoTimeout(2000);
            
            // Send HTTP GET request
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            
            writer.println("GET /api/info HTTP/1.1");
            writer.println("Host: " + ip + ":" + port);
            writer.println();
            writer.flush();
            
            // Read response
            String line = reader.readLine();
            if (line != null && line.contains("200")) {
                // Read headers
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    // Skip headers
                }
                
                // Read JSON response
                StringBuilder json = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                
                String deviceName = "BShare Device";
                if (json.length() > 0) {
                    // Simple JSON parsing (extract name if present)
                    String jsonStr = json.toString();
                    int nameIndex = jsonStr.indexOf("\"name\":\"");
                    if (nameIndex >= 0) {
                        int nameEnd = jsonStr.indexOf("\"", nameIndex + 8);
                        if (nameEnd > nameIndex) {
                            deviceName = jsonStr.substring(nameIndex + 8, nameEnd);
                        }
                    }
                }
                
                // Server found!
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onDeviceFound(deviceName, ip));
                }
                
                Log.d(TAG, "BShare device found: " + deviceName + " @ " + ip);
            }
        } catch (IOException e) {
            // No server on this IP - ignore silently
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
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
    
    /**
     * Static method to stop server from notification
     * Called by BShareStopReceiver
     */
    private static BShareManager sInstance;
    
    public static void setInstance(BShareManager instance) {
        sInstance = instance;
    }
    
    public static void stopServerFromNotification(Context context) {
        if (sInstance != null) {
            sInstance.stopServer();
        } else {
            // Fallback: use SharedPreferences or start service to stop
            Log.w(TAG, "No BShareManager instance available to stop server");
        }
    }
}


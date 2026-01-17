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
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
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

    // Nearby Share mDNS constants
    private static final String NEARBY_SHARE_SERVICE_TYPE = "_FC9F5ED42C8A._tcp.";
    private static final String NEARBY_SHARE_SERVICE_NAME_PREFIX = "NEARBY_SHARE_";

    private final Context mContext;
    private Handler mHandler;
    private BShareCallback mCallback;
    private ServerSocket mServerSocket;
    private ExecutorService mExecutor;
    private boolean mIsRunning = false;
    private LocalOnlyHotspotReservation mHotspotReservation;
    private WifiManager mWifiManager;
    private NsdManager mNsdManager;
    private boolean mUseHotspotMode = false;

    // mDNS listeners for Nearby Share
    private RegistrationListener mRegistrationListener;
    private DiscoveryListener mDiscoveryListener;
    private ResolveListener mResolveListener;
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

    public void unregisterCallback() {
        mCallback = null;
    }

    public BShareManager(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newCachedThreadPool();
        mWifiManager = (WifiManager) context.getApplicationContext()
            .getSystemService(Context.WIFI_SERVICE);
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Test wakelock functionality
        acquireWakeLock();
        releaseWakeLock();

        setInstance(this); // Register instance for notification receiver
        Log.d(TAG, "BShareManager initialized successfully");
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
            Intent intent = new Intent(mContext, com.android.settings.bshare.BShareSettingsFragment.class);
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
                        final String finalIpAddress = ipAddress;
                        final String finalSsid = ssid;
                        final String finalPass = pass;
                        
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
                                showNotification(finalIpAddress, DEFAULT_PORT, finalSsid, finalPass);

                                if (mCallback != null) {
                                    mHandler.post(() -> {
                                        mCallback.onHotspotStarted(finalSsid, finalPass, finalIpAddress);
                                        mCallback.onServerStarted(finalIpAddress, DEFAULT_PORT);
                                    });
                                }

                                Log.d(TAG, "BShare hotspot started: " + finalSsid + " @ " + finalIpAddress);
                                
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
                        final String finalErrorMsg = errorMsg;
                        if (mCallback != null) {
                            mHandler.post(() -> mCallback.onError(finalErrorMsg));
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
            final String ipAddress = getLocalIpAddress();
            if (ipAddress == null) {
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Not connected to WiFi"));
                }
                return;
            }

            int port = DEFAULT_PORT;
            try {
                final ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);

                boolean bound = false;
                for (int attempt = 0; attempt < 10 && !bound; attempt++) {
                    try {
                        serverSocket.bind(new InetSocketAddress(port), 50);
                        bound = true;
                        Log.d(TAG, "Bound to port " + port);
                    } catch (IOException e) {
                        port = DEFAULT_PORT + attempt + 1;
                        Log.w(TAG, "Port busy, trying " + port, e);
                    }
                }

                if (!bound) {
                    throw new IOException("Could not bind to any port");
                }

                serverSocket.setSoTimeout(1000);
                mServerSocket = serverSocket;
                mIsRunning = true;

                acquireWakeLock();
                showNotification(ipAddress, port, null, null);

                startNearbyShareAdvertisement();
                startUdpDiscoveryResponder();

                if (mCallback != null) {
                    final int finalPort = port;
                    mHandler.post(() -> mCallback.onServerStarted(ipAddress, finalPort));
                }

                Log.d(TAG, "BShare server started on " + ipAddress + ":" + port);

                while (mIsRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClientConnection(clientSocket);
                    } catch (SocketTimeoutException e) {
                        // Expected - allow checking mIsRunning periodically.
                    } catch (IOException e) {
                        if (mIsRunning) {
                            Log.e(TAG, "Error accepting connection", e);
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to start server", e);
                mIsRunning = false;
                releaseWakeLock();
                hideNotification();
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Failed to start server: " + e.getMessage()));
                }
                if (mServerSocket != null) {
                    try {
                        mServerSocket.close();
                    } catch (IOException closeException) {
                        Log.w(TAG, "Error closing server socket after failure", closeException);
                    }
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
        
        // Stop Nearby Share mDNS services
        stopNearbyShareServices();

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
            try {
                // Method 1: Multicast UDP discovery (LocalSend primary method)
                discoverViaMulticast();

                // Method 1.5: Nearby Share mDNS discovery (Google compatibility)
                startNearbyShareDiscovery();

                final String localIp = getLocalIpAddress();
                if (localIp != null) {
                    // Method 2: HTTP fallback discovery (for Windows compatibility)
                    discoverViaHttpFallback(localIp);
                    // Method 3: Network scan fallback
                    scanLocalNetwork(localIp);
                } else {
                    if (mCallback != null) {
                        mHandler.post(() -> mCallback.onError("Not connected to WiFi"));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during device discovery", e);
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onError("Discovery failed: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Discover devices using multicast UDP (LocalSend primary method)
     */
    private void discoverViaMulticast() {
        java.net.MulticastSocket socket = null;
        try {
            Log.d(TAG, "Starting LocalSend multicast discovery on port 53317");

            // Use LocalSend protocol: Multicast UDP discovery
            socket = new java.net.MulticastSocket(53317);
            socket.setSoTimeout(5000); // 5 second timeout
            socket.setReuseAddress(true); // Allow reuse for Windows compatibility

            // Join multicast group (LocalSend uses 224.0.0.167)
            java.net.InetAddress multicastGroup = java.net.InetAddress.getByName("224.0.0.167");
            socket.joinGroup(multicastGroup);

            Log.d(TAG, "Joined multicast group 224.0.0.167:53317");

            // Send LocalSend announcement
            String announcement = "{\"alias\":\"" + android.os.Build.MODEL +
                "\",\"version\":\"2.0\",\"deviceModel\":\"" + android.os.Build.MODEL +
                "\",\"deviceType\":\"mobile\",\"fingerprint\":\"" + generateFingerprint() +
                "\",\"port\":" + DEFAULT_PORT + ",\"protocol\":\"http\",\"download\":true,\"announce\":true}";

            byte[] sendData = announcement.getBytes(StandardCharsets.UTF_8);
            java.net.DatagramPacket sendPacket = new java.net.DatagramPacket(
                sendData, sendData.length, multicastGroup, 53317);

            socket.send(sendPacket);
            Log.d(TAG, "Sent LocalSend multicast announcement: " + announcement);

            // Listen for responses (both multicast and HTTP register responses)
            byte[] recvBuf = new byte[4096];
            java.net.DatagramPacket receivePacket = new java.net.DatagramPacket(recvBuf, recvBuf.length);

            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 5000) { // Listen for 5 seconds
                try {
                    socket.receive(receivePacket);
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength(),
                        StandardCharsets.UTF_8);

                    Log.d(TAG, "Received multicast response: " + response);

                    // Parse LocalSend announcement response
                    if (response.contains("\"alias\"") && response.contains("\"fingerprint\"")) {
                        try {
                            // Extract device info from JSON
                            String alias = extractJsonValue(response, "alias");
                            String deviceModel = extractJsonValue(response, "deviceModel");
                            String fingerprint = extractJsonValue(response, "fingerprint");
                            String protocol = extractJsonValue(response, "protocol");
                            int port = Integer.parseInt(extractJsonValue(response, "port"));
                            String deviceType = extractJsonValue(response, "deviceType");

                            // Get IP from the packet
                            String ipAddress = receivePacket.getAddress().getHostAddress();

                            // Send HTTP register response back (LocalSend protocol)
                            sendHttpRegisterResponse(ipAddress, port, alias, deviceModel, fingerprint, protocol);

                            if (mCallback != null) {
                                String deviceName = alias != null ? alias : (deviceModel != null ? deviceModel : "Unknown Device");
                                mHandler.post(() -> mCallback.onDeviceFound(deviceName, ipAddress));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing discovery response", e);
                        }
                        String ipAddress = receivePacket.getAddress().getHostAddress();
                        String deviceName = extractDeviceName(response);
                        if (mCallback != null) {
                            mHandler.post(() -> mCallback.onDeviceFound(deviceName, ipAddress));
                        }
                        Log.d(TAG, "Found device: " + deviceName + " at " + ipAddress);
                    }
                } catch (java.net.SocketTimeoutException e) {
                    // Timeout is expected, continue
                    break;
                }
            }

            socket.close();

        } catch (Exception e) {
            Log.e(TAG, "Multicast discovery failed: " + e.getMessage() + " (This is normal on some networks, HTTP fallback will work)", e);
            // For Windows compatibility: multicast may fail on public networks
            // The HTTP fallback discovery will still work
        }
    }

    private String extractDeviceName(String json) {
        try {
            // Simple JSON parsing for device name
            int nameStart = json.indexOf("\"deviceName\":\"") + 14;
            if (nameStart > 13) {
                int nameEnd = json.indexOf("\"", nameStart);
                if (nameEnd > nameStart) {
                    return json.substring(nameStart, nameEnd);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to parse device name from discovery response", e);
        }
        return "Unknown Device";
    }

    /**
     * Start UDP discovery responder that listens for discovery broadcasts
     */
    private void startUdpDiscoveryResponder() {
        mExecutor.execute(() -> {
            java.net.MulticastSocket socket = null;
            try {
                Log.d(TAG, "Starting LocalSend multicast responder on port 53317");

                socket = new java.net.MulticastSocket(53317); // LocalSend port
                socket.setSoTimeout(1000); // 1 second timeout
                socket.setReuseAddress(true); // Windows compatibility

                // Join multicast group
                java.net.InetAddress multicastGroup = java.net.InetAddress.getByName("224.0.0.167");
                socket.joinGroup(multicastGroup);

                Log.d(TAG, "Multicast responder joined group 224.0.0.167:53317");

                byte[] recvBuf = new byte[4096];

                while (mIsRunning) {
                    try {
                        java.net.DatagramPacket receivePacket = new java.net.DatagramPacket(recvBuf, recvBuf.length);
                        socket.receive(receivePacket);

                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength(),
                            StandardCharsets.UTF_8);

                        Log.d(TAG, "Received multicast message: " + message);

                        // Check for LocalSend announcement
                        if (message.contains("\"alias\"") && message.contains("\"announce\":true")) {
                            try {
                                // Extract device info
                                String alias = extractJsonValue(message, "alias");
                                String deviceModel = extractJsonValue(message, "deviceModel");
                                String fingerprint = extractJsonValue(message, "fingerprint");
                                String protocol = extractJsonValue(message, "protocol");
                                int port = Integer.parseInt(extractJsonValue(message, "port"));
                                String deviceType = extractJsonValue(message, "deviceType");

                                String senderIp = receivePacket.getAddress().getHostAddress();

                                // Don't respond to our own announcements
                                String myFingerprint = generateFingerprint();
                                if (!fingerprint.equals(myFingerprint)) {
                                    // Send HTTP register response (LocalSend protocol)
                                    sendHttpRegisterResponse(senderIp, port, alias, deviceModel, fingerprint, protocol);

                                    // Notify UI about discovered device
                                    if (mCallback != null) {
                                        String deviceName = alias != null ? alias : (deviceModel != null ? deviceModel : "Unknown Device");
                                        mHandler.post(() -> mCallback.onDeviceFound(deviceName, senderIp));
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing LocalSend announcement", e);
                            }
                        }

                    } catch (java.net.SocketTimeoutException e) {
                        // Timeout is expected, continue listening
                    } catch (Exception e) {
                        if (mIsRunning) {
                            Log.e(TAG, "Error in UDP multicast discovery responder", e);
                        }
                    }
                }

                socket.leaveGroup(multicastGroup);

            } catch (Exception e) {
                Log.e(TAG, "Failed to start UDP multicast discovery responder", e);
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
    }
    
    /**
     * Get local IP address - improved method that works on all Android versions
     */
    String getLocalIpAddress() {
        try {
            // Method 1: Try WiFiManager (works on older Android)
            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();

                if (ipAddress != 0) {
                    String wifiIp = String.format("%d.%d.%d.%d",
                        (ipAddress & 0xff),
                        (ipAddress >> 8 & 0xff),
                        (ipAddress >> 16 & 0xff),
                        (ipAddress >> 24 & 0xff));
                    Log.d(TAG, "WiFi IP address: " + wifiIp);
                    return wifiIp;
                }
            }

            // Method 2: Use NetworkInterface (works on all Android versions)
            // Prioritize WiFi interfaces for Windows compatibility
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            NetworkInterface bestInterface = null;
            InetAddress bestAddress = null;

            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                // Skip loopback and down interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                String interfaceName = networkInterface.getName().toLowerCase();
                Log.d(TAG, "Checking interface: " + interfaceName);

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address instanceof java.net.Inet4Address) {
                        String ip = address.getHostAddress();
                        Log.d(TAG, "Interface " + interfaceName + " has IP: " + ip);

                        // Filter out link-local addresses (APIPA)
                        if (ip.startsWith("169.254.")) {
                            continue;
                        }

                        // Prioritize WiFi interfaces (wlan, wifi) for Windows compatibility
                        if (interfaceName.contains("wlan") || interfaceName.contains("wifi") ||
                            interfaceName.startsWith("wl")) {
                            Log.d(TAG, "Using WiFi interface: " + interfaceName + " with IP: " + ip);
                            return ip;
                        }

                        // Keep track of best non-WiFi interface as fallback
                        if (bestAddress == null) {
                            bestInterface = networkInterface;
                            bestAddress = address;
                        }
                    }
                }
            }

            // Return best available address
            if (bestAddress != null) {
                String ip = bestAddress.getHostAddress();
                Log.d(TAG, "Using fallback interface with IP: " + ip);
                return ip;
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
                } else if (path.equals("/api/localsend/v2/info")) {
                    // LocalSend info endpoint
                    sendLocalSendInfo(writer, outputStream);
                } else {
                    sendHttpError(writer, outputStream, 404, "Not Found");
                }
            }
            // Handle POST request (register/file upload)
            else if ("POST".equals(method)) {
                if (path.equals("/api/localsend/v2/register")) {
                    // LocalSend device registration
                    handleLocalSendRegister(rawInputStream, writer, outputStream, contentLength);
                } else if (path.startsWith("/api/v1/files") || path.startsWith("/api/localsend/v2/prepare")) {
                    // File upload (legacy or LocalSend)
                    handleFileUpload(rawInputStream, outputStream, writer, contentLength, boundary);
                } else {
                    sendHttpError(writer, outputStream, 404, "Not Found");
                }
            } else if ("OPTIONS".equals(method)) {
                // Handle CORS preflight
                sendCorsPreflight(writer, outputStream);
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
     * Send LocalSend v2 info response
     */
    private void sendLocalSendInfo(PrintWriter writer, OutputStream outputStream) throws IOException {
        String deviceName = android.os.Build.MODEL;
        String ipAddress = getLocalIpAddress();

        String jsonResponse = String.format(
            "{\"alias\":\"%s\",\"version\":\"2.0\",\"deviceModel\":\"%s\",\"deviceType\":\"mobile\",\"fingerprint\":\"%s\",\"port\":%d,\"protocol\":\"http\",\"download\":true}",
            deviceName, deviceName, generateFingerprint(), DEFAULT_PORT);

        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        writer.println("Access-Control-Allow-Headers: Content-Type");
        writer.println("Content-Length: " + jsonResponse.length());
        writer.println();
        writer.flush();

        outputStream.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();

        Log.d(TAG, "Sent LocalSend info response");
    }

    /**
     * Handle LocalSend device registration
     */
    private void handleLocalSendRegister(java.io.InputStream inputStream, PrintWriter writer, OutputStream outputStream, int contentLength) throws IOException {
        if (contentLength > 0) {
            byte[] buffer = new byte[contentLength];
            int bytesRead = inputStream.read(buffer, 0, contentLength);
            if (bytesRead > 0) {
                String registerData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                Log.d(TAG, "LocalSend register received: " + registerData);

                // Extract device info and notify callback
                String alias = extractJsonValue(registerData, "alias");
                String deviceModel = extractJsonValue(registerData, "deviceModel");
                String fingerprint = extractJsonValue(registerData, "fingerprint");

                if (mCallback != null && alias != null) {
                    // This is a device registering with us, notify UI
                    mHandler.post(() -> mCallback.onDeviceFound(alias, "registered"));
                }
            }
        }

        // Send success response
        String response = "{\"status\":\"success\"}";
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: application/json");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Content-Length: " + response.length());
        writer.println();
        writer.flush();

        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * Send CORS preflight response
     */
    private void sendCorsPreflight(PrintWriter writer, OutputStream outputStream) throws IOException {
        writer.println("HTTP/1.1 200 OK");
        writer.println("Access-Control-Allow-Origin: *");
        writer.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        writer.println("Access-Control-Allow-Headers: Content-Type");
        writer.println("Content-Length: 0");
        writer.println();
        writer.flush();
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
            Log.w(TAG, "Invalid IP address format for network scan");
            return;
        }

        String networkPrefix = parts[0] + "." + parts[1] + "." + parts[2] + ".";

        // Scan common device IP ranges first (gateways, common devices)
        List<String> priorityIPs = new ArrayList<>();
        priorityIPs.add(networkPrefix + "1");  // Gateway
        priorityIPs.add(networkPrefix + "100"); // Common device
        priorityIPs.add(networkPrefix + "101"); // Common device
        priorityIPs.add(networkPrefix + "102"); // Common device

        // Add local device IPs in a smaller range for efficiency
        for (int i = 2; i <= 20; i++) { // Reduced range for faster scanning
            String testIp = networkPrefix + i;
            if (!testIp.equals(localIp) && !priorityIPs.contains(testIp)) {
                priorityIPs.add(testIp);
            }
        }

        Log.d(TAG, "Scanning " + priorityIPs.size() + " IPs for BShare servers");

        // Use smaller batches for better performance
        final int batchSize = 5;
        CountDownLatch latch = new CountDownLatch(priorityIPs.size());

        for (String ip : priorityIPs) {
            mExecutor.execute(() -> {
                try {
                    checkBShareServer(ip, DEFAULT_PORT);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to check server at " + ip, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for scans to complete (reduced timeout for efficiency)
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                Log.w(TAG, "Network scan timed out, some IPs may not have been checked");
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Discovery interrupted", e);
        }

        Log.d(TAG, "Network discovery scan completed");
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

                final String finalDeviceName = deviceName;
                final String finalIp = ip;

                // Server found!
                if (mCallback != null) {
                    mHandler.post(() -> mCallback.onDeviceFound(finalDeviceName, finalIp));
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
     * Check if current network is compatible with Windows LocalSend
     */
    public boolean checkNetworkCompatibilityForWindows() {
        return isNetworkCompatibleWithWindows();
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

    /**
     * HTTP fallback discovery for Windows compatibility (LocalSend protocol)
     * Tries to connect to common LocalSend ports on local network devices
     */
    private void discoverViaHttpFallback(String localIp) {
        mExecutor.execute(() -> {
            try {
                // Extract local network prefix (e.g., 192.168.1.)
                String[] ipParts = localIp.split("\\.");
                if (ipParts.length != 4) return;

                String networkPrefix = ipParts[0] + "." + ipParts[1] + "." + ipParts[2] + ".";

                Log.d(TAG, "Starting HTTP fallback discovery on network: " + networkPrefix + "x");

                // Try common LocalSend ports: 53317 (primary), and some fallbacks
                int[] portsToTry = {53317, 53318, 53319};

                // Scan a reasonable range (e.g., .1 to .254, but limit for performance)
                for (int i = 1; i <= 254; i++) {
                    if (!mIsRunning) break; // Stop if server stopped

                    String testIp = networkPrefix + i;

                    // Skip our own IP
                    if (testIp.equals(localIp)) continue;

                    for (int port : portsToTry) {
                        if (!mIsRunning) break;

                        try {
                            // Try to connect to LocalSend info endpoint
                            java.net.URL url = new java.net.URL("http://" + testIp + ":" + port + "/api/localsend/v2/info");
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(500); // 500ms timeout
                            conn.setReadTimeout(1000);
                            conn.setRequestMethod("GET");

                            int responseCode = conn.getResponseCode();
                            if (responseCode == 200) {
                                // Read response
                                java.io.BufferedReader reader = new java.io.BufferedReader(
                                    new java.io.InputStreamReader(conn.getInputStream()));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                reader.close();

                                String responseStr = response.toString();
                                Log.d(TAG, "HTTP fallback found device at " + testIp + ":" + port + " - " + responseStr);

                                // Parse LocalSend info response
                                String alias = extractJsonValue(responseStr, "alias");
                                String deviceModel = extractJsonValue(responseStr, "deviceModel");
                                String fingerprint = extractJsonValue(responseStr, "fingerprint");

                                if (alias != null && mCallback != null) {
                                    String deviceName = alias;
                                    if (deviceModel != null && !deviceModel.equals(alias)) {
                                        deviceName += " (" + deviceModel + ")";
                                    }

                                    final String discoveredDeviceName = deviceName;
                                    final String discoveredIp = testIp;
                                    mHandler.post(() -> mCallback.onDeviceFound(discoveredDeviceName, discoveredIp));
                                }
                            }

                            conn.disconnect();

                        } catch (java.io.IOException e) {
                            // Expected for most IPs - no device listening
                            continue;
                        }
                    }

                    // Small delay to avoid overwhelming network
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                Log.d(TAG, "HTTP fallback discovery completed");

            } catch (Exception e) {
                Log.e(TAG, "Error in HTTP fallback discovery", e);
            }
        });
    }

    /**
     * Start Nearby Share mDNS service advertisement
     */
    private void startNearbyShareAdvertisement() {
        if (mNsdManager == null) {
            Log.e(TAG, "NsdManager not available for Nearby Share advertisement");
            return;
        }

        // Create endpoint ID (4 random alphanumeric characters)
        String endpointId = generateEndpointId();

        // Create service name (10 bytes encoded in URL-safe base64)
        String serviceName = createNearbyShareServiceName(endpointId);

        // Create TXT record with endpoint info
        String endpointInfo = createEndpointInfo(endpointId);

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(NEARBY_SHARE_SERVICE_TYPE);
        serviceInfo.setPort(DEFAULT_PORT);
        serviceInfo.setAttribute("n", endpointInfo);

        mRegistrationListener = new RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Nearby Share mDNS registration failed: " + errorCode);
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Nearby Share mDNS unregistration failed: " + errorCode);
            }

            @Override
            public void onServiceRegistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Nearby Share mDNS service registered: " + serviceInfo.getServiceName());
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Nearby Share mDNS service unregistered: " + serviceInfo.getServiceName());
            }
        };

        try {
            mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
            Log.d(TAG, "Started Nearby Share mDNS advertisement: " + serviceName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Nearby Share mDNS advertisement", e);
        }
    }

    /**
     * Start Nearby Share mDNS service discovery
     */
    private void startNearbyShareDiscovery() {
        if (mNsdManager == null) {
            Log.e(TAG, "NsdManager not available for Nearby Share discovery");
            return;
        }

        mDiscoveryListener = new DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "Nearby Share mDNS discovery started for: " + serviceType);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(TAG, "Nearby Share mDNS discovery stopped for: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Nearby Share mDNS service found: " + serviceInfo.getServiceName());

                // Resolve the service to get IP address and port
                mResolveListener = new ResolveListener() {
                    @Override
                    public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                        Log.e(TAG, "Failed to resolve Nearby Share service: " + errorCode);
                    }

                    @Override
                    public void onServiceResolved(NsdServiceInfo serviceInfo) {
                        String serviceName = serviceInfo.getServiceName();
                        String ipAddress = serviceInfo.getHost().getHostAddress();
                        int port = serviceInfo.getPort();

                        Log.d(TAG, "Nearby Share service resolved: " + serviceName +
                              " at " + ipAddress + ":" + port);

                        // Extract device name from TXT record
                        String deviceName = extractDeviceNameFromTxtRecord(serviceInfo);

                        if (mCallback != null && deviceName != null) {
                            mHandler.post(() -> mCallback.onDeviceFound(deviceName, ipAddress));
                        }
                    }
                };

                mNsdManager.resolveService(serviceInfo, mResolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Nearby Share mDNS service lost: " + serviceInfo.getServiceName());
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Nearby Share mDNS discovery start failed: " + errorCode);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Nearby Share mDNS discovery stop failed: " + errorCode);
            }
        };

        try {
            mNsdManager.discoverServices(NEARBY_SHARE_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
            Log.d(TAG, "Started Nearby Share mDNS discovery");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start Nearby Share mDNS discovery", e);
        }
    }

    /**
     * Stop Nearby Share mDNS services
     */
    private void stopNearbyShareServices() {
        if (mNsdManager != null) {
            try {
                if (mRegistrationListener != null) {
                    mNsdManager.unregisterService(mRegistrationListener);
                    mRegistrationListener = null;
                }
                if (mDiscoveryListener != null) {
                    mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                    mDiscoveryListener = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping Nearby Share mDNS services", e);
            }
        }
    }

    /**
     * Generate a 4-character endpoint ID for Nearby Share
     */
    private String generateEndpointId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(4);
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * Create Nearby Share service name (10 bytes encoded in URL-safe base64)
     */
    private String createNearbyShareServiceName(String endpointId) {
        try {
            // PCP byte (0x23) + 4-byte endpoint ID + 3-byte service ID + 2 zero bytes
            byte[] data = new byte[10];
            data[0] = 0x23; // PCP
            System.arraycopy(endpointId.getBytes(StandardCharsets.UTF_8), 0, data, 1, 4);
            data[5] = (byte) 0xFC; // Service ID part 1
            data[6] = (byte) 0x9F; // Service ID part 2
            data[7] = (byte) 0x5E; // Service ID part 3
            // data[8] and data[9] are already 0

            // Encode in URL-safe base64
            return android.util.Base64.encodeToString(data, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e(TAG, "Error creating Nearby Share service name", e);
            return NEARBY_SHARE_SERVICE_NAME_PREFIX + endpointId;
        }
    }

    /**
     * Create endpoint info TXT record for Nearby Share
     */
    private String createEndpointInfo(String endpointId) {
        try {
            // Get device name
            String deviceName = android.os.Build.MODEL;
            byte[] nameBytes = deviceName.getBytes(StandardCharsets.UTF_8);

            // Create endpoint info data
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();

            // Bit field (1 byte): version=1, visibility=0 (visible), device_type=1 (phone)
            baos.write(0b00100001); // version=1, visible, phone

            // 16 bytes of random data (salt + encrypted metadata)
            byte[] randomData = new byte[16];
            new java.util.Random().nextBytes(randomData);
            baos.write(randomData);

            // Device name (length + UTF-8 bytes)
            baos.write(nameBytes.length);
            baos.write(nameBytes);

            // Encode in URL-safe base64
            byte[] endpointInfoBytes = baos.toByteArray();
            return android.util.Base64.encodeToString(endpointInfoBytes,
                android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);

        } catch (Exception e) {
            Log.e(TAG, "Error creating endpoint info", e);
            return "";
        }
    }

    /**
     * Extract device name from TXT record
     */
    private String extractDeviceNameFromTxtRecord(NsdServiceInfo serviceInfo) {
        try {
            byte[] endpointInfoBase64Bytes = serviceInfo.getAttributes().get("n");
            if (endpointInfoBase64Bytes == null) return null;

            String endpointInfoBase64 = new String(endpointInfoBase64Bytes, StandardCharsets.UTF_8);
            if (endpointInfoBase64.isEmpty()) return null;

            byte[] endpointInfoBytes = android.util.Base64.decode(endpointInfoBase64,
                android.util.Base64.URL_SAFE);

            // Skip bit field (1) + random data (16) to get to name
            if (endpointInfoBytes.length < 18) return null;

            int nameLength = endpointInfoBytes[17] & 0xFF;
            if (endpointInfoBytes.length < 18 + nameLength) return null;

            byte[] nameBytes = new byte[nameLength];
            System.arraycopy(endpointInfoBytes, 18, nameBytes, 0, nameLength);

            return new String(nameBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            Log.e(TAG, "Error extracting device name from TXT record", e);
            return null;
        }
    }

    /**
     * Generate a random fingerprint for device identification
     */
    private String generateFingerprint() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * Check if current network setup is compatible with Windows LocalSend
     */
    private boolean isNetworkCompatibleWithWindows() {
        try {
            String localIp = getLocalIpAddress();
            if (localIp == null) return false;

            // Check if we're on a private network (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
            if (localIp.startsWith("192.168.") || localIp.startsWith("10.")) {
                return true;
            }
            if (localIp.startsWith("172.")) {
                String[] parts = localIp.split("\\.");
                if (parts.length >= 2) {
                    int secondOctet = Integer.parseInt(parts[1]);
                    if (secondOctet >= 16 && secondOctet <= 31) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.w(TAG, "Could not determine network compatibility", e);
            return false;
        }
    }

    /**
     * Extract value from simple JSON string
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting JSON value for key: " + key, e);
        }
        return null;
    }

    /**
     * Send HTTP register response to discovered device (LocalSend protocol)
     */
    private void sendHttpRegisterResponse(String ipAddress, int port, String alias, String deviceModel, String fingerprint, String protocol) {
        mExecutor.execute(() -> {
            try {
                java.net.URL url = new java.net.URL(protocol + "://" + ipAddress + ":" + port + "/api/localsend/v2/register");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String registerJson = "{\"alias\":\"" + android.os.Build.MODEL +
                    "\",\"version\":\"2.0\",\"deviceModel\":\"" + android.os.Build.MODEL +
                    "\",\"deviceType\":\"mobile\",\"fingerprint\":\"" + generateFingerprint() +
                    "\",\"port\":" + DEFAULT_PORT + ",\"protocol\":\"http\",\"download\":true}";

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(registerJson.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "HTTP register response to " + ipAddress + ":" + port + " - Code: " + responseCode);

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Error sending HTTP register response", e);
            }
        });
    }
}


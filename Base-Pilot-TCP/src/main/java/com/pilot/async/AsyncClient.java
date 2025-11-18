package com.pilot.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncClient {

    private final Set<AsynchronousSocketChannel> connections =
            Collections.synchronizedSet(new HashSet<>());
    private final AsynchronousChannelGroup group;

    public AsyncClient() throws IOException {
        this.group = AsynchronousChannelGroup.withThreadPool(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        );
    }

    public void connectWithRetry(InetSocketAddress server, int maxRetries) {
        if (maxRetries <= 0) return;

        try {
            AsynchronousSocketChannel client = AsynchronousSocketChannel.open(group);

            client.connect(server, client, new CompletionHandler<>() {
                @Override
                public void completed(Void result, AsynchronousSocketChannel ch) {
                    connections.add(ch);
                    System.out.println("Connected to server: " + server);
                    startReading(ch);

                    // optional direkt Nachricht senden
                    //sendMessage(ch, "Hello Server!");
                }

                @Override
                public void failed(Throwable exc, AsynchronousSocketChannel ch) {
                    closeQuietly(ch);
                    System.out.println("Connect failed, retrying in 1s...");
                    Executors.newSingleThreadScheduledExecutor().schedule(
                            () -> connectWithRetry(server, maxRetries - 1),
                            1, TimeUnit.SECONDS
                    );
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startReading(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        client.read(buffer, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                if (bytesRead == -1) {
                    closeQuietly(client);
                    return;
                }
                buf.flip();
                byte[] data = new byte[bytesRead];
                buf.get(data);
                String message = new String(data);

                // Nachrichten intern verarbeiten
                handleMessage(client, message);

                buf.clear();
                client.read(buf, buf, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                exc.printStackTrace();
                closeQuietly(client);
            }
        });
    }

    private void handleMessage(AsynchronousSocketChannel client, String message) {
        System.out.println("Received from server: " + message);
        // hier kann man intern weitere Logik einbauen

    }

    public void sendMessage(AsynchronousSocketChannel client, String msg) {
        byte[] data = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(data);

        client.write(buffer, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer buf) {
                if (buf.hasRemaining()) {
                    client.write(buf, buf, this);
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                exc.printStackTrace();
                closeQuietly(client);
            }
        });
    }

    public void broadcast(String msg) {
        synchronized (connections) {
            for (AsynchronousSocketChannel ch : connections) {
                sendMessage(ch, msg);
            }
        }
    }

    private void closeQuietly(AsynchronousSocketChannel client) {
        connections.remove(client);
        try { client.close(); } catch (IOException ignored) {}
    }

    public void shutdown() throws IOException, InterruptedException {
        synchronized (connections) {
            for (AsynchronousSocketChannel ch : connections) closeQuietly(ch);
        }
        group.shutdown();
        group.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Client shutdown complete");
    }
}

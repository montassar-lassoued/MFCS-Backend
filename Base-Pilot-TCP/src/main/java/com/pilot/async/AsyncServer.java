package com.pilot.async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AsyncServer {

    private final AsynchronousServerSocketChannel serverChannel;
    private final AsynchronousChannelGroup group;
    private final Set<AsynchronousSocketChannel> clients =
            Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public AsyncServer(int port) throws IOException {
        this.group = AsynchronousChannelGroup.withThreadPool(
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)
        );
        this.serverChannel = AsynchronousServerSocketChannel.open(group)
                .bind(new InetSocketAddress(port));
    }

    public void start() {
        acceptNext();
    }

    private void acceptNext() {
        serverChannel.accept(null, new CompletionHandler<>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                clients.add(client);
                System.out.println("Client connected: " + client);
                startReading(client);
                acceptNext();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
                acceptNext();
            }
        });
    }

    private void startReading(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        client.read(buffer, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer bytesRead, ByteBuffer buf) {
                if (bytesRead == -1) {
                    closeClient(client);
                    return;
                }
                buf.flip();
                byte[] data = new byte[bytesRead];
                buf.get(data);
                String message = new String(data);

                // Nachrichtenverarbeitung intern
                handleMessage(client, message);

                buf.clear();
                client.read(buf, buf, this);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                exc.printStackTrace();
                closeClient(client);
            }
        });
    }

    private void handleMessage(AsynchronousSocketChannel client, String message) {
        System.out.println("Received from client '': " + message);

        // Beispiel: echo zurücksenden
        sendMessage(client, "Server echo: " + message);
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
                closeClient(client);
            }
        });
    }

    public void broadcast(String msg) {
        synchronized (clients) {
            for (AsynchronousSocketChannel ch : clients) {
                sendMessage(ch, msg);
            }
        }
    }

    private void closeClient(AsynchronousSocketChannel client) {
        clients.remove(client);
        try { client.close(); } catch (IOException ignored) {}
    }

    public void shutdown() throws IOException, InterruptedException {
        synchronized (clients) {
            for (AsynchronousSocketChannel ch : clients) closeClient(ch);
        }
        serverChannel.close();
        group.shutdown();
        group.awaitTermination(5, TimeUnit.SECONDS);
        executor.shutdownNow();
        System.out.println("Server shutdown complete");
    }
}
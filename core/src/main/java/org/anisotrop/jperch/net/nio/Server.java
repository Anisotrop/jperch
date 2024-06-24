package org.anisotrop.jperch.net.nio;

import org.anisotrop.jperch.net.nio.handler.DataHandler;
import org.anisotrop.jperch.net.nio.handler.ControlHandler;
import org.anisotrop.jperch.net.nio.handler.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * TODO:
 *   - Results calculation and reply
 */

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    static State state = State.INITIALIZING;
    static ByteBuffer buffer = ByteBuffer.allocate(256);


    @CommandLine.Command(name = "jperch-server", mixinStandardHelpOptions = true, version = "jperch-server 0.0.1",
            description = "Starts the server.")
    static class ServerCommand implements Callable<Void> {

        @CommandLine.Option(names = {"-p", "--port"}, description = "The port on which connections are accepted")
        private int port = 9901;

        @CommandLine.Option(names = {"-t", "--virtual-threads"}, description = "Set to true to use virtual threads")
        private Boolean useVirtualThreads = false;

        @Override
        public Void call() {
            LOGGER.info("Start of server on port {} with virtual threads {}.", port, useVirtualThreads ?"enabled":"disabled");
            try {
                Selector selector = Selector.open();
                ServerSocketChannel serverSocket = ServerSocketChannel.open();
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 9901);
                serverSocket.bind(addr);
                serverSocket.configureBlocking(false);
                int ops = serverSocket.validOps();
                serverSocket.register(selector, ops, null);

                while (true) {
                    ControlHandler controlHandler = null;

                    LOGGER.info("I'm a server and I'm waiting for new connection and buffer select...");
                    int readyChannels = selector.select();
                    if(readyChannels == 0) continue;
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        // Tests whether this key's channel is ready to accept a new socket connection
                        try {
                        if (key.isAcceptable()) {
                            IHandler handler = acceptConnection(serverSocket, selector);
                            key.attach(handler);
                        } else if (key.isReadable() && key.isValid()) {
                            readData(key);
                        } else if (!key.isValid()) {
                            SocketChannel client = (SocketChannel) key.channel();
                            client.close();
                            key.cancel();
                        }
                        } finally {
                            iterator.remove();
                        }
                    }
                }

//                ServerController controller = new ServerController();
//                controller.runTestLoop(useVirtualThreads, serverSocket);
//                return null;
            } catch (IOException e) {
                LOGGER.error("Exception caught when trying to listen on port {} or listening for a connection", port, e);
                throw new IllegalStateException();
            }
        }


        private static IHandler acceptConnection(ServerSocketChannel serverSocket, Selector selector) throws IOException {
            IHandler handler;
            SocketChannel client = serverSocket.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            LOGGER.info("Connection Accepted: " + client.getLocalAddress() + "\n");

            if (state == State.INITIALIZING) {
                handler = new ControlHandler(client);
                state = State.RUNNING_TEST;
            } else if (state == State.RUNNING_TEST) {
                handler = new DataHandler(client, new AtomicBoolean(false));
            } else {
                throw new IllegalStateException("Unexpected connection in state: " + state);
            }

            return handler;
        }
    }


    private static void readData(SelectionKey myKey) throws IOException {
        SocketChannel client = (SocketChannel) myKey.channel();
        IHandler handler = (IHandler) myKey.attachment();

        // ByteBuffer: A byte buffer.
        // This class defines six categories of operations upon byte buffers:
        // Absolute and relative get and put methods that read and write single bytes;
        // Absolute and relative bulk get methods that transfer contiguous sequences of bytes from this buffer into an array;

        int bytes = client.read(buffer);
        if (bytes == -1) {
            client.close();
            LOGGER.info("\nIt's time to close connection as we got -1 read bytes");
        } else {
            // TODO correct buffer re-use
            buffer.flip();
            if(buffer.remaining() == 0){
                buffer.clear();
                return;
            }
            String result = new String(buffer.array()).trim();
            LOGGER.info("Message received: " + result);
        }
    }

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new ServerCommand()).execute(args);
        System.exit(exitCode);
    }
}

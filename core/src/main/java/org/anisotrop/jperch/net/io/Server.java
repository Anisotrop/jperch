package org.anisotrop.jperch.net.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;


/**
 * TODO:
 *   - Results calculation and reply
 */

public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

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
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                ServerController controller = new ServerController();
                controller.runTestLoop(useVirtualThreads, serverSocket);
                return null;
            } catch (IOException e) {
                LOGGER.error("Exception caught when trying to listen on port {} or listening for a connection", port, e);
                throw new IllegalStateException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int exitCode = new CommandLine(new ServerCommand()).execute(args);
        System.exit(exitCode);
    }
}

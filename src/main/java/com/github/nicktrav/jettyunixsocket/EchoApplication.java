package com.github.nicktrav.jettyunixsocket;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.unixsocket.UnixSocketConnector;

public class EchoApplication {

  private static final String SOCKET_PATH = "/tmp/jetty.sock";

  public static void main (String... args) throws Exception {
    Server server = new Server();

    File file = new File(SOCKET_PATH);
    if (file.exists()) {
      file.delete();
    }
    file.deleteOnExit();

    UnixSocketConnector connector = new UnixSocketConnector(server);
    connector.setUnixSocket(file.getAbsolutePath());

    if (args.length > 0) {
      int queueSize = Integer.valueOf(args[0]);
      System.err.println("overriding accept queue size: " + queueSize);
      connector.setAcceptQueueSize(queueSize);
    }

    server.addConnector(connector);
    server.setHandler(new EchoHandler());
    server.start();
    server.join();
  }

  public static class EchoHandler extends AbstractHandler {

    @Override public void handle(String target, Request baseRequest, HttpServletRequest request,
        HttpServletResponse response) throws IOException {
      response.setStatus(200);
      ByteStreams.copy(request.getInputStream(), response.getOutputStream());
      baseRequest.setHandled(true);
    }
  }
}

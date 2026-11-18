package fr.clustering.manager.service;

import fr.clustering.manager.model.BackendServer;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@Service
public class HealthService {
    public boolean online(BackendServer server) {
        if (!server.enabled()) return false;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(server.ip(), server.port()), (int) TimeUnit.SECONDS.toMillis(2));
            return true;
        } catch (Exception ignored) { return false; }
    }
}

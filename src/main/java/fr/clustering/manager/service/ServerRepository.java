package fr.clustering.manager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.clustering.manager.model.BackendServer;
import fr.clustering.manager.model.ServerStore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class ServerRepository {
    private final ObjectMapper mapper;
    private final Path file;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private List<BackendServer> servers = new ArrayList<>();

    public ServerRepository(ObjectMapper mapper, @Value("${manager.servers-file:./data/servers.json}") String file) {
        this.mapper = mapper;
        this.file = Path.of(file).toAbsolutePath().normalize();
    }

    @PostConstruct
    void load() throws IOException {
        lock.writeLock().lock();
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                ServerStore store = mapper.readValue(file.toFile(), ServerStore.class);
                servers = store.servers() == null ? new ArrayList<>() : new ArrayList<>(store.servers());
            } else {
                servers = new ArrayList<>(List.of(
                        new BackendServer(1, "Serveur 1", "127.0.0.1", 8081, true),
                        new BackendServer(2, "Serveur 2", "127.0.0.1", 8082, true)));
                save();
            }
        } finally { lock.writeLock().unlock(); }
    }

    public List<BackendServer> findAll() {
        lock.readLock().lock();
        try { return List.copyOf(servers); }
        finally { lock.readLock().unlock(); }
    }

    public BackendServer find(long id) {
        return findAll().stream().filter(s -> s.id() == id).findFirst().orElse(null);
    }

    public BackendServer create(BackendServer server) throws IOException {
        lock.writeLock().lock();
        try {
            long id = servers.stream().mapToLong(BackendServer::id).max().orElse(0) + 1;
            BackendServer created = server.withId(id);
            servers.add(created); save(); return created;
        } finally { lock.writeLock().unlock(); }
    }

    public BackendServer update(long id, BackendServer value) throws IOException {
        lock.writeLock().lock();
        try {
            int index = indexOf(id);
            if (index < 0) return null;
            BackendServer updated = value.withId(id); servers.set(index, updated); save(); return updated;
        } finally { lock.writeLock().unlock(); }
    }

    public BackendServer toggle(long id, boolean enabled) throws IOException {
        lock.writeLock().lock();
        try {
            int index = indexOf(id);
            if (index < 0) return null;
            BackendServer updated = servers.get(index).withEnabled(enabled); servers.set(index, updated); save(); return updated;
        } finally { lock.writeLock().unlock(); }
    }

    public boolean delete(long id) throws IOException {
        lock.writeLock().lock();
        try { boolean removed = servers.removeIf(s -> s.id() == id); if (removed) save(); return removed; }
        finally { lock.writeLock().unlock(); }
    }

    private int indexOf(long id) {
        for (int i = 0; i < servers.size(); i++) if (servers.get(i).id() == id) return i;
        return -1;
    }

    private void save() throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), new ServerStore(servers));
        try { Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (IOException unsupported) { Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING); }
    }
}

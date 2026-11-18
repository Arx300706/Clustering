package fr.clustering.manager.web;

import fr.clustering.manager.model.BackendServer;
import fr.clustering.manager.service.HaproxyService;
import fr.clustering.manager.service.HealthService;
import fr.clustering.manager.service.ServerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ServerRepository repository; private final HealthService health; private final HaproxyService haproxy;
    public ApiController(ServerRepository repository, HealthService health, HaproxyService haproxy) {
        this.repository = repository; this.health = health; this.haproxy = haproxy;
    }
    @GetMapping("/servers") public List<Map<String, Object>> servers() {
        return repository.findAll().stream().map(s -> Map.<String, Object>of("id", s.id(), "name", s.name(), "ip", s.ip(), "port", s.port(), "enabled", s.enabled(), "status", health.online(s) ? "ONLINE" : "OFFLINE")).toList();
    }
    @PostMapping("/servers") @ResponseStatus(HttpStatus.CREATED)
    public BackendServer create(@Valid @RequestBody BackendServer server) throws IOException { return repository.create(server); }
    @PutMapping("/servers/{id}") public BackendServer update(@PathVariable long id, @Valid @RequestBody BackendServer server) throws IOException {
        BackendServer result = repository.update(id, server); if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return result;
    }
    @PatchMapping("/servers/{id}/enabled") public BackendServer enabled(@PathVariable long id, @RequestBody Map<String, Boolean> body) throws IOException {
        BackendServer result = repository.toggle(id, Boolean.TRUE.equals(body.get("enabled"))); if (result == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND); return result;
    }
    @DeleteMapping("/servers/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) throws IOException { if (!repository.delete(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND); }
    @GetMapping("/dashboard") public Map<String, Object> dashboard() {
        List<Map<String, Object>> list = servers(); long active = list.stream().filter(s -> "ONLINE".equals(s.get("status"))).count();
        return Map.of("haproxy", "NON_VERIFIE", "total", list.size(), "active", active, "unavailable", list.size() - active, "servers", list, "updatedAt", Instant.now().toString());
    }
    @PostMapping("/haproxy/generate") public Map<String, Object> generate(@RequestBody(required = false) Map<String, Boolean> request) throws IOException, InterruptedException {
        return haproxy.generate(repository.findAll(), request != null && Boolean.TRUE.equals(request.get("apply")));
    }
}

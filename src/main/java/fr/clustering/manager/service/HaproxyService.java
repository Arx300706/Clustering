package fr.clustering.manager.service;

import fr.clustering.manager.model.BackendServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class HaproxyService {
    private static final Pattern SAFE = Pattern.compile("[A-Za-z0-9_.-]+");
    private final Path output;
    private final String bind;
    private final String algorithm;
    private final String validateCommand;
    private final String reloadCommand;

    public HaproxyService(@Value("${manager.haproxy-file:./haproxy/generated.cfg}") String output,
                          @Value("${manager.haproxy-bind:*:8080}") String bind,
                          @Value("${manager.haproxy-algorithm:roundrobin}") String algorithm,
                          @Value("${manager.haproxy-validate-command:}") String validateCommand,
                          @Value("${manager.haproxy-reload-command:}") String reloadCommand) {
        this.output = Path.of(output).toAbsolutePath().normalize();
        if (!bind.matches("[A-Za-z0-9.*:_-]+")) throw new IllegalArgumentException("Adresse frontend invalide");
        if (!List.of("roundrobin", "leastconn", "source").contains(algorithm)) throw new IllegalArgumentException("Algorithme HAProxy invalide");
        this.bind = bind; this.algorithm = algorithm;
        this.validateCommand = validateCommand; this.reloadCommand = reloadCommand;
    }

    public Map<String, Object> generate(List<BackendServer> servers, boolean apply) throws IOException, InterruptedException {
        String config = render(servers);
        Files.createDirectories(output.getParent());
        Path temp = output.resolveSibling(output.getFileName() + ".tmp");
        Files.writeString(temp, config);
        if (!validateCommand.isBlank()) {
            Result check = run(validateCommand, temp);
            if (check.code != 0) { Files.deleteIfExists(temp); return Map.of("valid", false, "applied", false, "output", check.output); }
        }
        Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING);
        boolean applied = false;
        String message = "Configuration générée";
        if (apply) {
            if (validateCommand.isBlank() || reloadCommand.isBlank()) {
                message = "Configuration générée. Définissez les commandes de validation et de rechargement pour appliquer.";
            } else {
                Result reload = run(reloadCommand, output);
                applied = reload.code == 0; message = reload.output;
            }
        }
        return Map.of("valid", true, "applied", applied, "path", output.toString(), "message", message);
    }

    private String render(List<BackendServer> servers) {
        StringBuilder cfg = new StringBuilder("global\n    log stdout format raw local0\n    maxconn 1024\n\ndefaults\n    log global\n    mode http\n    option httplog\n    timeout connect 5s\n    timeout client 30s\n    timeout server 30s\n\nfrontend http_in\n    bind ").append(bind).append("\n    default_backend web_servers\n\nbackend web_servers\n    balance ").append(algorithm).append("\n    option httpchk GET /\n    http-check expect status 200\n");
        int n = 1;
        for (BackendServer server : servers) if (server.enabled()) {
            String name = SAFE.matcher(server.name().replace(' ', '_')).replaceAll("");
            if (name.isBlank()) name = "server" + n;
            cfg.append("    server ").append(name).append(' ').append(server.ip()).append(':').append(server.port()).append(" check inter 5s fall 2 rise 2\n"); n++;
        }
        return cfg.toString();
    }

    private Result run(String command, Path config) throws IOException, InterruptedException {
        // Commands are admin-controlled deployment settings; {config} is passed as one argument.
        List<String> args = new java.util.ArrayList<>(List.of(command.split("\\s+")));
        args.replaceAll(arg -> arg.replace("{config}", config.toString()));
        Process process = new ProcessBuilder(args).redirectErrorStream(true).start();
        boolean done = process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS);
        if (!done) { process.destroyForcibly(); return new Result(124, "Commande expirée"); }
        return new Result(process.exitValue(), new String(process.getInputStream().readAllBytes()));
    }
    private record Result(int code, String output) { }
}

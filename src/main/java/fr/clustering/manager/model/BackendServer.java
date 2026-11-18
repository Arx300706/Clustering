package fr.clustering.manager.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record BackendServer(
        long id,
        @NotBlank(message = "Le nom est obligatoire") String name,
        @NotBlank(message = "L’adresse IP ou le nom DNS est obligatoire")
        @Pattern(regexp = "[A-Za-z0-9.-]+", message = "Adresse invalide") String ip,
        @Min(value = 1, message = "Le port doit être entre 1 et 65535")
        @Max(value = 65535, message = "Le port doit être entre 1 et 65535") int port,
        boolean enabled) {
    public BackendServer withId(long value) { return new BackendServer(value, name, ip, port, enabled); }
    public BackendServer withEnabled(boolean value) { return new BackendServer(id, name, ip, port, value); }
}

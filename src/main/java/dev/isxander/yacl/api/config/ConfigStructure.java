package dev.isxander.yacl.api.config;

import java.util.Collection;

public record ConfigStructure(Collection<FilledConfigEntry> entries) {
    public FilledConfigEntry findEntry(String name) {
        return entries.stream()
                .filter(entry -> entry.field().name().equals(name))
                .findAny()
                .orElse(null);
    }
}

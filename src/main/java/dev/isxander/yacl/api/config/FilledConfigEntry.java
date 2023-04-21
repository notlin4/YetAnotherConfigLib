package dev.isxander.yacl.api.config;

import dev.isxander.yacl.api.config.FieldAccess;
import dev.isxander.yacl.config.ConfigEntry;

public record FilledConfigEntry(ConfigEntry entry, FieldAccess field) {
    public String name() {
        return entry.name().isEmpty() ? field.name() : entry.name();
    }
}

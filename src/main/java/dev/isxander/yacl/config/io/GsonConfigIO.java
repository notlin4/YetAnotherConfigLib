package dev.isxander.yacl.config.io;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.isxander.yacl.api.config.ConfigInput;
import dev.isxander.yacl.api.config.ConfigOutput;
import dev.isxander.yacl.api.config.ConfigStructure;
import dev.isxander.yacl.impl.utils.YACLConstants;

import java.nio.file.Files;
import java.nio.file.Path;

public class GsonConfigIO implements ConfigInput, ConfigOutput {
    private final Gson gson;
    private final Path path;

    public GsonConfigIO(Gson gson, Path path) {
        this.gson = gson;
        this.path = path;
    }

    @Override
    public boolean load(ConfigStructure structure, ConfigStructure defaultStructure) throws Exception {
        if (Files.notExists(path)) {
            return false;
        }

        boolean dirty = false;

        JsonObject object = gson.fromJson(Files.newBufferedReader(path), JsonObject.class);
        if (object == null) {
            object = new JsonObject();
        }

        for (var entry : structure.entries()) {
            String name = entry.entry().name();
            if (name.isEmpty()) name = entry.field().name();

            if (object.has(name)) {
                entry.field().set(gson.fromJson(object.get(name), entry.field().type()));
            } else {
                entry.field().set(defaultStructure.findEntry(entry.field().name()).field().get());
                YACLConstants.LOGGER.warn("Missing config entry: " + name);
                dirty = true;
            }
        }

        return !dirty;
    }

    @Override
    public void save(ConfigStructure structure) {
        JsonObject object = new JsonObject();

        for (var entry : structure.entries()) {
            String name = entry.entry().name();
            if (name.isEmpty()) name = entry.field().name();

            object.add(name, gson.toJsonTree(entry.field().get()));
        }

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, gson.toJson(object));
        } catch (Exception e) {
            YACLConstants.LOGGER.error("Failed to save config!", e);
        }
    }
}

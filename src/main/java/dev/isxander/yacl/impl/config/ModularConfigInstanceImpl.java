package dev.isxander.yacl.impl.config;

import dev.isxander.yacl.api.config.*;
import dev.isxander.yacl.config.ConfigEntry;
import dev.isxander.yacl.config.ConfigInstance;
import dev.isxander.yacl.impl.utils.YACLConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class ModularConfigInstanceImpl<T> extends ConfigInstance<T> {
    private final ConfigInput configInput;
    private final ConfigOutput configOutput;

    private final ConfigStructure configStructure, defaultStructure;

    ModularConfigInstanceImpl(Class<T> configClass, ConfigInput configInput, ConfigOutput configOutput) {
        super(configClass);
        this.configInput = configInput;
        this.configOutput = configOutput;
        this.configStructure = createConfigStructure(getConfig());
        this.defaultStructure = createConfigStructure(getDefaults());
    }

    private ConfigStructure createConfigStructure(T config) {
        Field[] fields = getConfigClass().getDeclaredFields();
        List<FilledConfigEntry> entries = Arrays.stream(fields)
                .peek(field -> field.setAccessible(true))
                .filter(field -> field.isAnnotationPresent(ConfigEntry.class))
                .map(field -> new FilledConfigEntry(field.getAnnotation(ConfigEntry.class), new FieldAccessImpl(field, config)))
                .toList();

        return new ConfigStructure(entries);
    }

    @Override
    public void save() {
        YACLConstants.LOGGER.info("Saving config {}...", getConfigClass().getSimpleName());
        configOutput.save(configStructure);
    }

    @Override
    public void load() {
        try {
            YACLConstants.LOGGER.info("Loading config {}...", getConfigClass().getSimpleName());
            if (!configInput.load(configStructure, defaultStructure)) {
                YACLConstants.LOGGER.info("Config {} dirty, saving...", getConfigClass().getSimpleName());
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private record FieldAccessImpl(Field field, Object object) implements FieldAccess {
        @Override
        public Object get() {
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Object value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        public Type type() {
            return field.getType();
        }
    }
}

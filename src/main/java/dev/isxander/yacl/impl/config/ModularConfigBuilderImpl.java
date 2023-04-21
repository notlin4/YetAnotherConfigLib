package dev.isxander.yacl.impl.config;

import dev.isxander.yacl.api.config.ConfigInput;
import dev.isxander.yacl.api.config.ModularConfigBuilder;
import dev.isxander.yacl.api.config.ConfigOutput;
import dev.isxander.yacl.config.ConfigInstance;

public class ModularConfigBuilderImpl<T> implements ModularConfigBuilder<T> {
    private final Class<T> type;
    private ConfigOutput configOutput;
    private ConfigInput configInput;

    public ModularConfigBuilderImpl(Class<T> type) {
        this.type = type;
    }

    @Override
    public ModularConfigBuilder<T> out(ConfigOutput configOutput) {
        this.configOutput = configOutput;
        return this;
    }

    @Override
    public ModularConfigBuilder<T> in(ConfigInput configInput) {
        this.configInput = configInput;
        return this;
    }

    @Override
    public <U extends ConfigOutput & ConfigInput> ModularConfigBuilder<T> io(U io) {
        this.configInput = io;
        this.configOutput = io;
        return this;
    }

    @Override
    public ConfigInstance<T> build() {
        return new ModularConfigInstanceImpl<>(type, configInput, configOutput);
    }
}

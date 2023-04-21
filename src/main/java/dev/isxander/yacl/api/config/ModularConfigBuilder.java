package dev.isxander.yacl.api.config;

import dev.isxander.yacl.config.ConfigInstance;
import dev.isxander.yacl.impl.config.ModularConfigBuilderImpl;

public interface ModularConfigBuilder<T> {
    ModularConfigBuilder<T> out(ConfigOutput configOutput);

    ModularConfigBuilder<T> in(ConfigInput configInput);

    <U extends ConfigOutput & ConfigInput> ModularConfigBuilder<T> io(U io);

    ConfigInstance<T> build();

    static <T> ModularConfigBuilder<T> create(Class<T> type) {
        return new ModularConfigBuilderImpl<>(type);
    }
}

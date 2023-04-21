package dev.isxander.yacl.api.config;

public interface ConfigInput {
    boolean load(ConfigStructure structure, ConfigStructure defaultStructure) throws Exception;
}

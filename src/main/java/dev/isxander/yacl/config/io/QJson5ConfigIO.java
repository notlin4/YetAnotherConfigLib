package dev.isxander.yacl.config.io;

import dev.isxander.yacl.api.config.*;
import dev.isxander.yacl.impl.utils.YACLConstants;
import org.quiltmc.json5.JsonReader;
import org.quiltmc.json5.JsonWriter;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

import static java.util.Map.entry;

public class QJson5ConfigIO implements ConfigInput, ConfigOutput, QJson5ConfigIO.SerializationContext {
    private final boolean strictJson;
    private final Path path;
    private final Map<Type, JsonSerializer> serializers;

    public QJson5ConfigIO(boolean strictJson, Path path) {
        this.strictJson = strictJson;
        this.path = path;
        this.serializers = Map.ofEntries(
                entry(Boolean.TYPE, JsonSerializer.of(JsonReader::nextBoolean, JsonWriter::value)),
                entry(Boolean.class, JsonSerializer.of(JsonReader::nextBoolean, JsonWriter::value)),
                entry(Integer.TYPE, JsonSerializer.of(JsonReader::nextInt, JsonWriter::value)),
                entry(Integer.class, JsonSerializer.of(JsonReader::nextInt, JsonWriter::value)),
                entry(Long.TYPE, JsonSerializer.of(JsonReader::nextLong, JsonWriter::value)),
                entry(Long.class, JsonSerializer.of(JsonReader::nextLong, JsonWriter::value)),
                entry(Float.TYPE, JsonSerializer.of(reader -> (float) reader.nextDouble(), JsonWriter::value)),
                entry(Float.class, JsonSerializer.of(reader -> (float) reader.nextDouble(), JsonWriter::value)),
                entry(Double.TYPE, JsonSerializer.of(JsonReader::nextDouble, JsonWriter::value)),
                entry(Double.class, JsonSerializer.of(JsonReader::nextDouble, JsonWriter::value)),
                entry(String.class, JsonSerializer.of(JsonReader::nextString, JsonWriter::value)),
                entry(List.class, JsonSerializer.of(
                        (reader, field, context) -> {
                            var list = new ArrayList<>();
                            Type type = field.genericType().getActualTypeArguments()[0];

                            reader.beginArray();
                            while (reader.hasNext()) {
                                list.add(context.readField(reader, type));
                            }
                            reader.endArray();

                            return list;
                        },
                        (writer, value, field, context) -> {
                            Type type = field.genericType().getActualTypeArguments()[0];

                            writer.beginArray();
                            for (Object o : value) {
                                context.writeField(o, writer, type);
                            }
                            writer.endArray();
                        }
                ))
        );
    }

    @Override
    public boolean load(ConfigStructure structure, ConfigStructure defaultStructure) throws Exception {
        boolean dirty = false;

        try (JsonReader reader = strictJson ? JsonReader.json(path) : JsonReader.json5(path)) {
            List<FilledConfigEntry> entries = new ArrayList<>(structure.entries());

            reader.beginObject();
            while (reader.hasNext()) {
                String fieldName = reader.nextName();

                FilledConfigEntry configEntry = entries.stream()
                        .filter(entry -> entry.name().equals(fieldName))
                        .findAny()
                        .orElse(null);
                if (configEntry == null) {
                    reader.skipValue();
                    continue;
                }
                entries.remove(configEntry);

                configEntry.field().set(readField(reader, configEntry.field()));
            }
            reader.endObject();
        }

        return !dirty;
    }

    @Override
    public void save(ConfigStructure structure) throws Exception {
        try (JsonWriter writer = strictJson ? JsonWriter.json(path) : JsonWriter.json5(path)) {
            writer.beginObject();
            for (FilledConfigEntry entry : structure.entries()) {
                writer.name(entry.name());
                writeField(entry.field().get(), writer, entry.field());
            }
            writer.endObject();
        }
    }

    @Override
    public void writeField(Object value, JsonWriter writer, FieldAccess field) throws Exception {
        if (!serializers.containsKey(field.type())) {
            YACLConstants.LOGGER.warn("Missing serializer for type: " + field.type());
            return;
        }

        JsonSerializer serializer = serializers.get(field.type());
        serializer.write(writer, value, field, this);
    }

    @Override
    public Object readField(JsonReader reader, FieldAccess field) throws Exception {
        if (!serializers.containsKey(field.type())) {
            YACLConstants.LOGGER.warn("Missing serializer for type: " + field.type());
            reader.skipValue();
            return null;
        }

        JsonSerializer serializer = serializers.get(field.type());
        return serializer.read(reader, field, this);
    }

    private interface JsonSerializer {
        Object read(JsonReader reader, FieldAccess field, SerializationContext context);

        void write(JsonWriter writer, Object value, FieldAccess field, SerializationContext context);

        static <T> JsonSerializer of(ReaderFunction<T> readerF, WriterFunction<T> writerF) {
            return of((reader, field, context) -> readerF.read(reader), (writer, value, field, context) -> writerF.write(writer, value));
        }

        static <T> JsonSerializer of(ReaderFunctionCtx<T> readerF, WriterFunctionCtx<T> writerF) {
            return new JsonSerializer() {
                @Override
                public Object read(JsonReader reader, FieldAccess field, SerializationContext context) {
                    try {
                        return readerF.read(reader, field, context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void write(JsonWriter writer, Object value, FieldAccess field, SerializationContext context) {
                    try {
                        writerF.write(writer, (T) value, field, context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }

        interface ReaderFunction<T> {
            T read(JsonReader reader) throws Exception;
        }

        interface WriterFunction<T> {
            void write(JsonWriter writer, T value) throws Exception;
        }

        interface ReaderFunctionCtx<T> {
            T read(JsonReader reader, FieldAccess field, SerializationContext context) throws Exception;
        }

        interface WriterFunctionCtx<T> {
            void write(JsonWriter writer, T value, FieldAccess field, SerializationContext context) throws Exception;
        }
    }

    interface SerializationContext {
        void writeField(Object value, JsonWriter writer, FieldAccess field) throws Exception;

        Object readField(JsonReader reader, FieldAccess field) throws Exception;
    }
}

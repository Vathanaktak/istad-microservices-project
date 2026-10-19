package kh.edu.istad.platform.customer.persistence.convert;

import org.bson.types.Binary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

@ReadingConverter
public class BinaryToStringConverter implements Converter<Binary, String> {
    @Override
    public String convert(Binary source) {
        if (source == null) {
            return null;
        }
        if (source.getData().length == 16) {
            ByteBuffer buffer = ByteBuffer.wrap(source.getData());
            return new UUID(buffer.getLong(), buffer.getLong()).toString();
        }
        return source.toString();
    }
}

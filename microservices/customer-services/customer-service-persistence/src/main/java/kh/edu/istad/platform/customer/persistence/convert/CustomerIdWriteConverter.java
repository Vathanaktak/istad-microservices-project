package kh.edu.istad.platform.customer.persistence.convert;

import kh.edu.istad.common.valueobject.CustomerId;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class CustomerIdWriteConverter implements Converter<CustomerId, Document> {
    @Override
    public Document convert(CustomerId source) {

        return new Document("_id", source.value().toString());
    }
}

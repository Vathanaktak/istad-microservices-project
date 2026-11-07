package kh.edu.istad.platform.iam.application.result;
import kh.edu.istad.platform.iam.application.command.*; import kh.edu.istad.platform.iam.application.result.*; import kh.edu.istad.platform.iam.application.query.*; import kh.edu.istad.platform.iam.application.port.out.*; import kh.edu.istad.platform.iam.application.support.*;
import kh.edu.istad.platform.iam.domain.model.*; import kh.edu.istad.platform.iam.domain.exception.*;
import java.time.*; import java.util.*;
public record PageResult<T>(List<T> content, int number, int size, long totalElements, int totalPages) {
    public <R> PageResult<R> map(java.util.function.Function<T, R> f) {
        return new PageResult<>(content.stream().map(f).toList(), number, size, totalElements, totalPages);
    }
}

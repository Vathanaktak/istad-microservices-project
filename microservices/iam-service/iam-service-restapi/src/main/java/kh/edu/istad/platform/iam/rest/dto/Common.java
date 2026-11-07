package kh.edu.istad.platform.iam.rest.dto;

import kh.edu.istad.platform.iam.application.result.PageResult;
import java.util.List;

public final class Common {
    private Common() {}
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";
    public static final String PASSWORD_MSG = "must be 8-72 chars with upper, lower, digit and symbol";

    public record PageInfo(int number, int size, long totalElements, int totalPages) {}
    public record PageResponse<T>(List<T> content, PageInfo page) {
        public static <T> PageResponse<T> of(PageResult<T> p) {
            return new PageResponse<>(p.content(), new PageInfo(p.number(), p.size(), p.totalElements(), p.totalPages()));
        }
    }
}

package kh.edu.istad.platform.iam.domain.model;

import java.util.Locale;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Permission extends Protectable {
    private String name;      // resource:action
    private String resource;
    private String action;

    public static String nameOf(
            String resource, String action) {
        return resource.trim().
                toLowerCase(Locale.ROOT) + ":" +
                action.trim().toLowerCase(Locale.ROOT);
    }
}

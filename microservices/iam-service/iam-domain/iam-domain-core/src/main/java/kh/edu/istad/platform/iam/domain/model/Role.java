package kh.edu.istad.platform.iam.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Role extends Protectable {
    private String name;
    public String authority() { return "ROLE_" + name; }
}

package io.github.erfangc.iam.mocks.roles;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.erfangc.iam.authz.models.Role;

import java.io.IOException;

public class RoleProvider {
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static Role forId(String id) {
        try {
            return yamlMapper.readValue(RoleProvider.class.getClassLoader().getResourceAsStream("roles" + id + ".yaml"), Role.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

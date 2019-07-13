package io.github.erfangc.iam.mocks.roles;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.erfangc.iam.authz.models.RoleBinding;

import java.io.IOException;

public class RoleBindingProvider {
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static RoleBinding forId(String id) {
        try {
            return yamlMapper.readValue(RoleBindingProvider.class.getClassLoader().getResourceAsStream("role-bindings/" + id + ".yaml"), RoleBinding.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

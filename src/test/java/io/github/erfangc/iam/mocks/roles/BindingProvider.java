package io.github.erfangc.iam.mocks.roles;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.erfangc.iam.authz.models.Binding;
import io.github.erfangc.iam.authz.models.Role;

import java.io.IOException;

public class BindingProvider {
    private static final YAMLMapper yamlMapper = new YAMLMapper();

    public static Binding forId(String id) {
        try {
            return yamlMapper.readValue(BindingProvider.class.getClassLoader().getResourceAsStream("bindings" + id + ".yaml"), Binding.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

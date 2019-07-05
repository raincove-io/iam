package io.github.erfangc.iam.authz.services;

public class Namespaces {

    public static String ROLE_NS = "iam:role";
    public static String BINDING_NS = "iam:bindings:";
    public static String SUB_ROLE_MAPPING_NS = "iam:bindings:subs:";
    public static String ROLE_BINDINGS_NS = "iam:bindings:roles:";

    public static String roleKey(String roleId) {
        return ROLE_NS + ":" + roleId;
    }

    public static String bindingKey(String bindingId) {
        return BINDING_NS + bindingId;
    }

    public static String subBindingsKey(String type, String sub) {
        return SUB_ROLE_MAPPING_NS + type + ":" + sub;
    }

    public static String roleBindingsKey(String roleId) {
        return ROLE_BINDINGS_NS + roleId;
    }
}

package org.jeecg.module.buildingControl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSML XML 节点封装
 * 对应 enteliWEB CSML 格式的 XML 结构
 */
public class CsmlNode {
    public enum Type {
        STRING, INTEGER, REAL, BOOLEAN, UNSIGNED, ENUMERATED,
        DATETIME, ARRAY, STRUCT, SEQUENCE, CHOICE, COLLECTION, LIST, UNKNOWN
    }

    private Type type;
    private String name;
    private String value;
    private Map<String, String> attributes;
    private List<CsmlNode> children;

    public CsmlNode() {
        this.attributes = new HashMap<>();
        this.children = new ArrayList<>();
        this.type = Type.UNKNOWN;
    }

    public CsmlNode(Type type, String name) {
        this();
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public List<CsmlNode> getChildren() {
        return children;
    }

    public void addChild(CsmlNode child) {
        this.children.add(child);
    }

    public CsmlNode getChild(String name) {
        for (CsmlNode child : children) {
            if (name.equals(child.getName())) {
                return child;
            }
        }
        return null;
    }

    public List<CsmlNode> getChildren(String name) {
        List<CsmlNode> result = new ArrayList<>();
        for (CsmlNode child : children) {
            if (name.equals(child.getName())) {
                result.add(child);
            }
        }
        return result;
    }

    public String asString() {
        return value != null ? value : "";
    }

    public int asInt() {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public double asDouble() {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public boolean asBoolean() {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return "1".equals(value) || "true".equalsIgnoreCase(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    private void toString(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append(type);
        if (name != null) {
            sb.append("[").append(name).append("]");
        }
        if (value != null) {
            sb.append("=").append(value);
        }
        sb.append("\n");
        for (CsmlNode child : children) {
            child.toString(sb, indent + 1);
        }
    }
}

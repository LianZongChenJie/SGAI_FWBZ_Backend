package org.jeecg.module.buildingControl.util;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * CSML XML 解析器
 * 解析 enteliWEB 返回的 CSML 格式 XML
 */
public class CsmlParser {
    private final DocumentBuilderFactory factory;

    public CsmlParser() {
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (Exception e) {
            // ignore
        }
    }

    public CsmlNode parse(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML content is empty");
        }
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));
        Element root = doc.getDocumentElement();
        return parseNode(root);
    }

    private CsmlNode parseNode(Node node) {
        CsmlNode csmlNode = new CsmlNode();

        String localName = getLocalName(node);
        CsmlNode.Type type = parseType(localName);
        csmlNode.setType(type);

        NamedNodeMap attrs = node.getAttributes();
        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                String attrName = getLocalName(attr);
                String attrValue = attr.getNodeValue();
                csmlNode.setAttribute(attrName, attrValue);

                if ("name".equals(attrName)) {
                    csmlNode.setName(attrValue);
                } else if ("value".equals(attrName) && csmlNode.getValue() == null) {
                    csmlNode.setValue(attrValue);
                }
            }
        }

        if (node.getTextContent() != null) {
            String text = node.getTextContent().trim();
            if (!text.isEmpty() && csmlNode.getValue() == null) {
                boolean hasElementChild = false;
                NodeList children = node.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        hasElementChild = true;
                        break;
                    }
                }
                if (!hasElementChild) {
                    csmlNode.setValue(text);
                }
            }
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String childLocalName = getLocalName(child);
                if (childLocalName != null && !childLocalName.isEmpty()) {
                    csmlNode.addChild(parseNode(child));
                }
            }
        }

        return csmlNode;
    }

    private CsmlNode.Type parseType(String localName) {
        if (localName == null) {
            return CsmlNode.Type.UNKNOWN;
        }
        switch (localName.toLowerCase()) {
            case "string": return CsmlNode.Type.STRING;
            case "integer": return CsmlNode.Type.INTEGER;
            case "real":
            case "float": return CsmlNode.Type.REAL;
            case "boolean": return CsmlNode.Type.BOOLEAN;
            case "unsigned": return CsmlNode.Type.UNSIGNED;
            case "enumerated":
            case "enum": return CsmlNode.Type.ENUMERATED;
            case "datetime":
            case "timestamp":
            case "date": return CsmlNode.Type.DATETIME;
            case "array": return CsmlNode.Type.ARRAY;
            case "struct":
            case "structure": return CsmlNode.Type.STRUCT;
            case "sequence": return CsmlNode.Type.SEQUENCE;
            case "choice": return CsmlNode.Type.CHOICE;
            case "collection": return CsmlNode.Type.COLLECTION;
            case "list": return CsmlNode.Type.LIST;
            default: return CsmlNode.Type.UNKNOWN;
        }
    }

    private String getLocalName(Node node) {
        String name = node.getLocalName();
        if (name == null || name.isEmpty()) {
            name = node.getNodeName();
            int colonIndex = name.indexOf(':');
            if (colonIndex > 0) {
                name = name.substring(colonIndex + 1);
            }
        }
        return name;
    }

    /**
     * 解析简单值，直接返回文本
     */
    public String parseSimpleValue(String xml) throws Exception {
        CsmlNode node = parse(xml);
        return node.asString();
    }

    public List<String> parseArray(String xml) throws Exception {
        List<String> result = new ArrayList<>();
        CsmlNode node = parse(xml);
        if (node.getType() == CsmlNode.Type.ARRAY) {
            for (CsmlNode child : node.getChildren()) {
                result.add(child.asString());
            }
        }
        return result;
    }
}

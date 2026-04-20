package project10;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

public class Grammer {
    private ArrayList<String> keywords = new ArrayList<>();
    private ArrayList<String> symbols = new ArrayList<>();
    private ArrayList<String> parents = new ArrayList<>();
    private String endingDelimiter = "";
    private Document xmlDoc;

    public Grammer() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.xmlDoc = builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

        this.keywords.add("class");
        this.keywords.add("constructor");
        this.keywords.add("function");
        this.keywords.add("method");
        this.keywords.add("field");
        this.keywords.add("static");
        this.keywords.add("var");
        this.keywords.add("int");
        this.keywords.add("char");
        this.keywords.add("boolean");
        this.keywords.add("void");
        this.keywords.add("true");
        this.keywords.add("false");
        this.keywords.add("null");
        this.keywords.add("this");
        this.keywords.add("let");
        this.keywords.add("do");
        this.keywords.add("if");
        this.keywords.add("else");
        this.keywords.add("while");
        this.keywords.add("return");
        this.keywords.add("Array");
        this.keywords.add("and");
        this.keywords.add("or");

        this.symbols.add("{");
        this.symbols.add("}");
        this.symbols.add("(");
        this.symbols.add(")");
        this.symbols.add("[");
        this.symbols.add("]");
        this.symbols.add(".");
        this.symbols.add(",");
        this.symbols.add(":");
        this.symbols.add(";");
        this.symbols.add("+");
        this.symbols.add("-");
        this.symbols.add("*");
        this.symbols.add("/");
        this.symbols.add("&");
        this.symbols.add("|");
        this.symbols.add("<");
        this.symbols.add(">");
        this.symbols.add("=");
        this.symbols.add("~");
    }

    public Document build(ArrayList<String> tokens) {
        Element root = this.xmlDoc.createElement("root");
        this.xmlDoc.appendChild(root);

        do {
            String token = tokens.removeLast();

            if (token.equals(this.endingDelimiter)) {
                if (!this.parents.isEmpty()) {
                    this.parents.removeLast();
                }

                this.endingDelimiter = "";
            }

            if (token.equals("{")) {
                this.endingDelimiter = "}";
            } else if (token.equals("(")) {
                this.endingDelimiter = ")";
            } else if (token.equals("[")) {
                this.endingDelimiter = "]";
            }

            if (this.keywords.contains(token)) {
                if (this.parents.isEmpty()) {
                    this.parents.add(token);
                }

                Element parentElement = this.getCurrentParent();

                switch (token) {
                    case "class": {
                        String className = this.safeRemoveLast(tokens);
                        Element element = this.xmlDoc.createElement(token);
                        element.setAttribute("name", className);
                        parentElement.appendChild(element);
                        this.parents.add(token);
                        break;
                    }
                    case "constructor":
                    case "function":
                    case "method":
                    case "field":
                    case "static": {
                        String name = this.safeRemoveLast(tokens);
                        Element element = this.xmlDoc.createElement(token);
                        element.setAttribute("name", name);
                        parentElement.appendChild(element);
                        this.parents.add(token);
                        break;
                    }
                    case "var":
                    case "int":
                    case "char":
                    case "boolean":
                    case "let": {
                        String name = this.safeRemoveLast(tokens);
                        String value = this.safeRemoveLast(tokens);
                        Element element = this.xmlDoc.createElement(token);
                        element.setAttribute("name", name);
                        element.setAttribute("value", value);
                        parentElement.appendChild(element);
                        break;
                    }
                    case "return": {
                        Element element = this.xmlDoc.createElement(token);
                        parentElement.appendChild(element);
                        break;
                    }
                }
            }
        } while (!tokens.isEmpty());

        return this.xmlDoc;
    }

    private Element getCurrentParent() {
        for (int i = this.parents.size() - 1; i >= 0; i--) {
            NodeList found = this.xmlDoc.getElementsByTagName(this.parents.get(i));
            if (found.getLength() > 0) {
                return (Element) found.item(0);
            }
        }

        return this.xmlDoc.getDocumentElement();
    }

    private String safeRemoveLast(ArrayList<String> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }
        return tokens.removeLast();
    }
}
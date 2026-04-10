package project10;

import java.util.ArrayList;

public class Tokenizer {
    private ArrayList<String> keywords = new ArrayList<String>();
    private ArrayList<String> symbols = new ArrayList<String>();
    private ArrayList<String> escapedChars = new ArrayList<String>();

    public Tokenizer() {
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

        this.escapedChars.add("{");
        this.escapedChars.add("(");
        this.escapedChars.add(")");
        this.escapedChars.add("[");
        this.escapedChars.add("]");
    }

    public String[] tokenize(String line) {
        String escapedLine = "";
        line = line.replaceAll("/\\*.*?\\*/", "").trim();
        line = line.replaceAll("\t{1,5}", "^");
        line = line.replaceAll(" {2,5}", "^");
        line = line.replaceAll(";", "^;^");
        line = line.replaceAll("\\(", "^(^");
        line = line.replaceAll("\\)", "^)^");
        line = line.replaceAll("\\{", "^{^");
        line = line.replaceAll("}", "^}^");

        for(String keyword : this.keywords) {
            line = line.replaceAll(keyword + " ", keyword + "^");
        }

        for(String symbol : this.symbols) {
            if(this.escapedChars.contains(symbol)) {
                escapedLine = "\\";
            }
            line = line.replaceAll(String.format(" %s%s ", escapedLine, symbol), String.format("^%s^", symbol));
        }

        line = line.replaceAll("\\^\\^", "^");
        line = line.replaceAll("\\^ \\^", "^");

        if(!line.startsWith("//") || !line.startsWith("/*")) {
            return line.split("\\^");
        }
        return new String[0];
    }

    private boolean needsEscapeChar(char c) {
        return escapedChars.contains(String.valueOf(c));
    }
}

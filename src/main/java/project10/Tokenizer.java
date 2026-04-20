package project10;

import java.util.ArrayList;
import java.util.Arrays;

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

    public ArrayList<String> tokenize(String file) {
        String escapedLine = "";
        file = file.replaceAll("/\\*.*?\\*/", "").trim();
        file = file.replaceAll("\t{1,5}", "^");
        file = file.replaceAll(" {2,5}", "^");
        file = file.replaceAll(";", "^;^");
        file = file.replaceAll("\\(", "^(^");
        file = file.replaceAll("\\)", "^)^");
        file = file.replaceAll("\\{", "^{^");
        file = file.replaceAll("}", "^}^");

        for(String keyword : this.keywords) {
            file = file.replaceAll(keyword + " ", keyword + "^");
        }

        for(String symbol : this.symbols) {
            if(this.escapedChars.contains(symbol)) {
                escapedLine = "\\";
            }
            file = file.replaceAll(String.format(" %s%s ", escapedLine, symbol), String.format("^%s^", symbol));
        }

        file = file.replaceAll("\\^\\^", "^");
        file = file.replaceAll("\\^ \\^", "^");

        if(!file.startsWith("//") || !file.startsWith("/*")) {
            String[] tokens = file.split("\\^");
            return new ArrayList<String>(Arrays.asList(tokens));
        }
        return new ArrayList<String>();
    }

    private boolean needsEscapeChar(char c) {
        return escapedChars.contains(String.valueOf(c));
    }
}

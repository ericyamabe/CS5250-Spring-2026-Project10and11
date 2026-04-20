package project11;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class Assemble {
    private Map<String, Symbol> classSymbols = new HashMap<>();
    private Map<String, Symbol> subroutineSymbols = new HashMap<>();
    private int staticCount = 0;
    private int fieldCount = 0;
    private int argCount = 0;
    private int localCount = 0;
    private String className = "";
    private String subroutineName = "";
    private int ifLabelCount = 0;
    private int whileLabelCount = 0;
    private StringBuilder vmOutput = new StringBuilder();

    private static class Symbol {
        String type;
        String kind;
        int index;

        Symbol(String type, String kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
    }

    private void resetClassSymbols() {
        classSymbols.clear();
        staticCount = 0;
        fieldCount = 0;
    }

    private void resetSubroutineSymbols() {
        subroutineSymbols.clear();
        argCount = 0;
        localCount = 0;
    }

    private void defineSymbol(String name, String type, String kind) {
        switch (kind) {
            case "static" -> { classSymbols.put(name, new Symbol(type, kind, staticCount++)); }
            case "field"  -> { classSymbols.put(name, new Symbol(type, kind, fieldCount++)); }
            case "argument" -> { subroutineSymbols.put(name, new Symbol(type, kind, argCount++)); }
            case "local"  -> { subroutineSymbols.put(name, new Symbol(type, kind, localCount++)); }
        }
    }

    private Symbol lookupSymbol(String name) {
        if (subroutineSymbols.containsKey(name)) return subroutineSymbols.get(name);
        if (classSymbols.containsKey(name))      return classSymbols.get(name);
        return null;
    }

    private String kindToSegment(String kind) {
        return switch (kind) {
            case "static"   -> "static";
            case "field"    -> "this";
            case "argument" -> "argument";
            case "local"    -> "local";
            default         -> throw new RuntimeException("Unknown kind: " + kind);
        };
    }

    private void emit(String line) {
        vmOutput.append(line).append("\n");
    }

    private void emitPush(String segment, int index) {
        emit("push " + segment + " " + index);
    }

    private void emitPop(String segment, int index) {
        emit("pop " + segment + " " + index);
    }

    private void emitArithmetic(String op) {
        emit(op);
    }

    private void emitLabel(String label) {
        emit("label " + label);
    }

    private void emitGoto(String label) {
        emit("goto " + label);
    }

    private void emitIfGoto(String label) {
        emit("if-goto " + label);
    }

    private void emitCall(String name, int nArgs) {
        emit("call " + name + " " + nArgs);
    }

    private void emitFunction(String name, int nLocals) {
        emit("function " + name + " " + nLocals);
    }

    private void emitReturn() {
        emit("return");
    }

    public String compile(Document doc) {
        Element root = doc.getDocumentElement();
        NodeList classes = doc.getElementsByTagName("class");
        if (classes.getLength() == 0) {
            throw new RuntimeException("No <class> element found in document.");
        }
        compileClass((Element) classes.item(0));
        return vmOutput.toString();
    }

    private void compileClass(Element classEl) {
        resetClassSymbols();
        this.className = classEl.getAttribute("name");

        compileClassVarDecs(classEl);

        NodeList constructors = classEl.getElementsByTagName("constructor");
        NodeList functions    = classEl.getElementsByTagName("function");
        NodeList methods      = classEl.getElementsByTagName("method");

        for (int i = 0; i < constructors.getLength(); i++)
            compileSubroutine((Element) constructors.item(i), "constructor");
        for (int i = 0; i < functions.getLength(); i++)
            compileSubroutine((Element) functions.item(i), "function");
        for (int i = 0; i < methods.getLength(); i++)
            compileSubroutine((Element) methods.item(i), "method");
    }

    private void compileClassVarDecs(Element classEl) {
        NodeList fields  = classEl.getElementsByTagName("field");
        NodeList statics = classEl.getElementsByTagName("static");

        for (int i = 0; i < fields.getLength(); i++) {
            Element el = (Element) fields.item(i);
            defineSymbol(el.getAttribute("name"), el.getAttribute("type"), "field");
        }
        for (int i = 0; i < statics.getLength(); i++) {
            Element el = (Element) statics.item(i);
            defineSymbol(el.getAttribute("name"), el.getAttribute("type"), "static");
        }
    }

    private void compileSubroutine(Element subEl, String subroutineType) {
        resetSubroutineSymbols();
        ifLabelCount = 0;
        whileLabelCount = 0;

        this.subroutineName = subEl.getAttribute("name");
        String fullName = className + "." + subroutineName;

        if (subroutineType.equals("method")) {
            defineSymbol("this", className, "argument");
        }

        NodeList varNodes = subEl.getElementsByTagName("var");
        for (int i = 0; i < varNodes.getLength(); i++) {
            Element varEl = (Element) varNodes.item(i);
            defineSymbol(varEl.getAttribute("name"), varEl.getAttribute("type"), "local");
        }

        emitFunction(fullName, localCount);

        switch (subroutineType) {
            case "constructor" -> {
                emitPush("constant", fieldCount);
                emitCall("Memory.alloc", 1);
                emitPop("pointer", 0);
            }
            case "method" -> {
                emitPush("argument", 0);
                emitPop("pointer", 0);
            }
        }

        compileStatements(subEl);
    }

    private void compileStatements(Element parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element)) continue;
            Element el = (Element) children.item(i);
            switch (el.getTagName()) {
                case "let"    -> compileLet(el);
                case "do"     -> compileDo(el);
                case "if"     -> compileIf(el);
                case "while"  -> compileWhile(el);
                case "return" -> compileReturn(el);
            }
        }
    }

    private void compileLet(Element letEl) {
        String varName = letEl.getAttribute("name");
        String value   = letEl.getAttribute("value");

        pushValue(value);

        Symbol sym = lookupSymbol(varName);
        if (sym != null) {
            emitPop(kindToSegment(sym.kind), sym.index);
        } else {
            throw new RuntimeException("Undefined variable in let: " + varName);
        }
    }

    private void compileDo(Element doEl) {
        String call = doEl.getAttribute("call");
        if (call == null || call.isEmpty()) {
            compileStatements(doEl);
            return;
        }
        compileCall(call, doEl);
        emitPop("temp", 0);
    }

    private void compileReturn(Element returnEl) {
        String value = returnEl.getAttribute("value");
        if (value == null || value.equals("void") || value.isEmpty()) {
            emitPush("constant", 0);
        } else {
            pushValue(value);
        }
        emitReturn();
    }

    private void compileIf(Element ifEl) {
        int labelIndex = ifLabelCount++;
        String trueLabel  = "IF_TRUE"  + labelIndex;
        String falseLabel = "IF_FALSE" + labelIndex;
        String endLabel   = "IF_END"   + labelIndex;

        String condition = ifEl.getAttribute("condition");
        pushValue(condition);
        emitIfGoto(trueLabel);
        emitGoto(falseLabel);
        emitLabel(trueLabel);
        NodeList children = ifEl.getChildNodes();
        boolean inElse = false;

        for (int i = 0; i < children.getLength(); i++) {
            if (!(children.item(i) instanceof Element)) continue;
            Element child = (Element) children.item(i);
            if (child.getTagName().equals("else")) {
                inElse = true;
                continue;
            }
            if (!inElse) compileStatements(child);
        }

        emitGoto(endLabel);
        emitLabel(falseLabel);

        NodeList elseNodes = ifEl.getElementsByTagName("else");
        if (elseNodes.getLength() > 0) {
            compileStatements((Element) elseNodes.item(0));
        }

        emitLabel(endLabel);
    }

    private void compileWhile(Element whileEl) {
        int labelIndex = whileLabelCount++;
        String topLabel = "WHILE_EXP"  + labelIndex;
        String endLabel = "WHILE_END" + labelIndex;

        emitLabel(topLabel);
        String condition = whileEl.getAttribute("condition");
        pushValue(condition);
        emitArithmetic("not");
        emitIfGoto(endLabel);
        compileStatements(whileEl);
        emitGoto(topLabel);
        emitLabel(endLabel);
    }

    private void pushValue(String value) {
        if (value == null || value.isEmpty()) return;

        if (value.matches("-?\\d+")) {
            int intVal = Integer.parseInt(value);
            if (intVal >= 0) {
                emitPush("constant", intVal);
            } else {
                emitPush("constant", -intVal);
                emitArithmetic("neg");
            }
            return;
        }

        switch (value) {
            case "true"  -> { emitPush("constant", 0); emitArithmetic("not"); return; }
            case "false" -> { emitPush("constant", 0); return; }
            case "null"  -> { emitPush("constant", 0); return; }
            case "this"  -> { emitPush("pointer", 0);  return; }
        }

        if (value.startsWith("\"") && value.endsWith("\"")) {
            String str = value.substring(1, value.length() - 1);
            emitPush("constant", str.length());
            emitCall("String.new", 1);
            for (char c : str.toCharArray()) {
                emitPush("constant", c);
                emitCall("String.appendChar", 2);
            }
            return;
        }

        String[] parts = splitExpression(value);
        if (parts != null) {
            pushValue(parts[0]);
            pushValue(parts[2]);
            emitArithmetic(opToVM(parts[1]));
            return;
        }

        if (value.contains("(")) {
            compileCall(value, null);
            return;
        }

        Symbol sym = lookupSymbol(value);
        if (sym != null) {
            emitPush(kindToSegment(sym.kind), sym.index);
        } else {
            System.err.println("WARNING: Unknown symbol '" + value + "', pushing 0");
            emitPush("constant", 0);
        }
    }

    private void compileCall(String callStr, Element contextEl) {
        int parenOpen  = callStr.indexOf('(');
        int parenClose = callStr.lastIndexOf(')');

        if (parenOpen < 0) return;

        String callee   = callStr.substring(0, parenOpen).trim();
        String argsStr  = (parenOpen + 1 <= parenClose)
                ? callStr.substring(parenOpen + 1, parenClose).trim()
                : "";
        String[] args   = argsStr.isEmpty() ? new String[0] : argsStr.split(",");

        int nArgs = 0;
        String fullCallee;

        if (callee.contains(".")) {
            String[] parts = callee.split("\\.", 2);
            String objOrClass = parts[0];
            String methodName = parts[1];

            Symbol sym = lookupSymbol(objOrClass);
            if (sym != null) {
                emitPush(kindToSegment(sym.kind), sym.index);
                fullCallee = sym.type + "." + methodName;
                nArgs = 1;
            } else {
                fullCallee = objOrClass + "." + methodName;
            }
        } else {
            emitPush("pointer", 0);
            fullCallee = className + "." + callee;
            nArgs = 1;
        }

        for (String arg : args) {
            pushValue(arg.trim());
            nArgs++;
        }

        emitCall(fullCallee, nArgs);
    }

    private String[] splitExpression(String expr) {
        String[] ops = {"|", "&", "<", ">", "=", "+", "-", "*", "/"};
        for (String op : ops) {
            int idx = expr.lastIndexOf(op);
            if (idx > 0 && idx < expr.length() - 1) {
                return new String[]{
                        expr.substring(0, idx).trim(),
                        op,
                        expr.substring(idx + 1).trim()
                };
            }
        }
        return null;
    }

    private String opToVM(String op) {
        return switch (op) {
            case "+"  -> "add";
            case "-"  -> "sub";
            case "*"  -> "call Math.multiply 2";
            case "/"  -> "call Math.divide 2";
            case "&"  -> "and";
            case "|"  -> "or";
            case "<"  -> "lt";
            case ">"  -> "gt";
            case "="  -> "eq";
            default   -> throw new RuntimeException("Unknown operator: " + op);
        };
    }
}
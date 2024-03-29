package valid;

import com.bpodgursky.jbool_expressions.And;
import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.Or;
import com.bpodgursky.jbool_expressions.Variable;

import com.bpodgursky.jbool_expressions.rules.RuleSet;

import com.google.errorprone.annotations.Var;
import objects.ClickElement;
import objects.InputText;
import objects.SelectCheckbox;
import objects.SelectRadioButton;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogicParser {
    public static Expression<String> createTextExpression(Element element) {
        String type = element.getElementsByTagName("type").item(0).getTextContent();
        if (!type.equals("and") && !type.equals("or")) {
            String text;
            if (type.equals("Input Text")) text = element.getElementsByTagName("text").item(0).getTextContent();
            else text = null;
            return Variable.of(text);
        }
        if (type.equals("and")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();
            List<String> childString = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }
            for (int i = 0; i < childElements.size(); i++) {
                childString.add(childElements.get(i).getElementsByTagName("text").item(0).getTextContent());
            }
            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createTextExpression(childElements.get(i));
            }

            Expression expression = And.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;

        } else if (type.equals("or")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();


            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }

            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createTextExpression(childElements.get(i));
            }
            Expression expression = Or.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;
        }
        System.out.println("Wrong type of logic expression!");
        return null;
    }
    public static Expression<objects.Expression> createAction(Element element) {
        String type = element.getElementsByTagName("type").item(0).getTextContent();
        if (!type.equals("and") && !type.equals("or")) {
            switch (type) {
                case "Input Text": {
                    String locator = element.getElementsByTagName("locator").item(0).getTextContent();
                    String text = element.getElementsByTagName("text").item(0).getTextContent();
                    return Variable.of(new InputText(locator, text));
                }

                case "Click Element": {
                    String locator = element.getElementsByTagName("locator").item(0).getTextContent();
                    return Variable.of(new ClickElement(locator));
                }
                case "Select Checkbox": {
                    String locator = element.getElementsByTagName("answer").item(0).getTextContent();
                    return Variable.of(new SelectCheckbox(locator));
                }
                case "Select Radio Button": {
                    String groupName = element.getElementsByTagName("question").item(0).getTextContent();
                    String value = element.getElementsByTagName("choice").item(0).getTextContent();
                    return Variable.of(new SelectRadioButton("",groupName,value));
                }
            }
//            String text;
//
//            if (type.equals("Input Text")) text = element.getElementsByTagName("text").item(0).getTextContent();
//            else text = null;
//            return Variable.of(action);
        }
        if (type.equals("and")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();
            List<String> childString = new ArrayList<>();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }
            for (int i = 0; i < childElements.size(); i++) {
                childString.add(childElements.get(i).getElementsByTagName("text").item(0).getTextContent());
            }
            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createAction(childElements.get(i));
            }

            Expression expression = And.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;

        } else if (type.equals("or")) {
            NodeList childNodes = element.getChildNodes();
            List<Element> childElements = new ArrayList<>();


            for (int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE && ((Element) child).getTagName().equals("LogicExpressionOfActions"))
                    childElements.add((Element) child);
            }

            Expression[] expressionList = new Expression[childElements.size()];
            for (int i = 0; i < childElements.size(); i++) {
                expressionList[i] = createAction(childElements.get(i));
            }
            Expression expression = Or.of(expressionList, Expression.LEXICOGRAPHIC_COMPARATOR);
            return expression;
        }
        System.out.println("Wrong type of logic expression!");
        return null;
    }


    public static List<List<Action>> createDNFList(Expression expr) {
        expr = RuleSet.toDNF(expr);
        List<List<Action>> list = new ArrayList<>();
        if (expr.getExprType().equals("and")) {
            List<Action> childOfAnd = expr.getAllK().stream().toList();
            ArrayList<Action> sortable = new ArrayList<>(childOfAnd);
            Collections.sort(sortable);
            list.add(sortable);
            return list;
        }

        List<Expression> expressionList = expr.getChildren();
        for (Expression expression : expressionList) {
            List<Expression> andExpressions = expression.getChildren();
            List<Action> andOfActions = new ArrayList<>();
            for (Expression e : andExpressions) {
                andOfActions.add((Action) e.getAllK().stream().toList().get(0));
            }
            if (expression.getExprType().equals("variable"))
                andOfActions.add((Action) expression.getAllK().stream().toList().get(0));
            list.add(andOfActions);
        }
        return list;
    }
}

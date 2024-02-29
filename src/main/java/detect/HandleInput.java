package detect;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;

public class HandleInput {
    public static Element searchInputElementInSubtree(String text, Element e) {
        Elements input = HandleElement.selectInteractableElementsInSubtree(e);
        if (input.size() > 1 || (input.size() == 1 && !TypeElement.isInputElement(input.get(0))) || !e.text().equals(text)) {
            return null;
        }
        if (input.size() == 1 && TypeElement.isInputElement(input.get(0)) && e.text().equals(text)) {
            return input.get(0);
        }
        return searchInputElementInSubtree(text, e.parent());
    }

    public static Elements getInputElements(Document document) {
        Elements textarea_tag = document.getElementsByTag("textarea");
        Elements input_tag = document.getElementsByTag("input");
        Elements res = new Elements(textarea_tag);
        for (Element e : input_tag) {
            if (TypeElement.isInputElement(e)) {
                res.add(e);
            }
        }
        return res;
    }

    public static String getTextForInput(Element e) {
        if (!e.ownText().isEmpty()) {
            return e.ownText();
        }
        String res = "";
        if (e.hasAttr("id") && !e.attr("id").isEmpty()) {
            res = HandleElement.getAssociatedLabel(e.attr("id"), e);
            if (!res.isEmpty()) {
                return res;
            }
        }
        res = getTextForInputInSubtree(e);
        return res;
    }

    public static String getTextForInputInSubtree(Element e) {
        Elements elements = e.select("*");
        int cnt_text = 0;
        String tmp = "";
        for (Element ele : elements) {
            if (TypeElement.isInteractableElement(ele) && !TypeElement.isInputElement(ele)) {
                return "";
            }
            String text = ele.ownText();
            if (!text.isEmpty()) {
                tmp = text;
                cnt_text++;
                if (cnt_text > 1) {
                    return "";
                }
            }
        }
        if (cnt_text == 1) {
            return tmp;
        }
        return getTextForInputInSubtree(e.parent());
    }
}

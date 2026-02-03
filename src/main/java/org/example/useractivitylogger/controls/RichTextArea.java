package org.example.useractivitylogger.controls;

import org.fxmisc.richtext.InlineCssTextArea;

public class RichTextArea extends InlineCssTextArea {
    public RichTextArea() {
        super();
        this.setWrapText(true);
        this.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
    }
}

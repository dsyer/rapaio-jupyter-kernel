package org.rapaio.jupyter.kernel.core.display.text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * This incorporates some commands for manipulation of the text output format for Jupyter
 * notebook output and error output.
 * <p>
 * Guidance follows:
 * <ul>
 *     <li><a href="https://pypi.org/project/ansi-text/">PyPi ansi-text</a></li>
 *     <li><a href="https://en.wikipedia.org/wiki/ANSI_escape_code">Wikipedia on ANSI escape code</a></li>
 * </ul>
 */
public final class ANSIText {

    public static final int RESET = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 3;
    public static final int UNDERLINE = 4;
    public static final int FG_COLOR = 38;
    public static final int BG_COLOR = 48;

    public static final int FG_BLACK = 30;
    public static final int FG_RED = 31;
    public static final int FG_GREEN = 32;
    public static final int FG_YELLOW = 33;
    public static final int FG_BLUE = 34;
    public static final int FG_MAGENTA = 35;
    public static final int FG_CYAN = 36;
    public static final int FG_WHITE = 37;
    public static final int FG_BRIGHT_BLACK = 90;
    public static final int FG_BRIGHT_RED = 91;
    public static final int FG_BRIGHT_GREEN = 92;
    public static final int FG_BRIGHT_YELLOW = 93;
    public static final int FG_BRIGHT_BLUE = 94;
    public static final int FG_BRIGHT_MAGENTA = 95;
    public static final int FG_BRIGHT_CYAN = 96;
    public static final int FG_BRIGHT_WHITE = 97;

    public static final int BG_BLACK = 40;
    public static final int BG_RED = 41;
    public static final int BG_GREEN = 42;
    public static final int BG_YELLOW = 43;
    public static final int BG_BLUE = 44;
    public static final int BG_MAGENTA = 45;
    public static final int BG_CYAN = 46;
    public static final int BG_WHITE = 47;
    public static final int BG_BRIGHT_BLACK = 100;
    public static final int BG_BRIGHT_RED = 101;
    public static final int BG_BRIGHT_GREEN = 102;
    public static final int BG_BRIGHT_YELLOW = 103;
    public static final int BG_BRIGHT_BLUE = 104;
    public static final int BG_BRIGHT_MAGENTA = 105;
    public static final int BG_BRIGHT_CYAN = 106;
    public static final int BG_BRIGHT_WHITE = 107;

    private static final int COLOR_SET_8_BIT = 5;
    private static final int COLOR_SET_TRUE_COLOR = 2;

    private final StringBuilder sb = new StringBuilder();

    public static ANSIText start() {
        return new ANSIText().reset();
    }

    public ANSIText codes(int... codes) {
        sb.append(escape(codes));
        return this;
    }

    public ANSIText text(String text) {
        sb.append(text);
        return this;
    }

    public ANSIText bold() {
        sb.append(escape(BOLD));
        return this;
    }

    public ANSIText fgBlue() {
        sb.append(escape(FG_BLUE));
        return this;
    }

    public ANSIText fgGreen() {
        sb.append(escape(FG_GREEN));
        return this;
    }

    public ANSIText fgColor(int index) {
        sb.append(escape(FG_COLOR, COLOR_SET_8_BIT, index));
        return this;
    }

    public ANSIText fgColor(Color color) {
        sb.append(escape(FG_COLOR, COLOR_SET_TRUE_COLOR, color.getRed(), color.getGreen(), color.getBlue()));
        return this;
    }

    public ANSIText bgColor(int index) {
        sb.append(escape(BG_COLOR, COLOR_SET_8_BIT, index));
        return this;
    }

    public ANSIText bgColor(Color color) {
        sb.append(escape(BG_COLOR, COLOR_SET_TRUE_COLOR, color.getRed(), color.getGreen(), color.getBlue()));
        return this;
    }

    public ANSIText reset() {
        sb.append(escape(RESET));
        return this;
    }

    public String build() {
        return sb.toString();
    }

    private static String escape(int... codes) {
        StringBuilder sb = new StringBuilder();
        sb.append("\u001b[");
        for (int i = 0; i < codes.length; i++) {
            sb.append(codes[i]);
            if (i != codes.length - 1) {
                sb.append(";");
            }
        }
        sb.append("m");
        return sb.toString();
    }

    private static final String CODE_LINE_PROMPT = "|    ";

    public static List<String> errorTypeHeader(String errorType) {
        return List.of(new ANSIText().reset().codes(BOLD, FG_RED).text(errorType).text(":").build());
    }

    public static List<String> sourceCode(String code) {
        return sourceCode(code, -1, -1, -1);
    }

    public static List<String> sourceCode(String code, int position, int startPosition, int endPosition) {
        List<String> lines = new ArrayList<>();
        if (position == -1) {
            for (String line : code.split("\\R")) {
                lines.add(new ANSIText().reset().codes(BOLD, FG_BLACK).text(CODE_LINE_PROMPT).text(line).reset().build());
            }
        } else {
            int start = 0;
            for (String line : code.split("\\R")) {
                int end = start + line.length();

                if (end < startPosition || start > endPosition) {
                    lines.add(new ANSIText().reset().codes(BOLD, FG_BLACK).text(CODE_LINE_PROMPT).text(line).build());
                } else {
                    int startMark = Math.max(start, startPosition);
                    int endMark = Math.min(end, endPosition);

                    lines.add(new ANSIText()
                            .reset().codes(BOLD, FG_BLACK).text(CODE_LINE_PROMPT).text(line.substring(0, startMark - start))
                            .reset().codes(BOLD, BG_RED).fgColor(Color.YELLOW).text(line.substring(startMark - start, endMark - start))
                            .reset().codes(BOLD, FG_BLACK).text(line.substring(endMark - start))
                            .build()
                    );
                }
                start += line.length() + 1; // one position for newline
            }
        }
        return lines;
    }

    public static List<String> errorMessages(String errorMessage) {
        List<String> lines = new ArrayList<>();
        for (String line : errorMessage.split("\\R")) {
            if (!line.trim().startsWith("location:")) {
                lines.add(new ANSIText().reset().codes(BOLD, FG_BLUE).text(line).build());
            }
        }
        return lines;
    }

}

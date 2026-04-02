import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PdfExporter {

    private static final float PAGE_WIDTH = 595f;
    private static final float PAGE_HEIGHT = 842f;
    private static final float MARGIN = 42f;
    private static final float HEADER_HEIGHT = 88f;
    private static final float FOOTER_HEIGHT = 28f;
    private static final float CONTENT_TOP = PAGE_HEIGHT - HEADER_HEIGHT - 22f;
    private static final float CONTENT_BOTTOM = MARGIN + FOOTER_HEIGHT;
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final DateTimeFormatter EXPORTED_AT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void exportReport(String reportText, Path path) throws IOException {
        List<PdfLine> lines = buildLines(reportText);
        List<List<PdfLine>> pages = paginate(lines);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        int objectCount = 4 + (pages.size() * 2);
        int[] offsets = new int[objectCount];

        write(pdf, "%PDF-1.4\n");

        offsets[0] = pdf.size();
        write(pdf, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

        offsets[1] = pdf.size();
        StringBuilder kids = new StringBuilder();
        for (int page = 0; page < pages.size(); page++) {
            int pageObjectNumber = 5 + (page * 2);
            kids.append(pageObjectNumber).append(" 0 R ");
        }
        write(pdf, "2 0 obj\n<< /Type /Pages /Count " + pages.size() + " /Kids [ " + kids + "] >>\nendobj\n");

        offsets[2] = pdf.size();
        write(pdf, "3 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        offsets[3] = pdf.size();
        write(pdf, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            int pageObjectNumber = 5 + (pageIndex * 2);
            int contentObjectNumber = pageObjectNumber + 1;

            offsets[pageObjectNumber - 1] = pdf.size();
            write(pdf,
                    pageObjectNumber + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 "
                            + format(PAGE_WIDTH) + " " + format(PAGE_HEIGHT) + "] "
                            + "/Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> "
                            + "/Contents " + contentObjectNumber + " 0 R >>\nendobj\n");

            byte[] contentBytes = renderPageContent(pages.get(pageIndex), pageIndex + 1, pages.size());
            offsets[contentObjectNumber - 1] = pdf.size();
            write(pdf, contentObjectNumber + " 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n");
            pdf.write(contentBytes);
            write(pdf, "\nendstream\nendobj\n");
        }

        int xrefOffset = pdf.size();
        write(pdf, "xref\n0 " + (objectCount + 1) + "\n");
        write(pdf, "0000000000 65535 f \n");
        for (int offset : offsets) {
            write(pdf, String.format("%010d 00000 n \n", offset));
        }

        write(pdf,
                "trailer\n<< /Size " + (objectCount + 1) + " /Root 1 0 R >>\nstartxref\n"
                        + xrefOffset + "\n%%EOF");

        Files.write(path, pdf.toByteArray());
    }

    private static List<PdfLine> buildLines(String reportText) {
        String normalized = reportText.replace("\r\n", "\n").replace('\r', '\n');
        String[] rawLines = normalized.split("\n", -1);
        List<PdfLine> lines = new ArrayList<>();

        for (String rawLine : rawLines) {
            String trimmed = rawLine.trim();

            if (trimmed.isEmpty()) {
                lines.add(new PdfLine("", LineStyle.SPACER));
                continue;
            }

            if (trimmed.startsWith("===") && trimmed.endsWith("===")) {
                String title = trimmed.replace("=", "").trim();
                lines.add(new PdfLine(title, LineStyle.SECTION));
                continue;
            }

            if (trimmed.contains(":")) {
                int colonIndex = trimmed.indexOf(':');
                String label = trimmed.substring(0, colonIndex + 1).trim();
                String value = trimmed.substring(colonIndex + 1).trim();
                lines.add(new PdfLine(label + " " + value, LineStyle.KEY_VALUE));
                continue;
            }

            lines.add(new PdfLine(trimmed, LineStyle.BODY));
        }

        return lines;
    }

    private static List<List<PdfLine>> paginate(List<PdfLine> lines) {
        List<List<PdfLine>> pages = new ArrayList<>();
        List<PdfLine> currentPage = new ArrayList<>();
        float remainingHeight = CONTENT_TOP - CONTENT_BOTTOM;

        for (PdfLine line : lines) {
            List<String> wrappedLines = wrapText(line.text, line.style.maxCharsPerLine);
            float blockHeight = line.style.topPadding + line.style.bottomPadding;

            if (wrappedLines.isEmpty()) {
                wrappedLines.add("");
            }

            for (String ignored : wrappedLines) {
                blockHeight += line.style.lineHeight;
            }

            if (blockHeight > remainingHeight && !currentPage.isEmpty()) {
                pages.add(currentPage);
                currentPage = new ArrayList<>();
                remainingHeight = CONTENT_TOP - CONTENT_BOTTOM;
            }

            currentPage.add(new PdfLine(String.join("\n", wrappedLines), line.style));
            remainingHeight -= blockHeight;
        }

        if (currentPage.isEmpty()) {
            currentPage.add(new PdfLine("No report data available.", LineStyle.BODY));
        }

        pages.add(currentPage);
        return pages;
    }

    private static byte[] renderPageContent(List<PdfLine> lines, int pageNumber, int totalPages) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String exportedAt = LocalDateTime.now().format(EXPORTED_AT_FORMAT);

        write(out, "q\n");
        write(out, "0.07 0.12 0.20 rg\n");
        write(out, "0 " + format(PAGE_HEIGHT - HEADER_HEIGHT) + " " + format(PAGE_WIDTH) + " "
                + format(HEADER_HEIGHT) + " re f\n");
        write(out, "0.10 0.16 0.28 rg\n");
        write(out, "0 0 " + format(PAGE_WIDTH) + " " + format(PAGE_HEIGHT - HEADER_HEIGHT) + " re f\n");
        write(out, "Q\n");

        writeText(out, "Personal Finance Report", MARGIN, PAGE_HEIGHT - 38f, "/F2", 22f, 0.97f, 0.98f, 1f);
        writeText(out, "Exported " + exportedAt, MARGIN, PAGE_HEIGHT - 58f, "/F1", 10f, 0.75f, 0.84f, 0.98f);
        writeText(out, "Analytics, category performance, and monthly insight summary",
                MARGIN, PAGE_HEIGHT - 73f, "/F1", 10f, 0.67f, 0.74f, 0.84f);

        write(out, "q\n");
        write(out, "0.26 0.39 0.78 rg\n");
        write(out, format(MARGIN) + " " + format(PAGE_HEIGHT - HEADER_HEIGHT - 6f) + " "
                + format(CONTENT_WIDTH) + " 2 re f\n");
        write(out, "Q\n");

        float y = CONTENT_TOP;
        for (PdfLine line : lines) {
            y -= line.style.topPadding;
            String[] wrapped = line.text.split("\n", -1);
            for (String part : wrapped) {
                if (line.style == LineStyle.SECTION) {
                    writeText(out, part.toUpperCase(), MARGIN, y, "/F2", line.style.fontSize,
                            0.49f, 0.76f, 0.98f);
                } else if (line.style == LineStyle.KEY_VALUE) {
                    writeText(out, part, MARGIN + 8f, y, "/F1", line.style.fontSize,
                            0.93f, 0.96f, 0.99f);
                } else {
                    writeText(out, part, MARGIN + 8f, y, "/F1", line.style.fontSize,
                            0.82f, 0.87f, 0.93f);
                }
                y -= line.style.lineHeight;
            }
            y -= line.style.bottomPadding;
        }

        write(out, "q\n");
        write(out, "0.35 0.42 0.53 rg\n");
        write(out, format(MARGIN) + " " + format(FOOTER_HEIGHT + 8f) + " " + format(CONTENT_WIDTH) + " 1 re f\n");
        write(out, "Q\n");
        writeText(out, "Generated by Personal Finance Simulator", MARGIN, FOOTER_HEIGHT - 2f, "/F1", 9f,
                0.60f, 0.67f, 0.76f);
        writeText(out, "Page " + pageNumber + " of " + totalPages, PAGE_WIDTH - MARGIN - 58f, FOOTER_HEIGHT - 2f,
                "/F1", 9f, 0.60f, 0.67f, 0.76f);

        return out.toByteArray();
    }

    private static List<String> wrapText(String text, int maxChars) {
        List<String> lines = new ArrayList<>();
        if (text.isEmpty()) {
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() == 0) {
                current.append(word);
                continue;
            }

            if (current.length() + 1 + word.length() <= maxChars) {
                current.append(' ').append(word);
            } else {
                lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }

        return lines;
    }

    private static void writeText(ByteArrayOutputStream out, String text, float x, float y, String font,
            float fontSize, float r, float g, float b) throws IOException {
        write(out, "BT\n");
        write(out, font + " " + format(fontSize) + " Tf\n");
        write(out, format(r) + " " + format(g) + " " + format(b) + " rg\n");
        write(out, "1 0 0 1 " + format(x) + " " + format(y) + " Tm\n");
        write(out, "(" + escapePdfText(text) + ") Tj\n");
        write(out, "ET\n");
    }

    private static String escapePdfText(String text) {
        String escaped = text.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");

        StringBuilder builder = new StringBuilder();
        for (char ch : escaped.toCharArray()) {
            if (ch >= 32 && ch <= 126) {
                builder.append(ch);
            } else {
                builder.append('?');
            }
        }
        return builder.toString();
    }

    private static String format(float value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private static void write(ByteArrayOutputStream out, String text) throws IOException {
        out.write(text.getBytes(StandardCharsets.US_ASCII));
    }

    private enum LineStyle {
        SECTION(14f, 18f, 8f, 8f, 52),
        KEY_VALUE(11f, 15f, 2f, 2f, 76),
        BODY(11f, 15f, 2f, 2f, 78),
        SPACER(6f, 6f, 0f, 0f, 1);

        final float fontSize;
        final float lineHeight;
        final float topPadding;
        final float bottomPadding;
        final int maxCharsPerLine;

        LineStyle(float fontSize, float lineHeight, float topPadding, float bottomPadding, int maxCharsPerLine) {
            this.fontSize = fontSize;
            this.lineHeight = lineHeight;
            this.topPadding = topPadding;
            this.bottomPadding = bottomPadding;
            this.maxCharsPerLine = maxCharsPerLine;
        }
    }

    private static class PdfLine {
        final String text;
        final LineStyle style;

        PdfLine(String text, LineStyle style) {
            this.text = text;
            this.style = style;
        }
    }
}

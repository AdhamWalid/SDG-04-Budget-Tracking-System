import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

public class PdfTextExtractor {

    private static final Pattern STREAM_PATTERN = Pattern.compile("<<(.*?)>>\\s*stream\\r?\\n", Pattern.DOTALL);
    private static final Pattern TEXT_BLOCK_PATTERN = Pattern.compile("BT(.*?)ET", Pattern.DOTALL);
    private static final Pattern LITERAL_TEXT_PATTERN = Pattern.compile("\\((.*?)(?<!\\\\)\\)\\s*Tj", Pattern.DOTALL);
    private static final Pattern ARRAY_TEXT_PATTERN = Pattern.compile("\\[(.*?)]\\s*TJ", Pattern.DOTALL);
    private static final Pattern ARRAY_LITERAL_PATTERN = Pattern.compile("\\((.*?)(?<!\\\\)\\)", Pattern.DOTALL);
    private static final Pattern HEX_TEXT_PATTERN = Pattern.compile("<([0-9A-Fa-f]+)>");

    public static String extractText(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        String pdf = new String(bytes, StandardCharsets.ISO_8859_1);
        StringBuilder extracted = new StringBuilder();

        Matcher matcher = STREAM_PATTERN.matcher(pdf);
        int searchStart = 0;
        while (matcher.find(searchStart)) {
            String dictionary = matcher.group(1);
            int streamDataStart = matcher.end();
            int streamEnd = pdf.indexOf("endstream", streamDataStart);
            if (streamEnd == -1) {
                break;
            }

            byte[] streamBytes = slice(bytes, streamDataStart, streamEnd);
            if (dictionary.contains("/FlateDecode")) {
                streamBytes = inflate(streamBytes);
            }

            String streamText = new String(streamBytes, StandardCharsets.ISO_8859_1);
            appendTextBlocks(streamText, extracted);
            searchStart = streamEnd + "endstream".length();
        }

        return normalizeWhitespace(extracted.toString());
    }

    private static void appendTextBlocks(String streamText, StringBuilder extracted) {
        Matcher blockMatcher = TEXT_BLOCK_PATTERN.matcher(streamText);
        while (blockMatcher.find()) {
            String block = blockMatcher.group(1);

            Matcher literalMatcher = LITERAL_TEXT_PATTERN.matcher(block);
            while (literalMatcher.find()) {
                extracted.append(unescapePdfString(literalMatcher.group(1))).append('\n');
            }

            Matcher arrayMatcher = ARRAY_TEXT_PATTERN.matcher(block);
            while (arrayMatcher.find()) {
                String arrayContent = arrayMatcher.group(1);
                Matcher arrayLiteralMatcher = ARRAY_LITERAL_PATTERN.matcher(arrayContent);
                while (arrayLiteralMatcher.find()) {
                    extracted.append(unescapePdfString(arrayLiteralMatcher.group(1)));
                }
                extracted.append('\n');

                Matcher hexMatcher = HEX_TEXT_PATTERN.matcher(arrayContent);
                while (hexMatcher.find()) {
                    extracted.append(decodeHexString(hexMatcher.group(1)));
                }
            }
        }
    }

    private static byte[] inflate(byte[] compressed) throws IOException {
        try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(trimLineBreaks(compressed)));
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[2048];
            int read;
            while ((read = inflater.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (IOException ex) {
            return compressed;
        }
    }

    private static byte[] trimLineBreaks(byte[] bytes) {
        int start = 0;
        int end = bytes.length;
        while (start < end && (bytes[start] == '\r' || bytes[start] == '\n')) {
            start++;
        }
        while (end > start && (bytes[end - 1] == '\r' || bytes[end - 1] == '\n')) {
            end--;
        }
        return slice(bytes, start, end);
    }

    private static byte[] slice(byte[] bytes, int start, int end) {
        int safeStart = Math.max(0, start);
        int safeEnd = Math.max(safeStart, Math.min(bytes.length, end));
        byte[] result = new byte[safeEnd - safeStart];
        System.arraycopy(bytes, safeStart, result, 0, result.length);
        return result;
    }

    private static String unescapePdfString(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaping) {
                switch (ch) {
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    case 'b':
                        builder.append('\b');
                        break;
                    case 'f':
                        builder.append('\f');
                        break;
                    default:
                        builder.append(ch);
                        break;
                }
                escaping = false;
            } else if (ch == '\\') {
                escaping = true;
            } else {
                builder.append(ch);
            }
        }
        return builder.toString();
    }

    private static String decodeHexString(String hex) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i + 1 < hex.length(); i += 2) {
            try {
                int value = Integer.parseInt(hex.substring(i, i + 2), 16);
                if (value >= 32 && value <= 126) {
                    builder.append((char) value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return builder.toString();
    }

    private static String normalizeWhitespace(String text) {
        String cleaned = text.replace("\r", "\n").replaceAll("\n{3,}", "\n\n").trim();
        return cleaned;
    }
}

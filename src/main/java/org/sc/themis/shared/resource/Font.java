package org.sc.themis.shared.resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sc.themis.shared.resource.exception.ImageNotLoadedException;


public class Font {

    private Image image;
    private int lineHeight;
    private int size;
    private int scaleW;
    private int scaleH;
    private Map<Character, CharacterProperties> properties;

    public static void main(String[] args) {

        Font font = Font.of("src/main/resources/font/arial.fnt");

        String myText = "PLOP";
        CharacterProperties[] decoded = font.decode(myText);

        for (CharacterProperties cProperties : decoded) {
            System.out.println(cProperties);
        }

    }

    public static Font of(String filename) {

        Path path = Path.of(filename);

        if (path.toFile().exists()) {
            try {

                List<String> lines = Files.readAllLines(path);

                Font font = new Font();
                font.image = readImage(lines, path.getParent());
                font.properties = readCharacters(lines);
                font.lineHeight = Integer.parseInt(readAttribute(lines, 1, "lineHeight"));
                font.size = Integer.parseInt(readAttribute(lines, 0, "size"));
                font.scaleW = Integer.parseInt(readAttribute(lines, 1, "scaleW"));
                font.scaleH = Integer.parseInt(readAttribute(lines, 1, "scaleH"));
                return font;

            } catch (IOException | ImageNotLoadedException e) {
                throw new RuntimeException(e);
            }
        }

        return null;

    }

    public Image getImage() {
        return image;
    }

    public int getLineHeight() {
        return this.lineHeight;
    }

    public int getSize() {
        return this.size;
    }

    public int getScaleW() {
        return scaleW;
    }

    public int getScaleH() {
        return scaleH;
    }

    public CharacterProperties getCharacterProperties(char character) {
        return this.properties.get(character);
    }

    public CharacterProperties[] decode(String text) {

        byte [] content = text.getBytes();
        CharacterProperties[] decoded = new CharacterProperties[content.length];

        for (int i=0; i<content.length; i++) {
            decoded[i] = this.properties.get((char) content[i]);
        }

        return decoded;

    }

    private static Map<String, String> lineToMap(String line) {
        Map<String, String> map = new HashMap<>();
        for (String chunk : line.split(" ")) {
            String [] parts = chunk.split("=");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }

    private static String readAttribute(List<String> lines, int line, String key) {
        return lineToMap(lines.get(line)).get(key).replaceAll("\"", "");
    }

    private static Image readImage(List<String> lines, Path directory) throws ImageNotLoadedException {
        return Image.of(directory.resolve(readAttribute(lines, 2, "file")).toString());
    }

    private static int readLineHeight(List<String> lines) throws ImageNotLoadedException {
        Map<String, String> attributes = lineToMap(lines.get(1));
        return Integer.parseInt(attributes.get("lineHeight"));
    }

    private static int readSize(List<String> lines) throws ImageNotLoadedException {
        Map<String, String> attributes = lineToMap(lines.getFirst());
        return Integer.parseInt(attributes.get("size"));
    }

    private static Map<Character, CharacterProperties> readCharacters(List<String> lines) {

        String line = lines.get(3);
        int sepIndex = line.indexOf('=');
        int charCount = Integer.parseInt(line.substring(sepIndex + 1));

        Map<Character, CharacterProperties> properties = new HashMap<>();
        for (int lineIdx = 4; lineIdx < (4 + charCount); lineIdx++) {

            line = lines.get(lineIdx);
            line = line.replaceAll("\\s+", " ");

            String [] lineChunks = line.split(" ");

            properties.put(
                (char) extractValue(lineChunks[1]),
                new CharacterProperties(
                    (char) extractValue(lineChunks[1]),
                    extractValue(lineChunks[1]),
                    extractValue(lineChunks[2]),
                    extractValue(lineChunks[3]),
                    extractValue(lineChunks[4]),
                    extractValue(lineChunks[5]),
                    extractValue(lineChunks[6]),
                    extractValue(lineChunks[7]),
                    extractValue(lineChunks[8])
                )
            );

        }

        return properties;
    }

    private static int extractValue(String lineChunk) {
        int sepIndex = lineChunk.indexOf('=');
        return Integer.parseInt(lineChunk.substring(sepIndex + 1));
    }
    public record CharacterProperties(char cId, int id, int x, int y, int width, int height, int xOffset, int yOffset, int xAdvance) {}

}

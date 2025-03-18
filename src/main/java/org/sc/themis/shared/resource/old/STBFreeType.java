package org.sc.themis.shared.resource.old;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.sc.themis.shared.resource.Image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.stb.STBTruetype.*;

public class STBFreeType {

  public final static STBFreeType INSTANCE;

  private final static char firstChar = ' ';
  private final static int nbChar = 95;

  static {
    try {
      INSTANCE = of(64, "./src/main/resources/playground/font/CenturyGothic.ttf");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final Map<Character, FontCharacter> fontCharacters = new HashMap<>();
  private final String filename;
  private final ByteBuffer data;

  private Image fontTexture;

  private int fontsize = 12;
  private int ascent;
  private int descent;
  private int lineGap;

  public static STBFreeType of(int fontsize, String filename) throws IOException {
    STBFreeType freetype = new STBFreeType(filename, readfile(filename));
    freetype.fontsize = fontsize;
    load(freetype);
    return freetype;
  }

  public STBFreeType(String filename, ByteBuffer data) {
    this.filename = filename;
    this.data = data;
  }

  public int size() {
    return this.fontCharacters.size();
  }

  public Image getFontTexture() {
    return this.fontTexture;
  }

  public FontCharacter getCharacter(char c) {
    return this.fontCharacters.get(c);
  }

  public Map<Character, FontCharacter> fontCharacters() {
    return fontCharacters;
  }

  public String filename() {
    return filename;
  }

  public ByteBuffer data() {
    return data;
  }

  public int ascent() {
    return ascent;
  }

  public int descent() {
    return descent;
  }

  public int lineGap() {
    return lineGap;
  }

  public FontCharacter[] decode(String text) {

    byte[] content = text.getBytes();
    FontCharacter[] result = new FontCharacter[content.length];

    for (int i = 0; i < content.length; i++) {
      result[i] = this.fontCharacters.get((char) content[i]);
    }

    return result;

  }

  private static ByteBuffer readfile(String filename) throws IOException {
    byte[] data = Files.readAllBytes(Paths.get(filename));
    ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
    buffer.put(data);
    buffer.flip();
    return buffer;
  }

  private static void load(STBFreeType freetype) {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      STBTTFontinfo info = STBTTFontinfo.malloc(stack);
      stbtt_InitFont(info, freetype.data);

      loadInfo(stack, freetype, info);
      loadBitmap(stack, freetype, info);

    }

  }

  //https://github.com/shreyaspranav/stb-truetype-example/blob/main/Main.cpp
  //https://stackoverflow.com/questions/48022431/translating-c-library-to-java-getting-mangled-garbage-data-in-top-left-of-resul
  private static void loadBitmap(MemoryStack stack, STBFreeType freetype, STBTTFontinfo info) {

    /** localstate **/
    int atlasWidth = 512;
    int atlasHeight = 512;
    STBTTPackedchar.Buffer cdata = STBTTPackedchar.malloc(nbChar);
    STBTTAlignedQuad.Buffer alignedQuads = STBTTAlignedQuad.malloc(nbChar);

    STBTTPackRange.Buffer ranges = STBTTPackRange.malloc(nbChar);
    STBRPRect.Buffer rects = STBRPRect.malloc(nbChar);

    ByteBuffer backend = BufferUtils.createByteBuffer(atlasWidth * atlasHeight);

    STBTTPackContext context = STBTTPackContext.calloc(stack);
    stbtt_PackBegin(context, backend, atlasWidth, atlasHeight, 0, 1);
    stbtt_PackFontRange(context, freetype.data, 0, freetype.fontsize, firstChar, cdata);
    stbtt_PackEnd(context);

    stbi_write_png("c:/fontAtlas.png", atlasWidth, atlasHeight, 1, backend, atlasWidth);

    freetype.fontTexture = Image.of(backend, atlasWidth, atlasHeight);

    for (int i = 0; i < nbChar; i++) {

      stbtt_GetPackedQuad(
          cdata, atlasWidth, atlasHeight,
          i, new float[] {1.0f}, new float[] {1.0f},
          alignedQuads.get(i), false
      );


      STBTTPackedchar packedChar = cdata.get(i);
      STBTTAlignedQuad alignedQuad = alignedQuads.get(i);
      char currentChar = (char) (firstChar + i);

      freetype.fontCharacters.put(
          currentChar,
          new FontCharacter(
              currentChar, packedChar.xadvance(),
              alignedQuad.x1() - alignedQuad.x0(), alignedQuad.y1() - alignedQuad.y0(),
              alignedQuad.s0(), alignedQuad.t0(),
              alignedQuad.s1(), alignedQuad.t1(),
              packedChar.xoff(), packedChar.yoff()
          )
      );

    }

  }

  private static void loadInfo(MemoryStack stack, STBFreeType freetype, STBTTFontinfo info) {
    IntBuffer pAscent = stack.mallocInt(1);
    IntBuffer pDescent = stack.mallocInt(1);
    IntBuffer pLineGap = stack.mallocInt(1);
    stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);
    freetype.ascent = pAscent.get(0);
    freetype.descent = pDescent.get(0);
    freetype.lineGap = pLineGap.get(0);
  }

}
package org.sc.themis.shared.resource;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

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

  /*
  public final static char[] characters = new char[]{
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
  };
   */

  private final static char firstChar = ' ';
  private final static int nbChar = 95;

  static {
    try {
      INSTANCE = of(18, "./src/main/resources/playground/font/CenturyGothic.ttf");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public final static record FontCharacter(
      char character, float advance,
      float width, float height,
      float u0, float v0, float u1, float v1,
      float xOffset, float yOffset
  ) {
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
  private static void loadBitmap(MemoryStack stack, STBFreeType freetype, STBTTFontinfo info) {

    /** localstate **/
    int atlasWidth = 512;
    int atlasHeight = 512;
    STBTTPackedchar.Buffer cdata = STBTTPackedchar.malloc(nbChar);
    STBTTAlignedQuad.Buffer alignedQuads = STBTTAlignedQuad.malloc(nbChar);

    ByteBuffer backend = BufferUtils.createByteBuffer(atlasWidth * atlasHeight);

    STBTTPackContext context = STBTTPackContext.calloc(stack);
    stbtt_PackBegin(context, backend, atlasWidth, atlasHeight, 0, 1);
    stbtt_PackFontRange(context, freetype.data, 0, freetype.fontsize, firstChar, cdata);
    stbtt_PackEnd(context);

    freetype.fontTexture = Image.of(backend, atlasWidth, atlasHeight);

    for (int i = 0; i < nbChar; i++) {

      stbtt_GetPackedQuad(
          cdata, atlasWidth, atlasHeight,
          i, new float[] {1.0f}, new float[] {1.0f},
          alignedQuads.get(i), false
      );


      STBTTPackedchar packedChar = cdata.get(i);
      STBTTAlignedQuad alignedQuad = alignedQuads.get(i);

      freetype.fontCharacters.put(
          (char) (firstChar + i),
          new FontCharacter(
              (char) (firstChar + i), packedChar.xadvance(),
              alignedQuad.x1() - alignedQuad.x0(), alignedQuad.y1() - alignedQuad.y0(),
              alignedQuad.s0(), alignedQuad.t0(),
              alignedQuad.s1(), alignedQuad.t1(),
              packedChar.xoff(), packedChar.yoff()
          )
      );

      System.out.println((char) (firstChar + i));
      System.out.println(alignedQuads.get(i).s0() + "/" + alignedQuads.get(i).t0());
      System.out.println(alignedQuads.get(i).s1() + "/" + alignedQuads.get(i).t1());
      System.out.println(alignedQuads.get(i).x0() + "/" + alignedQuads.get(i).y0());
      System.out.println(alignedQuads.get(i).x1() + "/" + alignedQuads.get(i).y1());
    }

    stbi_write_png("c:/fontAtlas.png", atlasWidth, atlasHeight, 1, backend, atlasWidth);


    /*
    stbtt_BakeFontBitmap(freetype.data, 12, backend, 512, 512, 'a', cdata);

    int counter = 0;
    while (backend.hasRemaining()) {
      System.out.print(backend.getFloat() + " ");
      counter++;

      if (counter % 4 == 0) {
        System.out.println();
        System.out.flush();
      }
    }

    freetype.fontTexture = Image.of(backend, 512, 512);

    for (int i=0; i<96; i++) {
      STBTTBakedChar character = cdata.get(i);
      System.out.println(
          "x0="+character.x0()+
              ", y0="+character.y0()+
              ",advance="+character.xadvance()+
              ",xOffset="+character.xoff()+
              ",yOffset="+character.yoff()
      );
    }
  */
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
/**
  private static void loadCharacter(MemoryStack stack, STBFreeType freetype, char character, STBTTFontinfo info) {

    ByteBuffer bitmap = BufferUtils.createByteBuffer(512 * 512);
    STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(96);
    stbtt_BakeFontBitmap(freetype.data, 16, bitmap, 512, 512, 32, cdata);

    IntBuffer pWidth = stack.mallocInt(1);
    IntBuffer pHeight = stack.mallocInt(1);
    IntBuffer pXOffset = stack.mallocInt(1);
    IntBuffer pYOffset = stack.mallocInt(1);
    //ByteBuffer data = stbtt_GetCodepointBitmap(
    //    info, 0,
    //    1, //stbtt_ScaleForPixelHeight(info, freetype.fontsize),
    //    character, pWidth, pHeight, pXOffset, pYOffset);
    FontCharacter fCharacter = new FontCharacter(
        character,
        512, 512, pXOffset.get(0), pYOffset.get(0),
        Image.of(bitmap, 512, 512)
    );

    System.out.println(fCharacter);

    freetype.fontCharacters.put(character, fCharacter);

  }
*/
}
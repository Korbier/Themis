package org.sc.themis.shared.resource.old;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.sc.themis.shared.resource.Image;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.util.freetype.FreeType.*;

public class FreeType {

  public final static FreeType INSTANCE;

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

  public static FreeType of(int fontsize, String filename) throws IOException {
    FreeType freetype = new FreeType(filename, readfile(filename));
    freetype.fontsize = fontsize;
    load(freetype);
    return freetype;
  }

  public FreeType(String filename, ByteBuffer data) {
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

  private static void load(FreeType freetype) {

    try (MemoryStack stack = MemoryStack.stackPush()) {

      PointerBuffer pLibrary = stack.mallocPointer(1);
      int err = FT_Init_FreeType(pLibrary);

      if (err != FT_Err_Ok) {
        throw new IllegalStateException("Failed to initialize FreeType: " + FT_Error_String(err));
      }

      long library = pLibrary.get(0);

      IntBuffer major = stack.mallocInt(1);
      IntBuffer minor = stack.mallocInt(1);
      IntBuffer patch = stack.mallocInt(1);

      FT_Library_Version(library, major, minor, patch);
      System.out.println("Loaded FreeType " + major.get(0) + "." + minor.get(0) + "." + patch.get(0));

      String path  = "C:\\Users\\Public\\Workspace\\001_Themis_V2\\src\\main\\resources\\playground\\font\\CenturyGothic.ttf";
      ByteBuffer buffer = BufferUtils.createByteBuffer(path.getBytes().length + 1);
      buffer.put(path.getBytes());
      buffer.put((byte) 0);
      buffer.flip();

      PointerBuffer pFace = stack.mallocPointer(1);
      err = FT_New_Face(library, buffer, 0, pFace);

      if (err != FT_Err_Ok) {
        throw new IllegalStateException("Failed to initialize Face: " + FT_Error_String(err));
      }

      FT_Face face = FT_Face.create(pFace.get(0));
      FT_Set_Pixel_Sizes(face, 0, 48);

      int glyphIndex = FT_Get_Char_Index(face, 'M');

      FT_Load_Glyph(face, glyphIndex, FT_LOAD_DEFAULT);
      FT_Render_Glyph(face.glyph(), FT_RENDER_MODE_NORMAL);

      FT_GlyphSlot slot = face.glyph();

      System.out.println("Bitmap.width="+slot.bitmap().width());
      System.out.println("left="+slot.bitmap_left());
      System.out.println("top="+slot.bitmap_top());

      //Bitmap.width=44 left=-6 top=34

      FT_Done_Face(face);
      FT_Done_FreeType(library);


    }

  }

}
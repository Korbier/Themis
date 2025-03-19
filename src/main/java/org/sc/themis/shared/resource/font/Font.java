package org.sc.themis.shared.resource.font;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.util.freetype.FreeType.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.sc.themis.shared.resource.Image;

//https://levelup.gitconnected.com/how-to-create-a-bitmap-font-with-freetype-58e8c31878a9
public class Font {

  private static final char firstChar = (char) 32;
  private static final int nbChar = 95;

  private Map<Character, FontCharacter> characters;
  private final int size;
  private final Path font;

  private final boolean sdf;
  private final float sdfWidth;
  private final float sdfEdge;

  private ByteBuffer bitmapBuffer;
  private Image texture;
  private int bitmapPadding = 32;
  private int bitmapCols = 16;
  private int bitmapRows = 16;

  public static Font normal(int size, Path font) {
    return new Font(size, false, 0.0f, 0.0f, font);
  }

  public static Font sdf(int size, float sdfWidth, float sdfEdge, Path font ) {
    return new Font(size, true, sdfWidth, sdfEdge, font);
  }

  private Font(int size, boolean sdf, float sdfWidth, float sdfEdge, Path font) {
    this.size = size;
    this.sdf = sdf;
    this.sdfWidth = sdfWidth;
    this.sdfEdge = sdfEdge;
    this.font = font;
    setup();
  }

  public int getFontSize() {
    return this.size;
  }

  public boolean isSdfFont() {
    return this.sdf;
  }

  public float getSdfWidth() {
    return this.sdfWidth;
  }

  public float getSdfEdge() {
    return this.sdfEdge;
  }

  public FontCharacter[] toCharacters(String text) {

    byte[] input = text.getBytes();
    FontCharacter[] characters = new FontCharacter[input.length];

    for (int i = 0; i < input.length; i++) {
      characters[i] = this.characters.get((char) input[i]);
    }

    return characters;

  }

  public Image getTexture() {
    return this.texture;
  }

  private void setup() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      long library = fetchLibrary(stack);
      ByteBuffer filename = filenameToBuffer(this.font);
      FT_Face face = setupFace(stack, library, filename);
      createBitmapBuffer(face, firstChar, nbChar);
    }
  }

  private FT_Face setupFace(MemoryStack stack, long library, ByteBuffer filename) {

    PointerBuffer pFace = stack.mallocPointer(1);
    int err = FT_New_Face(library, filename, 0, pFace);

    if (err != FT_Err_Ok) {
      throw new IllegalStateException("Failed to initialize Face: " + FT_Error_String(err));
    }

    FT_Face face = FT_Face.create(pFace.get(0));
    FT_Set_Pixel_Sizes(face, 0, this.size);
    return face;

  }

  private ByteBuffer filenameToBuffer(Path font) {
    String path  = font.toAbsolutePath().toString();
    return BufferUtils
        .createByteBuffer(path.getBytes().length + 1)
        .put(path.getBytes())
        .put((byte) 0)
        .flip();
  }

  private long fetchLibrary(MemoryStack stack) {

    PointerBuffer pLibrary = stack.mallocPointer(1);
    int err = FT_Init_FreeType(pLibrary);

    if (err != FT_Err_Ok) {
      throw new IllegalStateException("Failed to initialize FreeType: " + FT_Error_String(err));
    }

    return pLibrary.get(0);

  }

  private void createBitmapBuffer(FT_Face face, char firstChar, int nbChar) {

    int imageWidth  = (64 + this.bitmapPadding) * this.bitmapCols;//(this.size + this.bitmapPadding) * this.bitmapCols;
    int imageHeight = (64 + this.bitmapPadding) * this.bitmapRows;//(this.size + this.bitmapPadding) * this.bitmapRows;

    this.characters = new HashMap<>();
    this.bitmapBuffer = BufferUtils.createByteBuffer(imageWidth * imageHeight + bitmapPadding);

    for (int i = 0; i < nbChar; i++) {

      char currentChar = (char) (firstChar + i);
      int glyphIndex = FT_Get_Char_Index(face, currentChar);

      int error = FT_Load_Glyph(face, glyphIndex, FT_LOAD_DEFAULT);
      if (error != FT_Err_Ok) {
        throw new IllegalStateException("Failed to initialize Face: " + FT_Error_String(error));
      }

      // convert to an anti-aliased bitmap
      error = FT_Render_Glyph(face.glyph(), this.sdf ? FT_RENDER_MODE_SDF : FT_RENDER_MODE_NORMAL);
      if (error != FT_Err_Ok) {
        throw new IllegalStateException("Failed to initialize Face: " + FT_Error_String(error));
      }

      FT_GlyphSlot glyph = face.glyph();
      FT_Bitmap    charBitmap = glyph.bitmap();
      int charWidth = charBitmap.width();
      int charHeight = charBitmap.rows();

      ByteBuffer charBitmapBuffer = charBitmap.buffer(charWidth * charHeight);


      int x = (i % this.bitmapCols) * (this.size + this.bitmapPadding);
      int y = (i / this.bitmapCols) * (this.size + this.bitmapPadding);

      x += 1;
      y += 1;

      // System.out.println("Character = " + currentChar + " => " +  face.glyph().metrics().height() / 64 + ":" + face.glyph().metrics().horiBearingY() / 64);
      // System.out.println("(x,y)=" + x + "x" + y);

      FontCharacter fontChar = new FontCharacter(
          currentChar,
          new Vector2i(charBitmap.width(), charBitmap.rows()),
          new Vector2i(glyph.bitmap_left(), glyph.bitmap_top()),
          new Vector2f((float) x / imageWidth, (float) y / imageHeight),
          new Vector2f((float) (x + charBitmap.width()) / imageWidth, (float) (y + charBitmap.rows()) / imageHeight),
          glyph.advance().x() / 64
      );

      this.characters.put(currentChar, fontChar);

      if (charBitmapBuffer != null) {

        //  stbi_write_png("C:/Users/Public/Workspace/001_Themis_V2/target/" + (int) currentChar + "_" + currentChar + ".png", charWidth, charHeight, 1, buffer, charWidth);

        for (int j = 0; j < charHeight; j++) {

          int srcStart  = j * charWidth;
          int srcLength = charWidth;

          this.bitmapBuffer.put(x + ((y + j) * imageWidth), charBitmapBuffer, srcStart, srcLength);

        }

      }

    }

    this.texture = Image.of(this.bitmapBuffer, imageWidth, imageHeight);

    // stbi_write_png("C:/Users/Public/Workspace/001_Themis_V2/target/" + this.size + "_" + this.sdf + "_" + this.font.getFileName().toString() + ".png", imageWidth, imageHeight, 1, this.bitmapBuffer, imageWidth);

  }

}

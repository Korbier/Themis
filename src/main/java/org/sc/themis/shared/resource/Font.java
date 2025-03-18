package org.sc.themis.shared.resource;

import static org.lwjgl.stb.STBImageWrite.stbi_write_png;
import static org.lwjgl.util.freetype.FreeType.*;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Glyph_Metrics;

//https://levelup.gitconnected.com/how-to-create-a-bitmap-font-with-freetype-58e8c31878a9
public class Font {

  private static final char firstChar = (char) 32;
  private static final int nbChar = 95;

  private Map<Character, FontCharacter> characters;
  private final int size;
  private final Path font;
  private final boolean sdf;

  private ByteBuffer bitmapBuffer;
  private int bitmapPadding = 32;
  private int bitmapCols = 16;
  private int bitmapRows = 16;

  public Font(int size, boolean sdf, Path font) {
    this.size = size;
    this.sdf = sdf;
    this.font = font;
  }

  public void setup() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      long library = fetchLibrary(stack);
      ByteBuffer filename = filenameToBuffer(this.font);
      FT_Face face = setupFace(stack, library, filename);
      this.characters = readCharacters(face, firstChar, nbChar);
      this.bitmapBuffer = createBitmapBuffer(face, firstChar, nbChar);
    }
  }

  private ByteBuffer createBitmapBuffer(FT_Face face, char firstChar, int nbChar) {

    int imageWidth  = (this.size + this.bitmapPadding) * this.bitmapCols;
    int imageHeight = (this.size + this.bitmapPadding) * this.bitmapRows;

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

      int x = (i % this.bitmapCols) * (this.size + this.bitmapPadding);
      int y = (i / this.bitmapCols) * (this.size + this.bitmapPadding);

      x += 1;
      y += 1;

      FT_Bitmap bitmap = face.glyph().bitmap();
      int charWidth = bitmap.width();
      int charHeight = bitmap.rows();

      ByteBuffer buffer = bitmap.buffer(charWidth * charHeight);

      if (buffer != null) {

        stbi_write_png("C:/Users/Public/Workspace/001_Themis_V2/target/" + (int) currentChar + "_" + currentChar + ".png", charWidth, charHeight, 1, buffer, charWidth);

        for (int j = 0; j < charHeight; j++) {

          int srcStart  = j * charWidth;
          int srcLength = charWidth;

          this.bitmapBuffer.put(x + ((y + j) * imageWidth), buffer, srcStart, srcLength);

        }

      }

    }

    stbi_write_png("C:/Users/Public/Workspace/001_Themis_V2/target/atlas.png", imageWidth, imageHeight, 1, this.bitmapBuffer, imageWidth);

    return this.bitmapBuffer;

  }

  public FontCharacter[] toCharacters(String text) {

    byte[] input = text.getBytes();
    FontCharacter[] characters = new FontCharacter[input.length];

    for (int i = 0; i < input.length; i++) {
      characters[i] = this.characters.get(input[i]);
    }

    return characters;

  }

  private Map<Character, FontCharacter> readCharacters(FT_Face face, char firstChar, int nbChar) {

    Map<Character, FontCharacter> characters = new HashMap<>();

    for (int i = 0; i < nbChar; i++) {
      char currentChar = (char) (firstChar + i);
      characters.put(currentChar, readCharacter(face, currentChar));
    }

    return characters;

  }

  private FontCharacter readCharacter(FT_Face face, char currentChar) {

    int glyphIndex = FT_Get_Char_Index(face, currentChar);

    FT_Load_Glyph(face, glyphIndex, FT_LOAD_DEFAULT);
    FT_Render_Glyph(face.glyph(), this.sdf ? FT_RENDER_MODE_SDF : FT_RENDER_MODE_NORMAL);
    FT_GlyphSlot slot = face.glyph();

    ByteBuffer buffer = slot.bitmap().buffer(slot.bitmap().width() * slot.bitmap().rows());

    FontCharacter fontChar = new FontCharacter(
        currentChar,
        buffer != null ? Image.of(buffer, slot.bitmap().width(), slot.bitmap().rows()) : null,
        new Vector2i(slot.bitmap().width(), slot.bitmap().rows()),
        new Vector2i(slot.bitmap_left(), slot.bitmap_top()),
        slot.advance().x() / 64
    );

    //if (buffer != null) {
    //  stbi_write_png("C:/Users/Public/Workspace/001_Themis_V2/target/" + currentChar + ".png", fontChar.size().x, fontChar.size().y, 1, buffer, fontChar.size().x);
    //}

    return fontChar;
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

}

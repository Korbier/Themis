package org.sc.themis.shared.resource;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Memory;
import org.lwjgl.util.freetype.FreeType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FontFreeType {

  public static void main(String[] args) {
    of("./src/main/resources/playground/font/CenturyGothic.ttf");
  }

  public final static void of(String filename) {
    try {
      ByteBuffer fontbuffer = readfile(filename);

      try (MemoryStack stack = MemoryStack.stackPush(); FT_Memory memory = FT_Memory.create()) {

        PointerBuffer pLibraryBuffer = stack.callocPointer(1);
        PointerBuffer pFaceBuffer = stack.callocPointer(1);

        FreeType.FT_New_Library(memory, pLibraryBuffer);
        FreeType.FT_Init_FreeType(pLibraryBuffer);
/*
        FreeType.FT_New_Face(pLibraryBuffer.get(0), fontbuffer, 0, pFaceBuffer );
        FT_Face face = FreeType.
        System.out.println("Nombre de symboles = " + face.num_glyphs());
        System.out.println("Ascender = " + face.ascender());


        for (int c = 48; c < 50; c++) {

          int index = FreeType.FT_Get_Char_Index(face, 48);

          FreeType.FT_Load_Glyph(face, index, FreeType.FT_LOAD_DEFAULT);

          FT_GlyphSlot slot = face.glyph();
          FreeType.FT_Render_Glyph(slot, FreeType.FT_RENDER_MODE_NORMAL);

          int bLeft = slot.bitmap_left();
          int bTop = slot.bitmap_top();
          long bWidth = slot.metrics().width() / 64;
          long bHeight = slot.metrics().height() / 64;
          long bAdvance = slot.advance().x() / 64;

          System.out.println((char) c + " (left=" + bLeft + ", top=" + bTop + ")");
        }
*/
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }


  private static ByteBuffer readfile(String filename) throws IOException {
    byte[] data = Files.readAllBytes(Paths.get(filename));
    ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
    buffer.put(data);
    buffer.flip();
    return buffer;
  }
}

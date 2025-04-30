package org.sc.themis.renderer.material;

import org.sc.themis.renderer.Renderer;
import org.sc.themis.shared.exception.ThemisException;

public interface MaterialVariantSetter<O> {

  void set(Renderer renderer, MaterialVariant<O> variant, O value) throws ThemisException;

}

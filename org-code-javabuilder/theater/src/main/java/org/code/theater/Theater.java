package org.code.theater;
import org.checkerframework.checker.mustcall.qual.Owning;

public final class Theater {
  public static @Owning Stage stage;
  public static final Prompter prompter = new Prompter();

  protected Theater() {
    stage = new Stage();
    stage.play();
  }
}

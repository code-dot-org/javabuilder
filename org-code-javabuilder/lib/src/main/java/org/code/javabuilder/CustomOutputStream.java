package org.code.javabuilder;

import java.io.PrintStream;

public class CustomOutputStream extends PrintStream {
  private final OutputAdapter outputAdapter;
  public CustomOutputStream(java.io.OutputStream out, OutputAdapter outputAdapter) {
    super(out, true);
    this.outputAdapter = outputAdapter;
  }

  @Override
  public void print(boolean b)
  {
    outputAdapter.sendMessage(String.valueOf(b));
    super.print(b);
  }

  @Override
  public void print(char c)
  {
    outputAdapter.sendMessage(String.valueOf(c));
    super.print(c);
  }

  @Override
  public void print(int i)
  {
    outputAdapter.sendMessage(String.valueOf(i));
    super.print(i);
  }

  @Override
  public void print(long l)
  {
    outputAdapter.sendMessage(String.valueOf(l));
    super.print(l);
  }

  @Override
  public void print(float f)
  {
    outputAdapter.sendMessage(String.valueOf(f));
    super.print(f);
  }

  @Override
  public void print(double d)
  {
    outputAdapter.sendMessage(String.valueOf(d));
    super.print(d);
  }

  @Override
  public void print(char[] s)
  {
    outputAdapter.sendMessage(String.valueOf(s));
    super.print(s);
  }

  @Override
  public void print(String s)
  {
    if(s == null) {
      outputAdapter.sendMessage("null");
    } else {
      outputAdapter.sendMessage(s);
    }
    super.print(s);
  }

  @Override
  public void print(Object obj)
  {
    outputAdapter.sendMessage(String.valueOf(obj));
    super.print(obj);
  }

  @Override
  public void println()
  {
    this.print(System.lineSeparator());
    super.println();
  }

  @Override
  public void println(boolean x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(char x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(int x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(long x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(float x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(double x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(char[] x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(String x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }

  @Override
  public void println(Object x)
  {
    this.print(x + System.lineSeparator());
    super.println(x);
  }
}

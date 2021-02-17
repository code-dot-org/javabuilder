package org.code.codebuilderapp;

import java.security.Principal;

/**
 * A principal associated with a single anonymous session.
 */
public class AnonymousUser implements Principal {

  String name;

  AnonymousUser(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}

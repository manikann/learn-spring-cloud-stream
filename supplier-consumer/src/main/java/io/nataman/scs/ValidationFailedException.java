package io.nataman.scs;

public class ValidationFailedException extends RuntimeException {

  public ValidationFailedException(String error) {
    super(error);
  }
}

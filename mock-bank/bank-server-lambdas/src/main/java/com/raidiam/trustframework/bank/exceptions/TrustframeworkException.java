package com.raidiam.trustframework.bank.exceptions;

/**
 * Exception to be raised when there is no alternative to proceed execution. Examples include a non-valid identifier passed, or no records could be found for the combination passed
 */
public class TrustframeworkException extends RuntimeException {
  public TrustframeworkException() {
  }

  public TrustframeworkException(String message) {
    super(message);
  }

  public TrustframeworkException(String message, Throwable cause) {
    super(message, cause);
  }

  public TrustframeworkException(Throwable cause) {
    super(cause);
  }

  public TrustframeworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

package com.raidiam.trustframework.bank;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.raidiam.trustframework.bank.exceptions.TrustframeworkException;
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamLambdaHandler implements RequestStreamHandler {
  private static final Logger log = LoggerFactory.getLogger(StreamLambdaHandler.class);

  private static MicronautLambdaContainerHandler handler;

  static {
    try {
      handler = new MicronautLambdaContainerHandler();
      log.info("Handler created");
    } catch (ContainerInitializationException e) {
      // if we fail here. We re-throw the exception to force another cold start
      throw new TrustframeworkException("Could not initialize Micronaut", e);
    }
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
    log.info("Request started");
    handler.proxyStream(inputStream, outputStream, context);
    log.info("Request finished");
  }
}

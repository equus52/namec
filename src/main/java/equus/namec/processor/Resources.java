package equus.namec.processor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Resources {
  private static final int BUF_SIZE = 0x800;

  private Resources() {}

  public static String loadText(Object context, String fileName) {
    URL url = context.getClass().getResource(fileName);
    return loadText(url, StandardCharsets.UTF_8);
  }

  private static String loadText(URL url, Charset charset) {
    try (BufferedInputStream bis = new BufferedInputStream(url.openStream(), BUF_SIZE)) {
      byte[] bytes = new byte[BUF_SIZE];
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream(BUF_SIZE);
      int len = 0;
      while ((len = bis.read(bytes, 0, BUF_SIZE)) > 0) {
        byteStream.write(bytes, 0, len);
      }
      return new String(byteStream.toByteArray(), charset);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}

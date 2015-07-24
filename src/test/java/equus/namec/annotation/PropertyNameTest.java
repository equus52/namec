package equus.namec.annotation;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class PropertyNameTest {
  @Test
  public void test_CLASS_NAME() {
    assertThat(PropertyName.CLASS_NAME, is(PropertyName.class.getName()));
  }
}

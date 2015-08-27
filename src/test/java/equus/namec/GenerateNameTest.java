package equus.namec;

import static org.assertj.core.api.StrictAssertions.*;

import org.junit.Test;

public class GenerateNameTest {
  @Test
  public void test_CLASS_NAME() {
    assertThat(GenerateName.CLASS_NAME).isEqualTo(GenerateName.class.getName());
  }
}

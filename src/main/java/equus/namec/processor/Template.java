package equus.namec.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Simple template like Handlebars.
 */
public class Template {

  private static final Pattern VARIABLE = Pattern.compile("\\{\\{(.*?)\\}\\}");
  private static final Pattern COLLECTION = Pattern.compile("\\{\\{#each (.*?)\\}\\}(.*?)\\{\\{/each\\}\\}",
      Pattern.DOTALL);
  private static final String LINE_SEPARATOR_PATTERN = "\r\n|[\n\r\u2028\u2029\u0085]";

  private final String template;

  public Template(String template) {
    this.template = template;
  }

  public String apply(Object context) {
    return apply(template, new Context(context));
  }

  private String apply(String template, Context context) {
    String convertedTemplate = convertCollection(template, context);
    return convertVariable(convertedTemplate, context);
  }

  private String convertCollection(String template, Context context) {
    StringBuffer buffer = new StringBuffer();
    Matcher matcher = COLLECTION.matcher(template);
    while (matcher.find()) {
      String key = matcher.group(1);
      String body = matcher.group(2);
      matcher.appendReplacement(buffer, expandCollection(context, key, body));
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private String expandCollection(Context context, String key, String body) {
    Object list = context.get(key);
    if (list == null) {
      return "";
    }
    if (!Iterable.class.isAssignableFrom(list.getClass())) {
      return "";
    }
    String trimedBody = trimLineSeparator(body);
    Stream<?> stream = StreamSupport.stream(((Iterable<?>) list).spliterator(), false);
    return stream.map(e -> apply(trimedBody, new Context(e))).collect(Collectors.joining(System.lineSeparator()));
  }

  private String trimLineSeparator(String body) {
    body = body.replaceAll("^" + LINE_SEPARATOR_PATTERN, "");
    body = body.replaceAll(LINE_SEPARATOR_PATTERN + "$", "");
    return body;
  }

  private String convertVariable(String template, Context context) {
    Matcher matcher = VARIABLE.matcher(template);
    StringBuffer buffer = new StringBuffer();
    while (matcher.find()) {
      String match = matcher.group(1);
      matcher.appendReplacement(buffer, context.get(match).toString());
    }
    matcher.appendTail(buffer);
    return buffer.toString();
  }

  private static class Context {

    private final Object model;

    public Context(Object model) {
      this.model = model;
    }

    public Object get(String key) {
      return new BeanAccessor(model).get(key);
    }
  }
}

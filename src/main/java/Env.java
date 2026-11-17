import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 설정값(API 키 등)을 읽는 클래스.
 * 시스템 환경변수를 먼저 보고, 없으면 프로젝트 폴더의 .env 파일에서 읽는다.
 *
 * <pre>
 * String apiKey = Env.require("GEMINI_API_KEY");
 * </pre>
 */
public class Env {

    private static final Path ENV_FILE = Path.of(".env");
    private static final Map<String, String> FILE_VALUES = loadFile();

    private Env() {
    }

    /** 값이 없으면 null 을 돌려준다. */
    public static String get(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            value = FILE_VALUES.get(name);
        }
        return (value == null || value.isBlank()) ? null : value;
    }

    /** 값이 없으면 무엇을 해야 하는지 알려 주는 예외를 던진다. */
    public static String require(String name) {
        String value = get(name);
        if (value == null) {
            throw new IllegalStateException(
                    name + " 값이 없습니다. 프로젝트 폴더의 .env 파일에 " + name + "=키값 을 적거나 환경변수로 설정하세요.");
        }
        return value;
    }

    private static Map<String, String> loadFile() {
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(ENV_FILE)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(ENV_FILE, StandardCharsets.UTF_8)) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).strip();
                String value = line.substring(eq + 1).strip();
                if (value.length() >= 2
                        && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
        } catch (IOException e) {
            throw new IllegalStateException(".env 파일을 읽을 수 없습니다: " + e.getMessage(), e);
        }
        return values;
    }
}

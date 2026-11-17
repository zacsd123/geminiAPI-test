import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 텍스트를 txt 파일로 저장하는 클래스. 항상 UTF-8 로 저장한다.
 *
 * <pre>
 * TxtSaver.save("내용", "output/result.txt");
 * TxtSaver.saveAll(List.of(result1, result2), "output/results.txt");
 * </pre>
 */
public class TxtSaver {

    private static final String DEFAULT_PATH = "output/results.txt";

    private TxtSaver() {
    }

    /** 문자열 하나를 파일로 저장한다. 같은 이름의 파일이 있으면 덮어쓴다. */
    public static Path save(String content, String filePath) throws IOException {
        Path path = Path.of(filePath);
        createParentDirectories(path);
        Files.writeString(path, content == null ? "" : content, StandardCharsets.UTF_8);
        return path;
    }

    /** 문자열 목록을 "N번째:" 머리글과 함께 한 파일에 저장한다. null 항목은 "(실패)"로 적는다. */
    public static Path saveAll(List<String> contents, String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contents.size(); i++) {
            String content = contents.get(i);
            sb.append(i + 1).append("번째:").append(System.lineSeparator());
            sb.append(content == null ? "(실패)" : content).append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        return save(sb.toString(), filePath);
    }

    /** 배열 버전. */
    public static Path saveAll(String[] contents, String filePath) throws IOException {
        return saveAll(java.util.Arrays.asList(contents), filePath);
    }

    /** 경로 없이 목록만 넘기면 기본 경로(output/results.txt)에 저장한다. */
    public static Path saveAll(List<String> contents) throws IOException {
        return saveAll(contents, DEFAULT_PATH);
    }

    /** 경로 없이 배열만 넘기면 기본 경로(output/results.txt)에 저장한다. */
    public static Path saveAll(String[] contents) throws IOException {
        return saveAll(contents, DEFAULT_PATH);
    }

    private static void createParentDirectories(Path path) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

/**
 * 이미지 분석 전용 클래스.
 *
 * <pre>
 * Image image = new Image(apiKey);
 * String result = image.run("src/main/iamge/1.jpg");
 * </pre>
 */
public class Image {

    private static final String DEFAULT_MODEL = "gemini-3.1-flash-lite";
    private static final String DEFAULT_PROMPT = """
            첨부된 이미지는 영수증입니다. 영수증을 읽고 결제 총금액과 구매한 물품별 수량·금액을 추출하세요.

            [추출 규칙]
            - 총금액: 영수증의 최종 결제 금액(할인과 부가세가 모두 반영된 실제 결제액)입니다.
            - 물품: 영수증에 적힌 구매 항목을 위에서 아래 순서대로 하나도 빠뜨리지 말고 모두 포함하세요. 쇼핑백, 종이봉투, 종량제봉투처럼 값을 내고 산 봉투도 구매 항목이므로 반드시 포함합니다. 금액이 0원인 서비스 항목도 포함합니다.
            - 상품명: 영수증에 인쇄된 글자를 한 글자씩 그대로 옮기세요. 비슷해 보이는 글자를 일반적인 제품명으로 바꾸거나 추측해서 고치지 마세요. 상품명 안의 괄호와 기호(예: "크린손)니트릴장갑")는 그대로 두고, 상품명 앞에 붙은 면세 표시 "*"와 "-"는 상품명에서 제외하세요.
            - 수량: 정수 뒤에 "개"를 붙인 문자열로 쓰세요. 예: "2개". 수량이 적혀 있지 않으면 "1개"로 쓰세요.
            - 금액: 영수증의 금액 열에 적힌 해당 물품의 합계 금액(단가 x 수량)입니다. 쉼표, "원", 통화기호 없이 숫자만 쓰세요.
            - 할인, 쿠폰, 포인트, 부가세처럼 상품이 아닌 줄은 물품에 넣지 마세요.
            - 같은 상품명이 두 번 이상 나오면 하나로 합쳐 수량과 금액을 더하세요.
            - 글씨가 흐리거나 읽을 수 없는 값은 추측하지 말고 null로 쓰세요.

            [출력 형식]
            - 아래 JSON 형식 그대로, 물품 한 개당 한 줄로 출력하세요. 키와 문자열에는 큰따옴표(")를 쓰세요.
            - 설명, 인사말, 마크다운 코드블록(```)은 절대 쓰지 말고 JSON만 출력하세요.
            {
              "총금액": 15000,
              "물품": {
                "아메리카노": ["2개", 9000],
                "치즈케이크": ["1개", 6000]
              }
            }

            [예외]
            - 영수증이 아닌 이미지이면 다음만 출력하세요: {"오류": "영수증이 아닙니다"}
            """;

    private final Client client;
    private final String modelName;

    /** 환경변수 GOOGLE_API_KEY 또는 GEMINI_API_KEY 의 키를 사용한다. */
    public Image() {
        this.client = new Client();
        this.modelName = DEFAULT_MODEL;
    }

    /** API 키를 직접 지정한다. */
    public Image(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    /** API 키와 모델명을 직접 지정한다. */
    public Image(String apiKey, String modelName) {
        this.client = Client.builder().apiKey(apiKey).build();
        this.modelName = modelName;
    }

    /** 이미지 경로를 받아 기본 프롬프트로 분석한 결과를 돌려준다. */
    public String run(String imagePath) throws IOException {
        return run(imagePath, DEFAULT_PROMPT);
    }

    /** 이미지 경로와 프롬프트를 받아 분석한 결과를 돌려준다. */
    public String run(String imagePath, String prompt) throws IOException {
        Path path = Path.of(imagePath);
        byte[] imageBytes = Files.readAllBytes(path);

        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(imageBytes, mimeTypeOf(path)));

        GenerateContentResponse response = client.models.generateContent(modelName, content, null);
        return response.text();
    }

    private static String mimeTypeOf(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".gif")) return "image/gif";
        throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + path);
    }
}

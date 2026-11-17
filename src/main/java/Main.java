import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    private static final String API_KEY = "YOUR_API_KEY"; // 여기에 API 키를 입력하세요
    private static final String MODEL_NAME = "gemini-3.1-flash-lite"; // 사용할 모델을 선택하세요

    // 선택 가능한 Gemini 모델:
    // "gemini-pro": 일반적인 프로 모델 (유료)
    // "gemini-pro-vision": 멀티모달 입력을 지원하는 프로 모델 (유료) - 이미지 검색 및 텍스트 생성 기능 포함
    // "gemini-ultra": 최고 성능을 자랑하는 울트라 모델 (유료) - 대규모 데이터 처리 및 복잡한 작업에 최적
    // "gemini-nano": 경량화된 모델 (무료) - 기본적인 텍스트 생성 및 간단한 작업에 적합
    // 요금 정보: https://ai.google.dev/pricing

    public static void main(String[] args) {
        String prompt = "인공지능(AI)이 어떻게 작동하는지 설명해 주세요\n" +
		                "가급적 전문용어보다는 이해하기 쉽도록 설명해 주세요.\n" +
		                "유익한 예제를 추가해주세요.";

        // try {
        //     String response = callGeminiApi(prompt);
        //     System.out.println("---succese---");
        //     System.out.println("Response from API: " + response);
        // } catch (Exception e) {
        //     System.out.println("---fail---");
        //     e.printStackTrace();
        // }

        // 이미지 분석: Image 객체를 만들고 run 에 이미지 경로를 넘기면 결과를 돌려준다
        String[] results = new String[100];
        Image image = new Image(API_KEY);
        for (int i = 0; i < 100; i++) {
            try {
                String result = image.run("src/main/iamge/" + (i % 10 + 1) + ".jpg");
                System.out.println(i+"번째 출력");
                results[i] = result;
                Thread.sleep(5);
            } catch (Exception e) {
                System.out.println("---image fail---");
                e.printStackTrace();
            }
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println((i + 1) + "번째: " + results[i]);
        }

        // results 를 넘기면 output/results.txt 로 바로 저장한다
        try {
            System.out.println("저장 완료: " + TxtSaver.saveAll(results).toAbsolutePath());
        } catch (Exception e) {
            System.out.println("---save fail---");
            e.printStackTrace();
        }
    }

    private static String callGeminiApi(String prompt) throws Exception {
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL_NAME + ":generateContent?key=" + API_KEY;
        URL url = URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // JSON 형식이 올바른지 확인
        String jsonInputString = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                prompt.replace("\n", "\\n")
        );

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) { // 성공적인 응답 코드 200을 체크
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // JSON 응답에서 메시지만 추출
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("candidates")) {
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    StringBuilder message = new StringBuilder();
                    for (int i = 0; i < candidates.length(); i++) {
                        JSONObject content = candidates.getJSONObject(i).getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        for (int j = 0; j < parts.length(); j++) {
                            message.append(parts.getJSONObject(j).getString("text")).append("\n");
                        }
                    }
                    return message.toString().trim();
                } else {
                    return "No candidates found in the response.";
                }
            }
        } else if (responseCode == 401) {
            throw new RuntimeException("Failed : HTTP error code : 401 Unauthorized. Please check your API key and permissions.");
        } else if (responseCode == 400) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                throw new RuntimeException("Failed : HTTP error code : 400 Bad Request. Response: " + response.toString());
            }
        } else {
            throw new RuntimeException("Failed : HTTP error code : " + responseCode);
        }
    }
}

/*
필요한 라이브러리의 Maven 정보:
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20210307</version>
</dependency>
*/

/*
cURL 호출 예제:
curl -H 'Content-Type: application/json' \
     -d '{"contents":[{"parts":[{"text":"Explain how AI works"}]}]}' \
     -X POST 'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=YOUR_API_KEY'
*/
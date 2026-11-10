import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {

    // 프로그램 진입점
    public static void main(String[] args) throws Exception {
        int port = 9990;
        // 포트 바인딩: 8080 포트에서 TCP 연결을 받을 준비
        // 자원 해제: try 블록 종료 시 ServerSocket 자동으로 닫기
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println(port + " 서버 시작");

            // TCP 연결 수락·블로킹: 연결이 들어올 때까지 대기
            // 자원 해제: try 블록 종료 시 연결된 Socket 자동으로 닫기
            try (Socket socket = serverSocket.accept()) {

                // 입력 스트림 + 문자 디코딩·버퍼링
                BufferedReader reader = new BufferedReader(
                        // 받은 바이트를 UTF-8 문자로 디코딩
                        new InputStreamReader(
                                // 클라이언트가 보낸 바이트를 읽는 입력 스트림
                                socket.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                // HTTP 요청 구조: 첫 줄에서 메서드·경로·버전 읽기
                // readLine()은 줄바꿈 문자를 제외한 한 줄을 반환
                String requestLine = reader.readLine();
                System.out.println("요청: " + requestLine);

                // 요청 없이 입력 스트림이 끝났으면 처리 종료
                if (requestLine == null) {
                    return;
                }

                // HTTP 요청 구조: 첫 줄 이후의 헤더를 한 줄씩 읽기
                // 빈 줄은 헤더의 끝을 의미함
                String headerLine;
                while ((headerLine = reader.readLine()) != null
                        && !headerLine.isEmpty()) {
                    System.out.println("헤더: " + headerLine);
                }

                //응답은 3부분으로 나눈다
                String[] requestParts= requestLine.split(" ");

                int statusCode;
                String statusText;
                String responseBody;

                //나눠진 응답이 3개가 아니라면 400 호출
                if(requestParts.length != 3){
                    statusCode = 400;
                    statusText = "Bad Request";
                    responseBody = "잘못된 요청입니다.";
                }else{
                    String method = requestParts[0];
                    String path = requestParts[1];

                    System.out.println("메소드: " + method);
                    System.out.println("경로: " + path);

                    if("/hello".equals(path)){
                        statusCode = 200;
                        statusText = "OK";
                        responseBody = "hello!";
                    }else{
                        statusCode = 404;
                        statusText = "Not Found";
                        responseBody = "찾을 수 없는 경로입니다.";
                    }
                }

                // 문자 인코딩: 응답 본문을 UTF-8 바이트 배열로 변환
                byte[] body =
                        responseBody.getBytes(StandardCharsets.UTF_8);

                // HTTP 응답 구조: 상태 줄 → 헤더 → 빈 줄
                // \r\n은 HTTP에서 사용하는 줄바꿈
                // Content-Length는 응답 본문의 바이트 수
                String responseHeaders =
                        "HTTP/1.1 " + statusCode + " " + statusText + "\r\n"
                                + "Content-Type: text/plain; charset=UTF-8\r\n"
                                + "Content-Length: " + body.length + "\r\n"
                                + "Connection: close\r\n"
                                + "\r\n";

                // 출력 스트림: 클라이언트에게 바이트를 보내는 통로
                OutputStream output = socket.getOutputStream();

                // 응답 헤더를 ASCII 바이트로 변환해 전송
                output.write(
                        responseHeaders.getBytes(StandardCharsets.US_ASCII)
                );

                // 헤더 뒤에 응답 본문 전송
                output.write(body);

                // 버퍼링된 출력이 있다면 밀어내기. 자원 해제는 아님
                output.flush();

            } // Socket 자동 종료: 연결 및 관련 스트림 자원 해제
        } // ServerSocket 자동 종료: 연결 대기 소켓 자원 해제
    }
}
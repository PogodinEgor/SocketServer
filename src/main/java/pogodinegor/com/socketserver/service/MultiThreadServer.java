package pogodinegor.com.socketserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pogodinegor.com.socketserver.dto.ClientRequestDTO;
import pogodinegor.com.socketserver.dto.ServerResponseDTO;
import pogodinegor.com.socketserver.model.ClientRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiThreadServer {
    @Value("${server.port}")
    private int SERVER_PORT;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ClientRequestService clientRequestService;

    @PostConstruct
    public void init() {
        connectServer();
    }

    private void connectServer() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Сервер готов к работе и слушает порт " + SERVER_PORT);
            while (true) {
                final Socket connectSocket = serverSocket.accept();
                connectSocket.setSoTimeout(5000);

                executorService.execute(() -> handleClient(connectSocket));
            }
        } catch (IOException e) {
            System.err.println("Ошибка при работе сервера: " + e.getMessage());
            log.error("Ошибка при работе сервера: " + e.getMessage());
        } finally {
            executorService.shutdownNow(); // Завершаем работу пула потоков немедленно
        }
    }

    private void handleClient(Socket connectSocket) {
        try (PrintWriter writer = new PrintWriter(connectSocket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()))) {

            while (true) {
                sendInitialResponse(writer);
                ClientRequestDTO requestDTO = readClientRequest(reader);


                if (requestDTO == null) break;

                    if(requestDTO.getMessage().equals("Последний объект")){
                      writer.write(objectMapper.writeValueAsString(clientRequestService.getLastMessage()));
                    } else {
                        writer.write("Неверные данные в сообщении");
                    }

                processClientRequest(requestDTO, writer);

            }
        } catch (SocketTimeoutException e) {
            System.out.println("Соединение закрыто из-за таймаута");
            log.error("Соединение закрыто из-за таймаута" + e.getMessage());
        } catch (Exception e) { // Ловим все исключения для упрощения
            System.err.println("Ошибка при работе с клиентом: " + e.getMessage());
            log.error("Ошибка при работе с клиентом: " + e.getMessage());
        }
    }

    private void sendInitialResponse(PrintWriter writer) throws JsonProcessingException {
        ServerResponseDTO response = new ServerResponseDTO("Привет от сервера, готов принять сообщение");
        String jsonResponse = objectMapper.writeValueAsString(response);
        writer.println(jsonResponse);
    }

    private ClientRequestDTO readClientRequest(BufferedReader reader) throws IOException {
        String inputLine = reader.readLine();
        if (inputLine == null) return null;

        System.out.println("Получено сообщение от клиента: " + inputLine);
        return objectMapper.readValue(inputLine, ClientRequestDTO.class);
    }

    private void processClientRequest(ClientRequestDTO requestDTO, PrintWriter writer) throws InterruptedException, ExecutionException, JsonProcessingException {
        ClientRequest entity = convertToEntity(requestDTO);
        ScheduledFuture<?> future = clientRequestService.saveClientRequestWithDelay(entity);
        future.get(); // Ожидаем завершения задачи

        ServerResponseDTO responseToClient = new ServerResponseDTO("Данные успешно обработаны и сохранены");
        String responseJson = objectMapper.writeValueAsString(responseToClient);
        writer.println(responseJson);
    }

    public ClientRequest convertToEntity(ClientRequestDTO requestDTO) {
        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setWaitingTime(requestDTO.getWaitingTime());
        clientRequest.setMessage(requestDTO.getMessage());
        clientRequest.setCreatedAt(new Date());
        return clientRequest;
    }


}


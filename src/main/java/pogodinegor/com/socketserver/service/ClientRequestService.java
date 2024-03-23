package pogodinegor.com.socketserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pogodinegor.com.socketserver.model.ClientRequest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientRequestService {

    private final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);
    private final DataSource dataSource;


    public ScheduledFuture<?> saveClientRequestWithDelay(ClientRequest clientRequest) {
        long delay = clientRequest.getWaitingTime() * 1000L;
        System.out.println("Планирование сохранения с задержкой: " + delay + "ms");

       return scheduledThreadPool.schedule(() -> {
            System.out.println("Начало выполнения задачи с задержкой");
            saveClientRequest(clientRequest);
        },clientRequest.getWaitingTime(), TimeUnit.SECONDS);
    }

    public void saveClientRequest(ClientRequest clientRequest) {
        String sql = "INSERT INTO servers(waiting_time, message, created_at) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, clientRequest.getWaitingTime());
            pstmt.setString(2, clientRequest.getMessage());
            pstmt.setTimestamp(3, new Timestamp(clientRequest.getCreatedAt().getTime()));

            pstmt.executeUpdate();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}

package pogodinegor.com.socketserver.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ClientRequestDTO {
    private int waitingTime;
    private String message;
}

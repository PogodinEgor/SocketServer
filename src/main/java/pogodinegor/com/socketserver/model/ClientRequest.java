package pogodinegor.com.socketserver.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "servers")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ClientRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "waiting_time")
    private int waitingTime;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private Date createdAt;

}

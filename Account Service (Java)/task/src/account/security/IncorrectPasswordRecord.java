package account.security;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "incorrect_password_records")
@Data
@NoArgsConstructor
public class IncorrectPasswordRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String username;
    private int count;

    public IncorrectPasswordRecord(String username, int count) {
        this.username = username;
        this.count = count;
    }
}

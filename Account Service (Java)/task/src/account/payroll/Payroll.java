package account.payroll;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "payrolls")
@IdClass(PayrollID.class)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payroll {
    @Id
    @NotNull
    private String employee;
    @Id
    @NotNull
    private LocalDate period;
    @NotNull
    private long salary;

    @JsonCreator
    public Payroll(@JsonProperty("employee") String employee, @JsonProperty("period") String period, @JsonProperty("salary") long salary) {
        this.employee = employee;
        String[] periodParts = period.split("-");
        int month = Integer.parseInt(periodParts[0]);
        int year = Integer.parseInt(periodParts[1]);
        this.period = LocalDate.of(year, month, 1);
        this.salary = salary;
    }
}

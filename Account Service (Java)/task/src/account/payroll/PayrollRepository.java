package account.payroll;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PayrollRepository extends CrudRepository<Payroll, PayrollID> {
    List<Payroll> getPayrollsByEmployeeIgnoreCaseOrderByPeriodDesc(String employee);
}

package jp.ac.ir.service.management;

import jp.ac.ir.service.TableData;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(value = "/users", accept = "application/json", contentType = "application/json")
public interface UserManagementService {
    @GetExchange("/grid")
    TableData<UsersData> getUsersData();
}

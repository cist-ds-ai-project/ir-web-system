package jp.ac.ir.repository.gpa;
import jp.ac.ir.model.dto.gpa.*;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.HttpExchange;

import org.springframework.web.bind.annotation.RequestBody;
@HttpExchange("/gpa")
public interface GpaApiClient {
    @GetExchange("/options")
    GpaSelectionOptionsResponse getOptions();

    @PostExchange("/raw")
    GpaRawResponse getRawGpa(@RequestBody GpaFilter filter);

    @PostExchange("/by-department")
    DepartmentGpaResponse getByDepartment(@RequestBody GpaFilter filter);

    @PostExchange("/stats")
    GpaStatsResponse getStats(@RequestBody GpaFilter filter);

}

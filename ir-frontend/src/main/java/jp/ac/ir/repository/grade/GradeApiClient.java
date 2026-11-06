package jp.ac.ir.repository.grade;
import com.fasterxml.jackson.annotation.JsonProperty;
import jp.ac.ir.model.dto.grade.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;


@HttpExchange("/grade")
public interface GradeApiClient {

    @GetExchange("/detail/{lecture_name}")
    GradeDetailResponse getGradeDetailResponse(
            @PathVariable("lecture_name") String lectureName
    );
}


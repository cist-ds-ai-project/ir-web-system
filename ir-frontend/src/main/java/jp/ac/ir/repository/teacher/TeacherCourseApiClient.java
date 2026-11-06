package jp.ac.ir.repository.teacher;
import jp.ac.ir.model.dto.teacher.TeacherCourseResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/teacher")
public interface TeacherCourseApiClient {
    @GetExchange("/courses/{person_id}")
    TeacherCourseResponse getTeacherCourses(
            @PathVariable("person_id") String personId
    );
}

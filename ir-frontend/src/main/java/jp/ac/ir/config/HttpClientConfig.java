package jp.ac.ir.config;

import jp.ac.ir.repository.chat.ChatApiClient;
import jp.ac.ir.repository.enrollment.EnrollmentApiClient;
import jp.ac.ir.repository.grade.GradeApiClient;
import jp.ac.ir.repository.lecture.LectureApiClient;
import jp.ac.ir.repository.questionnaire.QuestionnaireApiClient;
import jp.ac.ir.repository.scholarship.ScholarshipApiClient;
import jp.ac.ir.service.management.UserManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * HTTP Interface クライアントの設定
 *
 * <p>{@link ImportHttpServices} により、{@code @HttpExchange} を付与した
 * インターフェースが自動的に Spring Bean として登録される。
 * 新しい API インターフェースを追加する場合は {@code types} に追加する。</p>
 */

@Configuration
@ImportHttpServices(group = "backend", types = {
        GradeApiClient.class,
        LectureApiClient.class,
        UserManagementService.class,
        EnrollmentApiClient.class,
        ChatApiClient.class,
        ScholarshipApiClient.class,
        ChatApiClient.class,
        QuestionnaireApiClient.class

})

public class HttpClientConfig {

    /**
     * 全グループのクライアントに共通の baseUrl を設定する
     */
    @Bean
    public RestClientHttpServiceGroupConfigurer groupConfigurer(
            @Value("${fastapi.url}") String fastapiUrl) {
        return groups -> groups
                .forEachClient((group, clientBuilder) -> {
                    clientBuilder.baseUrl(fastapiUrl);
                    // #region agent log
                    clientBuilder.requestInterceptor((request, body, execution) -> {
                        org.springframework.http.client.ClientHttpResponse response = execution.execute(request, body);
                        int status = -1;
                        try {
                            status = response.getStatusCode().value();
                        } catch (Exception ignored) {
                        }

                        return response;
                    });
                    // #endregion
                });
    }
    @Bean
    public UserManagementService userManagementService() {
        RestClient client = RestClient.builder()
                .baseUrl("http://localhost:8000")
                .build();

        HttpServiceProxyFactory factory =
                HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client))
                        .build();

        return factory.createClient(UserManagementService.class);
    }


}

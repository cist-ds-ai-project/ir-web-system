package jp.ac.ir.service.management;

public record UsersData(
        long id,
        String login_id,
        String person_id,
        String user_name,
        boolean is_available,
        String display_name,
        String created_at,
        String deleted_at
) {

}

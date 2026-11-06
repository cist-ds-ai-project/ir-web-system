package jp.ac.ir.service.management;

public record User(
        long id,
        String login_id,
        String person_id,
        String name,
        boolean is_available
) {
}
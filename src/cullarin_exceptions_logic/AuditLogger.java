package cullarin_exceptions_logic;

import peco_users_menus.User;
import titong_model_data.AuditLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class AuditLogger {
    private static final String FILE_AUDIT = "audit_log.txt";

    /**
     * Appends one audit entry to audit_log.txt immediately.
     *
     * @param actor  the staff/admin member performing the action
     * @param action short uppercase identifier, e.g. "ADD_MOVIE"
     * @param detail human-readable description of what changed
     */
    public static void log(User actor, String action, String detail) {
        String timestamp = LocalDateTime.now().toString();
        String role      = actor.getClass().getSimpleName().toUpperCase();
        AuditLog entry   = new AuditLog(timestamp, role, actor.getId(),
                                        actor.getName(), action, detail);
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_AUDIT, true))) {
            pw.println(entry.toFileLine());
        } catch (IOException e) {
            System.out.println("Warning: could not write to audit log.");
        }
    }
}

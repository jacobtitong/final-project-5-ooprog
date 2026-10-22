package titong_model_data;

public class AuditLog {
    private String timestamp;
    private String actorRole;
    private int actorId;
    private String actorName;
    private String action;
    private String detail;

    public AuditLog(String timestamp, String actorRole, int actorId,
                    String actorName, String action, String detail) {
        this.timestamp  = timestamp;
        this.actorRole  = actorRole;
        this.actorId    = actorId;
        this.actorName  = actorName;
        this.action     = action;
        this.detail     = detail;
    }

    public String getTimestamp()  { return timestamp; }
    public String getActorRole()  { return actorRole; }
    public int    getActorId()    { return actorId; }
    public String getActorName()  { return actorName; }
    public String getAction()     { return action; }
    public String getDetail()     { return detail; }

    /** Fields joined by | — detail may contain spaces but never | */
    public String toFileLine() {
        return timestamp + "|" + actorRole + "|" + actorId + "|"
             + actorName + "|" + action + "|" + detail;
    }
}

public class Event extends Task {
    private String duration;

    public Event(String description, String duration) {
        super(description);
        this.duration = duration;
    }

    public String toString() {
        return ("[E]"+ super.toString() + "(at:" + this.duration + ")");
    }
}
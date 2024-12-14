package fianlTest;

import java.time.LocalTime;


class TimerList {
    private LocalTime time;
    private String type; // "공부" 또는 "쉬는 시간"
    private int duration; // 지속 시간 (초 단위)

    public TimerList(LocalTime time, String type, int duration) {
        this.time = time;
        this.type = type;
        this.duration = duration;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return time + "," + type + "," + duration;
    }

    public static TimerList fromString(String data) {
        String[] parts = data.split(",");
        return new TimerList(LocalTime.parse(parts[0]), parts[1], Integer.parseInt(parts[2]));
    }
}
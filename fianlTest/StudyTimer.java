package fianlTest;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * StudyTimer 클래스는 공부 시간과 쉬는 시간을 설정하고 알람을 관리하는 프로그램입니다.
 * 사용자는 공부 시간과 쉬는 시간을 설정하고, 타이머가 종료되면 알람이 울립니다.
 * 알람 설정을 파일로 저장하고 불러오는 기능도 제공합니다.
 * 
 * @created 2024-12-12
 * @lastModified 2024-12-14
 */
public class StudyTimer {
    private final JFrame frame;
    private final JTextField studyTimeField;
    private final JTextField breakTimeField;
    private final JButton setAlarmButton;
    private final JButton saveButton;
    private final JButton loadButton;
    private Timer countdownTimer; // 카운트다운 타이머
    private JLabel countdownLabel; // 남은 시간 표시 레이블
    private boolean isStudying = true; // 공부 중인지 쉬는 중인지 추적하는 변수
    private final List<TimerList> alarms = new ArrayList<>(); // 알람 목록
    private final Map<String, TimerList> alarmMap = new HashMap<>(); // Map을 추가

    /**
     * StudyTimer의 GUI를 초기화하고 이벤트 핸들러를 설정합니다.
     * @created 2024-12-12
     * @lastModified 2024-12-14
     */
    public StudyTimer() {
        frame = new JFrame("공부 & 쉬는 시간 알람");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 250);
        frame.setLayout(new FlowLayout());

        studyTimeField = new JTextField(5);
        studyTimeField.setToolTipText("공부 시간을 분 단위로 입력하세요");

        breakTimeField = new JTextField(5);
        breakTimeField.setToolTipText("쉬는 시간을 분 단위로 입력하세요");

        setAlarmButton = new JButton("타이머 시작");
        setAlarmButton.addActionListener(e -> {
            try {
                int studyMinutes = Integer.parseInt(studyTimeField.getText());
                int breakMinutes = Integer.parseInt(breakTimeField.getText());
                setStudyBreakAlarm(studyMinutes, breakMinutes);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "숫자를 입력하세요!", "입력 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveButton = new JButton("저장");
        saveButton.addActionListener(e -> saveAlarmsToFile("alarms.txt"));

        loadButton = new JButton("불러오기");
        loadButton.addActionListener(e -> loadAlarmsFromFile("alarms.txt"));

        countdownLabel = new JLabel("남은 시간: 00:00", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Serif", Font.PLAIN, 14));

        frame.add(new JLabel("공부 시간(분):"));
        frame.add(studyTimeField);
        frame.add(new JLabel("쉬는 시간(분):"));
        frame.add(breakTimeField);
        frame.add(setAlarmButton);
        frame.add(saveButton);
        frame.add(loadButton);
        frame.add(countdownLabel);

        frame.setVisible(true);
    }

    /**
     * 공부 시간과 쉬는 시간을 설정하고 알람을 시작합니다.
     * 
     * @param studyMinutes 공부 시간 (분)
     * @param breakMinutes 쉬는 시간 (분)
     */
    private void setStudyBreakAlarm(int studyMinutes, int breakMinutes) {
        int studySeconds = studyMinutes * 60;
        int breakSeconds = breakMinutes * 60;

        TimerList studyAlarm = new TimerList(LocalTime.now(), "공부", studySeconds);
        TimerList breakAlarm = new TimerList(LocalTime.now().plusSeconds(studySeconds), "쉬는 시간", breakSeconds);

        // 알람을 리스트와 맵에 모두 추가
        alarms.add(studyAlarm);
        alarms.add(breakAlarm);
        alarmMap.put("공부", studyAlarm);
        alarmMap.put("쉬는 시간", breakAlarm);

        startStudySession();
    }

    /**
     * 공부 세션을 시작하고 카운트다운 타이머를 시작합니다.
     */
    private void startStudySession() {
        isStudying = true;
        startCountdownTimer();
    }

    /**
     * 쉬는 세션을 시작하고 카운트다운 타이머를 시작합니다.
     */
    private void startBreakSession() {
        isStudying = false;
        startCountdownTimer();
    }

    /**
     * 카운트다운 타이머를 시작하고, 시간이 다 되면 타이머가 종료됩니다.
     */
    private void startCountdownTimer() {
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1000, new ActionListener() {
            int remainingTime = isStudying ? alarms.get(0).getDuration() : alarms.get(1).getDuration();

            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                if (remainingTime <= 0) {
                    countdownTimer.stop();
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(frame, isStudying ? "공부 시간이 끝났습니다!" : "쉬는 시간이 끝났습니다!",
                            isStudying ? "공부 종료" : "쉬는 시간 종료", JOptionPane.WARNING_MESSAGE);
                    if (isStudying) {
                        startBreakSession();
                    } else {
                        startStudySession();
                    }
                    return;
                }
                updateCountdownLabel(remainingTime);
            }
        });
        countdownTimer.start();
    }

    /**
     * 남은 시간을 표시하는 레이블을 업데이트합니다.
     * @param remainingTime 남은 시간 (초)
     * @created 2024-12-13
     * @lastModified 2024-12-14
     */
    private void updateCountdownLabel(int remainingTime) {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        countdownLabel.setText(String.format("남은 시간: %02d:%02d", minutes, seconds));
    }

    /**
     * 알람 목록을 파일에 저장합니다.
     * 
     * @param filename 저장할 파일의 이름
     */
    private void saveAlarmsToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (TimerList alarm : alarms) {
                writer.write(alarm.toString());
                writer.newLine();
            }
            JOptionPane.showMessageDialog(frame, "알람이 저장되었습니다!", "저장 완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "파일 저장 실패!", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 파일에서 알람 목록을 불러옵니다.
     * 
     * @param filename 불러올 파일의 이름
     */
    private void loadAlarmsFromFile(String filename) {
        alarms.clear();
        alarmMap.clear(); // Map도 비워줌
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
            	TimerList alarm = TimerList.fromString(line);
                alarms.add(alarm);
                alarmMap.put(alarm.getType(), alarm); // Map에 알람 추가
            }
            JOptionPane.showMessageDialog(frame, "알람이 불러와졌습니다!", "불러오기 완료", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "파일 불러오기 실패!", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 프로그램을 실행하는 메인 메서드입니다.
     * 
     * @param args 명령줄 인수
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudyTimer::new);
    }
}
package ru.rootmen.backup.backend.messaging;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.*;
import static com.diogonunes.jcolor.Attribute.NONE;

import com.diogonunes.jcolor.AnsiFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import ru.iedt.database.controller.TaskDescription;

public class Logger {

  static int isPrint = 31;
  static AnsiFormat tableHead = new AnsiFormat(GREEN_TEXT(), NONE(), NONE());
  static AnsiFormat tableStartRow = new AnsiFormat(BLUE_TEXT(), NONE(), NONE());
  static AnsiFormat tableFinishRow = new AnsiFormat(CYAN_TEXT(), NONE(), NONE());
  static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss SSS");

  static void info() {
    isPrint++;
    if (isPrint > 30) {
      System.out.printf(
          "%-50s| %-25s| %-50s| %-40s| %-50s| %-50s| %-200s\n",
          colorize("Время", tableHead),
          colorize("Тип задачи", tableHead),
          colorize("UUID задачи", tableHead),
          colorize("Название задачи", tableHead),
          colorize("UUID пользователя", tableHead),
          colorize("Сокет подключения", tableHead),
          colorize("Данные", tableHead));
      isPrint = 0;
    }
  }

  static void logger(TaskDescription task, String message) {
    info();
    System.out.printf(
        "%-50s| %-25s| %-50s| %-40s| %-50s| %-50s| %-200s\n",
        colorize(dateFormat.format(new Date()), tableStartRow),
        colorize(message, tableStartRow),
        colorize(task.task_id.toString(), tableStartRow),
        colorize(
            task.task_name.substring(0, Math.min(27, task.task_name.length())) + "...",
            tableStartRow),
        colorize(task.user_id.toString(), tableStartRow),
        colorize(task.socket.substring(0, Math.min(38, task.socket.length())), tableStartRow),
        colorize(
            task.task_data.substring(0, Math.min(190, task.task_data.length())) + "...",
            tableStartRow));
  }

  static void loggerFinish(TaskDescription task, String message) {
    info();
    System.out.printf(
        "%-50s| %-25s| %-50s| %-40s| %-50s| %-50s| %-200s\n",
        colorize(dateFormat.format(new Date()), tableFinishRow),
        colorize(message, tableFinishRow),
        colorize(task.task_id.toString(), tableFinishRow),
        colorize(
            task.task_name.substring(0, Math.min(27, task.task_name.length())) + "...",
            tableFinishRow),
        colorize(task.user_id.toString(), tableFinishRow),
        colorize(task.socket.substring(0, Math.min(38, task.socket.length())), tableFinishRow),
        colorize(
            task.task_data.substring(0, Math.min(190, task.task_data.length())) + "...",
            tableFinishRow));
  }
}

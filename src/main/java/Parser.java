import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Reads and processes the commands given to Duke and returns the appropriate action.
 * Generates either todo, deadline or event tasks.
 */
public class Parser {
    private TaskList taskList;
    private Ui ui;
    private Storage storage;

    /**
     * Creates a Parser object with the given TaskList, Ui, Storage.
     * @param taskList TaskList that duke keeps track of.
     * @param ui Generates the output string.
     * @param storage Accesses the given file.
     */
    public Parser(TaskList taskList, Ui ui, Storage storage) {
        this.taskList = taskList;
        this.ui = ui;
        this.storage = storage;
    }

    /**
     * Processes the Command given by the user to Duke.
     * @param command String representing the command given to Duke.
     * @return A string representing the reply from Duke.
     */
    public String processCommand(String command) {
        String reply = "";

        // When command is 'list'
        if (command.equals("list")) {
            String list = taskList.printAllTasks();
            reply = "Here are the tasks in your list:" + "\n" + list;

        } else if (command.contains("done")) {
            String[] sentence = command.split(" ");

            // When command is 'done'
            try {
                if (sentence[0].equals("done")) { // Check if the first word is done
                    String taskIndexAsString = sentence[1];
                    assert isInteger(taskIndexAsString) : "Wrong format. Done command needs an integer index of task.";
                    int taskIndexAsInteger = Integer.parseInt(sentence[1]);
                    reply = taskList.markAsDone(taskIndexAsInteger);
                    // If it wasn't marked before, this would print out a notification saying it is now marked.

                    // Save new list to storage
                    try {
                        storage.saveToFile(taskList.toString());
                    } catch (IOException e) {
                        reply = ui.showLoadingError();
                    }

                } else {
                    throw new UnknownTaskTypeException();
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                reply = ui.promptDoneCompletion();
            } catch (IndexOutOfBoundsException e) {
                reply = ui.promptList();
            } catch (NumberFormatException e) {
                reply = ui.promptDoneFormat();
            } catch (UnknownTaskTypeException e) {
                reply = ui.showErrorMessage(e);
            }


        } else if (command.contains("delete")) {
            String[] sentence = command.split(" ");

            // When command is 'delete'
            try {
                if (sentence[0].equals("delete")) {
                    String taskIndexAsString = sentence[1];
                    assert isInteger(sentence[1]) : "Wrong format. Delete command needs an integer index of task";
                    int taskIndex = Integer.parseInt(taskIndexAsString);
                    Task deletedTask = taskList.deleteTask((taskIndex - 1));
                    reply = ui.showDeletedTask(deletedTask, taskList.numTasks);

                    // Save new list to storage
                    try {
                        storage.saveToFile(taskList.toString());
                    } catch (IOException e) {
                        reply = ui.showLoadingError();
                    }

                } else {
                    throw new UnknownTaskTypeException();
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                reply = ui.promptDeleteCompletion();
            } catch (IndexOutOfBoundsException e) {
                reply = ui.promptList();
            } catch (NumberFormatException e) {
                reply = ui.promptDeleteFormat();
            } catch (UnknownTaskTypeException e) {
                reply = ui.showErrorMessage(e);
            }


        } else if (command.contains("find")) {
            String[] sentence = command.split(" ");
            String keyword = "";

            try {
                if (sentence[0].equals("find")) {
                    keyword = sentence[1];
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                reply = ui.promptFindKeyword();
            }

            reply = ui.showSearchList(taskList.searchFor(keyword));

        } else {
            // Generate new task
            try {
                if (!command.isEmpty()) {
                    Task newTask = Parser.generateNewTask(command);
                    taskList.addTask(newTask);

                    // Save the new list to storage
                    try {
                        storage.saveToFile(taskList.toString());
                    } catch (IOException e) {
                        reply = ui.showLoadingError();
                    }

                    reply = ui.showAddTask(newTask, taskList.numTasks);
                }

            } catch (DukeException err) {
                reply = ui.showErrorMessage(err);
            }
        }

        // When command is bye
        if (command.equals("bye")) {
            reply = ui.showFarewell();
        }

        return reply;
    }



    /**
     * Generates the appropriate task that matches the command.
     * @param task command to create the task.
     * @return Task that is generated.
     * @throws DukeException Exceptions specific to Duke
     */
    public static Task generateNewTask(String task) throws DukeException {
        try {
            String type = task.substring(0, task.indexOf(' '));
            String taskDescription = task.substring(task.indexOf(' ') + 1);
            Task newTask = new Task("dummy");

            // Create the appropriate Task type
            if (type.equals("todo")) {
                newTask = new ToDo(taskDescription);
            }

            try {
                if (type.equals("deadline")) {
                    String[] sentence = taskDescription.split("/by");
                    String description = sentence[0];
                    String deadline = formatDateTime(sentence[1]);

                    newTask = new Deadline(description, deadline);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new MissingDateTimeException("DateTime missing."
                        + "Please set a deadline. (Eg. deadline read book /by Sunday)");
            }

            try {
                if (type.equals("event")) {
                    String[] sentence = taskDescription.split("/at");
                    String description = sentence[0];
                    String time = sentence[1];
                    System.out.println(time);
                    newTask = new Event(description, time);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new MissingDateTimeException("Event time period missing."
                        + "Please set a start and end time. (Eg. event dance /at Mon 2-4pm)");
            }

            return newTask;

        } catch (StringIndexOutOfBoundsException rootError) {
            // If task type is correct, the error is due to empty description
            // else the task type is unknown
            if (task.equals("todo") | task.equals("deadline") | task.equals("event")) {
                throw new EmptyDescriptionException(task, rootError);
            } else {
                throw new UnknownTaskTypeException();
            }
        }
    }

    /**
     * Formats the date time given as a string.
     * @param deadline to be formatted.
     * @return String representing the formatted date time.
     */
    private static String formatDateTime(String deadline) {
        // Split to individual components
        deadline = deadline.trim();
        String[] dd = deadline.split(" ");
        String[] date = dd[0].split("/");
        String time = dd[1];
        int hours = Integer.valueOf(time.substring(0,2));
        int minutes = Integer.valueOf(time.substring(2));

        LocalDateTime actualDateTime = LocalDateTime.of(Integer.valueOf(date[2]), Integer.valueOf(date[1]),
                Integer.valueOf(date[0]), hours, minutes);
        // maybe error here
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm a");
        String formatted = " " + dtf.format(actualDateTime);
        return formatted;
    }

    /**
     * Determines if a string is an integer.
     * @param ss to check if it is an integer.
     * @return true or false
     */
    private boolean isInteger(String ss) {
        try {
            Integer.parseInt(ss);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }

        // Only reaches here if input string is an integer and no exceptions are thrown.
        return true;
    }
}
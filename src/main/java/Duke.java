import java.util.Scanner;
import java.io.IOException;

public class Duke {
    private Storage storage;
    private TaskList taskList;
    private Ui ui;

    public Duke(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            taskList = new TaskList(storage.loadPreviousTasks());
        } catch (DukeException e) {
            ui.showLoadingError();
            taskList = new TaskList();
        }
    }
    public static void main(String[] args) {
        new Duke("../../../data/duke.txt").run();
    }

    public void run() {
        ui.showWelcome();
        System.out.println("Here is the list of tasks from where you've left off: ");
        ui.showList(taskList);

        // STOPPED HERE
        Scanner input = new Scanner(System.in);
        String command = ui.readCommand();

        while (!command.equals("bye")) {
            // When command is 'list'
            if (command.equals("list")) {
                System.out.println("Here are the tasks in your list:");
                taskList.printAllTasks();
                command = input.nextLine().trim();

            } else if (command.contains("done")) {
                String[] sentence = command.split(" ");

                // When command is 'done'
                try {
                    if (sentence[0].equals("done")) { // Check if the first word is done
                        int completedTaskIndex = Integer.parseInt(sentence[1]);
                        taskList.markAsDone(completedTaskIndex);
                        // If it wasn't marked before, this would print out a notification saying it is now marked.

                        // Save new list to storage
                        try {
                            storage.saveToFile(taskList.toString());
                        } catch(IOException e) {
                            ui.showLoadingError();
                        }

                    } else {
                        throw new UnknownTaskTypeException();
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    ui.promptDoneCompletion();
                } catch (IndexOutOfBoundsException e) {
                    ui.promptList();
                } catch (NumberFormatException e) {
                    ui.promptDoneFormat();
                } catch (UnknownTaskTypeException e) {
                    ui.showErrorMessage(e);
                }

                command = input.nextLine().trim();

            } else if (command.contains("delete")) {
                String[] sentence = command.split(" ");

                // When command is 'delete'
                try {
                    if (sentence[0].equals("delete")) {
                        int taskIndex = Integer.parseInt(sentence[1]);
                        Task deletedTask = taskList.deleteTask((taskIndex - 1));
                        ui.showDeletedTask(deletedTask, taskList.numTasks);

                        // Save new list to storage
                        try {
                            storage.saveToFile(taskList.toString());
                        } catch(IOException e) {
                            ui.showLoadingError();
                        }

                    } else {
                        throw new UnknownTaskTypeException();
                    }

                } catch (ArrayIndexOutOfBoundsException e) {
                    ui.promptDeleteCompletion();
                } catch (IndexOutOfBoundsException e) {
                    ui.promptList();
                } catch (NumberFormatException e) {
                    ui.promptDeleteFormat();
                } catch (UnknownTaskTypeException e) {
                    ui.showErrorMessage(e);
                }

                command = input.nextLine().trim();

            } else {
                // Generate new task
                try {
                    if (!command.isEmpty()) {
                        Task newTask = Parser.generateNewTask(command);
                        taskList.addTask(newTask);

                        // Save the new list to storage
                        try {
                            storage.saveToFile(taskList.toString());
                        } catch(IOException e) {
                            ui.showLoadingError();
                        }

                        ui.showAddTask(newTask, taskList.numTasks);
                    }

                } catch (DukeException err) {
                    ui.showErrorMessage(err);
                }
                command = input.nextLine().trim();
            }
        }
        // When command is bye
        ui.showFarewell();
    }
}

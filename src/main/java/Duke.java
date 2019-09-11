import java.io.IOException;

/**
 * Represents a Duke object that is able to keep track of tasks.
 */
public class Duke {

    private Storage storage;
    private TaskList taskList;
    private Ui ui;

    /**
     * Create a new Duke with access to duke.txt file.
     */
    public Duke() {
        //assert false: "Printed";
        String filePath = "duke.txt";
        ui = new Ui();
        storage = new Storage(filePath);
        try {
            taskList = new TaskList(storage.loadPreviousTasks());
        } catch (DukeException e) {
            ui.showLoadingError();
            storage = new Storage("duke.txt");
            taskList = new TaskList();
        }
    }

    /**
     * Runs Duke.
     */
    public String run(String command) {


        Parser parser = new Parser(taskList, ui, storage);

        String response = parser.processCommand(command); //returns string

        return response;
    }

    //REPLACE THIS WITH MY COMPLETED METHOD!!!!
    public String getResponse(String input) {
        //return "Duke heard: " + input;
        return run(input);
    }

    /**
     * Generate Duke's welcome message.
     * @return a string representing the welcome message.
     */
    public String showWelcome() {
        String s1 = ui.showWelcome();
        String s2 = "Here is the list of tasks from where you've left off: ";
        String s3 = ui.showList(taskList);
        return s1 + "\n" + s2 + "\n" + s3;
    }
}

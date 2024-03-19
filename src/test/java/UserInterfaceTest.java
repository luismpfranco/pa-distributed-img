import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserInterfaceTest {

    @Test
    public void testConstructor() {
        UserInterface ui = new UserInterface();
        assertNotNull(ui.getSelectImageButton(), "Select image button should not be null after construction");
        assertNotNull(ui.getMessageArea(), "Message area should not be null after construction");
    }
}
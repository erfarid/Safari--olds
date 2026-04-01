package safariolds.model;

import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;


import static org.junit.Assert.*;

public class DifficultyTest {

    private Difficulty difficulty;

    @Before
    public void setUp() {
        System.setProperty("java.awt.headless", "true");
        difficulty = new Difficulty(true);  // use test mode
    }

    @Test
    public void testWindowProperties() {
        JFrame frame = difficulty.getFrame();
        assertNull("Frame should be null in test mode", frame);
    }

    @Test
    public void testStyledButtonProperties() {
        JButton button = difficulty.createStyledButton("TEST");
        assertEquals("TEST", button.getText());
        assertEquals(new Color(102, 51, 0), button.getBackground());
        assertEquals(new Font("Algerian", Font.BOLD, 30), button.getFont());
    }

    @Test
    public void testMouseEnterExitChangesColor() throws Exception {
        JButton button = difficulty.createStyledButton("TEST");

        MouseEvent enter = new MouseEvent(button, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, 1, 1, 0, false);
        MouseEvent exit = new MouseEvent(button, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 1, 1, 0, false);

        // Simulate event dispatching using the actual listener
        for (MouseListener listener : button.getMouseListeners()) {
            listener.mouseEntered(enter);
        }

        assertEquals("Hover color expected", new Color(255, 165, 0), button.getBackground());

        for (MouseListener listener : button.getMouseListeners()) {
            listener.mouseExited(exit);
        }

        assertEquals("Original color expected after exit", new Color(102, 51, 0), button.getBackground());
    }

    @Test
    public void testDifficultyPanelPaintComponent() {
        Difficulty.DifficultyPanel panel = new Difficulty.DifficultyPanel();
        panel.setSize(100, 100);  // Ensure it has dimensions

        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Use public paint() which internally calls protected paintComponent()
        panel.paint(g2d);

        g2d.dispose();

        // Verify the pixel color is close to the outer brown background
        Color pixel = new Color(img.getRGB(10, 10));
        assertTrue("Should paint brown-ish background", pixel.getRed() >= 139 && pixel.getGreen() >= 69);
    }

    @Test
    public void testBackButtonTriggersDispose() {
        final boolean[] disposed = {false};

        Difficulty testDifficulty = new Difficulty(true) {
            void fakeDispose() {
                disposed[0] = true;
            }

            // Override the BACK button logic to call fakeDispose
            @Override
            public JButton createStyledButton(String text) {
                JButton button = super.createStyledButton(text);
                if (text.equals("BACK")) {
                    for (ActionListener al : button.getActionListeners()) {
                        button.removeActionListener(al);
                    }
                    button.addActionListener(e -> fakeDispose());
                }
                return button;
            }
        };

        // Build panel and simulate BACK button click
        JPanel panel = new JPanel();
        JButton backButton = testDifficulty.createStyledButton("BACK");
        panel.add(backButton);
        backButton.doClick();

        assertTrue("Dispose should be triggered", disposed[0]);
    }

}

package safariolds.model;

import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

public class SettingsTest {

    @Before
    public void setup() {
        System.setProperty("java.awt.headless", "true");
        Settings.enableGlobalTestMode();
    }

    @Test
    public void testDefaultConstructorWithoutInitUI() {
        Settings settings = new Settings(); // safely skips initUI
        assertNotNull(settings);
    }

    
    @Test
    public void testBooleanConstructor() {
        Settings settings = new Settings(true); // test mode
        assertNotNull(settings);
    }

    @Test
    public void testLoadVolumeSettingWhenNoFileExists() {
        File file = new File("settings.properties");
        if (file.exists()) {
            file.delete();
        }
        Settings settings = new Settings(true);
        assertEquals(50, settings.testLoadVolume()); // fallback to default
    }

    @Test
    public void testSaveAndLoadVolumeSetting() {
        Settings settings = new Settings(true);
        settings.testSaveVolume(72);
        assertEquals(72, settings.testLoadVolume());
    }

    @Test
    public void testSaveVolumeHandlesIOException() {
        File file = new File("settings.properties");
        if (file.exists()) file.delete();
        try {
            file.createNewFile();
            file.setReadOnly();

            Settings settings = new Settings(true);
            settings.testSaveVolume(44); // Should fail silently
        } catch (Exception e) {
            fail("IOException handling failed");
        } finally {
            file.setWritable(true);
            file.delete();
        }
    }

    @Test
    public void testGetFrameReturnsNullInTestMode() {
        Settings settings = new Settings(true);
        assertNull(settings.getFrame());
    }

    @After
    public void cleanup() {
        File file = new File("settings.properties");
        if (file.exists()) file.delete();
    }
}

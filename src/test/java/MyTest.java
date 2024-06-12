import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class MyTest {

    @Test
    public void test1() {

        URL testYmlResource = this.getClass().getResource("test.yml");

        if (testYmlResource != null) {


            File testYmlfile;
            try {
                testYmlfile = new File(testYmlResource.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            String test = ParseToYml.parse(testYmlfile);

            String expected = """
                    home:
                      - org.bukkit.Location
                      - x: 20
                      - y: 10""";

            Assertions.assertEquals(expected, test);
        }
    }
}

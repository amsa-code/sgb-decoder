package au.gov.amsa.sgb.decoder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class KeyPrettifyTest {
    
    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(KeyPrettify.class);
    }
    
    @Test
    public void test() {
        assertEquals("RLM Type 12 GNSS Thing", KeyPrettify.prettify("rlmType12GnssThing"));   
    }

}

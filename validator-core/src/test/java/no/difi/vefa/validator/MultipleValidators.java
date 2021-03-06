package no.difi.vefa.validator;

import no.difi.vefa.validator.source.RepositorySource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MultipleValidators {
    private Validator validator;
    
    @Before
    public void setup() throws Exception {
        validator = ValidatorBuilder.newValidator()
                .setSource(RepositorySource.forTest())
                .build();
    }
    
    @After
    public void cleanup() {
        validator.close();
    }
    
    @Test
    public void test1() {
    }
    
    @Test
    public void test2() {
    }
    
}
